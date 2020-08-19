package com.hsm;

public interface INetSessionEventHandler {

    void channelConnected(INetSession session);
    void channelDisconnected(INetSession session);
    void channelConnectFailed(INetSession session);
    void channelExceptionCaught(INetSession session, Throwable cause);
}
