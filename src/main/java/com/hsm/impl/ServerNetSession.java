package com.hsm.impl;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;


public class ServerNetSession extends AbstractNetSession {
    public volatile Channel channel;

    public ServerNetSession(SocketChannel ch, boolean reliable) {
        super(reliable);
        this.channel = ch;
    }
    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "ServerNetSession{" +
                "channel=" + channel.toString() +
                '}';
    }
}
