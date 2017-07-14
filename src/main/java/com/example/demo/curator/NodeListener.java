package com.example.demo.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

/**
 * Created by xushengxiang on 2017/7/14.
 */
public class NodeListener {
    public static void main(String[] args) throws Exception {
        String path = "/zk-demo";
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("112.74.206.153:2181")
                .sessionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        String childPath = path + "/c1";
        client.start();
        Stat stat = client.checkExists().forPath(path);
        if(stat == null){
            System.out.println("node not exist");
        } else {
           client.delete().deletingChildrenIfNeeded().forPath(path);
        }
        client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, "init parent".getBytes());
        final NodeCache nodeCache = new NodeCache(client, path, false);
        nodeCache.start(true);
        nodeCache.getListenable().addListener(() -> System.out.println("node data update, path = "+path+", new data =  "+new String(nodeCache.getCurrentData().getData())));
        final PathChildrenCache childrenCache = new PathChildrenCache(client, path, true);
        childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        childrenCache.getListenable().addListener((curatorFramework, pathChildrenCacheEvent) -> {
            switch (pathChildrenCacheEvent.getType()){
                case CHILD_ADDED:
                    System.out.println("Child Added, path = "+pathChildrenCacheEvent.getData().getPath()+", data = "+
                    new String(pathChildrenCacheEvent.getData().getData()));
                    break;
                case CHILD_UPDATED:
                    System.out.println("Child Updated, path = "+pathChildrenCacheEvent.getData().getPath()+", data = "+
                            new String(pathChildrenCacheEvent.getData().getData()));
                    break;
                case CHILD_REMOVED:
                    System.out.println("Child Removed, path = "+pathChildrenCacheEvent.getData().getPath()+", data = "+
                            new String(pathChildrenCacheEvent.getData().getData()));
                    break;
                default:
                    break;
            }
        });
        Thread.sleep(1000);
        client.setData().forPath(path, "update".getBytes());
        Thread.sleep(1000);
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(childPath, "init child".getBytes());
        Thread.sleep(1000);
        client.setData().forPath(childPath, "update child".getBytes());
        client.delete().forPath(childPath);
        Thread.sleep(1000);
        client.delete().deletingChildrenIfNeeded().forPath(path);
    }
}
