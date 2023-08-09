package com.leichu.terminal.console.interactive.exception;

/**
 * 协议匹配异常.
 *
 * @author leichu.
 * @since 2023-07-30.
 */
public class ProtocolMatchException extends GenericException {

	private static final Integer CODE = 500;
	private static final String MSG_FORMAT = "Protocol match error! vendor:%s neType:%s";
	private final String vendor;
	private final String neType;

	public ProtocolMatchException(String vendor, String neType) {
		super(CODE, String.format(MSG_FORMAT, vendor, neType));
		this.vendor = vendor;
		this.neType = neType;
	}

	public ProtocolMatchException(String vendor, String neType, Throwable cause) {
		super(CODE, String.format(MSG_FORMAT, vendor, neType), cause);
		this.vendor = vendor;
		this.neType = neType;
	}

	public String getVendor() {
		return vendor;
	}

	public String getNeType() {
		return neType;
	}
}
