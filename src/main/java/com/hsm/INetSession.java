package com.hsm;

import io.netty.channel.Channel;

public interface INetSession {

    boolean getReliable();

    Channel getChannel();

    boolean callRemote(GameMessage gameMessage);

    void callSelf(GameMessage gameMessage);

    String getName();

    void setName(String name);

    int getPort();

    String getRemoteAddress();

    <T> T newOutputProxy(Class<T> clazz);

    /**
     * 消息转发到对应服务，有服务转发到pc
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T newRelayProxy(Class<T> clazz);
}
