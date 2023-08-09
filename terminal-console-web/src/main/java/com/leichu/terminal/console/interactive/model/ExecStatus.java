package com.leichu.terminal.console.interactive.model;

/**
 * 执行状态.
 *
 * @author leichu.
 * @since 2023-07-30.
 */
public enum ExecStatus {

	WAITING,
	EXECUTING,
	CANCELED,
	SUCCESS,
	FAILED,
	BREAK

}
