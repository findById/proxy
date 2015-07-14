package org.core.socket.core;

import java.nio.ByteBuffer;

import org.apache.commons.pool.PoolableObjectFactory;

public class ByteBufferFactory implements PoolableObjectFactory<ByteBuffer> {
	private final boolean direct;
	private final int capacity;

	public ByteBufferFactory(boolean direct, int capacity) {
		this.direct = direct;
		this.capacity = capacity;

	}

	@Override
	public ByteBuffer makeObject() throws Exception {
		if (direct) {
			return ByteBuffer.allocateDirect(capacity);
		} else {
			return ByteBuffer.allocate(capacity);
		}
	}

	@Override
	public void destroyObject(ByteBuffer buffer) throws Exception {
		buffer.clear();
	}

	@Override
	public boolean validateObject(ByteBuffer obj) {
		return true;
	}

	@Override
	public void activateObject(ByteBuffer obj) throws Exception {
	}

	@Override
	public void passivateObject(ByteBuffer obj) throws Exception {
	}

}
