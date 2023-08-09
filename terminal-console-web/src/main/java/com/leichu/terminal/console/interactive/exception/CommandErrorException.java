package com.leichu.terminal.console.interactive.exception;

/**
 * 命令执行出错异常.
 *
 * @author leichu.
 * @since 2023-07-30.
 */
public class CommandErrorException extends GenericException {

	private static final Integer CODE = 500;
	private static final String MSG_FORMAT = "Command execution error! Command:%s Error message:%s";
	private final String command;
	private final String result;
	private final String errMsg;

	public CommandErrorException(String command, String result, String errMsg) {
		super(CODE, String.format(MSG_FORMAT, command, errMsg));
		this.command = command;
		this.result = result;
		this.errMsg = errMsg;

	}

	public CommandErrorException(String command, String result, String errMsg, Throwable cause) {
		super(CODE, String.format(MSG_FORMAT, command, errMsg), cause);
		this.command = command;
		this.result = result;
		this.errMsg = errMsg;
	}

	public String getCommand() {
		return command;
	}

	public String getResult() {
		return result;
	}

	public String getErrMsg() {
		return errMsg;
	}
}
