/**
 * 
 */
package ru.lgi.main;

import java.io.Serializable;

/**
 * @author LaughingMaan
 *
 */
@SuppressWarnings("serial")
public class Settings implements Serializable {
	private String ip;
	private int port;

	

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
}
