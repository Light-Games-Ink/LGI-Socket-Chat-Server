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
public class Users implements Serializable {
	private String md5;
	private boolean isAdmin;


	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}
}
