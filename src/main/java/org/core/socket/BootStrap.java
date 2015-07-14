package org.core.socket;

import org.core.socket.core.SocketServer;

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
