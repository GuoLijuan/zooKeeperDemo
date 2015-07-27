package com.zookeeperdemo;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * zookeeper api
 */
public class SimpleConnector {
	
	private static final Logger logger = Logger.getLogger(SimpleConnector.class);
	
	// root
	public static final String ROOT = "/root-ktv";
	
	private static final CountDownLatch connectedSemaphore = new CountDownLatch(1);
	
	private static ZooKeeper zk = null;

	public static void main(String[] args) throws Exception {
		// create connect
		zk = new ZooKeeper("localhost:2181", 30000, new Watcher() {
			// watcher event
			public void process(WatchedEvent event) {
				if(event.getState() == KeeperState.SyncConnected ) {
					if(event.getPath() == null && EventType.None == event.getType()) {
						connectedSemaphore.countDown();
					} else if (EventType.NodeDeleted == event.getType()) {
						Stat stat = new Stat();
						try {
	                        System.out.println("note is deleted: [" + event.getPath() + "] data: [" + zk.getData(event.getPath(), false, stat));
                        } catch (KeeperException e) {
                        	logger.error(e.getMessage(), e);
	                        e.printStackTrace();
                        } catch (InterruptedException e) {
                        	logger.error(e.getMessage(), e);
	                        e.printStackTrace();
                        }
					}
				}
				
				System.out.println("state:" + event.getState() + ":"
						+ event.getType() + ":" + event.getWrapper() + ":"
						+ event.getPath());
			}
		});

		System.out.println(zk.getState());
		
		connectedSemaphore.await();
		
		// root persistent
		zk.create(ROOT, "root-ktv".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
				CreateMode.PERSISTENT);

		// 
		zk.create(ROOT + "/hangzhouKTV", "hangzhouKTV".getBytes(),
				ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);

		// EPHEMERAL session
		zk.create(ROOT + "/beijingKTV", "beijingKTV".getBytes(),
				ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

		// 
		zk.create(ROOT + "/beijingKTV-c1", "beijingKTV-c1".getBytes(),
				ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

		// total
		List<String> ktvs = zk.getChildren(ROOT, true);
		System.out.println(Arrays.toString(ktvs.toArray()));
		
		for (String node : ktvs) {
			// delete note
			zk.delete(ROOT + "/" + node, -1);
		}
		// delete root
		zk.delete(ROOT, -1);
		
		zk.close();
	}
	
}
