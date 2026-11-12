package com.example.core.util;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * 集群执行器
 * <p>
 * 集群执行器，用于执行集群任务
 * @author xujiajun
 * @since 2026/11/10
 */
public class ClusterExecutor {

    private final Executor executor;

    public ClusterExecutor(Executor executor) {
        this.executor = executor;
    }

    public void execute(Runnable task) {
        execute(task, null);
    }

    public void execute(Runnable task, Consumer<Exception> failCallback) {
        execute("", task, failCallback);
    }

    public void execute(String taskName, Runnable task, Consumer<Exception> failCallback) {
        Runnable wrap = TaskUtils.wrap(taskName, task::run, failCallback);
        executor.execute(wrap);
        // 清理threadLocal资源
        TaskUtils.removeContext();
    }

}