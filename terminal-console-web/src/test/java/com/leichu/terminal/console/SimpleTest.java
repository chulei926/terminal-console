package com.leichu.terminal.console;

import com.google.common.primitives.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.nio.ByteBuffer;

public class SimpleTest {

	public static void main(String[] args) {
//		byte[] bytes = new byte[0];
		ByteBuffer buffer = ByteBuffer.allocate(1024);

		buffer.put("hello".getBytes());
		System.out.println(buffer.limit());
		System.out.println(buffer.capacity());
		System.out.println(buffer.flip());
		System.out.println(new String(buffer.array()));
		buffer.clear();
		System.out.println(new String(buffer.array()));



	}

}
