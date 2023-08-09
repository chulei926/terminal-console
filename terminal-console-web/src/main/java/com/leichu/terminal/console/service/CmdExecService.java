package com.leichu.terminal.console.service;


import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.leichu.terminal.console.common.AppCtxWrapper;
import com.leichu.terminal.console.handler.WebSocketHandler;
import com.leichu.terminal.console.model.Msg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("cmdExecService")
public class CmdExecService implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(CmdExecService.class);

	@Resource
	private SshService sshService;

	@Subscribe
	public void handleEvent(Msg msg) {
		logger.info("获取到EventBus:{}", msg);
		String res = sshService.exec(msg.getSshSid(), msg.getCommand());
		WebSocketHandler socketHandler = AppCtxWrapper.getBean(WebSocketHandler.class);
		socketHandler.reply2User(msg.getWsSid(), res);
	}

	public void send(Msg msg) {
		EventBus eventBus = AppCtxWrapper.getBean(EventBus.class);
		eventBus.post(msg);
		logger.info("消息已通过EventBus发送：{}", msg);
	}
}
