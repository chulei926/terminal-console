package com.leichu.terminal.console.interactive;

import com.leichu.terminal.console.interactive.config.InteractiveConfig;
import com.leichu.terminal.console.interactive.exception.CommandErrorException;
import com.leichu.terminal.console.interactive.exception.CommandTimeoutException;
import com.leichu.terminal.console.interactive.exception.InteractiveException;
import com.leichu.terminal.console.interactive.model.AuthParam;
import com.leichu.terminal.console.interactive.model.Command;
import com.leichu.terminal.console.interactive.model.ExecStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

public abstract class AbstractExecutor implements Executor {

	private static final Logger logger = LoggerFactory.getLogger(AbstractExecutor.class);

	protected AuthParam authParam;
	protected List<Command> commands;
	protected InteractiveConfig interactiveConfig;
	protected Charset charset = Charset.defaultCharset();
	protected volatile Boolean connected = Boolean.FALSE;
	protected InteractiveConsole interactiveConsole;

	@Override
	public void setAuthParam(AuthParam authParam) {
		this.authParam = authParam;
	}

	@Override
	public void setInteractiveConfig(InteractiveConfig interactiveConfig) {
		this.interactiveConfig = interactiveConfig;
	}

	@Override
	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public abstract void connect();

	public abstract void disconnect();

	public abstract void preHandle();

	public abstract void postHandle();

	public abstract String sendCommand(Command command) throws Exception;

	@Override
	public void execute(List<Command> commands, Callback callback) {
		if (null == commands){
			throw new InteractiveException(this.authParam.getIp(), "Command list cannot be empty!");
		}
		this.commands = commands;
		try {
			if (Boolean.FALSE.equals(connected)){
				connect();
			}
			preHandle();
			for (Command command : this.commands) {
				execute(command, callback);
			}
			postHandle();
		} finally {
			disconnect();
		}
	}

	@Override
	public void interrupt() {
		if (null == this.commands){
			return;
		}
		for (Command command : this.commands) {
			if (ExecStatus.WAITING == command.getStatus()) {
				command.setInterrupt(Boolean.TRUE);
			}
		}
	}

	@Override
	public void execute(Command command, Callback callback) {
		logger.info("Executor ---> execute");
		if (Boolean.FALSE.equals(connected)){
			throw new InteractiveException(this.authParam.getIp(), "Please establish a connection first!");
		}
		if (Boolean.TRUE.equals(command.getInterrupt())) {
			logger.warn("Command execution is canceled！{}", command);
			command.setStatus(ExecStatus.BREAK);
			try {
				callback.emit(command);
			} catch (Exception e) {
				logger.error("Callback function execution error! Command:{}", command, e);
			}
			return;
		}
		command.setStartTime(new Date());
		String result = null;
		try {
			command.setStatus(ExecStatus.EXECUTING);
			result = sendCommand(command);
			command.setStatus(ExecStatus.SUCCESS);
		} catch (Exception e) {
			command.setStatus(ExecStatus.FAILED);
			logger.error("Command execution error! Command:{}, Msg:{}", command, e.getMessage(), e);
			if (e instanceof CommandTimeoutException) {
				CommandTimeoutException exp = (CommandTimeoutException) e;
				command.setFailDesc(exp.getMessage());
			} else if (e instanceof CommandErrorException) {
				CommandErrorException exp = (CommandErrorException) e;
				command.setFailDesc(exp.getErrMsg());
			} else if (e instanceof InteractiveException) {
				InteractiveException exp = (InteractiveException) e;
				command.setFailDesc(exp.getErrMsg());
			} else {
				command.setFailDesc(e.getMessage());
			}
		} finally {
			command.setResult(result);
			command.setEndTime(new Date());
			command.setCostTime(command.getEndTime().getTime() - command.getStartTime().getTime());
			try {
				callback.emit(command);
			} catch (Exception e) {
				logger.error("Callback function execution error! Command:{}", command, e);
			}
		}
	}

	@Override
	public InteractiveConsole getConsole() {
		return interactiveConsole;
	}
}
