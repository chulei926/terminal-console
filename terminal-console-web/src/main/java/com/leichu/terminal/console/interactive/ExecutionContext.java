package com.leichu.terminal.console.interactive;


import com.leichu.terminal.console.interactive.config.InteractiveConfig;
import com.leichu.terminal.console.interactive.model.AuthParam;
import com.leichu.terminal.console.interactive.model.Command;

import java.nio.charset.Charset;
import java.util.List;

/**
 * 执行下上文.
 *
 * @author leichu.
 * @since 2023-07-30.
 */
public class ExecutionContext {

	private String reqId;
	private AuthParam authParam;
	private InteractiveConfig config;
	private List<Command> commands;
	private Executor executor;
	private Charset charset = Charset.defaultCharset();
	private Boolean executorInitialized = Boolean.FALSE;

	public String getReqId() {
		return reqId;
	}

	public void setReqId(String reqId) {
		this.reqId = reqId;
	}

	public AuthParam getAuthParam() {
		return authParam;
	}

	public void setAuthParam(AuthParam authParam) {
		this.authParam = authParam;
	}

	public InteractiveConfig getConfig() {
		return config;
	}

	public void setConfig(InteractiveConfig config) {
		this.config = config;
	}

	public List<Command> getCommands() {
		return commands;
	}

	public void setCommands(List<Command> commands) {
		this.commands = commands;
	}

	public Executor getExecutor() {
		return executor;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public Charset getCharset() {
		return charset;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public void execute(Callback callback) {
		if (Boolean.FALSE.equals(this.executorInitialized)){
			this.executor.setAuthParam(authParam);
			this.executor.setInteractiveConfig(config);
			this.executor.setCharset(charset);
		}
		this.executor.execute(commands, callback);
	}

	public void initExecutor() {
		this.executor.setAuthParam(authParam);
		this.executor.setInteractiveConfig(config);
		this.executor.setCharset(charset);
		this.executorInitialized = Boolean.TRUE;
	}

	public void interrupt() {
		this.executor.interrupt();
	}
}
