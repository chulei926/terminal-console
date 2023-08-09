package com.leichu.terminal.console.interactive.ssh;

import com.leichu.terminal.console.interactive.exception.AuthException;
import com.leichu.terminal.console.interactive.model.AuthParam;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.config.hosts.HostConfigEntry;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.session.ClientSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class SshSession {

	public static final Logger logger = LoggerFactory.getLogger(SshSession.class);

	private final AuthParam authParam;
	private final SshClient client;
	private ClientSession session;

	public static final long DEFAULT_TIMEOUT = 30L;

	public SshSession(AuthParam authParam) {
		this.authParam = authParam;
		client = SshClientHolder.getInstance().getSshClient();
	}

	public void connect() {
		try {

			AuthParam proxy = authParam.getProxy();
			if (null != proxy){
				client.addPasswordIdentity(proxy.getPwd());
				session = client.connect(new HostConfigEntry("", authParam.getIp(), authParam.getPort(), authParam.getUsername(),
						String.format("%s@%s:%d", proxy.getUsername(), proxy.getIp(), proxy.getPort())))
						.verify(TimeUnit.SECONDS.toMillis(DEFAULT_TIMEOUT)).getSession();
				session.addPasswordIdentity(authParam.getPwd());
			} else {
				session = client.connect(authParam.getUsername(), authParam.getIp(), authParam.getPort())
						.verify(TimeUnit.SECONDS.toMillis(DEFAULT_TIMEOUT)).getSession();
				session.addPasswordIdentity(authParam.getPwd());
			}


			final AuthFuture verify = session.auth().verify(TimeUnit.SECONDS.toMillis(DEFAULT_TIMEOUT));
			if (!verify.isSuccess()) {
				logger.error("ssh verify failed!", verify.getException());
				throw new AuthException(authParam.getIp(), "SSH login timeout!");
			}
			logger.info("ssh session auth and create success! authParam:{}", authParam);
		} catch (Exception e) {
			logger.error("ssh session auth and create error! authParam:{}", authParam, e);
			throw new AuthException(authParam.getIp(), "SSH auth failed!");
		}


	}

	public ClientSession getSession() {
		return session;
	}

	public void destroy() {
		try {
			if (null != session) {
				session.close();
				logger.info("SSH Session closed!");
			}
		} catch (Exception e) {
			logger.error("SSH session close error!", e);
		}
	}

}
