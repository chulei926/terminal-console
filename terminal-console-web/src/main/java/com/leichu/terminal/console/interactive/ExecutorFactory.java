package com.leichu.terminal.console.interactive;


import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.leichu.terminal.console.common.BizException;
import com.leichu.terminal.console.interactive.exception.ExecutorNotFoundException;
import com.leichu.terminal.console.interactive.exception.ProtocolMatchException;
import com.leichu.terminal.console.interactive.model.Protocol;
import org.apache.commons.io.FileUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutorFactory {

	private static final Logger logger = LoggerFactory.getLogger(ExecutorFactory.class);

	private static final Map<Protocol, Class<? extends Executor>> EXECUTORS = new ConcurrentHashMap<>();
	private static final Table<String, String, Protocol> PROTOCOL_TABLE = HashBasedTable.create();

	private static final String PROTOCOL_CONFIG_PREFIX = "PROTOCOL.";

	private ExecutorFactory() {
		// 初始化执行器
		String packageName = Executor.class.getPackage().getName();
		Reflections reflections = new Reflections(packageName);
		Set<Class<? extends AbstractExecutor>> clazzSet = reflections.getSubTypesOf(AbstractExecutor.class);
		Set<String> executorName = new TreeSet<>();
		for (Class<? extends Executor> clazz : clazzSet) {
			executorName.add(clazz.getSimpleName());
			ProtocolType protocolType = clazz.getAnnotation(ProtocolType.class);
			if (Objects.nonNull(protocolType.type())) {
				EXECUTORS.put(protocolType.type(), clazz);
			}
		}
		if (logger.isDebugEnabled()) {
			logger.info("Command executor init success. {}", executorName);
		}
		// 初始化协议表
		try {
			String classPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
			initProtocol(Paths.get(classPath, "config", "application.properties"));
			initProtocol(Paths.get(classPath, "config", "protocol.properties"));
		} catch (Exception e) {
			logger.error("Protocol properties init error!", e);
		}
		if (logger.isDebugEnabled()) {
			logger.info("Protocol properties init success. {}", PROTOCOL_TABLE);
		}
	}

	private void initProtocol(Path path) {
		try {
			File file = path.toFile();
			if (!file.exists()) {
				logger.warn("{} file not found!", path);
				return;
			}
			List<String> lines = FileUtils.readLines(file, StandardCharsets.UTF_8);
			for (String line : lines) {
				if (Objects.isNull(line) || line.trim().length() == 0 || !line.startsWith(PROTOCOL_CONFIG_PREFIX)) {
					continue;
				}
				line = line.substring(PROTOCOL_CONFIG_PREFIX.length());
				String[] split = line.split("=");
				String[] keys = split[0].split("\\.");
				String vendor = keys[0];
				String neType = keys[1];
				String protocol = split[1];
				PROTOCOL_TABLE.put(vendor, neType, Protocol.match(protocol));
			}
		} catch (Exception e) {
			logger.error("Protocol properties init error! Path:{}", path, e);
		}
	}


	private static class ExecutorFactoryHolder {
		private static final ExecutorFactory INSTANCE = new ExecutorFactory();
	}

	public static ExecutorFactory getInstance() {
		return ExecutorFactoryHolder.INSTANCE;
	}

	public Executor getExecutor(String vendor, String neType) {
		Protocol protocol = PROTOCOL_TABLE.get(vendor, "*");
		if (Objects.isNull(protocol)) {
			protocol = PROTOCOL_TABLE.get(vendor, neType);
		}
		if (Objects.isNull(protocol)) {
			throw new ProtocolMatchException(vendor, neType);
		}
		return getExecutor(protocol);
	}

	public Executor getExecutor(Protocol protocol) {
		Class<? extends Executor> clazz = EXECUTORS.get(protocol);
		if (Objects.isNull(clazz)) {
			throw new ExecutorNotFoundException(protocol.name());
		}
		Executor executor;
		try {
			executor = clazz.newInstance();
		} catch (Exception e) {
			throw new BizException("Error creating executor instance!");
		}
		return executor;
	}

}
