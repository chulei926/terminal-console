package com.leichu.terminal.console.interactive.exception;

/**
 * FTP/SFTP交互异常.
 *
 * @author leichu.
 * @since 2023-07-30.
 */
public class FtpException extends GenericException {

	private static final Integer CODE = 500;
	private static final String MSG_FORMAT = "Ftp interactive error! IP:%s Reason:%s";
	private final String neIp;
	private final String reason;

	public FtpException(String neIp, String reason) {
		super(CODE, null == neIp ? String.format("Ftp interactive error! %s", reason) : String.format(MSG_FORMAT, neIp, reason));
		this.neIp = neIp;
		this.reason = reason;
	}

	public FtpException(String neIp, String reason, Throwable cause) {
		super(CODE, null == neIp ? String.format("Ftp interactive error! %s", reason) : String.format(MSG_FORMAT, neIp, reason));
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
