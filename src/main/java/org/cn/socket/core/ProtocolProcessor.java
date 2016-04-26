package org.cn.socket.core;

public interface ProtocolProcessor {

	void process() throws Throwable;

	void close();

}
