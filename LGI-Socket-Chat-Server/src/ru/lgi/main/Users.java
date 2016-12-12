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
	private String color_s;
	private String login;
	


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

	public String getColor_s() {
		return color_s;
	}

	public void setColor_s(String color_s) {
		this.color_s = color_s;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}
}
