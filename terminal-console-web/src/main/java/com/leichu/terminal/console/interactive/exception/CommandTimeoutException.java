package com.leichu.terminal.console.interactive.exception;

/**
 * 命令执行超时异常.
 *
 * @author leichu.
 * @since 2023-07-30.
 */
public class CommandTimeoutException extends GenericException {

	private static final Integer CODE = 500;
	private static final String MSG_FORMAT = "Command execution timeout!";
	private final String command;

	public CommandTimeoutException(String command) {
		super(CODE, MSG_FORMAT);
		this.command = command;
	}

	public CommandTimeoutException(String command, Throwable cause) {
		super(CODE, MSG_FORMAT, cause);
		this.command = command;
	}

	public String getCommand() {
		return command;
	}

}
