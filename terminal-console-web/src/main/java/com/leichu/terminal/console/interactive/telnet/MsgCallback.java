package com.leichu.terminal.console.interactive.telnet;

@FunctionalInterface
public interface MsgCallback {

	void emit(byte[] bytes);

}
