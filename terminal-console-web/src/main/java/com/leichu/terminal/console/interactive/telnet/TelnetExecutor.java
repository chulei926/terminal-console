package com.leichu.terminal.console.interactive.telnet;

import com.leichu.terminal.console.interactive.AbstractExecutor;
import com.leichu.terminal.console.interactive.ProtocolType;
import com.leichu.terminal.console.interactive.model.Command;
import com.leichu.terminal.console.interactive.model.Protocol;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@ProtocolType(type = Protocol.TELNET)
public class TelnetExecutor extends AbstractExecutor {

	private static final Logger logger = LoggerFactory.getLogger(TelnetExecutor.class);

	private TelnetClientConsole telnetClientConsole;

	@Override
	public void connect() {
		logger.info("TelnetExecutor ---> connect");
		telnetClientConsole = new TelnetClientConsole(authParam.getIp(), authParam.getPort(), interactiveConfig, charset);
		telnetClientConsole.connect();
		telnetClientConsole.login(authParam.getUsername(), authParam.getPwd());
		super.connected = Boolean.TRUE;
	}

	@Override
	public void disconnect() {
		logger.info("TelnetExecutor ---> disconnect");
		telnetClientConsole.disconnect();
	}

	@Override
	public void preHandle() {
		logger.info("TelnetExecutor ---> preHandle");
	}

	@Override
	public void postHandle() {
		logger.info("TelnetExecutor ---> postHandle");
	}

	@Override
	public String sendCommand(Command command) throws Exception {
		return telnetClientConsole.sendCommand(command);
	}

}
