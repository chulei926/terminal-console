package com.leichu.terminal.console;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.leichu.terminal.console.interactive.ExecutionContext;
import com.leichu.terminal.console.interactive.Executor;
import com.leichu.terminal.console.interactive.ExecutorFactory;
import com.leichu.terminal.console.interactive.ExecutorRegistry;
import com.leichu.terminal.console.interactive.config.ConfigFactory;
import com.leichu.terminal.console.interactive.config.InteractiveConfig;
import com.leichu.terminal.console.interactive.exception.InteractiveException;
import com.leichu.terminal.console.interactive.model.AuthParam;
import com.leichu.terminal.console.interactive.model.Command;
import com.leichu.terminal.console.interactive.model.Protocol;
import com.leichu.terminal.console.interactive.model.Vendor;
import com.leichu.terminal.console.interactive.soap.SoapClient;
import okhttp3.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ExecutorTest {

	private static final AuthParam authParam = new AuthParam();

	@BeforeEach
	void init() {
		authParam.setIp("116.62.163.67");
		authParam.setPort(22);
		authParam.setUsername("root");
		authParam.setPwd("csx@123456");

		AuthParam proxy = new AuthParam();
		proxy.setIp("123.249.85.241");
		proxy.setPort(22);
		proxy.setUsername("root");
		proxy.setPwd("csx@20190821");
		authParam.setProxy(proxy);
	}


	@Test
	void configFactoryTest() {
		InteractiveConfig config1 = ConfigFactory.getInstance().getConfig(Vendor.LINUX);
		System.out.println(JSONObject.toJSONString(config1));
		InteractiveConfig config2 = ConfigFactory.getInstance().getConfig(Vendor.FH);
		System.out.println(JSONObject.toJSONString(config2));
	}

	@Test
	void executorFactoryTest() {
		ExecutorFactory factory = ExecutorFactory.getInstance();
		System.out.println(factory.getExecutor("ZTE", "MGCF"));
	}

	@Test
	void soapTest() throws Exception {
		String endpointUrl = "http://www.webxml.com.cn/webservices/DomesticAirline.asmx/getDomesticCity"; // Web服务端点URL
		String soapAction = "http://WebXml.com.cn/getDomesticCity"; // SOAP操作名
		// 构建SOAP请求报文
		String soapRequestData = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://WebXml.com.cn/\">\n" +
				"   <soapenv:Header/>\n" +
				"   <soapenv:Body>\n" +
				"      <web:getDomesticCity/>\n" +
				"   </soapenv:Body>\n" +
				"</soapenv:Envelope>";
		Response response = SoapClient.getInstance().post(endpointUrl, soapRequestData, new HashMap<String, String>() {{
//			put("SOAPAction", "http://WebXml.com.cn/getSupportDataSet");
		}});
		if (response.code() != SoapClient.OK) {
			throw new InteractiveException(null, response.message());
		}
		String result = SoapClient.extractResponseBody(response);
		System.out.println(result);
	}

	@Test
	void telnetTest() {
		authParam.setIp("192.168.60.60");
		authParam.setPort(9999);
		authParam.setUsername("huawei_user");
		authParam.setPwd("Huawei@1234!");

		List<Command> commands = Lists.newArrayList(new Command("sys"), new Command("dis cur"), new Command("quit").setIsLast(true));
		Executor executor = ExecutorFactory.getInstance().getExecutor(Protocol.TELNET);
		InteractiveConfig config = ConfigFactory.getInstance().getConfig(Vendor.HW);
		ExecutionContext context = new ExecutionContext();
		context.setAuthParam(authParam);
		context.setExecutor(executor);
		context.setCommands(commands);
		context.setConfig(config);
		context.setCharset(StandardCharsets.UTF_8);
		context.initExecutor();
		context.execute(res -> System.out.println("callback:" + res));
	}

	@Test
	void sshTest() {
		List<Command> commands = Lists.newArrayList(new Command("sys"), new Command("dis cur"), new Command("quit").setIsLast(true));
		Executor executor = ExecutorFactory.getInstance().getExecutor(Protocol.SSH);
		InteractiveConfig config = ConfigFactory.getInstance().getConfig(Vendor.HW);
		ExecutionContext context = new ExecutionContext();
		context.setAuthParam(authParam);
		context.setExecutor(executor);
		context.setCommands(commands);
		context.setConfig(config);
		context.execute(res -> System.out.println("callback:" + res));
	}

	@Test
	void consoleTest() {
		String sid = UUID.randomUUID().toString();
		InteractiveConfig config = ConfigFactory.getInstance().getConfig(Vendor.LINUX);
		Executor executor = ExecutorFactory.getInstance().getExecutor(Protocol.SSH);
		executor.setAuthParam(authParam);
		executor.setInteractiveConfig(config);
		executor.setCharset(StandardCharsets.UTF_8);
		executor.connect();
		ExecutorRegistry.getInstance().register(sid, executor);
		try {
			Command ls = new Command("ls -al");
			ExecutorRegistry.getInstance().getExecutor(sid).execute(ls, res -> System.out.println("ls >>> \n" + res));

			Command pwd = new Command("pwd");
			ExecutorRegistry.getInstance().getExecutor(sid).execute(pwd, res -> System.out.println("pwd >>> \n" + res));
//			while (true);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ExecutorRegistry.getInstance().getExecutor(sid).disconnect();
		}


	}
}
