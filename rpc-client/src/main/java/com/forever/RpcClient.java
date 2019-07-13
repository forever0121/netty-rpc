package com.forever;

import com.forever.util.RpcDecoder;
import com.forever.util.RpcEncoder;
import com.forever.util.RpcRequest;
import com.forever.util.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {

    Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private final Object obj = new Object();

    private String host;
    private int port;
    private RpcResponse response;
    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public RpcResponse send(RpcRequest rpcRequest){
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(worker)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new RpcEncoder(RpcRequest.class));
                            pipeline.addLast(new RpcDecoder(RpcResponse.class));
                            pipeline.addLast(RpcClient.this);
                        }
                    }).option(ChannelOption.SO_KEEPALIVE,true);
            ChannelFuture future = bootstrap.connect(host, port).sync();
            future.channel().writeAndFlush(rpcRequest).sync();
            synchronized (obj){
                obj.wait();
            }
            if (response!=null){
                future.channel().closeFuture().sync();
            }
            return response;
        }catch (Exception e){
            logger.error("",e);
        }finally {
            worker.shutdownGracefully();
        }
        return null;
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        this.response = rpcResponse;
        synchronized (obj){
            obj.notify();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("client caught exception", cause);
        ctx.close();
    }
}
