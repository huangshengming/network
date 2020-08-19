package com.hsm;

import com.hsm.impl.GameMessageEventGroup;
import com.hsm.impl.ServerNetSession;
import com.hsm.net.BaseServer;
import com.hsm.net.DispatcherHandler;
import com.hsm.handler.ChannelEventHandler;
import com.hsm.serialize.JObjectInputHandler;
import com.hsm.serialize.JObjectOutputHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

public class ApcServer extends BaseServer {

    public static Builder newBuilder(){ return new Builder();}

    public static class Builder {

        private ApcDispatcher apcDispatcher = new ApcDispatcher();
        private boolean reliable = true;
        private int bossCount = 1;  // 默认只有一个boss线程
        private int workerCount;
        private GameMessageEventGroup gameMessageEventGroup;
        private INetSessionEventHandler sessionEventHandler;


        public <O extends T, T> Builder registerCallBack(O instance, Class<T> clazz){
            this.apcDispatcher.registerMethod(instance, clazz);
            return this;
        }
        public Builder setGameMessageEventGroup(int nThreads){
            this.gameMessageEventGroup = new GameMessageEventGroup(nThreads);
            return this;
        }

        public Builder setWorkerCount(int workerCount) {
            this.workerCount = workerCount;
            return this;
        }

        public Builder setReliable(boolean reliable) {
            this.reliable = reliable;
            return this;
        }

        public Builder setSessionEventHandler(INetSessionEventHandler sessionEventHandler) {
            this.sessionEventHandler = sessionEventHandler;
            return this;
        }

        public ApcServer build(){
            return new ApcServer(reliable,
                    bossCount,
                    workerCount,
                    apcDispatcher,
                    gameMessageEventGroup,
                    sessionEventHandler);
        }
    }

    private ApcDispatcher apcDispatcher;
    private boolean reliable;
    private GameMessageEventGroup gameMessageEventGroup;
    private INetSessionEventHandler sessionEventHandler;

    private ApcServer(boolean reliable,
                      int bossCount,
                      int workerCount,
                      ApcDispatcher apcDispatcher,
                      GameMessageEventGroup gameMessageEventGroup,
                      INetSessionEventHandler sessionEventHandler){
        super(bossCount, workerCount);
        this.reliable = reliable;
        this.apcDispatcher = apcDispatcher;
        this.gameMessageEventGroup = gameMessageEventGroup;
        this.sessionEventHandler = sessionEventHandler;
    }

    @Override
    public void initClientPipeline(SocketChannel socketChannel, ChannelPipeline pipeline) {
        ServerNetSession serverNetSession = new ServerNetSession(socketChannel, reliable);
        serverNetSession.setGameMessageEventGroup(gameMessageEventGroup);

        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4, true));
        pipeline.addLast(new LengthFieldPrepender(4, 0));

        // 序列化
        pipeline.addLast(socketChannel.eventLoop(), new JObjectInputHandler());
        pipeline.addLast(socketChannel.eventLoop(), new JObjectOutputHandler());

        // 消息派发
        pipeline.addLast(gameMessageEventGroup, new DispatcherHandler(apcDispatcher, serverNetSession));

        pipeline.addLast(gameMessageEventGroup, new ChannelEventHandler(sessionEventHandler, serverNetSession));
    }
}
