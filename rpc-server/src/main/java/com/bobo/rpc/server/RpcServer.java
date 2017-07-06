package com.bobo.rpc.server;

import com.bobo.rpc.common.bean.RpcRequest;
import com.bobo.rpc.common.bean.RpcResponse;
import com.bobo.rpc.common.codec.RpcDecoder;
import com.bobo.rpc.common.codec.RpcEncoder;
import com.bobo.rpc.registry.ServiceRegistry;
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
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huabo on 2017/7/6.
 */
public class RpcServer implements ApplicationContextAware,InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    private String serviceAddress;

    private ServiceRegistry serviceRegistry;

    private Map<String,Object> handlerMap = new HashMap<String, Object>();


    public RpcServer(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public RpcServer(String serviceAddress, ServiceRegistry serviceRegistry) {
        this.serviceAddress = serviceAddress;
        this.serviceRegistry = serviceRegistry;
    }



    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        Map<String ,Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
        if(MapUtils.isNotEmpty(serviceBeanMap)){
            for(Object serviceBean:serviceBeanMap.values()){
                RpcService rpcService = serviceBean.getClass().getAnnotation(RpcService.class);
                String serviceName = rpcService.value().getName();
                String serviceVersion = rpcService.version();
                if(!StringUtils.isEmpty(serviceVersion)){
                    serviceName += serviceVersion;
                }
                handlerMap.put(serviceName,serviceBean);
            }
        }

    }


    public void afterPropertiesSet() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast(new RpcDecoder(RpcRequest.class));
                            pipeline.addLast(new RpcEncoder(RpcResponse.class));
                            pipeline.addLast(new RpcServerHandler(handlerMap));
                        }
                    });
            // 获取 RPC 服务器的 IP 地址与端口号
            String[] addressArray = serviceAddress.split(":");
            String ip = addressArray[0];
            int port = Integer.parseInt(addressArray[1]);

            //启动RPC服务器
            ChannelFuture future = bootstrap.bind(ip,port);

            //注册RPC服务地址
            if(serviceRegistry != null){
                for(String interfaceName:handlerMap.keySet()){
                    serviceRegistry.register(interfaceName,serviceAddress);
                    LOGGER.debug("register service: {} => {}", interfaceName, serviceAddress);
                }
            }
            LOGGER.debug("server started on port {}", port);

            // 关闭 RPC 服务器
            future.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }


}
