package org.cn.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class TestClient {

	public void startup(SocketAddress socketAddress) throws IOException {
		AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
		client.setOption(StandardSocketOptions.TCP_NODELAY, true);
		client.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		client.setOption(StandardSocketOptions.SO_KEEPALIVE, true);

		System.out.println("connect >>　" + socketAddress);

		client.connect(socketAddress, null, new CompletionHandler<Void, Object>() {
			@Override
			public void completed(Void result, Object attachment) {
				try {
					System.out.println("=============");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void failed(Throwable exc, Object attachment) {
				exc.printStackTrace();
			}
		});
	}

	public static void main(String[] args) {
		try {
			new TestClient().startup(new InetSocketAddress("", 80));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
