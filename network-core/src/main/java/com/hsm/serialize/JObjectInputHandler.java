package com.hsm.serialize;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.ObjectInputStream;

public class JObjectInputHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        try(ByteBufInputStream in = new ByteBufInputStream(byteBuf);
            ObjectInputStream inn = new ObjectInputStream(in)){

            Object obj = inn.readObject();
            ctx.fireChannelRead(obj);
        }
    }
}
