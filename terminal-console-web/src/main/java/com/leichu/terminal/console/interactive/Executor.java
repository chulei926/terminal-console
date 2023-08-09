package com.leichu.terminal.console.interactive;


import com.leichu.terminal.console.interactive.config.InteractiveConfig;
import com.leichu.terminal.console.interactive.model.AuthParam;
import com.leichu.terminal.console.interactive.model.Command;

import java.nio.charset.Charset;
import java.util.List;

/**
 * 执行器.
 *
 * @author leichu.
 * @since 2023-07-30.
 */
public interface Executor {

	void setAuthParam(AuthParam authParam);

	void setInteractiveConfig(InteractiveConfig interactiveConfig);

	void setCharset(Charset charset);

	void connect();

	void disconnect();

	void execute(List<Command> commands, Callback callback);

	void execute(Command command, Callback callback);

	void interrupt();

}
