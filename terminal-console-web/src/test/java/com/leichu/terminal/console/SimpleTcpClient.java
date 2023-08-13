package com.leichu.terminal.console;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

public class SimpleTcpClient {

	/** 连接地址 */
	protected volatile String addr = "192.168.60.60";
	/** 连接端口 */
	protected volatile int port = 9988;
	/** 关闭标识 */
	protected volatile boolean closed = false;
	/** {@link java.net.SocketAddress} endpoint */
	protected volatile SocketAddress endpoint;
	/** {@link org.apache.mina.transport.socket.nio.NioSocketConnector} connector */
	protected volatile NioSocketConnector connector;
	/** {@link org.apache.mina.core.session.IoSession} session */
	protected volatile IoSession session;

	/**
	 * Create a tcp client.
	 *
	 * @param addr      IP 地址
	 * @param port      端口号
	 */
	public SimpleTcpClient(String addr, int port) {
		this.addr = addr;
		this.port = port;
		this.endpoint = new InetSocketAddress(addr, port);
	}

	/**
	 * Close the tcp client.
	 */
	public void stop() {
		this.closed = true;
		Optional.ofNullable(this.session).ifPresent(x -> x.closeNow());
		Optional.ofNullable(this.connector).ifPresent(x -> x.dispose());
	}

	/**
	 * Connect to tcp server.
	 *
	 * @param consumer
	 * @throws Exception
	 */
	public void start(Consumer<NioSocketConnector> consumer) throws Exception {
		this.closed = false;
		this.connector = new NioSocketConnector();
		this.connector.setConnectTimeoutMillis(3000);
		this.connector.getSessionConfig().setReuseAddress(true);
		this.connector.getSessionConfig().setKeepAlive(true);
		this.connector.getSessionConfig().setTcpNoDelay(true);
		this.connector.getSessionConfig().setSoLinger(0);
		this.connector.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE, 30);
		this.connector.getFilterChain().addFirst("reconnection", new IoFilterAdapter() {
			@Override
			public void sessionClosed(NextFilter nextFilter, IoSession ioSession) throws Exception {
				CompletableFuture.runAsync(() -> doConnect());
			}
		});
		// this.connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(ProtocolCodecFactory));
		// this.connector.setHandler(IoHandler);
		Optional.ofNullable(consumer).ifPresent(x -> x.accept(this.connector));
		doConnect();
		System.out.println("connected");
	}

	/**
	 *
	 */
	protected void doConnect() {
		if (this.closed) {
			return;
		}
		this.connector.connect(this.endpoint).addListener(new IoFutureListener<ConnectFuture>() {
			@Override
			public void operationComplete(ConnectFuture future) {
				if (!future.isConnected()) {
					CompletableFuture.runAsync(() -> doConnect());
				} else {
					session = future.getSession();
				}
			}
		});
	}

	/**
	 *
	 * @param data
	 */
	public void sent(Object data) {
		Optional.ofNullable(this.session)
				.filter(x -> x.isActive())
				.ifPresent(x -> x.write(data));
	}

	public static void main(String[] args) throws Exception{
		SimpleTcpClient client = new SimpleTcpClient( "vm", 23);
		client.start(nioSocketConnector -> {
			nioSocketConnector.getFilterChain().addLast("cus", new IoFilterAdapter(){
				@Override
				public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
					System.out.println(">>> " + message);
					nextFilter.messageReceived(session, message);
				}
			});
		});
//		client.doConnect();

		client.sent("\n");
		while (true);

	}

}

