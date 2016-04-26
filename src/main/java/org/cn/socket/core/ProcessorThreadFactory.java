package org.cn.socket.core;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class ProcessorThreadFactory implements ThreadFactory {
	private final AtomicInteger threadNumber = new AtomicInteger(1);
	private final ThreadGroup group;
	private final String prefix;

	public ProcessorThreadFactory() {
		SecurityManager manager = System.getSecurityManager();
		group = (manager != null) ? manager.getThreadGroup() : Thread.currentThread().getThreadGroup();
		prefix = "processor-thread-";
	}

	@Override
	public Thread newThread(Runnable runnable) {
		Thread thread = new Thread(group, runnable, prefix + threadNumber.getAndIncrement(), 0);
		if (thread.isDaemon())
			thread.setDaemon(false);
		if (thread.getPriority() != Thread.MAX_PRIORITY) {
			thread.setPriority(Thread.MAX_PRIORITY);
		}
		return thread;
	}

}
