package com.forever;

import com.forever.util.RpcDecoder;
import com.forever.util.RpcEncoder;
import com.forever.util.RpcRequest;
import com.forever.util.RpcResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class RpcServer implements InitializingBean, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(RpcServer.class);

    private ServiceRegistry serviceRegistry;
    private String serverAddress;

    public RpcServer(ServiceRegistry serviceRegistry, String serverAddress) {
        this.serviceRegistry = serviceRegistry;
        this.serverAddress = serverAddress;
    }

    private Map<String,Object> handlerMap = new ConcurrentHashMap();

    public RpcServer(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void afterPropertiesSet() throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss,worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new RpcDecoder(RpcRequest.class));
                            pipeline.addLast(new RpcEncoder(RpcResponse.class));
                            pipeline.addLast(new RpcHandler(handlerMap));
                        }
                    }).option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true);
            String[] array = serverAddress.split(":");
            String host = array[0];
            int port = Integer.parseInt(array[1]);
            ChannelFuture future = serverBootstrap.bind(host, port).sync();
            LOGGER.info("server started on port {}", port);
            if (serviceRegistry!=null){
                serviceRegistry.register(serverAddress);
            }
            future.channel().closeFuture().sync();
        }finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    /**
     * 将带有RpcService注解的类加载到map中
     * @param applicationContext
     * @throws BeansException
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        final Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (MapUtils.isNotEmpty(beansWithAnnotation)){
            for (Object value : beansWithAnnotation.values()) {
                String name = value.getClass().getAnnotation(RpcService.class).value().getName();
                handlerMap.put(name,value);
            }
        }
    }
}
