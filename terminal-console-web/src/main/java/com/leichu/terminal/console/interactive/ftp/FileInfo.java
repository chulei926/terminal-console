package com.leichu.terminal.console.interactive.ftp;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.Date;

public class FileInfo implements Serializable {

	private String fileName;
	private String filePath;
	private boolean isDir;
	private Date lastModified;
	private Long fileSize;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public boolean getIsDir() {
		return isDir;
	}

	public void setIsDir(boolean isDir) {
		this.isDir = isDir;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public static FileInfo build(FTPFile ftpFile, String curPath) {
		FileInfo fileInfo = new FileInfo();
		fileInfo.setFileName(ftpFile.getName());
		fileInfo.setFilePath(Paths.get(curPath, ftpFile.getName()).toAbsolutePath().toString());
		fileInfo.setIsDir(ftpFile.isDirectory());
		fileInfo.setFileSize(ftpFile.getSize());
		fileInfo.setLastModified(ftpFile.getTimestamp().getTime());
		return fileInfo;
	}

	public static FileInfo build(File file, String curPath) {
		FileInfo fileInfo = new FileInfo();
		fileInfo.setFileName(file.getName());
		fileInfo.setFilePath(file.getAbsolutePath());
		fileInfo.setIsDir(file.isDirectory());
		fileInfo.setFileSize(file.length());
		fileInfo.setLastModified(new Date(file.lastModified()));
		return fileInfo;
	}

	@Override
	public String toString() {
		return JSONObject.toJSONString(this, SerializerFeature.WriteMapNullValue);
	}
}
