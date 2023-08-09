package com.leichu.terminal.console.service.ssh;

import com.google.common.collect.Maps;
import org.apache.sshd.client.channel.ClientChannel;

import java.util.Map;

public class SshChannelHolder {

	private static volatile SshChannelHolder instance;

	private static final Map<String, ClientChannel> channelMap = Maps.newConcurrentMap();

	static {
		Runtime runtime = Runtime.getRuntime();
		runtime.addShutdownHook(new Thread(SshClientHolder::close));
	}

	private SshChannelHolder() {
	}

	public static SshChannelHolder getInstance() {
		if (instance == null) {
			synchronized (SshChannelHolder.class) {
				if (instance == null) {
					instance = new SshChannelHolder();
				}
			}
		}
		return instance;
	}

	public void registerChannel(String sid, ClientChannel channel) {
		channelMap.put(sid, channel);
	}

	public ClientChannel getChannel(String sid) {
		return channelMap.get(sid);
	}

	public void removeChannel(String sid) {
		if (channelMap.containsKey(sid)) {
			channelMap.get(sid).close(Boolean.TRUE);
		}
		channelMap.remove(sid);
	}

}
