package com.leichu.terminal.console.handler;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.leichu.terminal.console.common.AppCtxWrapper;
import com.leichu.terminal.console.common.BizException;
import com.leichu.terminal.console.model.Msg;
import com.leichu.terminal.console.service.CmdExecService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

	public static final Map<String, WebSocketSession> sessionMap = Maps.newConcurrentMap();

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String payload = message.getPayload();
		if (StringUtils.equals(payload, "hb")) {
			reply2User(session.getId(), "hb_ok");
			return;
		}
		logger.info("收到信息: {}", payload);

		JSONObject obj = JSONObject.parseObject(payload);
		Msg msg = new Msg()
				.setSshSid(obj.getString("sid"))
				.setWsSid(session.getId())
				.setCommand(obj.getString("command"));
		CmdExecService cmdExecService = AppCtxWrapper.getBean(CmdExecService.class);
		cmdExecService.send(msg);
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		logger.info("Connected ... {}", session.getId());
		sessionMap.put(session.getId(), session);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		if (session.isOpen()) {
			session.close();
		}
		sessionMap.remove(session.getId());
		logger.info("Session {} closed because of {}", session.getId(), status.getReason());
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable throwable) throws Exception {
		logger.error("error occured at sender {}", session, throwable);
	}

	public void reply2User(String wsSid, String message) {
		try {
			if (!sessionMap.containsKey(wsSid)) {
				throw new BizException("没有找到会话信息");
			}
			TextMessage msg = new TextMessage(message);
			WebSocketSession session = sessionMap.get(wsSid);
			if (session.isOpen()) {
				session.sendMessage(msg);
				logger.info("发送定向消息成功...{}", msg.getPayload());
			}
		} catch (Exception e) {
			logger.error("发送定向消息异常！", e);
		}
	}


}
