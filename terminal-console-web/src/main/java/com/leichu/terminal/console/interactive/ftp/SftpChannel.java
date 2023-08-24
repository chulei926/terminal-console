package com.leichu.terminal.console.interactive.ftp;

import com.leichu.terminal.console.interactive.exception.FtpException;
import com.leichu.terminal.console.interactive.exception.GenericException;
import com.leichu.terminal.console.interactive.model.AuthParam;
import com.leichu.terminal.console.interactive.ssh.SshSession;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.apache.sshd.sftp.client.fs.SftpFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SftpChannel extends GenericChannel {

	private static final Logger logger = LoggerFactory.getLogger(SftpChannel.class);

	private SshSession session;
	private SftpClient sftpClient;
	private SftpFileSystem sftpFileSystem;

	public SftpChannel(AuthParam authParam) {
		super(authParam);
	}

	@Override
	public void open() {
		try {
			session = new SshSession(authParam);
			session.connect();
			ClientSession clientSession = session.getSession();
			SftpClientFactory instance = SftpClientFactory.instance();
			sftpClient = instance.createSftpClient(clientSession);
			sftpFileSystem = instance.createSftpFileSystem(clientSession);
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
			if (sftpFileSystem != null) {
				sftpFileSystem.close();
			}
			if (sftpClient != null) {
				sftpClient.close();
			}
			if (session != null) {
				session.destroy();
			}
		} catch (Exception e) {
			logger.error("Sftp client close error!", e);
		}
	}

	@Override
	public boolean exists(String fileName) {
		return Files.exists(sftpFileSystem.getDefaultDir().resolve(fileName));
	}

	@Override
	public List<FileInfo> list(String dir, boolean recursive) {
		List<FileInfo> fileInfos = new ArrayList<>();
		try {
			Path remoteFile = sftpFileSystem.getDefaultDir().resolve(dir);
			List<Path> list = Files.list(remoteFile).collect(Collectors.toList());
			for (Path path : list) {
				File file = path.toFile();
				fileInfos.add(FileInfo.build(file, dir));
			}

			if (recursive) {
				for (FileInfo fileInfo : new ArrayList<>(fileInfos)) {
					if (fileInfo.getIsDir()) {
						fileInfos.addAll(list(fileInfo.getFilePath(), recursive));

					}
				}
			}
		} catch (Exception e) {
			throw new FtpException(authParam.getIp(), e.getMessage(), e);
		}
		return fileInfos;
	}

	@Override
	public void download(File localFile, String remoteFileName) {
		try {
			Path localPath = Paths.get(localFile.getAbsolutePath());
			Files.deleteIfExists(localPath);
			Files.createDirectories(localPath.getParent());
			Path remoteFile = sftpFileSystem.getDefaultDir().resolve(Paths.get(remoteFileName));
			Files.copy(remoteFile, localPath);
		} catch (Exception e) {
			throw new FtpException(authParam.getIp(), e.getMessage(), e);
		}

	}

	@Override
	public void upload(File localFile, String remoteFileName) {
		try {
			Path localPath = Paths.get(localFile.getAbsolutePath());
			Path remoteFile = sftpFileSystem.getDefaultDir().resolve(Paths.get(remoteFileName));
			if (!Files.exists(remoteFile.getParent())) {
				Files.createDirectories(remoteFile.getParent());
			}
			Files.deleteIfExists(remoteFile);
			Files.copy(localPath, remoteFile);
		} catch (Exception e) {
			throw new FtpException(authParam.getIp(), e.getMessage(), e);
		}
	}
}
