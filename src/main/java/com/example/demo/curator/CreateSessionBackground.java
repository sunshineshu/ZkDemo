package com.example.demo.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by xushengxiang on 2017/7/14.
 */
public class CreateSessionBackground {
    public static void main(String[] args) throws Exception {
        String path = "/zk-demo/c1";
        //CuratorFramework client = CuratorFrameworkFactory.newClient("112.74.206.153:2181", 5000, 3000, retryPolicy);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("112.74.206.153:2181")
                .sessionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        CountDownLatch countDownLatch = new CountDownLatch(2);
        ExecutorService threadPool = Executors.newFixedThreadPool(2);

        client.start();
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).inBackground((curatorFramework, curatorEvent) -> {
            System.out.println("event[code = "+curatorEvent.getResultCode()+" ], type = ["+curatorEvent.getType()+" ]");
            countDownLatch.countDown();
        }, threadPool).forPath(path, "init".getBytes());

        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).inBackground((curatorFramework, curatorEvent) -> {
            System.out.println("event[code = "+curatorEvent.getResultCode()+" ], type = ["+curatorEvent.getType()+" ]");
            countDownLatch.countDown();
        }, threadPool).forPath(path, "init".getBytes());

        countDownLatch.await();
        threadPool.shutdown();


    }
}
