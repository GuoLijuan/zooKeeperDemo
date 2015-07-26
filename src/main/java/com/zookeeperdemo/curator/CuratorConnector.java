package com.zookeeperdemo.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;

public class CuratorConnector {

	public static final String ROOT = "/root-curator";
	
	private static CuratorFramework client = null;
	
	public static void main(String[] args) throws Exception {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		client = CuratorFrameworkFactory.builder()
				.connectString("127.0.0.1:2181")
				.sessionTimeoutMs(60000).retryPolicy(retryPolicy)
				.build();
		client.start();
		
		client.create().withMode(CreateMode.PERSISTENT).forPath(ROOT, ROOT.getBytes());
		
		client.getData().usingWatcher(new CuratorWatcher() {
			public void process(WatchedEvent event) throws Exception {
				// TODO Auto-generated method stub
				System.out.println("node is changed. new: " + client.getData().forPath(ROOT).toString());
			}
		}).inBackground().forPath(ROOT);
		
		Thread.sleep(50000);
		
		client.delete().deletingChildrenIfNeeded().forPath(ROOT);
		client.close();
	}
}
