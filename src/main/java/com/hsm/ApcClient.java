package com.hsm;

import com.hsm.handler.ChannelEventHandler;
import com.hsm.impl.ClientNetSession;
import com.hsm.impl.GameMessageEventGroup;
import com.hsm.net.DispatcherHandler;
import com.hsm.serialize.JObjectInputHandler;
import com.hsm.serialize.JObjectOutputHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.net.InetSocketAddress;

public class ApcClient {

    public static Builder newBuilder(){
        return new Builder();
    }
    public static class Builder {

        private NioEventLoopGroup nioEventLoopGroup;
        private GameMessageEventGroup messageEventGroup;
        private ApcDispatcher apcDispatcher = new ApcDispatcher();

        public <O extends T, T> Builder registerCallback(O instance, Class<T> clazz){
            apcDispatcher.registerMethod(instance, clazz);
            return this;
        }
        public  Builder setNioThreadCnt(int nThreads){
            this.nioEventLoopGroup = new NioEventLoopGroup(nThreads);
            return this;
        }
        public  Builder setMessageThreadCnt(int nThreads){
            this.messageEventGroup = new GameMessageEventGroup(nThreads);
            return this;
        }

        public ApcClient build(){
            return new ApcClient(apcDispatcher,
                    messageEventGroup,
                    nioEventLoopGroup);
        }
    }

    private NioEventLoopGroup nioEventLoopGroup;
    private GameMessageEventGroup messageEventGroup;
    private ApcDispatcher apcDispatcher;

    public ApcClient(ApcDispatcher dispatcher,
                     GameMessageEventGroup messageEventGroup,
                     NioEventLoopGroup nioEventLoopGroup){
        this.apcDispatcher = dispatcher;
        this.messageEventGroup = messageEventGroup;
        this.nioEventLoopGroup = nioEventLoopGroup;
    }


    public ClientNetSession connect(String name, String ip, int port, INetSessionEventHandler sessionEventHandler){
        ClientNetSession clientNetSession = new ClientNetSession(ip, port, this, true);
        return connect(clientNetSession, ip, port, sessionEventHandler);
    }

    public ClientNetSession connect(ClientNetSession session, String ip, int port, INetSessionEventHandler sessionEventHandler){

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(nioEventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(getInitializeHandler(session, sessionEventHandler));

        // option
        initOptions(bootstrap);

        // 建立与服务端连接
        ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(ip, port));
        Channel channel = channelFuture.channel();

        session.setChannel(channel);
        return session;
    }

    private ChannelInitializer<SocketChannel> getInitializeHandler(ClientNetSession session, INetSessionEventHandler sessionEventHandler){

        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                session.setChannel(ch);
                session.setGameMessageEventGroup(messageEventGroup);

                pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4, true));
                pipeline.addLast(new LengthFieldPrepender(4, 0));

                // 序列化
                pipeline.addLast(ch.eventLoop(), new JObjectInputHandler());
                pipeline.addLast(ch.eventLoop(), new JObjectOutputHandler());

                // 消息派发
                pipeline.addLast(messageEventGroup, new DispatcherHandler(apcDispatcher, session));

                pipeline.addLast(messageEventGroup, new ChannelEventHandler(sessionEventHandler, session));
            }
        };
    }

    public void initOptions(Bootstrap bootstrap){
        // 添加需要的tcp参数

    }

}
