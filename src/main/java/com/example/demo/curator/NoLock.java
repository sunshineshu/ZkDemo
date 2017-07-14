package com.example.demo.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * Created by xushengxiang on 2017/7/14.
 */
public class NoLock {
    public static void main(String[] args) {
        String path = "/zk-demo";
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("112.74.206.153:2181,112.74.206.153:2182,112.74.206.153:2183")
                .sessionTimeoutMs(50000)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        client.start();
        final InterProcessMutex mutex = new InterProcessMutex(client, path);
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try{
                    countDownLatch.await();
                    mutex.acquire();
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss|SSS");
                    System.out.println("order no :" + sdf.format(new Date()));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        mutex.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            }).start();
        }
        countDownLatch.countDown();
    }
}
