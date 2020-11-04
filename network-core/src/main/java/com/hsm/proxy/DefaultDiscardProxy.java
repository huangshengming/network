package com.hsm.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class DefaultDiscardProxy {
    private static Logger logger = LoggerFactory.getLogger(DefaultDiscardProxy.class);

    public static class DefaultInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            logger.warn("[DummyInvocationHandler] session is null or closed when calling {}", method.getName());
            return null;
        }
    }

    public static <T> T newDefaultClient(final Class<T> tClass){

        @SuppressWarnings("unchecked")
        T t = (T) Proxy.newProxyInstance(tClass.getClassLoader(),
                new Class[]{tClass},
                new DefaultInvocationHandler());
        return t;
    }
}
