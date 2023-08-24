package com.leichu.terminal.console;

import com.leichu.terminal.console.interactive.ftp.Channel;
import com.leichu.terminal.console.interactive.ftp.FileInfo;
import com.leichu.terminal.console.interactive.ftp.FtpChannel;
import com.leichu.terminal.console.interactive.ftp.SftpChannel;
import com.leichu.terminal.console.interactive.model.AuthParam;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

public class SftpTest {


	private static final AuthParam authParam = new AuthParam();
	private Channel channel;

	@BeforeEach
	void init() {
		authParam.setIp("vm");
		authParam.setPort(23);
		authParam.setUsername("root");
		authParam.setPwd("csx@20190821");
		channel = new SftpChannel(authParam);
		channel.open();
	}

	@AfterEach
	void after(){
		channel.close();
	}

	@Test
	void existsTest() {
		System.out.println(channel.exists("/usr/local"));
	}

	@Test
	void listTest() {
		List<FileInfo> list = channel.list("/", true);
		for (FileInfo info : list) {
			System.out.println(info);
		}
	}


	@Test
	void downloadTestTest() {
		Channel channel = new SftpChannel(authParam);
		try {
			channel.open();
			channel.download(new File("demo.py"), "/a/b/c/d/demo.py");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			channel.close();
		}
	}


	@Test
	void uploadTestTest() {
		Channel channel = new SftpChannel(authParam);
		try {
			channel.open();
			channel.upload(new File("/Users/leichu/workspace/py_code/demo.py"), "/a/b/c/d/demo.py");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			channel.close();
		}
	}
}
