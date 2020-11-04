package com.hsm.impl;

import com.hsm.GameMessage;
import com.hsm.INetSession;
import com.hsm.IRelayProxy;
import com.hsm.net.DispatcherHandler;
import com.hsm.proxy.RelayInvocationHandler;
import com.hsm.proxy.RemoteInvocationHandler;
import com.hsm.proxy.SelfInvocationHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public abstract class AbstractNetSession implements INetSession, ChannelFutureListener {

    private static final Logger logger = LoggerFactory.getLogger(AbstractNetSession.class);

    volatile private boolean reliable;
    volatile private String name = "unknown";
    private GameMessageEventGroup gameMessageEventGroup;

    public AbstractNetSession(boolean reliable){
        this.reliable = reliable;
    }

    @Override
    public boolean getReliable() {
        return reliable;
    }

    @Override
    public void callSelf(GameMessage gameMessage) throws InvocationTargetException, IllegalAccessException {
        if (getChannel().eventLoop().inEventLoop()){
            // 如果是在netty线程里面
            final DispatcherHandler dispatcherHandler = getChannel().pipeline().get(DispatcherHandler.class);
            dispatcherHandler.callSelf(gameMessage);
        }else {
            // 在业务线程里面,封装成runnable丢到netty线程池
            getChannel().eventLoop().execute(()->{
                final DispatcherHandler dispatcherHandler = getChannel().pipeline().get(DispatcherHandler.class);
                try {
                    dispatcherHandler.callSelf(gameMessage);
                } catch (Exception e) {
                    logger.error("callSelf ", e);
                }
            });
        }
    }

    @Override
    public boolean callRemote(GameMessage gameMessage) {
        Channel channel = getChannel();
        if (!channel.isActive()){
            if (reliable){
                logger.error("channel {} is disconnect while calling {}.", channel.toString(), gameMessage.getFunctionName());
            }else {
                logger.info("channel {} is disconnect while calling {}.", channel.toString(), gameMessage.getFunctionName());
            }
        }
        if (reliable){//直接写入缓冲区
            return forceWrite(gameMessage, channel);
        }else {
            // 判断是否可以写
            return tryWrite(gameMessage, channel);
        }
    }

    private boolean forceWrite(GameMessage gameMessage, Channel channel){
        ChannelFuture f = channel.writeAndFlush(gameMessage);
        f.addListener(this);
        return true;
    }

    private boolean tryWrite(GameMessage gameMessage, Channel channel){
        if (channel.isWritable()){ // 检查netty写缓冲区水位
            ChannelFuture f = channel.writeAndFlush(gameMessage);
            f.addListener(this);
            return true;
        }else {
            logger.error("channel is not write, [functionName: {}], isConnected:{}, isOpen:{}", gameMessage.getFunctionName(), channel.isActive(), channel.isOpen());
        }
        return false;
    }

    @Override
    public String getName() {
        return name;
    }
    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getRemoteAddress(){
        Channel channel = getChannel();
        if (null != channel){
            SocketAddress socketAddress = channel.remoteAddress();
            if (socketAddress instanceof InetSocketAddress){
                final InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
                final InetAddress inetAddress = inetSocketAddress.getAddress();
                return inetAddress != null ? inetAddress.getHostAddress() : inetSocketAddress.getHostName();
            }
        }
        return null;
    }

    @Override
    public int getPort(){
        Channel channel = getChannel();
        if (null != channel){
            SocketAddress socketAddress = channel.remoteAddress();
            if (socketAddress instanceof InetSocketAddress){
                final InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
                return inetSocketAddress.getPort();
            }
        }
        return 0;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        // 消息发送出去，记录消息数
    }

    @Override
    public <T> T newOutputProxy(final Class<T> clazz){
        @SuppressWarnings("unchecked")
        T f = (T) Proxy.newProxyInstance(clazz.getClassLoader(),
                new Class[] { clazz},
                new RemoteInvocationHandler<T>(clazz, this));
        return f;
    }

    @Override
    public <T> T newRelayProxy(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        T f = (T) Proxy.newProxyInstance(clazz.getClassLoader(),
                new Class[]{clazz, IRelayProxy.class},
                new RelayInvocationHandler<T>(clazz, this));
        return f;
    }

    @Override
    public <T> T newSelfProxy(Class<T> tClass) {
        @SuppressWarnings("unchecked")
        T f = (T) Proxy.newProxyInstance(tClass.getClassLoader(),
                new Class[]{tClass},
                new SelfInvocationHandler(this));
        return f;
    }

    public void setGameMessageEventGroup(GameMessageEventGroup gameMessageEventGroup) {
        this.gameMessageEventGroup = gameMessageEventGroup;
    }

    abstract public Channel getChannel();
}
