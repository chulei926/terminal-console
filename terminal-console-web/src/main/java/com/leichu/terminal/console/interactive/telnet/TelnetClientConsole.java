package com.leichu.terminal.console.interactive.telnet;

import com.leichu.terminal.console.interactive.GenericInteractiveConsole;
import com.leichu.terminal.console.interactive.config.InteractiveConfig;
import com.leichu.terminal.console.interactive.exception.ConnectionException;
import com.leichu.terminal.console.interactive.model.Command;
import com.leichu.terminal.console.interactive.utils.DateTimeUtils;
import com.leichu.terminal.console.interactive.utils.RegUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.mina.filter.codec.textline.TextLineDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;

public class TelnetClientConsole extends GenericInteractiveConsole {

	private static final Logger logger = LoggerFactory.getLogger(TelnetClientConsole.class);

	private final String host;
	private final Integer port;
	private final InteractiveConfig config;
	private final Charset charset;

	private final EventLoopGroup group;
	private final Bootstrap bootstrap;

	private Channel channel = null;

	private static final TelnetClientHandler CLIENT_HANDLER = new TelnetClientHandler();

	public TelnetClientConsole(String host, Integer port, InteractiveConfig interactiveConfig, Charset charset) {
		super(interactiveConfig, charset);
		this.host = host;
		this.port = port;
		this.config = interactiveConfig;
		this.charset = charset;

		this.group = new NioEventLoopGroup();
		this.bootstrap = new Bootstrap();
		this.registerHook();
	}

	private void registerHook() {
		Runtime runtime = Runtime.getRuntime();
		runtime.addShutdownHook(new Thread(this::disconnect));
	}

	public void connect() {
		// Start the connection attempt.
		try {
			bootstrap.group(group)
					.channel(NioSocketChannel.class)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline pipeline = ch.pipeline();
							pipeline.addLast(new StringDecoder(charset));
							pipeline.addLast(new StringEncoder(charset));
							pipeline.addLast(CLIENT_HANDLER);
						}
					});
			channel = bootstrap.connect(host, port).sync().channel();
			logger.info("Telnet channel connection success! Host:{} Port:{}", host, port);
		} catch (Exception e) {
			logger.error("Telnet channel connection error! Host:{} Port:{}", host, port, e);
			throw new ConnectionException(host, e.getMessage());
		}
	}

	public void login(String username, String password) {
		while (true) {
			String response = CLIENT_HANDLER.readResponse();
			if (StringUtils.isBlank(response)) {
				DateTimeUtils.sleep(1);
				continue;
			}
			if (match(response, config.getTelnetUsernameIdentifier())) {
				CLIENT_HANDLER.clearResponse();
				channel.writeAndFlush(username + ENTER_KEY);
				continue;
			}
			if (match(response, config.getTelnetPasswordIdentifier())) {
				CLIENT_HANDLER.clearResponse();
				channel.writeAndFlush(password + ENTER_KEY);
				String successFlag = readResponse4Login();
				logger.info("Login success：{}", successFlag);
				break;
			}
			DateTimeUtils.sleep(1);
		}
	}

	private boolean match(String response, String regex) {
		List<String> lines = SPLITTER.splitToList(response);
		if (CollectionUtils.isEmpty(lines)) {
			return false;
		}
		for (String line : lines) {
			if (RegUtils.match(line, regex)) {
				return true;
			}
		}
		return false;
	}


	@Override
	public void writeCommand(String command) throws Exception {
		CLIENT_HANDLER.clearResponse();
		channel.writeAndFlush(command + ENTER_KEY);
	}

	@Override
	public String readResponse() throws Exception {
		String response = CLIENT_HANDLER.readResponse();
		CLIENT_HANDLER.clearResponse();
		return response;
	}

	@Override
	public String sendCommand(Command command) throws Exception {
		return super.sendCommand(command);
	}

	private String readResponse4Login() {
		StringBuffer result = new StringBuffer();
		do {
			String resp = CLIENT_HANDLER.readResponse();
			if (StringUtils.isNotBlank(resp)) {
				result.append(resp);
				CLIENT_HANDLER.clearResponse();
				if (matchEnd(result.toString())) {
					break;
				}
			}
		} while (true);
		return result.toString();
	}


	public void disconnect() {
		try {
			if (null != channel) {
				channel.close();
			}
			if (null != group) {
				group.shutdownGracefully();
			}
			logger.info("Telnet client closed! Host:{} Port:{}", host, port);
		} catch (Exception e) {
			logger.error("Telnet client close error! Host:{} Port:{}", host, port, e);
		}
	}


	public String getHost() {
		return host;
	}

	public Integer getPort() {
		return port;
	}

	public InteractiveConfig getConfig() {
		return config;
	}

	public Charset getCharset() {
		return charset;
	}

	public EventLoopGroup getGroup() {
		return group;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}
}
