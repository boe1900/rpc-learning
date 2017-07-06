package com.bobo.rpc.client;

import com.bobo.rpc.common.bean.RpcRequest;
import com.bobo.rpc.common.bean.RpcResponse;
import com.bobo.rpc.registry.ServiceDiscovery;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * 代理类
 */
public class RpcProxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProxy.class);

    private String serviceAddress;

    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    public RpcProxy(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }


    public <T> T create(final Class<?> interfaceClass){
        return create(interfaceClass,"");
    }

    @SuppressWarnings("unchecked")
    public <T> T create(final Class<?> interfaceClass, final String serviceVersion){
        //创建动态代理对象
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, new InvocationHandler() {

            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //创建rpc请求并设置请求属性
                RpcRequest request = new RpcRequest();
                request.setRequestId(UUID.randomUUID().toString());
                request.setInterfaceName(method.getDeclaringClass().getName());
                request.setMethodName(method.getName());
                request.setParameterTypes(method.getParameterTypes());
                request.setParameters(args);
                //获取rpc服务地址
                if(serviceDiscovery != null){
                    String serviceName = interfaceClass.getName();
                    if(StringUtils.isNotEmpty(serviceVersion)){
                        serviceName += serviceVersion;
                    }
                    serviceAddress = serviceDiscovery.discover(serviceName);
                    LOGGER.debug("discover service: {} => {}", serviceName, serviceAddress);
                }

                if (StringUtils.isEmpty(serviceAddress)) {
                    throw new RuntimeException("server address is empty");
                }

                // 从 RPC 服务地址中解析主机名与端口号
                String[] addressArray = serviceAddress.split(":");
                String host = addressArray[0];
                int port = Integer.parseInt(addressArray[1]);
                // 创建 RPC 客户端对象并发送 RPC 请求
                RpcClient client = new RpcClient(host,port);
                long time = System.currentTimeMillis();
                RpcResponse response = client.send(request);
                LOGGER.debug("time: {}ms", System.currentTimeMillis() - time);
                if (response == null) {
                    throw new RuntimeException("response is null");
                }

                if(response.hasException()){
                    throw response.getException();
                }else {
                    return response.getResult();
                }

            }
        });


    }


}
