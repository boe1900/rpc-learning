package com.bobo.rpc.registry.Zookeeper;

import com.bobo.rpc.registry.ServiceRegistry;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by huabo on 2017/7/6.
 */
public class ZookeeperServiceRegistry implements ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperServiceRegistry.class);

    private final ZkClient zkClient;

    public ZookeeperServiceRegistry(String zkAddress) {
        zkClient = new ZkClient(zkAddress,Constant.ZK_SESSION_TIMEOUT,Constant.ZK_CONNECTION_TIMEOUT);
        LOGGER.debug("connect zookeeper");
    }

    public void register(String serviceName, String serviceAddress) {
        //创建 Registry 节点(持久)
        String registryPath = Constant.ZK_REGISTRY_PATH;
        if(!zkClient.exists(registryPath)){
            zkClient.createPersistent(registryPath);
            LOGGER.debug("create registry node :{}" + registryPath);
        }
        //创建 service节点
        String servicePath = registryPath + "/" +serviceName;
        if(!zkClient.exists(servicePath)){
            zkClient.createPersistent(servicePath);
            LOGGER.debug("create service node :{}" + servicePath);
        }
        //创建 address 节点(临时)
        String addressPath = servicePath + "/address-";
        String addressNode = zkClient.createEphemeralSequential(addressPath,serviceAddress);
        LOGGER.debug("create address node :{}",addressNode);
    }
}
