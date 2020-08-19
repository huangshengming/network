package com.hsm.serialize;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.io.ObjectOutputStream;

public class JObjectOutputHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ByteBuf byteBuf = Unpooled.buffer();
        try{
            ByteBufOutputStream out = new ByteBufOutputStream(byteBuf);
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(msg);
            oos.flush();
            oos.close();

            ctx.writeAndFlush(byteBuf, promise);

        }finally {
//            if (null != byteBuf){
//                byteBuf.release();
//            }
        }
    }
}
