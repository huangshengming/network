package com.hsm.impl;

import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.MultithreadEventExecutorGroup;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

public class GameMessageEventGroup extends MultithreadEventExecutorGroup {

    public GameMessageEventGroup(int nThreads) {
        this(nThreads, null);
    }
    protected GameMessageEventGroup(int nThreads, ThreadFactory threadFactory) {
        super(nThreads, threadFactory);
    }

    @Override
    protected EventExecutor newChild(Executor executor, Object... args) throws Exception {
        return new GameMessageEventExecutor(this, executor);
    }
}
