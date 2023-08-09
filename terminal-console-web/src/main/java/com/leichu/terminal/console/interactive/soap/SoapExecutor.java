package com.leichu.terminal.console.interactive.soap;

import com.leichu.terminal.console.interactive.AbstractExecutor;
import com.leichu.terminal.console.interactive.ProtocolType;
import com.leichu.terminal.console.interactive.exception.InteractiveException;
import com.leichu.terminal.console.interactive.model.Command;
import com.leichu.terminal.console.interactive.model.Protocol;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

@ProtocolType(type = Protocol.SOAP)
public class SoapExecutor extends AbstractExecutor {

	private static final Logger logger = getLogger(SoapExecutor.class);

	private SoapClient soapClient;
	private String endpoint;

	private static final Map<String, String> HEADERS = new HashMap<>();
	private final static ExecutorService CACHED_THREAD_POOL = Executors.newCachedThreadPool();
	protected static final int EXEC_WAIT_TIMEOUT = 500;

	@Override
	public void connect() {
		logger.info("SoapExecutor ---> connect");
		super.connected = Boolean.TRUE;
	}

	@Override
	public void disconnect() {
		logger.info("SoapExecutor ---> disconnect");
	}

	@Override
	public void preHandle() {
		logger.info("SoapExecutor ---> preHandle");
		soapClient = SoapClient.getInstance();
		// TODO: 封装 endpoint
		endpoint = String.format("http://%s:%s/", authParam.getIp(), authParam.getPort());
		// TODO: 还可以在此处添加一些请求头信息
	}

	@Override
	public void postHandle() {
		logger.info("SoapExecutor ---> postHandle");
	}


	@Override
	public String sendCommand(Command command) throws Exception {
		Future<String> future = CACHED_THREAD_POOL.submit(() -> {
			Response response = soapClient.post(endpoint, command.getCommand(), HEADERS);
			if (response.code() != SoapClient.OK || !response.isSuccessful()) {
				throw new InteractiveException(authParam.getIp(), response.message());
			}
			return SoapClient.extractResponseBody(response);
		});
		return future.get(null == command.getTimeout() ? EXEC_WAIT_TIMEOUT : command.getTimeout(), TimeUnit.MILLISECONDS);
	}

}
