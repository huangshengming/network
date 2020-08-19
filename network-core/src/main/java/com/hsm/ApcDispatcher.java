package com.hsm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApcDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(ApcDispatcher.class);

    static class Entry {
        Entry(Object instance, Method method)
        {
            this.instance = instance;
            this.method = method;
        }
        Object instance;
        Method method;
    }

    public Map<String, Entry> methodMap = new ConcurrentHashMap<>();

    public <O extends T, T> void registerMethod(O instance, Class<T> clazz){
        try{
            for (Method method : clazz.getMethods()){
                if (method.getDeclaringClass() != Object.class){

                    Method newMethod = instance.getClass().getMethod(method.getName(), method.getParameterTypes());
                    if (null != newMethod){
                        if (methodMap.containsKey(newMethod.getName())){
                            logger.error("method already register. {} in {}", newMethod.getName(), instance.getClass());
                        }
                        methodMap.put(method.getName(), new Entry(instance, method));
                    }else {
                        throw new IllegalArgumentException("method " + method.getName() + " not found in {}" + instance.getClass());
                    }
                }
            }
        }catch (Exception e){
            logger.error("register method failed.", e);
            throw new IllegalArgumentException(e);
        }
    }

    public Object call(INetSession netSession, GameMessage gameMessage) throws InvocationTargetException, IllegalAccessException {

        Object result = null;
        try{
            ApcHelper.setCurrentNetSession(netSession);
            Entry entry = methodMap.get(gameMessage.getFunctionName());
            if (null != entry){
                result = entry.method.invoke(entry.instance, gameMessage.getParameters());
            }else {
                logger.error("message {} not found in methodMap,", gameMessage.getFunctionName());
            }
        }finally {
            ApcHelper.setCurrentNetSession(null);
        }
        return result;
    }
}
