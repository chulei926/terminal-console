package com.leichu.terminal.console;

import com.leichu.terminal.console.interactive.ftp.Channel;
import com.leichu.terminal.console.interactive.ftp.FileInfo;
import com.leichu.terminal.console.interactive.ftp.FtpChannel;
import com.leichu.terminal.console.interactive.model.AuthParam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

public class FtpTest {


	private static final AuthParam authParam = new AuthParam();

	@BeforeEach
	void init() {
		authParam.setIp("192.168.60.60");
		authParam.setPort(2323);
		authParam.setUsername("chul");
		authParam.setPwd("Cl@2023!");
	}

	@Test
	void existsTest() {
		Channel channel = new FtpChannel(authParam);
		try {
			channel.open();
			System.out.println(channel.exists("/a/b/c1.txt"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			channel.close();
		}
	}

	@Test
	void listTest() {
		Channel channel = new FtpChannel(authParam);
		try {
			channel.open();
			List<FileInfo> list = channel.list("/", true);
			for (FileInfo info : list) {
				System.out.println(info);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			channel.close();
		}
	}


	@Test
	void downloadTestTest() {
		Channel channel = new FtpChannel(authParam);
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
		Channel channel = new FtpChannel(authParam);
		try {
			channel.open();
			channel.upload(new File("/Users/leichu/Downloads/ManageEngine_EventLogAnalyzer_64bit.exe"), "/ManageEngine_EventLogAnalyzer_64bit.exe");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			channel.close();
		}
	}
	
	
	
}
