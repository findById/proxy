package org.core.socket.core;

import java.nio.channels.CompletionHandler;

public class SocketReadHandler implements CompletionHandler<Integer, SocketSession> {

	@Override
	public void completed(Integer result, SocketSession attachment) {
		if (result == -1) {
			attachment.close();
			return;
		}
		try {
			attachment.process();
		} catch (Throwable e) {
			attachment.close();
			e.printStackTrace();
		}
	}

	@Override
	public void failed(Throwable exc, SocketSession attachment) {
		attachment.close();
	}

}
