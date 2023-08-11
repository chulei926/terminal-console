package com.leichu.terminal.console.interactive.ssh;

import com.leichu.terminal.console.interactive.AbstractExecutor;
import com.leichu.terminal.console.interactive.ProtocolType;
import com.leichu.terminal.console.interactive.model.Command;
import com.leichu.terminal.console.interactive.model.Protocol;
import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.client.channel.ChannelShell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@ProtocolType(type = Protocol.SSH)
public class SshExecutor extends AbstractExecutor {

	private static final Logger logger = LoggerFactory.getLogger(SshExecutor.class);

	private SshSession session;
	private ChannelShell channel;
	private SshInteractiveConsole sshInteractiveConsole;

	@Override
	public void connect() {
		logger.info("SshExecutor ---> connect");
		session = new SshSession(authParam);
		session.connect();
		channel = SshChannelHelper.getChannelShell(session.getSession(), interactiveConfig);
		sshInteractiveConsole = new SshInteractiveConsole(channel, interactiveConfig, charset);

		try {
			// 通道开启后，立即执行换行，防止出现设备的欢迎语，如：Welcome to Huawei Cloud Service
			Command command = new Command("\r\n");
			command.setIsLast(Boolean.TRUE);
			sshInteractiveConsole.sendCommand(command);
		} catch (Exception e) {
			logger.error("Reset ssh channel error!", e);
		}
		super.connected = Boolean.TRUE;
		super.interactiveConsole = sshInteractiveConsole;
	}

	@Override
	public void disconnect() {
		logger.info("SshExecutor ---> disconnect");
		SshChannelHelper.close(channel);
		session.destroy();
	}

	@Override
	public void preHandle() {
		logger.info("SshExecutor ---> preHandle");
	}

	@Override
	public void postHandle() {
		logger.info("SshExecutor ---> postHandle");
	}


	@Override
	public String sendCommand(Command command) throws Exception {
		return sshInteractiveConsole.sendCommand(command);
	}

}
