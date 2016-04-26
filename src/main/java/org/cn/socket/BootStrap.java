package org.cn.socket;

import org.cn.socket.core.SocketServer;

public final class BootStrap {

	public BootStrap() {
	}

	public void startup() {
		SocketServer server = new SocketServer();
		server.startup();
	}

	public static void main(String[] args) {
		new BootStrap().startup();
	}

}
