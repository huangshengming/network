package com.hsm.proxy;

import com.hsm.GameMessage;
import com.hsm.INetSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class SelfInvocationHandler implements InvocationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(SelfInvocationHandler.class);

    private final INetSession session;

    public SelfInvocationHandler(final INetSession session){
        this.session = session;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        try {
            if (null == session){
                LOG.error("session is null.functionName:{}", method.getName());
                return null;
            }
            synchronized (session){
                session.callSelf(new GameMessage(method.getName(), args));
            }
        }catch (Exception e){
            LOG.error("functionName:{}",method.getName(), e);
        }
        return null;
    }
}
