package com.leichu.terminal.console.interactive.config;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.ArrayList;
import java.util.List;

/**
 * 交互配置.
 *
 * @author leichu.
 * @since 2023-07-30.
 */
public class InteractiveConfig implements Configuration {

	/**
	 * 分页标识符.
	 */
	public String paginationIdentifier = "^\\s*-+\\s*[Mm]ore\\s*-+\\s*$";
	/**
	 * 翻页的键.
	 */
	public String paginationPressKey = "\\ ";
	/**
	 * 错误开始标识符.
	 */
	public String errorStartIdentifier = null;
	/**
	 * 自动确认标识符.
	 */
	public String autoConfirmIdentifier = "^.*\\[([Yy]([Ee][Ss])?)/[Nn][Oo]?\\]:?$";
	/**
	 * 自动继续标识符.
	 */
	public String autoContinueIdentifier = null;

	/**
	 * 出错继续.
	 * <p>部分命令出错后不影响后续执行，此处作为特殊配置。</p>
	 */
	public List<ErrorContinueIdentifier> errorContinueIdentifier = new ArrayList<>();

	/**
	 * 结束标识符.
	 * <p> 正则表达式，支持多个表达式。 </p>
	 */
	public List<String> endIdentifier = new ArrayList<>();
	/**
	 * 需要移除的控制符标识.
	 */
	public List<String> removeCtrlCharIdentifier = new ArrayList<String>() {{
		add("\\u001b\\[\\d+D.*?\\u001b\\[\\d+D");
	}};

	/**
	 * telnet用户名标识符.
	 * <p>
	 * SSH中执行telnet命令的场景，提示输入telnet用户名的标识符。
	 * </p>
	 */
	public String telnetUsernameIdentifier = null;

	/**
	 * telnet密码标识符.
	 * <p>
	 * SSH中执行telnet命令的场景，提示输入telnet密码的标识符。
	 * </p>
	 */
	public String telnetPasswordIdentifier = null;

	/**
	 * 读取时间间隔.单位：毫秒.
	 */
	public Long readTimeInterval = 500L;
	/**
	 * 设备退出命令.
	 */
	public String exitCommand;

	public String getPaginationIdentifier() {
		return paginationIdentifier;
	}

	public void setPaginationIdentifier(String paginationIdentifier) {
		this.paginationIdentifier = paginationIdentifier;
	}

	public String getPaginationPressKey() {
		return paginationPressKey;
	}

	public void setPaginationPressKey(String paginationPressKey) {
		this.paginationPressKey = paginationPressKey;
	}

	public String getErrorStartIdentifier() {
		return errorStartIdentifier;
	}

	public void setErrorStartIdentifier(String errorStartIdentifier) {
		this.errorStartIdentifier = errorStartIdentifier;
	}

	public String getAutoConfirmIdentifier() {
		return autoConfirmIdentifier;
	}

	public void setAutoConfirmIdentifier(String autoConfirmIdentifier) {
		this.autoConfirmIdentifier = autoConfirmIdentifier;
	}

	public String getAutoContinueIdentifier() {
		return autoContinueIdentifier;
	}

	public void setAutoContinueIdentifier(String autoContinueIdentifier) {
		this.autoContinueIdentifier = autoContinueIdentifier;
	}

	public List<ErrorContinueIdentifier> getErrorContinueIdentifier() {
		return errorContinueIdentifier;
	}

	public void setErrorContinueIdentifier(List<ErrorContinueIdentifier> errorContinueIdentifier) {
		this.errorContinueIdentifier = errorContinueIdentifier;
	}

	public List<String> getEndIdentifier() {
		return endIdentifier;
	}

	public void setEndIdentifier(List<String> endIdentifier) {
		this.endIdentifier = endIdentifier;
	}

	public List<String> getRemoveCtrlCharIdentifier() {
		return removeCtrlCharIdentifier;
	}

	public void setRemoveCtrlCharIdentifier(List<String> removeCtrlCharIdentifier) {
		this.removeCtrlCharIdentifier = removeCtrlCharIdentifier;
	}

	public String getTelnetUsernameIdentifier() {
		return telnetUsernameIdentifier;
	}

	public void setTelnetUsernameIdentifier(String telnetUsernameIdentifier) {
		this.telnetUsernameIdentifier = telnetUsernameIdentifier;
	}

	public String getTelnetPasswordIdentifier() {
		return telnetPasswordIdentifier;
	}

	public void setTelnetPasswordIdentifier(String telnetPasswordIdentifier) {
		this.telnetPasswordIdentifier = telnetPasswordIdentifier;
	}

	public Long getReadTimeInterval() {
		return readTimeInterval;
	}

	public void setReadTimeInterval(Long readTimeInterval) {
		this.readTimeInterval = readTimeInterval;
	}

	public String getExitCommand() {
		return exitCommand;
	}

	public void setExitCommand(String exitCommand) {
		this.exitCommand = exitCommand;
	}

	public static class ErrorContinueIdentifier implements Configuration {
		private String model;
		private String command;
		private String tip;

		public String getModel() {
			return model;
		}

		public void setModel(String model) {
			this.model = model;
		}

		public String getCommand() {
			return command;
		}

		public void setCommand(String command) {
			this.command = command;
		}

		public String getTip() {
			return tip;
		}

		public void setTip(String tip) {
			this.tip = tip;
		}

		@Override
		public String toString() {
			return JSONObject.toJSONString(this, SerializerFeature.WriteMapNullValue);
		}
	}

	@Override
	public String toString() {
		return JSONObject.toJSONString(this, SerializerFeature.WriteMapNullValue);
	}
}
