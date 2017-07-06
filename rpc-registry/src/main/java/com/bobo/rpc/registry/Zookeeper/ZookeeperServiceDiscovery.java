package com.bobo.rpc.registry.Zookeeper;

import com.bobo.rpc.registry.ServiceDiscovery;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by huabo on 2017/7/6.
 */
public class ZookeeperServiceDiscovery implements ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperServiceDiscovery.class);

    private String zkAddress;

    public ZookeeperServiceDiscovery(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    public String discover(String serviceName) {
        ZkClient zkClient = new ZkClient(zkAddress,Constant.ZK_SESSION_TIMEOUT,Constant.ZK_CONNECTION_TIMEOUT);
        LOGGER.debug("connect zookeeper");
        try{
            //获取service节点
            String servicePath = Constant.ZK_REGISTRY_PATH + "/" + serviceName;

            if(!zkClient.exists(servicePath)){
                throw new RuntimeException(String.format("can not find any service node on path : %s",servicePath));
            }

            List<String> addressList = zkClient.getChildren(servicePath);
            if(addressList == null || addressList.size()<=0){
                throw new RuntimeException(String.format("can not find any address node on path: %s", servicePath));
            }
            //获取address节点
            String address;
            if(addressList.size() == 1){
                //若只有一个地址则获取这个地址
                address = addressList.get(0);
                LOGGER.debug("get only address node: {}",address);
            }else {
                //若多个地址，则随机获取一个地址
                address = addressList.get(ThreadLocalRandom.current().nextInt(addressList.size()));
                LOGGER.debug("get random address node: {}", address);
            }
            //获取address节点的值
            String addressPath = servicePath + "/" + address;


            return zkClient.readData(addressPath);
        }finally {
            zkClient.close();
        }
    }
}
