package com.hsm.serialize;

import com.hsm.GameMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ObjectOutputStream;

public class Encoder extends MessageToByteEncoder<GameMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, GameMessage msg, ByteBuf out) throws Exception {
        ByteBufOutputStream byteBufOutputStream = new ByteBufOutputStream(out);
        ObjectOutputStream oos = new ObjectOutputStream(byteBufOutputStream);
        oos.writeObject(msg);
        oos.flush();
        oos.close();

        ctx.writeAndFlush(out);
    }
}
