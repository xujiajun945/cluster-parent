package com.example.core.util;

import com.example.core.exception.ClusterException;
import io.valkey.springframework.data.valkey.core.ValkeyTemplate;
import io.valkey.springframework.data.valkey.core.script.DefaultValkeyScript;
import io.valkey.springframework.data.valkey.core.script.ValkeyScript;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <h2>看门狗</h2>
 * <p>
 *     看门狗用于在获取分布式锁成功后，定期 renew锁，防止锁因为业务处理时间过长而过期
 *     1. 看门狗的续期采用lua脚本，原子执行如果get和expire非原子执行，会有并发问题
 *       1.1 lua脚本内容：
 *         if valkey.call('GET', KEYS[1]) == ARGV[1] then
 *             return valkey.call('EXPIRE', KEYS[1], ARGV[2])
 *         else
 *             return 0
 *         end
 *       1.2 lua脚本作用：
 *         1.2.1 获取锁的值并判断是否与当前锁的值相同
 *         1.2.2 如果相同则续期锁
 *         1.2.3 如果不相同则返回0，表示锁已经不是我的了
 *       1.3 lua脚本优势：
 *         1.3.1 原子执行，避免了并发问题(给别人“看门”)
 *         1.3.2 减少了redis的交互次数，提高了性能
 *     2. 为什么是守护线程？
 *       2.1 守护线程不会阻止JVM退出，即使有守护线程在运行，JVM也会在所有非守护线程结束后退出
 *       2.2 守护线程的优先级较低，不会占用过多的系统资源
 *       2.3 守护线程的生命周期由JVM管理，不需要手动启动和停止
 *     3. 为什么拒绝策略是直接抛异常？
 *       因为如果看门狗的池满了，说明当前业务会跑在无续锁状态，属于业务安全问题，宁可失败
 *     4. 为什么有isRunning()和isLockLost()两个flag？
 *       isRunning()用于判断看门狗是否正在运行，isLockLost()用于判断锁是否已经丢失，这两个并不等价
 *       isRunning()：锁丢失，调用stop方法，都可以更改running状态
 *       isLockLost()：只有锁丢失的情况下会更改，可以通过这个方法判断当前业务是否安全
 *     5. 关于调度器的调度api选择：
 *       5.1 scheduleAtFixedRate：按开始时间对齐，任务的预期启动间隔固定；任务慢了，后面任务会追赶、堆积
 *       5.2 scheduleWithFixedDelay：按结束时间算，上一轮跑完之后，再等固定延迟才跑下一轮；不会追赶。
 * </p>
 *
 * @author xujiajun
 * @since 2026/11/17
 */
@Slf4j
public class WatchDog {

    private static final ScheduledExecutorService EXECUTOR_SERVICE;

    private static final String RENEW_LUA =
            "if valkey.call('GET', KEYS[1]) == ARGV[1] then " +
                    "    return valkey.call('EXPIRE', KEYS[1], ARGV[2]) " +
                    "else " +
                    "    return 0 " +
                    "end";

    private static final ValkeyScript<Long> RENEW_SCRIPT = new DefaultValkeyScript<>(RENEW_LUA, Long.class);

    static {
        int corePoolSize = 10;
        AtomicInteger count = new AtomicInteger(0);
        ThreadFactory factory = r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("watch-dog-thread-" + count.getAndIncrement());
            thread.setUncaughtExceptionHandler((t, e) -> log.error("Thread {} 抛出异常：", t.getName(), e));
            return thread;
        };
        RejectedExecutionHandler watchDogRejectPolicy = (r, executor) -> {
            throw new ClusterException("watch-dog thread pool is full, cannot start!!");
        };
        EXECUTOR_SERVICE = new ScheduledThreadPoolExecutor(corePoolSize, factory, watchDogRejectPolicy);
    }

    private volatile boolean lockFlag = false;
    private volatile boolean lockLost = false;
    private ScheduledFuture<?> future;

    private final ValkeyTemplate<String, Object> valkeyTemplate;

    private final String lockKey;

    private final String lockValue;

    private final Duration ttl;

    public WatchDog(ValkeyTemplate<String, Object> valkeyTemplate, String lockKey, String lockValue, Duration ttl) {
        this.valkeyTemplate = valkeyTemplate;
        this.lockKey = lockKey;
        this.lockValue = lockValue;
        this.ttl = ttl;
    }

    public void start() {
        start("WatchDog");
    }

    public void start(String taskName) {
        // 避免重复启动
        if (lockFlag) {
            log.warn("watchdog already started, key:{}", lockKey);
            return;
        }
        lockFlag = true;
        // 丢锁后还可重新启动
        lockLost = false;
        long seconds = ttl.toSeconds();
        long period = Math.max(seconds / 3, 1);
        Runnable runnable = TaskUtils.wrap(
                taskName,
                () -> {
                    if (!lockFlag) {
                        return;
                    }
                    Long ret = valkeyTemplate.execute(
                            RENEW_SCRIPT,
                            Collections.singletonList(lockKey),
                            lockValue,
                            String.valueOf(ttl.toSeconds()));
                    // 返回0：锁已经不是我的了，停止看门狗
                    if (ret != null && ret == 0) {
                        log.warn("watch dog stop, lock is invalid, key:{}", lockKey);
                        lockLost = true;
                        stop();
                        return;
                    }
                    log.debug("Renewed lock {} with value {} and ttl {}", lockKey, lockValue, ttl);
                }, exception -> {
                    log.error("WatchDog renew exception, key={}", lockKey, exception);
                });
        future = EXECUTOR_SERVICE.scheduleWithFixedDelay(runnable, period, period, TimeUnit.SECONDS);
    }

    public void stop() {
        lockFlag = false;
        if (future != null && !future.isCancelled()) {
            future.cancel(false);
            future = null;
        }
    }

    public boolean isRunning() {
        return lockFlag;
    }

    public boolean isLockLost() {
        return lockLost;
    }

}
