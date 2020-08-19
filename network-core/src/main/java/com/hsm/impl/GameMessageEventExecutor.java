package com.hsm.impl;

import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.SingleThreadEventExecutor;

import java.util.concurrent.Executor;

public class GameMessageEventExecutor extends SingleThreadEventExecutor {

    private long totalTakeTime;
    private long totalExecTime;

    protected GameMessageEventExecutor(EventExecutorGroup parent, Executor executor) {
        super(parent, executor, true);
    }

    @Override
    protected void run() {
        for (;;) {
            long takeStart = System.nanoTime();
            Runnable task = takeTask();
            long takeEnd = System.nanoTime();
            totalTakeTime += takeEnd-takeStart;

            if (task != null) {

                task.run();
                long runEnd = System.nanoTime();
                totalExecTime += runEnd - takeEnd;

                updateLastExecutionTime();
            }

            if (confirmShutdown()) {
                break;
            }
        }
    }

    public void resetTime() {
        totalExecTime = 0;
        totalTakeTime = 0;
    }

    public long getTotalTakeTime() {
        return totalTakeTime;
    }

    public long getTotalExecTime() {
        return totalExecTime;
    }
}
