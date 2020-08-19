package com.hsm.handler;

import com.hsm.INetSession;
import com.hsm.INetSessionEventHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.net.ConnectException;

public class ChannelEventHandler extends ChannelInboundHandlerAdapter {
    private INetSessionEventHandler handler;
    private INetSession session;

    public ChannelEventHandler(INetSessionEventHandler handler, INetSession session){
        this.handler = handler;
        this.session = session;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        assert session.getChannel() == ctx.channel();
        ctx.fireChannelActive();

        if (null != handler){
            // 连接成功
            handler.channelConnected(session);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        assert session.getChannel() == ctx.channel();
        ctx.fireChannelInactive();

        if (null != handler){
            // 连接断开
            handler.channelDisconnected(session);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        assert session.getChannel() == ctx.channel();
        ctx.fireExceptionCaught(cause);

        if (null != handler){
            if (cause instanceof ConnectException){
                // 抛出异常
                handler.channelConnectFailed(session);
            }else if (cause instanceof IOException){
                // 抛出异常
                handler.channelExceptionCaught(session, cause);
            }
        }
    }
}
