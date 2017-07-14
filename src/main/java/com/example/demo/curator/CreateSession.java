package com.example.demo.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

/**
 * Created by xushengxiang on 2017/7/14.
 */
public class CreateSession {
    public static void main(String[] args) throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        String path = "/zk-demo/c1";
        //CuratorFramework client = CuratorFrameworkFactory.newClient("112.74.206.153:2181", 5000, 3000, retryPolicy);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("112.74.206.153:2181")
                .sessionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .build();
        client.start();
        client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath( path, "init".getBytes());

        Stat stat = new Stat();
        System.out.println(new String(client.getData().storingStatIn(stat).forPath(path)));
        int version = client.setData().withVersion(stat.getVersion()).forPath(path, "update".getBytes()).getVersion();
        try {
            client.setData().withVersion(stat.getVersion()).forPath(path, "update fail".getBytes());
        } catch (Exception e){
            System.out.println("Fail set node date due to " + e.getMessage());
        }

        client.delete().deletingChildrenIfNeeded().withVersion(version).forPath(path);

    }
}
