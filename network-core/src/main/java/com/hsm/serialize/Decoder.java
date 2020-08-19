package com.hsm.serialize;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.ObjectInputStream;
import java.util.List;

public class Decoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 对象反序列化
        ByteBufInputStream byteBufInputStream = new ByteBufInputStream(in);
        ObjectInputStream inn = new ObjectInputStream(byteBufInputStream);
        Object obj = inn.readObject();

        out.add(obj);
    }
}
