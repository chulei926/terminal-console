package com.leichu.terminal.console.interactive.ftp;

import com.leichu.terminal.console.interactive.model.AuthParam;

public abstract class GenericChannel implements Channel {

	protected AuthParam authParam;

	public GenericChannel(AuthParam authParam) {
		this.authParam = authParam;
	}

}
