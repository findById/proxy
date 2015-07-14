package org.core.socket.http;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.core.socket.core.ProtocolProcessor;
import org.core.socket.core.SocketServer;
import org.core.socket.core.SocketSession;

public class HttpProcessor implements ProtocolProcessor, Runnable, CompletionHandler<Integer, ByteBuffer> {

	private final SocketSession session;
	private final SocketServer server;
	private final SimpleDateFormat dateFormat;

	public HttpProcessor(SocketSession session, SocketServer server) {
		this.session = session;
		this.server = server;
		this.dateFormat = new SimpleDateFormat("EE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
		this.dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
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
		ByteBuffer buffer = session.getBuffer();
		try {

			buffer.clear();
			server.execute(this);

		} catch (Throwable e) {
			buffer.clear();

			byte[] content = "error".getBytes();
			write(content);
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
		byte[] content = null;
		try {

			try {
				StringBuffer sb = new StringBuffer();
				sb.append("<!DOCTYPE html>\n<html>\n<head>\n<title>test</title>\n</head>\n<body>\n");
				sb.append("aaa");
				sb.append("asdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfsadfsadfsadfsadfasdfasdf");
				sb.append("</body>\n</html>\n");
				content = sb.toString().getBytes();
			} catch (Throwable e) {

			}

		} finally {
			write(content);
		}
	}

	private void write(byte[] content) throws Exception {
		ByteBuffer outputHeader = session.getBuffer();

		ByteBuffer outputContent = ByteBuffer.wrap(content);

		outputHeader.flip();
		outputContent.flip();

		session.write(outputHeader, outputContent, this);
	}

}
