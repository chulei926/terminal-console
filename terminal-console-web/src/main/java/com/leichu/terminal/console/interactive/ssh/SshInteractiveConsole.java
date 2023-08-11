package com.leichu.terminal.console.interactive.ssh;

import com.leichu.terminal.console.interactive.GenericInteractiveConsole;
import com.leichu.terminal.console.interactive.config.InteractiveConfig;
import com.leichu.terminal.console.interactive.model.Command;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.EnumSet;

/**
 * 命令执行器.
 *
 * @author ffchul.
 * @since 2023-07-30.
 */
public class SshInteractiveConsole extends GenericInteractiveConsole {

	public static final Logger logger = LoggerFactory.getLogger(SshInteractiveConsole.class);

	private final ChannelShell channel;
	private final InteractiveConfig config;
	private final Charset charset;


	public SshInteractiveConsole(ChannelShell channel, InteractiveConfig interactiveConfig, Charset charset) {
		super(interactiveConfig, charset);
		this.channel = channel;
		this.config = interactiveConfig;
		this.charset = charset;
	}


	@Override
	public void writeCommand(String command) throws Exception {
		OutputStream stdIn = channel.getInvertedIn();
		stdIn.write((command.endsWith(ENTER_KEY) ? command : String.format("%s%s", command, ENTER_KEY)).getBytes());
		stdIn.flush();
	}

	@Override
	public String readResponse() throws Exception {
		// 切换到输出模式，获取命令执行的结果
		channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), EXEC_WAIT_TIMEOUT);
		InputStream stdOut = channel.getInvertedOut();
		byte[] resBytes = readInputStream(stdOut);
		return new String(resBytes, charset);
	}

	@Override
	public String sendCommand(Command command) throws Exception {
		return super.sendCommand(command);
	}

	@Override
	public void write(byte[] content) throws Exception {
		OutputStream stdIn = channel.getInvertedIn();
		stdIn.write(content);
		stdIn.flush();
	}

	@Override
	public byte[] read() throws Exception {
		// 切换到输出模式，获取命令执行的结果
		channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), EXEC_WAIT_TIMEOUT);
		InputStream stdOut = channel.getInvertedOut();
		return readInputStream(stdOut);
	}
}
