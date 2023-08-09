package com.leichu.terminal.console.interactive.model;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.Serializable;
import java.util.Date;

/**
 * 命令对象.
 *
 * @author leichu.
 * @since 2023-07-30.
 */
public class Command implements Serializable {

	private String cid;
	private Integer order;
	private String command;
	private Date startTime;
	private Date endTime;
	private Long costTime;
	private ExecStatus status;
	private String result;
	private String failDesc;
	private Boolean interrupt = Boolean.FALSE;
	private Command preCommand;
	private Command nextCommand;
	private Boolean isLast = Boolean.FALSE;
	private Long timeout = 1000L;

	public Command() {
	}

	public Command(String command) {
		this.command = command;
	}

	public String getCid() {
		return cid;
	}

	public Command setCid(String cid) {
		this.cid = cid;
		return this;
	}

	public Integer getOrder() {
		return order;
	}

	public Command setOrder(Integer order) {
		this.order = order;
		return this;
	}

	public String getCommand() {
		return command;
	}

	public Command setCommand(String command) {
		this.command = command;
		return this;
	}

	public Date getStartTime() {
		return startTime;
	}

	public Command setStartTime(Date startTime) {
		this.startTime = startTime;
		return this;
	}

	public Date getEndTime() {
		return endTime;
	}

	public Command setEndTime(Date endTime) {
		this.endTime = endTime;
		return this;
	}

	public Long getCostTime() {
		return costTime;
	}

	public Command setCostTime(Long costTime) {
		this.costTime = costTime;
		return this;
	}

	public ExecStatus getStatus() {
		return status;
	}

	public Command setStatus(ExecStatus status) {
		this.status = status;
		return this;
	}

	public String getResult() {
		return result;
	}

	public Command setResult(String result) {
		this.result = result;
		return this;
	}

	public String getFailDesc() {
		return failDesc;
	}

	public Command setFailDesc(String failDesc) {
		this.failDesc = failDesc;
		return this;
	}

	public Boolean getInterrupt() {
		return interrupt;
	}

	public Command setInterrupt(Boolean interrupt) {
		this.interrupt = interrupt;
		return this;
	}

	public Command getPreCommand() {
		return preCommand;
	}

	public Command setPreCommand(Command preCommand) {
		this.preCommand = preCommand;
		return this;
	}

	public Command getNextCommand() {
		return nextCommand;
	}

	public Command setNextCommand(Command nextCommand) {
		this.nextCommand = nextCommand;
		return this;
	}

	public Boolean getIsLast() {
		return isLast;
	}

	public Command setIsLast(Boolean isLast) {
		this.isLast = isLast;
		return this;
	}

	public Long getTimeout() {
		return timeout;
	}

	public Command setTimeout(Long timeout) {
		this.timeout = timeout;
		return this;
	}

	@Override
	public String toString() {
		return JSONObject.toJSONString(this, SerializerFeature.WriteMapNullValue);
	}
}
