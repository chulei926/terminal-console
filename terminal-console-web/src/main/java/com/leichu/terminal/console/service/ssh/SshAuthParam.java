package com.leichu.terminal.console.service.ssh;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.Objects;

/**
 * SSH认证参数.
 * <ol>
 *     <li>IP地址</li>
 *     <li>端口号</li>
 *     <li>用户名</li>
 *     <li>密码</li>
 *     <li>超时时间</li>
 * </ol>
 *
 * @author leichu 2022/3/9.
 */
public class SshAuthParam implements Serializable {

	private String ip;
	private Integer port;
	private String username;
	private String pwd;
	private Long timeout = 1L;
	private String sid;

	public String getIp() {
		return ip;
	}

	public SshAuthParam setIp(String ip) {
		this.ip = ip;
		return this;
	}

	public Integer getPort() {
		return port;
	}

	public SshAuthParam setPort(Integer port) {
		this.port = port;
		return this;
	}

	public String getUsername() {
		return username;
	}

	public SshAuthParam setUsername(String username) {
		this.username = username;
		return this;
	}

	public String getPwd() {
		return pwd;
	}

	public SshAuthParam setPwd(String pwd) {
		this.pwd = pwd;
		return this;
	}

	public Long getTimeout() {
		return timeout;
	}

	public SshAuthParam setTimeout(Long timeout) {
		this.timeout = timeout;
		return this;
	}

	public String getSid() {
		return sid;
	}

	public SshAuthParam setSid(String sid) {
		this.sid = sid;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SshAuthParam authParam = (SshAuthParam) o;
		return Objects.equals(ip, authParam.ip) && Objects.equals(port, authParam.port) && Objects.equals(username, authParam.username) && Objects.equals(pwd, authParam.pwd);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ip, port, username, pwd);
	}

	@Override
	public String toString() {
		JSONObject obj = JSONObject.parseObject(JSONObject.toJSONString(this));
		obj.remove("pwd");
		return obj.toJSONString();
	}

}
