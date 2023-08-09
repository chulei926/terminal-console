package com.leichu.terminal.console.model;

import java.io.Serializable;


public class Msg implements Serializable {

	private String sshSid;
	private String wsSid;
	private String command;

	public String getSshSid() {
		return sshSid;
	}

	public Msg setSshSid(String sshSid) {
		this.sshSid = sshSid;
		return this;
	}

	public String getWsSid() {
		return wsSid;
	}

	public Msg setWsSid(String wsSid) {
		this.wsSid = wsSid;
		return this;
	}

	public String getCommand() {
		return command;
	}

	public Msg setCommand(String command) {
		this.command = command;
		return this;
	}
}
