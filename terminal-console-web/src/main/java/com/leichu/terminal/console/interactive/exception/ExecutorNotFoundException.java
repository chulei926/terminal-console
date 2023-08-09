package com.leichu.terminal.console.interactive.exception;

/**
 * 未找到执行器.
 *
 * @author leichu.
 * @since 2023-07-30.
 */
public class ExecutorNotFoundException extends GenericException {

	private static final Integer CODE = 500;
	private static final String MSG_FORMAT = "Executor not found! protocol:%s";
	private final String protocol;

	public ExecutorNotFoundException(String protocol) {
		super(CODE, String.format(MSG_FORMAT, protocol));
		this.protocol = protocol;
	}

	public ExecutorNotFoundException(String protocol, Throwable cause) {
		super(CODE, String.format(MSG_FORMAT, protocol), cause);
		this.protocol = protocol;
	}

	public String getProtocol() {
		return protocol;
	}
}
