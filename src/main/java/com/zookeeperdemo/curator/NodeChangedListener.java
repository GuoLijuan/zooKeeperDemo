package com.zookeeperdemo.curator;

import java.io.IOException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.log4j.Logger;

public class NodeChangedListener implements IZKListener {

	private static final Logger logger = Logger.getLogger(NodeChangedListener.class);
	
	private String path;
	
	public NodeChangedListener(String path) {
		this.path = path;
	}
	
	public void executor(CuratorFramework client) {
		logger.info("nodeChangedListener executor");
		
		final NodeCache nodeCache = new NodeCache(client, path);
		
		nodeCache.getListenable().addListener(new NodeCacheListener() {
			
			public void nodeChanged() throws Exception {
				logger.info("nodeChangedListener nodeChanged");
				
				byte[] data = nodeCache.getCurrentData().getData();
				if(data != null) {
					String newData = new String(data);
					System.out.println("new data: " + newData);
				}
				
			}
		});
		
		try {
			nodeCache.start();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		} finally {
			// TODO to close cache
//			try {
//	            nodeCache.close();
//            } catch (IOException e) {
//	            // TODO Auto-generated catch block
//	            e.printStackTrace();
//            }
		}
	}

}
