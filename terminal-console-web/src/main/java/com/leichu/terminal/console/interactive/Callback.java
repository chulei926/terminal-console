package com.leichu.terminal.console.interactive;

import com.leichu.terminal.console.interactive.model.Command;

@FunctionalInterface
public interface Callback {

	void emit(Command command);

}
