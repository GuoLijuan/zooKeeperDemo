package com.zookeeperdemo.curator;

import java.util.Arrays;
import java.util.List;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;

public class ZookeeperFactoryBean {

	private static final Logger logger = Logger.getLogger(ZookeeperFactoryBean.class);
	private static final String PATH = "/zk-examples";

	private String connectString;
	private List<IZKListener> listeners;

	private CuratorFramework client;

	public ZookeeperFactoryBean(String connectString, IZKListener... listeners) {
		this.connectString = connectString;
		this.listeners = Arrays.asList(listeners);
	}

	public void startService() throws Exception {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		client = CuratorFrameworkFactory.builder().connectString(this.connectString).retryPolicy(retryPolicy)
		        .sessionTimeoutMs(50000).build();
		client.start();
		
		// TODO
		client.create().withMode(CreateMode.PERSISTENT).forPath(PATH, "zk-example".getBytes());
		

		registListeners();
	}

	private void registListeners() {
		if (this.listeners == null || this.listeners.size() == 0)
			return;

		client.getConnectionStateListenable().addListener(new ConnectionStateListener() {

			public void stateChanged(CuratorFramework client, ConnectionState newState) {
				logger.info("state is changed");
				if (newState == ConnectionState.CONNECTED || newState == ConnectionState.RECONNECTED) {
					for (IZKListener listener : listeners) {
						listener.executor(client);
					}
				}
			}
		});
	}

	public void stopService() throws Exception {
		if (client != null) {
			client.delete().forPath(PATH);
			client.close();
		}
	}

	public static void main(String[] args) throws InterruptedException {
		ZookeeperFactoryBean bean = new ZookeeperFactoryBean("127.0.0.1:2181", new NodeChangedListener(PATH));
		try {
	        bean.startService();
	        Thread.sleep(60000);
        } catch (Exception e) {
	        // TODO Auto-generated catch block
	        logger.info(e);
        	e.printStackTrace();
        } finally {
        	try {
	            bean.stopService();
            } catch (Exception e) {
	            // TODO Auto-generated catch block
            	logger.info(e);
	            e.printStackTrace();
            }
        }
	}

	public String getConnectString() {
		return connectString;
	}

	public void setConnectString(String connectString) {
		this.connectString = connectString;
	}

	public List<IZKListener> getListeners() {
		return listeners;
	}

	public void setListeners(List<IZKListener> listeners) {
		this.listeners = listeners;
	}

}
