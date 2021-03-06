package org.cn.socket.core;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class SocketAcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {

	private final SocketServer socketServer;

	public SocketAcceptHandler(SocketServer socketServer) {
		this.socketServer = socketServer;
	}

	@Override
	public void completed(AsynchronousSocketChannel result, Object attachment) {
		try {
			SocketSession session = new SocketSession(result, socketServer);
			System.out.println("Socket session create: " + session.hashCode());
			session.read();
		} catch (Throwable e) {
			e.printStackTrace();
			try {
				result.close();
			} catch (IOException ignored) {
			}
		} finally {
			socketServer.accept();
		}
	}

	@Override
	public void failed(Throwable exc, Object attachment) {
		socketServer.accept();
	}

}
