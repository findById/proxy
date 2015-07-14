package org.core.socket.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.core.socket.core.SocketSession;

public class ProxySerializer {

	public boolean decode(SocketSession session, ProxyProcessor processor) throws Exception {

		boolean finished = false;
		try {
			ByteBuffer buffer = session.getBuffer();

			buffer.flip();
			int index = buffer.position();

			switch (processor.getStatus()) {
			case READ_HEAD:
				try {
					byte byte1 = buffer.get(index++);
					if (byte1 != 0x05) {
						throw new IOException("Unsupported version " + byte1);
					}
					byte byte2 = buffer.get(index++);
					if (byte2 == 0x01) {
						processor.setStatus(SocketStatus.READ_ADDRESS);
					}

					processor.setStatus(SocketStatus.READ_ADDRESS);

				} catch (IndexOutOfBoundsException e) {
				}

				byte[] auth = new byte[] { 0x05, 0x00 };
				session.write(ByteBuffer.wrap(auth), null, processor);

				break;
			case READ_AUTH:

				break;
			case READ_ADDRESS:
				processor.setStatus(SocketStatus.READ_DATA);

				InetSocketAddress address = IOUtil.readAddress(buffer);

				processor.setRemoteAddress(address);

				connect(session, processor, address);

				break;
			case READ_DATA:
				finished = true;
				break;
			default:
				break;
			}
		} finally {
		}
		return finished;
	}

	private void connect(SocketSession session, ProxyProcessor processor, SocketAddress address)
			throws Exception {
		AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
		client.setOption(StandardSocketOptions.TCP_NODELAY, true);
		client.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		client.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
		client.setOption(StandardSocketOptions.SO_SNDBUF, 1024 * 10);

		System.out.println("connecting >>　" + address);

		client.connect(address, session, new CompletionHandler<Void, SocketSession>() {
			@Override
			public void completed(Void result, SocketSession attachment) {
				try {
					processor.setClientChannel(client);

					System.out.println("connected >>　" + address);

					byte[] addr = ((InetSocketAddress) client.getLocalAddress()).getAddress().getAddress();
					int port = ((InetSocketAddress) client.getLocalAddress()).getPort();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ByteBuffer portBuffer = ByteBuffer.allocate(2);
					portBuffer = portBuffer.order(ByteOrder.BIG_ENDIAN);
					portBuffer.putShort((short) port);

					int addrType = addr.length == 4 ? 0x01 : 0x04;
					baos.write(0x05);
					baos.write(0x00);
					baos.write(0x00);
					baos.write(addrType);
					baos.write(addr);
					baos.write(portBuffer.array());

					attachment.write(ByteBuffer.wrap(baos.toByteArray()), null, processor);
				} catch (Exception e) {
					e.printStackTrace();
					attachment.close();
				}

			}

			@Override
			public void failed(Throwable exc, SocketSession attachment) {
				exc.printStackTrace();
				attachment.close();
			}
		});

	}
}
