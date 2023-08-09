package com.leichu.terminal.console.interactive.model;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.Serializable;

/**
 * 认证参数.
 *
 * @author leichu.
 * @since 2023-07-30.
 */
public class AuthParam implements Serializable {

	private String ip;
	private Integer port;
	private String username;
	private String pwd;
	private AuthParam proxy;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public AuthParam getProxy() {
		return proxy;
	}

	public void setProxy(AuthParam proxy) {
		this.proxy = proxy;
	}

	@Override
	public String toString() {
		return JSONObject.toJSONString(this, SerializerFeature.WriteMapNullValue);
	}
}
