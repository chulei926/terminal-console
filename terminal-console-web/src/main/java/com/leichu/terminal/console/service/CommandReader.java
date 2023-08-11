package com.leichu.terminal.console.service;

import com.google.common.base.Splitter;
import com.leichu.terminal.console.common.BizException;
import com.leichu.terminal.console.interactive.Executor;
import com.leichu.terminal.console.interactive.ExecutorRegistry;
import com.leichu.terminal.console.interactive.model.Command;
import com.leichu.terminal.console.interactive.utils.DateTimeUtils;
import com.leichu.terminal.console.model.CtrlChar;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class CommandReader implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(CommandReader.class);

	private static final Map<Integer, String> COMMAND_HISTORY = new ConcurrentHashMap<>(new TreeMap<>());

	private static final StringBuffer BUFFER = new StringBuffer();
	public volatile boolean INTERRUPTED = false;

	private final WebSocketSession wsSession;
	private final Executor executor;

	private int historyPointer = -1;
	private int commandPointer = -1;

	private static final Pattern END_PATTERN = Pattern.compile(".*[\\r\\n]$");


	public CommandReader(WebSocketSession wsSession) {
		this.wsSession = wsSession;
		String sid = extractSid(wsSession);
		this.executor = ExecutorRegistry.getInstance().getExecutor(sid);

	}

	@Override
	public void run() {
		while (true) {
			// TODO 判断是否遇到换行，如果结尾是换行，说明命令输入结束，提交给执行器执行命令。
			DateTimeUtils.sleep(100L);
			if (INTERRUPTED) {
				clearBuffer();
				break;
			}
		}
	}


	public void append(byte[] commandSegment) {
		// 判断是否是控制字符，如果是控制字符，走控制字符的逻辑
		CtrlChar ctrlChar = CtrlChar.match(commandSegment);
		logger.info("Command segment has been received:{} CtrlChar:{}", commandSegment, ctrlChar);
		if (null != ctrlChar) {
			switch (ctrlChar) {
				case ENTER:
					Command command = new Command(BUFFER.toString());
					executor.execute(command, res -> {
						logger.info(">>>>> 命令[{}]执行完成：{}", command.getCommand(), res);
						send(String.format("%s%s", StringUtils.repeat("\b", command.getCommand().length()), command.getResult()));
					});
					if (BUFFER.length() == 0) {
						break;
					}
					historyPointer++;
					COMMAND_HISTORY.put(historyPointer, command.getCommand());
					clearBuffer();
					break;
				case UP:
					if (historyPointer < 0) {
						break;
					}
					if (historyPointer == COMMAND_HISTORY.size() - 1) {
						send(COMMAND_HISTORY.get(historyPointer));
					} else {
						String preCommand = COMMAND_HISTORY.get(historyPointer + 1);
						send(String.format("%s%s", StringUtils.repeat("\b", null == preCommand ? 0 : preCommand.length()), COMMAND_HISTORY.get(historyPointer)));
					}
					historyPointer--;
					break;
				case DOWN:
					if (historyPointer > COMMAND_HISTORY.size()) {
						historyPointer = COMMAND_HISTORY.size();
						break;
					}
					send(COMMAND_HISTORY.get(historyPointer));
					historyPointer++;
					break;
				case LEFT:
					break;
				case RIGHT:
					break;
				case ESC:
					break;
				default:
					throw new BizException("控制符未获取到");
			}
		} else {
			String str = new String(commandSegment, StandardCharsets.UTF_8);
			BUFFER.append(str);
			commandPointer = BUFFER.length();
			send(str);
		}

	}

	private void send(String content) {
		TextMessage msg = new TextMessage(content);
		try {
			wsSession.sendMessage(msg);
		} catch (Exception e) {
			throw new BizException("Error sending message: " + content);
		}
	}

	private void clearBuffer() {
		BUFFER.setLength(0);
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
