package com.leichu.terminal.console.interactive.ftp;

import java.io.File;
import java.util.List;

public interface Channel {

	void open();

	void close();

	boolean exists(String fileName);

	List<FileInfo> list(String dir, boolean recursive);

	void download(File localFile, String remoteFileName);

	void upload(File localFile, String remoteFileName);

}
