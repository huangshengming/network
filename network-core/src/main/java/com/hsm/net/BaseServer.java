package com.hsm.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseServer {
    private static final Logger LOG = LoggerFactory.getLogger(BaseServer.class);

    private ServerBootstrap serverBootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private Channel channel;


    public BaseServer(){
        this(1, 6);
    }
    public BaseServer(int bossCount, int workerCount){

        if (!Epoll.isAvailable()){
            serverBootstrap = new ServerBootstrap();
            bossGroup = new NioEventLoopGroup(bossCount);
            workerGroup = new NioEventLoopGroup(workerCount);
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);
        }else {
            serverBootstrap = new ServerBootstrap();
            bossGroup = new EpollEventLoopGroup(bossCount);
            workerGroup = new EpollEventLoopGroup(workerCount);
            serverBootstrap.group(bossGroup, workerGroup).channel(EpollServerSocketChannel.class);
        }
        // 初始化选项参数
        initOption(serverBootstrap);
    }


    public abstract void initClientPipeline(SocketChannel socketChannel, ChannelPipeline pipeline);

    private void clientChannelPipeFactory(ServerBootstrap serverBootstrap){
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ChannelPipeline p = ch.pipeline();
                initClientPipeline(ch, p);
            }
        });
    }

    private void initOption(ServerBootstrap serverBootstrap){
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
    }

    public void start(int port) throws InterruptedException {
        // 初始化处理消息handler
        clientChannelPipeFactory(serverBootstrap);
        // 绑定服务
        ChannelFuture future = serverBootstrap.bind(port).sync();
        future.syncUninterruptibly();
        this.channel = future.channel();
        this.channel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
                LOG.info("{}, 链路关闭。", future.channel());
            }
        });
    }

    public void shutDown(){
        try {
            if (this.channel != null){
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
                LOG.info("closed hook {}, 链路关闭。", this.channel);
            }
        } catch (Exception e) {
            LOG.error("关闭链路失败");
        }
    }
}
