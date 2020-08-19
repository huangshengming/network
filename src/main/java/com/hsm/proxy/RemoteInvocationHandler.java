package com.hsm.proxy;

import com.hsm.GameMessage;
import com.hsm.INetSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class RemoteInvocationHandler<T> implements InvocationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(RemoteInvocationHandler.class);

    private INetSession netSession;
    private Class<T> clazz;

    public RemoteInvocationHandler(final Class<T> clazz, INetSession session){
        this.clazz = clazz;
        this.netSession = session;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if (null == netSession){
                LOG.error("session is null.functionName:{}", method.getName());
                return null;
            }
            if (!netSession.getChannel().isActive()){
                LOG.error("channel is inactive.functionName:{}", method.getName());
                return null;
            }
            netSession.callRemote(new GameMessage(method.getName(), args));
            LOG.info("send message is success, message: {}", method.getName());
        }catch (Exception e){
            LOG.error("functionName:{}",method.getName(), e);
        }
        return null;
    }
}
