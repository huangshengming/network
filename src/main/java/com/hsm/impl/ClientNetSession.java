package com.hsm.impl;

import com.hsm.ApcClient;
import io.netty.channel.Channel;

public class ClientNetSession extends AbstractNetSession {

    private String ip;
    private int port;
    private volatile ApcClient apcClient;
    private volatile Channel channel;

    public ClientNetSession(String ip, int port, ApcClient apcClient, boolean reliable){
        super(reliable);
        this.ip = ip;
        this.port = port;
        this.apcClient = apcClient;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
