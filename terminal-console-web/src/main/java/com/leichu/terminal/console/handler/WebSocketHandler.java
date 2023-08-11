package com.leichu.terminal.console.handler;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.leichu.terminal.console.interactive.Executor;
import com.leichu.terminal.console.interactive.ExecutorRegistry;
import com.leichu.terminal.console.interactive.model.Command;
import com.leichu.terminal.console.service.CommandReader;
import com.leichu.terminal.console.service.StreamReader;
import com.leichu.terminal.console.utils.ThreadPoolHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

//	public static final Map<WebSocketSession, CommandReader> COMMAND_READER_MAP = Maps.newConcurrentMap();
	public static final Map<WebSocketSession, StreamReader> STREAM_READER_MAP = Maps.newConcurrentMap();

	private static final ThreadPoolTaskExecutor THREAD_POOL = ThreadPoolHelper.createSimplePool(100, 100, "READ-THREAD-");

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		THREAD_POOL.execute(()-> STREAM_READER_MAP.get(session).append(message.asBytes()));
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		logger.info("Connected ... {}", session.getId());
		StreamReader streamReader = new StreamReader(session);
		STREAM_READER_MAP.put(session, streamReader);
		THREAD_POOL.execute(streamReader);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		if (session.isOpen()) {
			session.close();
		}
		StreamReader streamReader = STREAM_READER_MAP.get(session);
		if (null != streamReader){
			streamReader.INTERRUPTED = true;
		}
		logger.info("Session {} closed because of {}", session.getId(), status.getReason());
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable throwable) throws Exception {
		logger.error("error occured at sender {}", session, throwable);
	}



	public void reply2User(WebSocketSession session, String message) {
		try {
			if (StringUtils.isBlank(message)) {
				logger.warn("消息为空");
				return;
			}
			TextMessage msg = new TextMessage(message);
			if (session.isOpen()) {
				session.sendMessage(msg);
				logger.info("发送定向消息成功...{}", msg.getPayload());
			}
		} catch (Exception e) {
			logger.error("发送定向消息异常！", e);
		}
	}


}
