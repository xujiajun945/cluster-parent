package com.example.core.util;

import com.example.core.config.ClusterAppContext;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 *
 * @author xujiajun
 * @since 2026/11/10
 */
@Slf4j
public class TaskUtils {

    private static final ThreadLocal<ClusterAppContext> CONTEXT = new ThreadLocal<>();

    public static void setContext(ClusterAppContext context) {
        CONTEXT.set(context);
    }

    public static void removeContext() {
        CONTEXT.remove();
    }

    @FunctionalInterface
    public interface AsyncBizTask {

        void execute() throws Exception;

    }

    /**
     * @param taskName 任务名
     * @param bizTask 业务逻辑
     * @param failCallback 失败回调：异常来了之后执行你自定义的补偿逻辑，可以传 null
     * @return 包装好的Runnable，可以丢给线程池execute
     */
    public static Runnable wrap(String taskName, AsyncBizTask bizTask, Consumer<Exception> failCallback) {
        ClusterAppContext context = CONTEXT.get();
        return () -> {
            try {
                // 设置登录上下文
                if (context != null) {
                    LoginUtils.setContext(context);
                }
                bizTask.execute();
            } catch (Exception e) {
                // 统一：必打的日志，兜底，所有任务都会走这里
                Thread thread = Thread.currentThread();
                log.error("异步任务执行异常, 进入callback，当前线程: {}, 当前任务：{}", thread.getName(), taskName, e);
                // 如果传入了失败回调，就执行任务自己专属的补偿逻辑
                if (failCallback != null) {
                    try {
                        failCallback.accept(e);
                    } catch (Exception exception) {
                        log.error("callback异常, 当前线程: {}", thread.getName(), exception);
                    }
                }
            } finally {
                // 清理threadLocal资源
                LoginUtils.removeContext();
            }
        };
    }

    public static Runnable wrap(String taskName, AsyncBizTask bizTask) {
        return wrap(taskName, bizTask, null);
    }

    public static Runnable wrap(AsyncBizTask bizTask, Consumer<Exception> failCallback) {
        return wrap("", bizTask, failCallback);
    }

    public static Runnable wrap(AsyncBizTask bizTask) {
        return wrap("", bizTask, null);
    }
}
