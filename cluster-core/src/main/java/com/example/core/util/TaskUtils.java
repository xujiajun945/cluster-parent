package com.example.core.util;

import com.example.core.config.ClusterAppContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.util.ObjectUtils;

import java.util.function.Consumer;

/**
 * 包装Runnable，处理日志、登录上下文、异常回调
 * spring原生也提供一个工具类，只需要配置好线程池并传值即可
 * @see org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor#setTaskDecorator
 * @see org.springframework.core.task.TaskDecorator
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
        // 处理日志traceId
        String traceId = MDC.get("traceId");
        ClusterAppContext context = CONTEXT.get();
        return () -> {
            try {
                // 设置MDC里的traceId
                if (!ObjectUtils.isEmpty(traceId)) {
                    MDC.put("traceId", traceId);
                }
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
                MDC.remove("traceId");
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
