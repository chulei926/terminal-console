package com.leichu.terminal.console.interactive.exception;

/**
 * 连接异常.
 *
 * @author leichu.
 * @since 2023-07-30.
 */
public class ConnectionException extends GenericException {

	private static final Integer CODE = 500;
	private static final String MSG_FORMAT = "Connection failed! IP:%s Reason:%s";
	private final String neIp;
	private final String reason;

	public ConnectionException(String neIp, String reason) {
		super(CODE, String.format(MSG_FORMAT, neIp, reason));
		this.neIp = neIp;
		this.reason = reason;
	}

	public ConnectionException(String neIp, String reason, Throwable cause) {
		super(CODE, String.format(MSG_FORMAT, neIp, reason), cause);
		this.neIp = neIp;
		this.reason = reason;
	}

	public String getNeIp() {
		return neIp;
	}

	public String getReason() {
		return reason;
	}
}
