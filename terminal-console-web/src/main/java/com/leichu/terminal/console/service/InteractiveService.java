package com.leichu.terminal.console.service;

import com.alibaba.fastjson.JSONObject;
import com.leichu.terminal.console.interactive.Executor;
import com.leichu.terminal.console.interactive.ExecutorFactory;
import com.leichu.terminal.console.interactive.ExecutorRegistry;
import com.leichu.terminal.console.interactive.config.ConfigFactory;
import com.leichu.terminal.console.interactive.config.InteractiveConfig;
import com.leichu.terminal.console.interactive.model.AuthParam;
import com.leichu.terminal.console.interactive.model.Protocol;
import com.leichu.terminal.console.interactive.model.Vendor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service("interactiveService")
public class InteractiveService {

	private static final Logger logger = LoggerFactory.getLogger(InteractiveService.class);

	public JSONObject auth(AuthParam authParam, Protocol protocol, Vendor vendor) {
		logger.info(">>> auth: {}", authParam);
		String sid = UUID.randomUUID().toString();
		InteractiveConfig config = ConfigFactory.getInstance().getConfig(vendor);
		Executor executor = ExecutorFactory.getInstance().getExecutor(protocol);
		executor.setAuthParam(authParam);
		executor.setInteractiveConfig(config);
		executor.setCharset(StandardCharsets.UTF_8);
		executor.connect();
		ExecutorRegistry.getInstance().register(sid, executor);
		return new JSONObject().fluentPut("sid", sid);
	}

}
