package org.cn.socket.core;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.cn.socket.proxy.ProxyProcessor;

public final class SocketSession implements Session {

	private ByteBuffer buffer;
	private AsynchronousSocketChannel socket;
	private long timeout;

	private SocketServer server;
	private SocketReadHandler socketReadHandler;
	private ProtocolProcessor processor;

	private boolean isClosed = false;

	private InetAddress remoteAddress;

	public SocketSession(AsynchronousSocketChannel socket, SocketServer server) throws Exception {
		this.socket = socket;
		this.timeout = server.getTimeout();
		this.server = server;
		this.socketReadHandler = server.getSocketReadHandler();

		this.remoteAddress = ((InetSocketAddress) socket.getRemoteAddress()).getAddress();

		this.buffer = server.borrowObject();

		this.processor = new ProxyProcessor(this, server);

	}

	public InetAddress getRemoteAddress() {
		return remoteAddress;
	}

	public void process() {
		try {
			processor.process();
		} catch (Throwable e) {
			e.printStackTrace();
			this.close();
		}
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

	public long getTimeout() {
		return timeout;
	}

	public void read() {
		try {
			if (this.buffer == null && this.server != null) {
				this.buffer = this.server.borrowObject();
			}
		} catch (Exception ignored) {
		}
		if (this.buffer == null) {
			this.buffer = ByteBuffer.allocateDirect(1024);
		}
		this.buffer.clear();
		try {
			this.socket.read(this.buffer, timeout, TimeUnit.MILLISECONDS, this, socketReadHandler);
		} catch (Throwable e) {
		}
	}

	public <A> void write(ByteBuffer buffer, A attachment, CompletionHandler<Integer, ? super A> handler) {
		this.socket.write(buffer, timeout, TimeUnit.MILLISECONDS, attachment, handler);
	}

	public Future<Integer> write(ByteBuffer buffer) {
		return this.socket.write(buffer);
	}

	public void close() {
		synchronized (this) {
			if (isClosed) {
				return;
			} else {
				isClosed = true;
			}
		}
		if (buffer != null) {
			buffer.clear();
			try {
				server.returnObject(buffer);
			} catch (Exception ignored) {
			}
		}
		if (this.socket != null) {
			try {
				this.socket.close();
			} catch (Throwable ignored) {
			}
		}
		if (processor != null) {
			try {
				processor.close();
			} catch (Throwable ignored) {
			}
		}

		System.out.println("Socket session closed: " + this.hashCode());

		// Release All Reference
		socket = null;
		buffer = null;
		server = null;
		processor = null;
	}

}
