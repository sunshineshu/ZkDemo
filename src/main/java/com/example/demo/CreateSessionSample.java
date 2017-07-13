package com.example.demo;

import org.I0Itec.zkclient.ZkClient;

/**
 * Created by xushengxiang on 2017/7/13.
 */
public class CreateSessionSample {
    public static void main(String[] args) throws InterruptedException {
        String path = "/zk-book";
        ZkClient zkClient = new ZkClient("112.74.206.153:2181",5000);
        System.out.println("zookeeper session established.");
        zkClient.subscribeChildChanges(path, (parentPath, currentChildren) ->
                System.out.println(parentPath + "'s child changed, currentChildren :" + currentChildren));
        zkClient.createPersistent(path);
        Thread.sleep(1000);
        System.out.println(zkClient.getChildren(path));
        zkClient.createEphemeral(path + "/c1");
        Thread.sleep(10000);
        zkClient.delete(path+"/c1");
        Thread.sleep(1000);
        zkClient.delete(path);
    }
}
