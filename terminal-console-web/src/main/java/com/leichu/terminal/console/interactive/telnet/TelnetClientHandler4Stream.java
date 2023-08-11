package com.leichu.terminal.console.interactive.telnet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Arrays;
import java.util.List;

@ChannelHandler.Sharable
public class TelnetClientHandler4Stream extends SimpleChannelInboundHandler<ByteBuf> implements TelnetTcpClientHandler {

	private final List<byte[]> RESPONSE;

	public TelnetClientHandler4Stream(List<byte[]> RESPONSE) {
		this.RESPONSE = RESPONSE;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("TelnetClientHandler4Stream.channelInactive called");
		super.channelInactive(ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("TelnetClientHandler4Stream.channelActive called");
	}


	//打印读取到的数据
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
		RESPONSE.add(ByteBufUtil.getBytes(msg));
	}

	//异常数据捕获
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	public List<byte[]> getRESPONSE() {
		return RESPONSE;
	}
}
