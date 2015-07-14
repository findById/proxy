package org.core.socket;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SocketConfig {

	private final String FILE_NAME = "proxy.properties";
	private Properties proerties = new Properties();

	public SocketConfig() {
		System.out.println("load properties classpath:" + FILE_NAME);
		InputStream in = null;
		try {
			in = SocketConfig.class.getClassLoader().getResourceAsStream(FILE_NAME);
			proerties.load(in);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	public String getString(String key, String defaultValue) {
		return proerties.getProperty(key, defaultValue);
	}

	public int getInteger(String key, int defaultValue) {
		String tmp = proerties.getProperty(key);
		int result = defaultValue;
		if (tmp != null) {
			try {
				result = Integer.parseInt(tmp);
			} catch (Exception e) {
				result = defaultValue;
			}
		}
		return result;
	}

	public long getLong(String key, long defaultValue) {
		String tmp = proerties.getProperty(key);
		long result = defaultValue;
		if (tmp != null) {
			try {
				result = Long.parseLong(tmp);
			} catch (Exception e) {
				result = defaultValue;
			}
		}
		return result;
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		String tmp = proerties.getProperty(key);
		boolean result = defaultValue;
		if (tmp != null) {
			try {
				result = Boolean.parseBoolean(tmp);
			} catch (Exception e) {
				result = defaultValue;
			}
		}
		return result;
	}

}
