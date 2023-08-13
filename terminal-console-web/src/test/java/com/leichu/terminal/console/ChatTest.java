package com.leichu.terminal.console;

import org.apache.commons.net.telnet.TelnetClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class ChatTest {
	public static void main(String[] args) throws Exception{
//		String host = "123.249.85.241";
		String host = "vm";
		int port = 23;

		TelnetClient telnetClient = new TelnetClient();

		try {
			telnetClient.connect(host, port);

			InputStream inputStream = telnetClient.getInputStream();
			PrintStream outputStream = new PrintStream(telnetClient.getOutputStream());

			// 读取服务器返回的欢迎消息或任何其他输出
			byte[] responseBuffer = new byte[1024];
			int bytesRead = inputStream.read(responseBuffer);
			String response = new String(responseBuffer, 0, bytesRead);
			System.out.println(response);

			// 发送命令到Telnet服务器
			String command = "ls\r"; // 示例命令，根据需要替换
			outputStream.print(command);
			outputStream.flush();

			// 读取服务器的响应
			bytesRead = inputStream.read(responseBuffer);
			response = new String(responseBuffer, 0, bytesRead);
			System.out.println(response);

			// 关闭连接
			telnetClient.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
