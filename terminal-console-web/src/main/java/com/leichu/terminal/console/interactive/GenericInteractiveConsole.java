package com.leichu.terminal.console.interactive;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.leichu.terminal.console.interactive.config.InteractiveConfig;
import com.leichu.terminal.console.interactive.exception.CommandErrorException;
import com.leichu.terminal.console.interactive.model.Command;
import com.leichu.terminal.console.interactive.utils.DateTimeUtils;
import com.leichu.terminal.console.interactive.utils.RegUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class GenericInteractiveConsole implements InteractiveConsole {

	private static final Logger logger = LoggerFactory.getLogger(GenericInteractiveConsole.class);

	protected static final int EXEC_WAIT_TIMEOUT = 100;

	protected static final String ENTER_KEY = "\n";
	protected static final String NEXT_LINE_REG = "[\r\n]";
	protected static final String MULTI_LINE_REG = "[\r\n]+";
	protected static final String LINE_REG = "\n";

	protected static final Splitter SPLITTER = Splitter.onPattern(NEXT_LINE_REG).trimResults().omitEmptyStrings();
	private final static ExecutorService CACHED_THREAD_POOL = Executors.newCachedThreadPool();

	private final InteractiveConfig config;
	private final Charset charset;


	protected GenericInteractiveConsole(InteractiveConfig config, Charset charset) {
		this.config = config;
		this.charset = charset;
	}

	/**
	 * 下发命令。
	 *
	 * @param command 命令文本。
	 * @return 命令执行结果。
	 * @throws Exception 异常。
	 */
	public String sendCommand(Command command) throws Exception {
		writeCommand(command.getCommand());
		String response = readResponse(command, command.getTimeout());
		if (StringUtils.isBlank(response)) {
			return "";
		}
		return response.replaceAll(MULTI_LINE_REG, LINE_REG).replaceAll("\n$", "");
	}

	public abstract void writeCommand(String command) throws Exception;

	public abstract String readResponse() throws Exception;

	private String readResponse(Command command, Long timeout) throws Exception {
		Future<String> future = CACHED_THREAD_POOL.submit(() -> readResponse(command));
		return future.get(null == timeout ? EXEC_WAIT_TIMEOUT : timeout, TimeUnit.SECONDS);
	}

	public String readResponse(Command command) throws Exception {
		StringBuffer result = new StringBuffer();
		for (; ; ) {
			String responseContent = readResponse();
			if (StringUtils.isBlank(responseContent)) {
				if (Boolean.TRUE.equals(command.getIsLast())) {
					break;
				}
				continue;
			}

			// 命令执行是否出错
			MutablePair<Boolean, String> pair = errResponse(responseContent);
			if (Boolean.TRUE.equals(pair.left)) {
				result.append(responseContent);
				throw new CommandErrorException(command.getCommand(), responseContent, pair.right);
			}

			if (autoConfirm(responseContent)) { // 场景：Are you sure to continue?[Y/N]
				result.append(responseContent);
				break;
			}
			if (autoContinue(responseContent)) { // 场景：Please Press ENTER.
				result.append(responseContent);
				writeCommand(ENTER_KEY);
				String continueResponse = readResponse(command);
				result.append(continueResponse);
				// 匹配到结束符，直接结束
				if (matchEnd(continueResponse)) {
					break;
				}
			}
			// 匹配到分页符
			if (matchMore(responseContent)) {
				responseContent = removeMore(responseContent);
				result.append(responseContent).append(LINE_REG);
				StringBuffer moreContent = new StringBuffer();
				readMore(moreContent);
				result.append(moreContent).append(ENTER_KEY);
				if (matchEnd(moreContent.toString())) {
					break;
				}
			}
			// 匹配到结束符，直接结束
			if (matchEnd(responseContent)) {
				result.append(responseContent);
				break;
			}
			result.append(responseContent);
			DateTimeUtils.sleep(config.getReadTimeInterval());
		}
		return result.toString();
	}


	public String format(String content) {
		if (StringUtils.isBlank(content) || CollectionUtils.isEmpty(config.getRemoveCtrlCharIdentifier())) {
			return content;
		}
		List<String> lines = Splitter.onPattern(NEXT_LINE_REG).splitToList(content);
		List<String> fresh = new ArrayList<>();
		for (String line : lines) {
			for (String reg : config.getRemoveCtrlCharIdentifier()) {
				line = line.replaceAll(reg, "");
			}
			fresh.add(line);
		}
		return Joiner.on(LINE_REG).join(fresh);
	}

	public void readMore(StringBuffer moreContent) throws Exception {
		// 匹配到分页符
		String moreResponse;
		do {
			writeCommand(config.getPaginationPressKey());
			moreResponse = readResponse(new Command(config.getPaginationPressKey()));
			moreResponse = removeMore(moreResponse);
			moreContent.append(moreResponse).append(ENTER_KEY);
		} while (!matchEnd(moreResponse));
	}

	public boolean autoConfirm(String response) {
		if (StringUtils.isBlank(response)) {
			return false;
		}
		List<String> lines = SPLITTER.splitToList(StringUtils.trim(response));
		for (int i = lines.size() - 1; i >= 0; i--) {
			String line = StringUtils.trim(lines.get(i));
			if (StringUtils.isBlank(line)) {
				continue;
			}
			if (RegUtils.match(line, config.getAutoConfirmIdentifier())) {
				return true;
			}
		}
		return false;
	}


	public boolean autoContinue(String response) {
		if (StringUtils.isBlank(response)) {
			return false;
		}
		List<String> lines = SPLITTER.splitToList(StringUtils.trim(response));
		for (int i = lines.size() - 1; i >= 0; i--) {
			String line = StringUtils.trim(lines.get(i));
			if (StringUtils.isBlank(line)) {
				continue;
			}
			if (RegUtils.match(line, config.getAutoContinueIdentifier())) {
				return true;
			}
		}
		return false;
	}

	public boolean matchEnd(String content) {
		if (StringUtils.isBlank(content)) {
			return false;
		}
		List<String> lines = SPLITTER.splitToList(StringUtils.trim(content));
		if (CollectionUtils.isEmpty(lines)) {
			return false;
		}
		String lastLine = lines.get(lines.size() - 1);
		lastLine = removeCtrlChar(lastLine);
		lastLine = StringUtils.trim(lastLine);
		if (StringUtils.isBlank(lastLine)) {
			return false;
		}
		boolean matches = false;
		for (String regex : config.getEndIdentifier()) {
			if (RegUtils.match(lastLine, regex)) {
				matches = true;
				break;
			}
		}
		return matches;
	}

	public boolean matchMore(String str) {
		if (StringUtils.isBlank(StringUtils.trim(str)) || StringUtils.isBlank(config.getPaginationIdentifier())) {
			// 没有结束的正则，默认返回 true.
			return false;
		}
		List<String> lines = SPLITTER.splitToList(str);
		if (CollectionUtils.isEmpty(lines)) {
			return false;
		}
		String lastLine = StringUtils.trim(lines.get(lines.size() - 1));
		lastLine = removeCtrlChar(lastLine);
		boolean matches = RegUtils.match(lastLine, config.getPaginationIdentifier());
		return matches;
	}

	public String removeMore(String res) {
		if (StringUtils.isBlank(StringUtils.trim(res)) || StringUtils.isBlank(config.getPaginationIdentifier())) {
			return res;
		}
		List<String> lines = Splitter.onPattern(NEXT_LINE_REG).splitToList(res);
		if (CollectionUtils.isEmpty(lines)) {
			return res;
		}
		// 一般场景  ---- More ----
		// 特殊场景  ---- More ----Item=34060672-001
		List<String> fresh = new ArrayList<>();
		for (String line : lines) {
			line = removeCtrlChar(line);
			if (RegUtils.match(line, config.getPaginationIdentifier())) {
				line = line.replaceAll(config.getPaginationIdentifier(), "");
			}
			if (StringUtils.isNotBlank(line)) {
				fresh.add(line);
			}
		}
		return Joiner.on(LINE_REG).join(fresh);
	}

	public byte[] readInputStream(InputStream inputStream) throws IOException {
		int available = inputStream.available();
		if (available <= 0) {
			return new byte[]{};
		}
		int readCount = 0;
		byte[] res = new byte[available];
		while (readCount < available) {
			readCount += inputStream.read(res, readCount, available - readCount);
		}
		return res;
	}

	public String removeCtrlChar(String content) {
		if (CollectionUtils.isEmpty(config.getRemoveCtrlCharIdentifier())) {
			return content;
		}
		for (String reg : config.getRemoveCtrlCharIdentifier()) {
			content = content.replaceAll(reg.equals("\\b") ? "\b" : reg, "");
		}
		return content;
	}


	public MutablePair<Boolean, String> errResponse(String response) {
		if (StringUtils.isBlank(response)) {
			return MutablePair.of(false, null);
		}
		List<String> lines = SPLITTER.splitToList(StringUtils.trim(response));
		for (String line : lines) {
			if (RegUtils.match(StringUtils.trim(line), config.getErrorStartIdentifier())) {
				return MutablePair.of(true, line);
			}
		}
		return MutablePair.of(false, null);
	}

	public InteractiveConfig getConfig() {
		return config;
	}

	public Charset getCharset() {
		return charset;
	}
}
