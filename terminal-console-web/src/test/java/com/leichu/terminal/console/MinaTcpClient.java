package com.leichu.terminal.console;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author
 * @version 1.0
 * @说明 Mina TCP客户端
 * @since
 */
public class MinaTcpClient extends IoHandlerAdapter {
	private static final String host = "123.249.85.241";
	private static final int port = 23;

	private IoConnector connector;
	private static IoSession session;

	private static final StringBuffer BUFFER = new StringBuffer();

	public MinaTcpClient() {
		connector = new NioSocketConnector();
		connector.setHandler(this);
		ConnectFuture connFuture = connector.connect(new InetSocketAddress(host, port));
		connFuture.awaitUninterruptibly();
		session = connFuture.getSession();
		System.out.println("TCP 客户端启动");

		// Send initial message including terminal type
		IoBuffer initialBuffer = IoBuffer.allocate(1);
		try {
			initialBuffer.setAutoExpand(true);
			initialBuffer.setAutoShrink(true);
			initialBuffer.putString("xterm\n", Charset.forName("US-ASCII").newEncoder());
			initialBuffer.flip();
			session.write(initialBuffer);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		MinaTcpClient client = new MinaTcpClient();


		IoBuffer buffer = IoBuffer.allocate(1);
		// 自动扩容
		buffer.setAutoExpand(true);
		// 自动收缩
		buffer.setAutoShrink(true);
		buffer.put("\n".getBytes(StandardCharsets.UTF_8));
		buffer.flip();
		session.write(buffer);



		while (true){

			try {

				Thread.sleep(1000);

				if (BUFFER.toString().contains("login")) {
					buffer = IoBuffer.allocate(1);
					// 自动扩容
					buffer.setAutoExpand(true);
					// 自动收缩
					buffer.setAutoShrink(true);
					buffer.put("root".getBytes());
					buffer.flip();
					session.write(buffer);
					BUFFER.setLength(0);
					continue;
				}

				if (BUFFER.toString().contains("Password")) {
					buffer = IoBuffer.allocate(1);
					// 自动扩容
					buffer.setAutoExpand(true);
					// 自动收缩
					buffer.setAutoShrink(true);
					buffer.put("csx@20190821".getBytes());
					buffer.flip();
					session.write(buffer);
					BUFFER.setLength(0);
					continue;
				}
				System.out.println(BUFFER);
			} catch (Exception e){
				e.printStackTrace();
				break;
			}

		}

		// 关闭会话，待所有线程处理结束后 
		client.connector.dispose(true);
	}

	@Override
	public void messageReceived(IoSession iosession, Object message)
			throws Exception {
		System.out.println("=========" + message);
		IoBuffer bbuf = (IoBuffer) message;
		System.out.println("+++++++++" + new String(bbuf.array()));
		byte[] byten = new byte[bbuf.limit()];
		bbuf.get(byten, bbuf.position(), bbuf.limit());
		System.out.println("客户端收到消息" + new String(byten, StandardCharsets.UTF_8));
		System.out.println("客户端收到消息" + new String(byten, StandardCharsets.US_ASCII));
		BUFFER.append(new String(byten));

	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		System.out.println("客户端异常");
		super.exceptionCaught(session, cause);
	}

	@Override
	public void messageSent(IoSession iosession, Object obj) throws Exception {
		System.out.println("客户端消息发送");
		super.messageSent(iosession, obj);
	}

	@Override
	public void sessionClosed(IoSession iosession) throws Exception {
		System.out.println("客户端会话关闭");
		super.sessionClosed(iosession);
	}

	@Override
	public void sessionCreated(IoSession iosession) throws Exception {
		System.out.println("客户端会话创建");
		super.sessionCreated(iosession);
	}

	@Override
	public void sessionIdle(IoSession iosession, IdleStatus idlestatus)
			throws Exception {
		System.out.println("客户端会话休眠");
		super.sessionIdle(iosession, idlestatus);
	}

	@Override
	public void sessionOpened(IoSession iosession) throws Exception {
		System.out.println("客户端会话打开");
		super.sessionOpened(iosession);
	}
}