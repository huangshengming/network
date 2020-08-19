package com.hsm.net;

import com.hsm.ApcDispatcher;
import com.hsm.GameMessage;
import com.hsm.INetSession;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class DispatcherHandler extends ChannelInboundHandlerAdapter {

    private ApcDispatcher apcDispatcher;
    private INetSession netSession;

    public DispatcherHandler(ApcDispatcher apcDispatcher, INetSession netSession){
        this.apcDispatcher = apcDispatcher;
        this.netSession = netSession;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        GameMessage gameMessage = (GameMessage) msg;
        gameMessage.setNetsession(netSession);

        apcDispatcher.call(this.netSession, gameMessage);
    }
}
