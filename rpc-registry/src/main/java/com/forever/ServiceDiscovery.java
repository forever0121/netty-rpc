package com.forever;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 从zookeeper中获取服务器的地址
 */
public class ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ServiceRegistry.class);

    private String registryAddress;

    private CountDownLatch latch = new CountDownLatch(1);

    private volatile List<String> dataList = new CopyOnWriteArrayList();

    public ServiceDiscovery(String registryAddress) {
        this.registryAddress = registryAddress;
        ZooKeeper zk = connectZkServer(registryAddress);
        if (zk!=null){
            watchNode(zk);
        }
    }

    /**
     * 节点监听
     * @param zk
     */
    private void watchNode(final ZooKeeper zk) {
        try {
            List<String> children = zk.getChildren(Constant.ZK_REGISTRY_PATH, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                        watchNode(zk);
                    }
                }
            });
            List<String> dataList = new CopyOnWriteArrayList<String>();
            for (String child : children) {
                byte[] bytes = zk.getData(Constant.ZK_REGISTRY_PATH + "/" + child, false, null);
                dataList.add(new String(bytes));
            }
            LOGGER.info("node data: {}", dataList);
            this.dataList = dataList;
        }catch (Exception e){
            LOGGER.error("",e);
        }

    }

    private ZooKeeper connectZkServer(String registryAddress) {
        ZooKeeper zooKeeper = null;
        try {
            zooKeeper = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getState() == Event.KeeperState.SyncConnected){
                        LOGGER.info("zookeeper连接成功");
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

    /**
     * 获取服务器的地址
     * @return
     */
    public String discovery(){
        String data = null;
        if (dataList.size()>0){
            if (dataList.size() == 1){
                data = dataList.get(0);
                LOGGER.info("data:{}",data);
            }else {
                data = dataList.get(ThreadLocalRandom.current().nextInt(dataList.size()));
                LOGGER.info("data:{}",data);
            }
        }
        return data;
    }
}
