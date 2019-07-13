package com.forever;

import com.forever.util.RpcRequest;
import com.forever.util.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * 代理类，封装了对远程服务器的调用
 */
public class RpcProxy {

    private String serverAddress;
    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(String serverAddress, ServiceDiscovery serviceDiscovery) {
        this.serverAddress = serverAddress;
        this.serviceDiscovery = serviceDiscovery;
    }

    public <T> T create(Class<?> interfaceClass){
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass},
                new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                RpcRequest rpcRequest = new RpcRequest();
                rpcRequest.setClassName(method.getDeclaringClass().getName());
                rpcRequest.setRequestId(UUID.randomUUID().toString());
                rpcRequest.setMethodName(method.getName());
                rpcRequest.setParameterTypes(method.getParameterTypes());
                rpcRequest.setParameters(args);
                if (serviceDiscovery != null){
                    serverAddress = serviceDiscovery.discovery();
                }
                String[] array = serverAddress.split(":");
                String host = array[0];
                String port = array[1];
                RpcClient client = new RpcClient(host,Integer.parseInt(port));
                RpcResponse response = client.send(rpcRequest);
                if (response.isError()){
                    return response.getError();
                }else {
                    return response.getResult();
                }
            }
        });
    }
}
