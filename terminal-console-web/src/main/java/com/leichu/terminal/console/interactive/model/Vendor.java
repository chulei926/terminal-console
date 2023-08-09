package com.leichu.terminal.console.interactive.model;

/**
 * 厂商.
 *
 * @author leichu.
 * @since 2023-07-30.
 */
public enum Vendor {

	HW,
	ZTE,
	FH,
	LINUX;

	public static Vendor match(String name){
		for (Vendor value : Vendor.values()) {
			if (value.name().equalsIgnoreCase(name)){
				return value;
			}
		}
		return null;
	}
}
