package com.zookeeperdemo.zkclient;

import java.util.Arrays;
import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

/**
 * zkClient sample
 * @author guolijuan
 *
 */
public class ZKClientConnector {

	private static final String ROOT = "/root-sample";
	
	public static void main(String[] args) throws InterruptedException {
		ZkClient zkClient = new ZkClient("127.0.0.1:2181", 30000);
		System.out.println("established.");
		
		// regist listener
		zkClient.subscribeChildChanges(ROOT, new IZkChildListener() {
			
			public void handleChildChange(String path, List<String> childList)
					throws Exception {
				System.out.println("path: " + path);
				
				System.out.println("children is: " + (childList == null ? "null" :Arrays.toString(childList.toArray())));
			}
		});
		
		zkClient.createPersistent(ROOT, ROOT);
		
		Thread.sleep(1000);
		zkClient.createPersistent(ROOT+"/c1", "c1");
		
		Thread.sleep(1000);
		zkClient.createPersistent(ROOT+"/c2", "c2");
		
		// regist data changes listener
		zkClient.subscribeDataChanges(ROOT+"/c2", new IZkDataListener() {
			
			public void handleDataDeleted(String path) throws Exception {
				System.out.println(path + " is deleted");
			}
			
			public void handleDataChange(String path, Object data) throws Exception {
				System.out.println(path + "[changed] " + data);
			}
		});
		
		Thread.sleep(1000);
		zkClient.writeData(ROOT+"/c2", "change c2");
		
		Thread.sleep(1000);
		zkClient.delete(ROOT+"/c2");
		
		Thread.sleep(1000);
		zkClient.delete(ROOT+"/c1");
		
		Thread.sleep(1000);
//		zkClient.delete(ROOT);
		
		Thread.sleep(1000);
		
		zkClient.close();
	}
}
