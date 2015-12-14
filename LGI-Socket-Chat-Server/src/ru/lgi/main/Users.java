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
	private String login, password;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}
}
