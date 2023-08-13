package com.leichu.terminal.console.interactive.telnet;

import com.leichu.terminal.console.interactive.GenericInteractiveConsole;
import com.leichu.terminal.console.interactive.config.InteractiveConfig;
import com.leichu.terminal.console.interactive.exception.AuthException;
import com.leichu.terminal.console.interactive.exception.ConnectionException;
import com.leichu.terminal.console.interactive.model.Command;
import com.leichu.terminal.console.interactive.utils.DateTimeUtils;
import com.leichu.terminal.console.interactive.utils.RegUtils;
import com.leichu.terminal.console.utils.ThreadPoolHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.telnet.EchoOptionHandler;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TelnetClientConsole extends GenericInteractiveConsole {

	private static final Logger logger = LoggerFactory.getLogger(TelnetClientConsole.class);

	private final String host;
	private final Integer port;
	private final InteractiveConfig config;
	private final Charset charset;

	private final TelnetClient telnetClient;

	private InputStream readStream;
	private PrintStream writeStream;

	private final ThreadPoolTaskExecutor responseReadThreadPool;


	private static final List<byte[]> BUFFER = new CopyOnWriteArrayList<>();

	public TelnetClientConsole(String host, Integer port, InteractiveConfig interactiveConfig, Charset charset) {
		super(interactiveConfig, charset);
		this.host = host;
		this.port = port;
		this.config = interactiveConfig;
		this.charset = charset;

		this.registerHook();
		this.telnetClient = new TelnetClient();

		try {
			telnetClient.addOptionHandler(new TerminalTypeOptionHandler("xterm", false, false, false, false));
			telnetClient.addOptionHandler(new EchoOptionHandler(true, true, false, false));
		} catch (Exception e) {
			logger.error("TelnetClient terminalTypeOption set error!", e);
		}

		responseReadThreadPool = ThreadPoolHelper.createSimplePool(1, 1, "TELNET-RESPONSE-READER-");
	}

	private void registerHook() {
		Runtime runtime = Runtime.getRuntime();
		runtime.addShutdownHook(new Thread(this::disconnect));
	}

	public void connect() {
		try {
			this.telnetClient.connect(host, port);
			this.readStream = this.telnetClient.getInputStream();
			this.writeStream = new PrintStream(telnetClient.getOutputStream());

			responseReadThreadPool.execute(() -> {
				while (true) {
					try {
						byte[] bytes = readInputStream(readStream);
						if (null != bytes && bytes.length > 0) {
							BUFFER.add(bytes);
						}
					} catch (Exception e) {
						logger.error("Telnet console response read error!", e);
					} finally {
						DateTimeUtils.sleep(100L);
					}
				}
			});


			logger.info("Telnet channel connection success! Host:{} Port:{}", host, port);
		} catch (Exception e) {
			logger.error("Telnet channel connection error! Host:{} Port:{}", host, port, e);
			throw new ConnectionException(host, e.getMessage());
		}
	}

	private String readBuffer() {
		if (BUFFER.size() == 0) {
			return "";
		}
		int size = 0;
		for (byte[] bytes : BUFFER) {
			if (null == bytes) {
				continue;
			}
			size += bytes.length;
		}
		if (size == 0) {
			return "";
		}
		byte[] data = new byte[size];
		int idx = 0;
		for (byte[] bytes : BUFFER) {
			if (null == bytes) {
				continue;
			}
			for (byte b : bytes) {
				data[idx] = b;
				idx++;
			}
		}
		return new String(data, charset);
	}

	public void login(String username, String password) {
		while (true) {
			try {
				String response = readBuffer();
				System.out.println(response);
				if (StringUtils.isBlank(response)) {
					DateTimeUtils.sleep(1);
					continue;
				}

				if (match(response, config.getTelnetUsernameIdentifier())) {
					logger.info("Prompt for username:{}", response);
					writeCommand(username);
					continue;
				}
				if (match(response, config.getTelnetPasswordIdentifier())) {
					logger.info("Prompt for password:{}", response);
					writeCommand(password);
					String successFlag = readResponse4Login();
					logger.info("Login success：{}", successFlag);
					clearCache();
					break;
				}
			} catch (Exception e) {
				logger.error("Login error!", e);
				throw new AuthException(host, e.getMessage());
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
		clearCache();
		writeStream.print(command.endsWith(ENTER_KEY) ? command : command + ENTER_KEY);
		writeStream.flush();
	}

	@Override
	public String readResponse() throws Exception {
		return readBuffer();
	}

	@Override
	public String sendCommand(Command command) throws Exception {
		return super.sendCommand(command);
	}

	private String readResponse4Login() throws Exception {
		StringBuffer result = new StringBuffer();
		do {
			String resp = readBuffer();
			if (StringUtils.isNotBlank(resp)) {
				result.append(resp);
				clearCache();
			}
			if (matchEnd(result.toString())) {
				break;
			}
		} while (true);
		return result.toString();
	}


	public void disconnect() {
		try {

			if (null != this.telnetClient) {
				this.telnetClient.disconnect();
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


	@Override
	public void write(byte[] content) throws Exception {
		writeStream.print(new String(content, charset));
		writeStream.flush();
	}

	@Override
	public byte[] read() throws Exception {
		byte[] bytes = readBuffer().getBytes(charset);
		clearCache();
		return bytes;
	}

	private void clearCache() {
		BUFFER.clear();
	}
}
