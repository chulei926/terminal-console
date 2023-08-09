package com.leichu.terminal.console.interactive.ssh;


import com.leichu.terminal.console.interactive.config.InteractiveConfig;
import com.leichu.terminal.console.interactive.exception.InteractiveException;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.channel.PtyChannelConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * SSH会话通道.
 *
 * @author ffchul.
 * @since 2023-07-30.
 */
public class SshChannelHelper {

	public static final Logger logger = LoggerFactory.getLogger(SshChannelHelper.class);

	public static final long DEFAULT_TIMEOUT = 30L;

	public static ChannelShell getChannelShell(ClientSession session, InteractiveConfig interactiveConfig) {
		ChannelShell channel;
		try {
			PtyChannelConfiguration ptyChannelConfiguration = new PtyChannelConfiguration();
			ptyChannelConfiguration.setPtyColumns(1000);
			channel = session.createShellChannel(ptyChannelConfiguration, new HashMap<>());
			channel.setRedirectErrorStream(true);
			channel.open().verify(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
			logger.info("Start creating an SSH channel.");
		} catch (Exception e) {
			throw new InteractiveException(null, "ClientChannel create failed!", e);
		}
		return channel;
	}

	public static void close(ChannelShell channelShell) {
		try {
			if (null != channelShell) {
				channelShell.close();
				logger.info("ChannelShell closed!");
			}
		} catch (Exception e) {
			logger.error("ChannelShell close error!", e);
		}
	}
}
