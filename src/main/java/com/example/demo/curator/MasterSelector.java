package com.example.demo.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

/**
 * Created by xushengxiang on 2017/7/14.
 */
public class MasterSelector {
    public static void main(String[] args) throws Exception {
        String path = "/zk-demo";
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("112.74.206.153:2181")
                .sessionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        client.start();
        LeaderSelector leaderSelector = new LeaderSelector(client, path, new LeaderSelectorListenerAdapter() {
            @Override
            public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
                System.out.println("select master");
                Stat stat = client.checkExists().forPath(path);
                if(stat == null){
                    System.out.println("node not exist");
                } else {
                    curatorFramework.delete().deletingChildrenIfNeeded().forPath(path);
                }
                curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, "init".getBytes());

                Thread.sleep(3000);
                curatorFramework.delete().deletingChildrenIfNeeded().forPath(path);
                System.out.println("release master");
            }
        });
        leaderSelector.autoRequeue();
        leaderSelector.start();
        Thread.sleep(Integer.MAX_VALUE);
    }
}
