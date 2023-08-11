package com.leichu.terminal.console.interactive.telnet;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class TelnetClientHandler extends SimpleChannelInboundHandler<String> implements TelnetTcpClientHandler {

	private final StringBuffer RESPONSE;

	public TelnetClientHandler(StringBuffer RESPONSE) {
		this.RESPONSE = RESPONSE;
	}

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
		System.out.println(">>>>> " + msg);
		RESPONSE.append(msg);
	}

	//异常数据捕获
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

}
