package com.leichu.terminal.console.interactive.model;

/**
 * 协议.
 *
 * @author leichu.
 * @since 2023-07-30.
 */
public enum Protocol {

	TELNET,
	SSH,
	SOAP;

	public static Protocol match(String protocolName){
		for (Protocol value : Protocol.values()) {
			if (value.name().equalsIgnoreCase(protocolName)){
				return value;
			}
		}
		return null;
	}
}
