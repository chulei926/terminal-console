package com.leichu.terminal.console;

public class MinaTest {

	public static void main(String[] args) {
		char c = '\r';
		byte[] bytes = String.valueOf(c).getBytes();
		for (byte aByte : bytes) {
			System.out.println(aByte);
		}

	}
}
