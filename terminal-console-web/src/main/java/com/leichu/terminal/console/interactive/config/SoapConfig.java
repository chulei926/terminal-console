package com.leichu.terminal.console.interactive.config;

public class SoapConfig {

	private Long connectTimeout = 60L;
	private Long writeTimeout = 60L;
	private Long readTimeout = 60L;
	private Boolean retryOnConnectionFailure = true;

	public Long getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(Long connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public Long getWriteTimeout() {
		return writeTimeout;
	}

	public void setWriteTimeout(Long writeTimeout) {
		this.writeTimeout = writeTimeout;
	}

	public Long getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(Long readTimeout) {
		this.readTimeout = readTimeout;
	}

	public Boolean getRetryOnConnectionFailure() {
		return retryOnConnectionFailure;
	}

	public void setRetryOnConnectionFailure(Boolean retryOnConnectionFailure) {
		this.retryOnConnectionFailure = retryOnConnectionFailure;
	}
}
