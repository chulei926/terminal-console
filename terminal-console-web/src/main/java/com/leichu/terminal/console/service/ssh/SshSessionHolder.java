package com.leichu.terminal.console.service.ssh;


import com.leichu.terminal.console.common.BizException;
import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * SSH会话工具类.
 *
 * @author leichu 2022/3/9.
 */
public class SshSessionHolder {

	private static final Logger logger = LoggerFactory.getLogger(SshSessionHolder.class);

	/**
	 * 用于存放已经创建的会话，同一个sid使用同一个会话。
	 */
	private static final Map<String, ClientSession> authedSessionMap = new ConcurrentHashMap<>();
	private static volatile SshSessionHolder instance;

	public static SshSessionHolder getInstance() {
		if (instance == null) {
			synchronized (SshSessionHolder.class) {
				if (instance == null) {
					instance = new SshSessionHolder();
				}
			}
		}
		return instance;
	}

	public synchronized String createSession(SshAuthParam authParam) {
		Objects.requireNonNull(authParam, "认证参数不能为空");
		if (StringUtils.isNotBlank(authParam.getSid()) && authedSessionMap.containsKey(authParam.getSid())) {
			logger.info("ssh session already exist, return immediately!");
			return authParam.getSid();
		}
		ClientSession session;
		long timeout = null == authParam.getTimeout() ? 1L : authParam.getTimeout();
		try {
			session = SshClientHolder.getInstance().getSshClient()
					.connect(authParam.getUsername(), authParam.getIp(), authParam.getPort())
					.verify(TimeUnit.SECONDS.toMillis(timeout)).getSession();
			session.addPasswordIdentity(authParam.getPwd());
			final AuthFuture verify = session.auth().verify(TimeUnit.SECONDS.toMillis(timeout));
			if (!verify.isSuccess()) {
				throw new BizException("会话创建失败，请检查用户名或密码");
			}
			logger.info("ssh session auth and create success ......");
		} catch (Exception e) {
			if (e.getMessage().contains("No more authentication methods available")) {
				throw new BizException("用户名或密码错误", e);
			}
			throw new BizException(e.getMessage(), e);
		}
		String sid = UUID.randomUUID().toString();
		authedSessionMap.put(sid, session);

		try {
			ClientChannel channel = session.createChannel(Channel.CHANNEL_SHELL);
			channel.open().verify(1, TimeUnit.SECONDS);
			SshChannelHolder.getInstance().registerChannel(sid, channel);
		} catch (Exception e) {
			throw new BizException("创建命令执行通道异常");
		}
		return sid;
	}

}
