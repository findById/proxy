package org.cn.socket.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class IOUtil {

	public static void copyStream(InputStream is, OutputStream os) throws IOException {
		byte[] bytes = new byte[1024 * 5];
		int len = 0;
		while ((len = is.read(bytes)) != -1) {
			os.write(bytes, 0, len);
		}
	}

	public static InetSocketAddress readAddress(ByteBuffer buffer) {
		String host = "";
		int port = 0;

		// buffer.position(3);

		int aType = buffer.get(3);
		switch (aType) {
		case 0x01:
			for (int i = 4; i < 7; i++) {
				host += Byte.toUnsignedInt(buffer.get(i)) + ".";
			}
			host += Byte.toUnsignedInt(buffer.get(7));
			break;
		case 0x04:
			break;
		case 0x06:
			break;
		default:
			break;
		}
		port = (Byte.toUnsignedInt(buffer.get(8)) << 8) | (Byte.toUnsignedInt(buffer.get(9)));
		return new InetSocketAddress(host, port);
	}

}
