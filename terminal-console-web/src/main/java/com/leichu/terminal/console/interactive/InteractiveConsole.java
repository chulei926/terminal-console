package com.leichu.terminal.console.interactive;

public interface InteractiveConsole {

	void write(byte[] content)throws Exception;

	byte[] read() throws Exception;

}
