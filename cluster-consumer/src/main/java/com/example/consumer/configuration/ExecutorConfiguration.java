package com.example.consumer.configuration;

import com.example.core.util.ClusterExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author xujiajun
 * @since 2026/11/10
 */
@Slf4j
@Configuration
public class ExecutorConfiguration implements SchedulingConfigurer {

    private final ClusterExecutor clusterExecutor = new ClusterExecutor(buildExecutor());

    @Bean("clusterExecutor")
    public ClusterExecutor clusterExecutor() {
        return clusterExecutor;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadFactory factory = buildThreadFactory("clusterSchedule-thread-");
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10, factory);
        taskRegistrar.setScheduler(executorService);
    }

    private Executor buildExecutor() {
        ThreadFactory threadFactory = buildThreadFactory("clusterExecutor-thread-");
        ThreadPoolExecutor.CallerRunsPolicy policy = new ThreadPoolExecutor.CallerRunsPolicy();
        Executor executor = new ThreadPoolExecutor(10, 20, 300L,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(100), threadFactory, policy);
        return executor;
    }

    private ThreadFactory buildThreadFactory(String threadNamePrefix) {
        // lambda表达式底层会生成内部类，并且把捕获的变量，作为这个生成类里面的成员变量存起来，形成强引用，保住堆上对象不被GC
        // 这里使用AtomicInteger来避免线程安全问题
        AtomicInteger threadCount = new AtomicInteger(0);
        return r -> {
            Thread thread = new Thread(r);
            thread.setName(threadNamePrefix + threadCount.getAndIncrement());
            thread.setUncaughtExceptionHandler((t, e) -> log.error("Thread {} 抛出异常：", t.getName(), e));
            return thread;
        };
    }
}
