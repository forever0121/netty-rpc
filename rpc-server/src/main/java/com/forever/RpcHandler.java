package com.forever;

import com.forever.util.RpcRequest;
import com.forever.util.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {


    Logger logger = LoggerFactory.getLogger(RpcHandler.class);

    private Map<String,Object> handlerMap;

    public RpcHandler(Map<String,Object> handlerMap){
        this.handlerMap = handlerMap;
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        RpcResponse response = new RpcResponse();
        response.setRequestId(rpcRequest.getRequestId());
        try {
            //根据request来处理具体的业务调用
            Object result = handle(rpcRequest);
            response.setResult(result);
        } catch (Throwable t) {
            response.setError(t);
        }
        channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private Object handle(RpcRequest rpcRequest) {
        try {
            final String className = rpcRequest.getClassName();
            final Object serviceBean = handlerMap.get(className);
            final String methodName = rpcRequest.getMethodName();
            System.out.println(methodName);
            Class<?> forName = Class.forName(className);
            Object[] parameters = rpcRequest.getParameters();
            final Method method = forName.getMethod(methodName, rpcRequest.getParameterTypes());
            return method.invoke(serviceBean,parameters);
        }catch (Exception e){
            logger.error("",e);
        }
        return null;
    }
}
