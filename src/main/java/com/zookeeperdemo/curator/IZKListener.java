package com.zookeeperdemo.curator;

import org.apache.curator.framework.CuratorFramework;

public interface IZKListener {
	void executor(CuratorFramework client);
}
