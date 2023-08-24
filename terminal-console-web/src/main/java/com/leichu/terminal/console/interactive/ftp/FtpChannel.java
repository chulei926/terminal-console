package com.leichu.terminal.console.interactive.ftp;

import com.leichu.terminal.console.interactive.exception.AuthException;
import com.leichu.terminal.console.interactive.exception.ConnectionException;
import com.leichu.terminal.console.interactive.exception.FtpException;
import com.leichu.terminal.console.interactive.exception.GenericException;
import com.leichu.terminal.console.interactive.model.AuthParam;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FtpChannel extends GenericChannel {

	private static final Logger logger = LoggerFactory.getLogger(FtpChannel.class);

	private FTPClient ftpClient;

	public FtpChannel(AuthParam authParam) {
		super(authParam);
	}

	@Override
	public void open() {
		try {
			ftpClient = new FTPClient();
			ftpClient.setCharset(StandardCharsets.UTF_8);
			ftpClient.setControlEncoding(StandardCharsets.UTF_8.name());

			ftpClient.connect(authParam.getIp(), authParam.getPort());
			// Check the connection status
			int reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftpClient.disconnect();
				throw new ConnectionException(authParam.getIp(), "FTP server refused connection.");
			}
			// Login to the server
			if (!ftpClient.login(authParam.getUsername(), authParam.getPwd())) {
				ftpClient.logout();
				throw new AuthException(authParam.getIp(), "Login failed.");
			}

			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		} catch (Exception e) {
			if (e instanceof GenericException) {
				throw (GenericException) e;
			}
			throw new FtpException(authParam.getIp(), e.getMessage(), e);
		}
	}

	@Override
	public void close() {
		try {
			ftpClient.logout();
			ftpClient.disconnect();
		} catch (Exception e) {
			logger.error("Ftp client close error!", e);
		}
	}

	@Override
	public boolean exists(String fileName) {
		FTPFile[] files;
		try {
			files = ftpClient.listFiles(fileName);
		} catch (IOException e) {
			throw new FtpException(authParam.getIp(), e.getMessage(), e);
		}
		if (null == files || files.length == 0) {
			return false;
		}
		String name = FilenameUtils.getName(fileName);
		return files[0].isFile() && files[0].getName().equals(name);
	}

	@Override
	public List<FileInfo> list(String dir, boolean recursive) {
		FTPFile[] files;
		try {
			files = ftpClient.listFiles(dir);
		} catch (IOException e) {
			throw new FtpException(authParam.getIp(), e.getMessage(), e);
		}
		List<FileInfo> fileInfos = new ArrayList<>();
		if (null == files || files.length == 0) {
			return fileInfos;
		}
		for (FTPFile file : files) {
			fileInfos.add(FileInfo.build(file, dir));
		}
		if (recursive) {
			for (FileInfo fileInfo : new ArrayList<>(fileInfos)) {
				if (fileInfo.getIsDir()) {
					fileInfos.addAll(list(fileInfo.getFilePath(), recursive));

				}
			}
		}

		return fileInfos;
	}



	@Override
	public void download(File localFile, String remoteFileName) {
		try (FileOutputStream outputStream = new FileOutputStream(localFile)) {
			ftpClient.retrieveFile(remoteFileName, outputStream);
		} catch (Exception e) {
			throw new FtpException(authParam.getIp(), e.getMessage(), e);
		}
	}

	@Override
	public void upload(File localFile, String remoteFileName) {
		try (FileInputStream inputStream = new FileInputStream(localFile)) {
			ftpClient.storeFile(remoteFileName, inputStream);
		} catch (Exception e) {
			throw new FtpException(authParam.getIp(), e.getMessage(), e);
		}
	}
}
