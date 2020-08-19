package com.hsm.proxy;

import com.hsm.GameMessage;
import com.hsm.INetSession;
import com.hsm.IRelayService;
import com.hsm.ITargetParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class RelayInvocationHandler<T> implements InvocationHandler, ITargetParam {

    private static final Logger Log = LoggerFactory.getLogger(RelayInvocationHandler.class);
    private INetSession session;
    private ThreadLocal<Integer> userIdThread = ThreadLocal.withInitial(()-> -1);
    private IRelayService iRelayService;

    public RelayInvocationHandler(final Class<T> clazz, INetSession session){
        iRelayService = session.newOutputProxy(IRelayService.class);
        this.session = session;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try{
            if (method.getDeclaringClass().equals(ITargetParam.class)){
                method.invoke(this, args);
            }else {
                if (null != session && session.getChannel().isActive()){
                    iRelayService.relayGameMessage(userIdThread.get(), new GameMessage(method.getName(), args));
                }else {
                    Log.error("session is null or closed, uid:{}, message:{}",this.userIdThread.get(), method.getName());
                }
            }
        }finally {
            this.userIdThread.set(-1);
        }
        return null;
    }


    @Override
    public void setParamInfo(int userId) {
        this.userIdThread.set(userId);
    }

}
