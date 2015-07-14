package org.core.socket.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.core.socket.SocketConfig;

public final class SocketServer {

	private AsynchronousChannelGroup workerGroup = null;
	private AsynchronousServerSocketChannel serverSocket = null;

	private ExecutorService channelWorkers;
	private ExecutorService processWorkers;

	private SocketConfig serverConfig = new SocketConfig();
	private SocketAcceptHandler socketAcceptHandler = null;
	private SocketReadHandler socketReadHandler = new SocketReadHandler();

	private GenericObjectPool<ByteBuffer> byteBufferPool = null;

	private long timeout;
	private String name;

	public void startup() {
		try {

			int port = serverConfig.getInteger("server.socket.port", 80);
			int backlog = serverConfig.getInteger("server.socket.backlog", 100);
			this.timeout = serverConfig.getInteger("server.socket.timeout", 0);
			this.name = serverConfig.getString("server.name", "unknown-name");
			int threadPoolSize = serverConfig.getInteger("server.process.workers", 0);

			int buffer = serverConfig.getInteger("server.channel.buffer", 8192);
			boolean direct = serverConfig.getBoolean("server.channel.direct", false);
			int maxActive = serverConfig.getInteger("server.channel.maxActive", 100);
			int maxWait = serverConfig.getInteger("server.channel.maxWait", 1000);

			System.out.println("server.bindPort " + port);
			System.out.println("server.backlog " + backlog);
			System.out.println("server.timeout " + timeout + " ms.");
			System.out.println("server.name " + this.name);
			System.out.println("server.worker.threads " + threadPoolSize);

			GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();

			poolConfig.maxActive = maxActive;
			poolConfig.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_FAIL;
			poolConfig.maxWait = maxWait;
			// poolConfig.minIdle = minIdle
			// poolConfig.maxIdle = maxIdle
			poolConfig.testOnBorrow = false;
			poolConfig.testOnReturn = false;
			poolConfig.timeBetweenEvictionRunsMillis = 900000;
			poolConfig.minEvictableIdleTimeMillis = 6;
			poolConfig.testWhileIdle = false;

			byteBufferPool = new GenericObjectPool<ByteBuffer>(new ByteBufferFactory(direct, buffer), poolConfig);

			int availableProcessors = Runtime.getRuntime().availableProcessors() + 1;
			channelWorkers = Executors.newFixedThreadPool(availableProcessors, new ProcessorThreadFactory());

			if (threadPoolSize > 0) {
				processWorkers = Executors.newFixedThreadPool(threadPoolSize);
			} else {
				processWorkers = Executors.newCachedThreadPool();
			}

			workerGroup = AsynchronousChannelGroup.withCachedThreadPool(channelWorkers, 1);
			serverSocket = AsynchronousServerSocketChannel.open(workerGroup);

			serverSocket.bind(new InetSocketAddress(port), backlog);

			socketAcceptHandler = new SocketAcceptHandler(this);

			this.accept();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void accept() {
		serverSocket.accept(null, socketAcceptHandler);
	}

	public void execute(Runnable session) {
		processWorkers.submit(session);
	}

	public void shutdown() throws IOException {
		this.channelWorkers.shutdown();
		this.processWorkers.shutdown();

		this.serverSocket.close();
		this.workerGroup.shutdown();
		System.out.println("service shutdown completed");
	}

	public long getTimeout() {
		return timeout;
	}

	public String getName() {
		return name;
	}

	public ByteBuffer borrowObject() throws Exception {
		return byteBufferPool.borrowObject();
	}

	public void returnObject(ByteBuffer buffer) throws Exception {
		byteBufferPool.returnObject(buffer);
	}

	public SocketReadHandler getSocketReadHandler() {
		return socketReadHandler;
	}

}
