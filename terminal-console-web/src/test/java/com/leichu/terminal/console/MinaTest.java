package com.leichu.terminal.console;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;

public class MinaTest {


	public static void main(String[] args) {
		NioSocketConnector connector = new NioSocketConnector();
		connector.setConnectTimeoutMillis(1000 * 10);
		connector.getFilterChain().addLast("codec",
				new ProtocolCodecFilter(new TextLineCodecFactory()));
		connector.getFilterChain().addLast("logger", new LoggingFilter());

		connector.setHandler(new IoHandlerAdapter());
		IoSession session;
		try {
			ConnectFuture future = connector.connect(new InetSocketAddress("vm", 23));
			future.awaitUninterruptibly();
			session = future.getSession();
			session.getConfig().setUseReadOperation(true);
			new Thread(()->{
				while (true){
					ReadFuture read = session.read();
					read.awaitUninterruptibly(1000);
					System.out.println(">>> Received2: " + read.getMessage());
				}
			}).start();

			session.write("\r\n");
			session.write("\r\n");
			session.write("\r\n");
			session.write("\r\n");
			session.write("\r\n");
			session.closeOnFlush();

//			session.getCloseFuture().awaitUninterruptibly();
//			connector.dispose();
		} catch (RuntimeIoException e) {
			System.err.println("Failed to connect.");
			e.printStackTrace();
		}

		// wait until the summation is done


	}

	public static class ClientSessionHandler extends IoHandlerAdapter {


	}
}
