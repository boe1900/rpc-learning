package com.bobo.rpc.registry;

/**
 * 服务发现
 */
public interface ServiceDiscovery {
    /**
     * 根据服务名称寻找服务地址
     *
     * @param serviceName 服务名称
     * @return 服务地址
     */
    String discover(String serviceName);
}
