package com.forever;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * 将服务端的地址注册到zookeeper中
 */
public class ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ServiceRegistry.class);

    private String registryAddress;

    private CountDownLatch latch = new CountDownLatch(1);

    public ServiceRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void register(String serverAddress){
        ZooKeeper zk = connectZkServer();
        if (zk!=null){
            createNode(zk,serverAddress);
        }
    }

    /**
     * 将服务地址放到zookeeper节点上
     * @param zk
     * @param serverAddress
     */
    private void createNode(ZooKeeper zk, String serverAddress) {
        try {
            if (zk.exists(Constant.ZK_REGISTRY_PATH,null)==null){
                zk.create(Constant.ZK_REGISTRY_PATH,null, ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
            }
            final String path = zk.create(Constant.ZK_DATA_PATH, serverAddress.getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            LOGGER.info("create zk node {} = {}",path,serverAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 连接zookeeper
     * @return
     */
    private ZooKeeper connectZkServer() {
        ZooKeeper zooKeeper = null;
        try {
            zooKeeper = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getState() == Event.KeeperState.SyncConnected){
                        LOGGER.info("zookeeper已连接上");
                        latch.countDown();
                    }
                }
            });
            latch.await();
        }catch (Exception e){
            LOGGER.error("",e);
        }
        return zooKeeper;
    }
}
