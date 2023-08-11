package com.leichu.terminal.console.service;

import com.google.common.base.Splitter;
import com.leichu.terminal.console.common.BizException;
import com.leichu.terminal.console.interactive.Executor;
import com.leichu.terminal.console.interactive.ExecutorRegistry;
import com.leichu.terminal.console.interactive.InteractiveConsole;
import com.leichu.terminal.console.interactive.utils.DateTimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class StreamReader implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(StreamReader.class);

	public volatile boolean INTERRUPTED = false;

	private final WebSocketSession wsSession;
	private final Executor executor;

	private final InteractiveConsole interactiveConsole;

	public StreamReader(WebSocketSession wsSession) {
		this.wsSession = wsSession;
		String sid = extractSid(wsSession);
		this.executor = ExecutorRegistry.getInstance().getExecutor(sid);
		this.interactiveConsole = this.executor.getConsole();
	}

	@Override
	public void run() {
		while (true) {
			try {
				byte[] resBytes = this.interactiveConsole.read();
				if (null != resBytes && resBytes.length > 0) {
					send(resBytes);
				}
			} catch (Exception e) {
				throw new BizException("读取通道出错", e);
			}

			DateTimeUtils.sleep(100L);
			if (INTERRUPTED) {
				break;
			}
		}
	}


	public void append(byte[] commandSegment) {
		try {
			this.interactiveConsole.write(commandSegment);
		} catch (Exception e) {
			throw new BizException("写入通道出错！", e);
		}
	}

	private void send(byte[] content) {
		TextMessage msg = new TextMessage(content);
		try {
			wsSession.sendMessage(msg);
		} catch (Exception e) {
			throw new BizException("Error sending message: " + new String(content, StandardCharsets.UTF_8));
		}
	}

	private String extractSid(WebSocketSession session) {
		if (session == null || session.getUri() == null) {
			return null;
		}
		String query = session.getUri().getQuery();
		if (StringUtils.isBlank(query) || !query.contains("sid")) {
			return null;
		}
		try {
			String decode = URLDecoder.decode(query, StandardCharsets.UTF_8.name());
			Map<String, String> split = Splitter.on("&").withKeyValueSeparator("=").split(decode);
			return split.get("sid");
		} catch (Exception e) {
			logger.error("Parameter sid extraction error！{}", session.getUri().toString());
		}
		return null;
	}


	public WebSocketSession getWsSession() {
		return wsSession;
	}

	public Executor getExecutor() {
		return executor;
	}
}
