package com.leichu.terminal.console.interactive.telnet;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class TelnetClientHandler extends SimpleChannelInboundHandler<String> {

	private static final StringBuffer RESPONSE = new StringBuffer();

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("TelnetClientHandler.channelInactive called");
		super.channelInactive(ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("TelnetClientHandler.channelActive called");
	}


	//打印读取到的数据
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		RESPONSE.append(msg);
	}

	//异常数据捕获
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	public void clearResponse() {
		if (RESPONSE.length() <= 0) {
			return;
		}
		RESPONSE.delete(0, RESPONSE.length());
	}

	public String readResponse() {
		return RESPONSE.toString();
	}

}
