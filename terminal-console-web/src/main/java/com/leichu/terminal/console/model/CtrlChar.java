package com.leichu.terminal.console.model;

public enum CtrlChar {


	ENTER(13),
	ESC(27),
	DEL(127),
	UP(183),
	DOWN(184),
	LEFT(186),
	RIGHT(185),


	;

	private final int ascii;

	CtrlChar(int ascii) {
		this.ascii = ascii;
	}


	public static CtrlChar match(byte[] commandSegment) {
		int sum = 0;
		for (int i = 0; i < commandSegment.length; i++) {
			sum += commandSegment[i];
		}

		for (CtrlChar value : CtrlChar.values()) {
			if (value.ascii == sum) {
				return value;
			}
		}
		return null;
	}
}
