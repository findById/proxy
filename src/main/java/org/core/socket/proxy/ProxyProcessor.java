package org.core.socket.proxy;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.core.socket.core.ProtocolProcessor;
import org.core.socket.core.SocketServer;
import org.core.socket.core.SocketSession;

public class ProxyProcessor implements ProtocolProcessor, Runnable, CompletionHandler<Integer, ByteBuffer> {

	private final SocketSession session;
	private final SocketServer server;

	private SocketStatus status;

	private SocketAddress address;

	private AsynchronousSocketChannel clientChannel = null;

	private ProxySerializer serializer;

	public ProxyProcessor(SocketSession session, SocketServer server) {
		this.session = session;
		this.server = server;
		status = SocketStatus.READ_HEAD;
		serializer = new ProxySerializer();
	}

	@Override
	public void run() {
		try {
			this.execute();
		} catch (Throwable e) {
			e.printStackTrace();
			session.close();
		}
	}

	@Override
	public void process() throws Throwable {

		try {
			if (serializer.decode(session, this)) {
				server.execute(this);
			}
		} catch (Throwable e) {
			session.close();
		}

	}

	@Override
	public void close() {
		// ignore
	}

	@Override
	public void completed(Integer result, ByteBuffer attachment) {
		if (attachment != null && attachment.remaining() > 0) {
			session.write(attachment, attachment, this);
		}
		if (result < 0) {
			session.close();
			return;
		}
		session.read();
	}

	@Override
	public void failed(Throwable exc, ByteBuffer attachment) {
		session.close();
	}

	private void execute() throws Exception {
		try {
			System.out.println(">> " + address);

			ByteBuffer clientBuffer = session.getBuffer();
			if (clientBuffer == null) {
				clientBuffer = ByteBuffer.allocate(0);
			}
			clientBuffer.position(0);
			clientChannel.write(clientBuffer, session, new CompletionHandler<Integer, SocketSession>() {
				@Override
				public void completed(Integer result, SocketSession attachment) {

					if (result < 0) {
						return;
					}

//					ProxySession proxySession = new ProxySession(attachment);
//					System.out.println("Proxy session create: " + proxySession.hashCode());
//					proxySession.read();

					ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 10);
					byteBuffer.position(0);
					System.out.println("<< " + session.getRemoteAddress());

					clientChannel.read(byteBuffer, attachment, new CompletionHandler<Integer, SocketSession>() {
						@Override
						public void completed(Integer result, SocketSession attachment) {

							try {
								if (result < 0) {
									attachment.close();
									return;
								}

								byteBuffer.flip();
								byteBuffer.position(0);

								StringBuffer sb = new StringBuffer();
								for (int i = 0; i < byteBuffer.limit(); i++) {
									sb.append(byteBuffer.get(i)).append(" ");
								}
								System.out.println(sb.toString());

								byteBuffer.position(0);
								int i = attachment.write(byteBuffer).get();
								if (i < 0) {
									return;
								}
							} catch (Exception e) {
								attachment.close();
							}

							try {
								byteBuffer.position(0);
								clientChannel.read(byteBuffer, attachment, this);
							} catch (Exception e) {
								// ignore maybe
							}
						}

						@Override
						public void failed(Throwable exc, SocketSession attachment) {
							// ignore maybe
							exc.printStackTrace();
						}
					});

				}

				@Override
				public void failed(Throwable exc, SocketSession attachment) {
					exc.printStackTrace();
					attachment.close();
				}
			});

		} finally {
		}
	}

	public SocketStatus getStatus() {
		return status;
	}

	public void setStatus(SocketStatus status) {
		this.status = status;
	}

	public void setRemoteAddress(InetSocketAddress inetSocketAddress) {
		this.address = inetSocketAddress;
	}

	public void setClientChannel(AsynchronousSocketChannel client) {
		this.clientChannel = client;
	}

}
