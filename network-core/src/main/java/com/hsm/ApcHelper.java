package com.hsm;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApcHelper {

    private static final Logger logger = LoggerFactory.getLogger(ApcHelper.class);

    private static final ThreadLocal<INetSession> sessionThreadLocal = ThreadLocal.withInitial(()->{
        logger.error("no connection, no session");
        return null;
    });

    public static INetSession getCurrentNetSession() {
        return sessionThreadLocal.get();
    }
    public static void setCurrentNetSession(INetSession session) {
        sessionThreadLocal.set(session);
    }

    public static void reset(){
        sessionThreadLocal.set(null);
    }
}
