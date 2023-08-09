package com.leichu.terminal.console.interactive;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 执行器注册中心.
 *
 * @author leichu.
 * @since 2023-07-30.
 */
public class ExecutorRegistry {

	private static volatile ExecutorRegistry instance;

	private static final Map<String, Executor> EXECUTOR_MAP = new ConcurrentHashMap<>();

	private ExecutorRegistry() {
	}

	public static ExecutorRegistry getInstance() {
		if (instance == null) {
			synchronized (ExecutorRegistry.class) {
				if (instance == null) {
					instance = new ExecutorRegistry();
				}
			}
		}
		return instance;
	}

	public void register(String sessionId, Executor executor) {
		EXECUTOR_MAP.put(sessionId, executor);
	}

	public Executor getExecutor(String sessionId) {
		return EXECUTOR_MAP.get(sessionId);
	}

}
