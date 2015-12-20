package ru.lgi.main;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import by.zti.SerializationManager;
import naga.NIOSocket;
import naga.SocketObserver;
import naga.eventmachine.DelayedEvent;
import naga.packetreader.AsciiLinePacketReader;
import naga.packetwriter.AsciiLinePacketWriter;
class User implements SocketObserver {
	private final static long LOGIN_TIMEOUT = 1 * 60 * 1000; // 1 min timeout
	private final static long INACTIVITY_TIMEOUT = 30 * 60 * 1000; // 30 min
																	// timeout
	private final Main m_server;
	private final NIOSocket m_socket;
	private String m_name, m_color;
	private DelayedEvent m_disconnectEvent;
	private Users m_userCreds;
	public boolean isInTheList;
	private boolean isAdmin;


	public String getM_name() {
		return m_name;
	}


	User(Main server, NIOSocket socket) {
		m_server = server;
		m_socket = socket;
		m_socket.setPacketReader(new AsciiLinePacketReader());
		m_socket.setPacketWriter(new AsciiLinePacketWriter());
		m_socket.listen(this);
		m_name = "";
		m_color = "#000000";
		m_userCreds = new Users();
		isInTheList = false;
		isAdmin = false;
	}

	public void connectionOpened(NIOSocket nioSocket) {
		// We start by scheduling a disconnect event for the login.
		m_disconnectEvent = m_server.getEventMachine().executeLater(new Runnable() {
			public void run() {
				m_socket.write("Disconnecting due to inactivity".getBytes());
				m_socket.closeAfterWrite();
			}
		}, LOGIN_TIMEOUT);

		// Send the request to log in.
		nioSocket.write("Please enter your name:".getBytes());
		//login/register request
		nioSocket.write("&LP".getBytes());
	}

	public String toString() {
		return !m_name.equals("") ? m_name + "@" + m_socket.getIp() : "anon@" + m_socket.getIp();
	}

	public void connectionBroken(NIOSocket nioSocket, Exception exception) {
		// Inform the other users if the user was logged in.
		if (!m_name.equals("")) {
			m_server.broadcast(this, m_name + " left the chat.");
		}
		// Remove the user.
		m_server.removeUser(this);
	}

	private void scheduleInactivityEvent() {
		// Cancel the last disconnect event, schedule another.
		if (m_disconnectEvent != null)
			m_disconnectEvent.cancel();
		m_disconnectEvent = m_server.getEventMachine().executeLater(new Runnable() {
			public void run() {
				m_socket.write("Disconnected due to inactivity.".getBytes());
				m_socket.closeAfterWrite();
			}
		}, INACTIVITY_TIMEOUT);
	}

	public void packetReceived(NIOSocket socket, byte[] packet) {
		
		// Create the string. For real life scenarios, you'd handle exceptions
		// here.
		String message = new String(packet).trim();
		// Ignore empty lines
		if (message.length() == 0)
			return;

		// Reset inactivity timer.
		scheduleInactivityEvent();

		// In this protocol, the first line entered is the name.

		if (m_name.equals("kk")) {
			// isInTheList = false;
			// for (User user : m_server.getM_users()) {
			// if (m_name.equals(user.m_name))
			// isInTheList = true;
			// socket.write(("This name already taken").getBytes());
			// return;
			// }
			// User joined the chat.
			m_name = message;
			System.out.println(this + " logged in.");
			if (m_color != null) {
				m_server.broadcast(this,
						"<span style=\"color:" + m_color + "\"><b>" + m_name + "</b></span> has joined the chat.");
				m_socket.write(("Welcome " + "<span style=\"color:" + m_color + "\"><b>" + m_name + "</b></span>"
						+ ". There are " + m_server.getM_users().size() + " user(s) currently logged in.").getBytes());

			} else {
				m_server.broadcast(this, "<b>" + m_name + "</b> has joined the chat.");
				m_socket.write(("Welcome <b>" + m_name + "</b>. There are " + m_server.getM_users().size()
						+ " user(s) currently logged in.").getBytes());
			}

			return;
		} else if (m_server.m_users.contains(m_name)) {
			m_socket.write("This nickname is busy".getBytes());
			return;
		}
		if (message.startsWith("&C")) {
			// color setting
			String temp_message = message.substring(2, 9);
			m_color = temp_message;
			message = message.substring(9, message.length());

		} else if (message.startsWith("&L")) {
			// login
			if (loginCheck(message.substring(2, message.lastIndexOf("&P")),
					message.substring(message.lastIndexOf("&P") + 2, message.length()))) {
				m_name = message.substring(2, message.lastIndexOf("&P"));
				System.out.println(this + " logged in.");
				m_server.broadcast(this, "<b>" + m_name + "</b> has joined the chat.");
				m_socket.write(("Welcome <b>" + m_name + "</b>. There are " + m_server.getM_users().size()
						+ " user(s) currently logged in.").getBytes());
				if (isAdmin) {
					socket.write("&A".getBytes());
				}
				
			} else {
				socket.write("Incorrect user name/password".getBytes());
				socket.write("&LP".getBytes());
			}
		}
		else if (message.startsWith("&R")) {
				// register
				byte i = 0;
				if (message.endsWith("&A")) {
					isAdmin = true;
					i = 2;
				}
				String tempLogin = message.substring(2, message.lastIndexOf("&P"));
				String tempPassword = message.substring(message.lastIndexOf("&P") + 2, message.length() - i);
				if (loginCheck(tempLogin, tempPassword)) {
					socket.write("Error! Check your creds.".getBytes());
					socket.write("&LP".getBytes());
				} else {
					m_userCreds = new Users();

					try {
						StringBuffer hexString = new StringBuffer();
						MessageDigest md5 = MessageDigest.getInstance("md5");
						md5.reset();
						md5.update((tempLogin+tempPassword).getBytes());
						byte[] theDigest = md5.digest();
						for (int k = 0; k < theDigest.length; k++) {
							hexString.append(Integer.toHexString((0xF0 & theDigest[k]) >> 4));
							hexString.append(Integer.toHexString(0x0F & theDigest[k]));
						}
						m_userCreds.setMd5(hexString.toString());
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
					m_server.users.add(m_userCreds);
					SerializationManager.serializeData(m_server.users, "Users", "ser", "");
					m_name = tempLogin;
					System.out.println(this + " registered.");
					m_server.broadcast(this, "<b>" + m_name + "</b> has joined the chat.");
					m_socket.write(("Welcome <b>" + m_name + "</b>. There are " + m_server.getM_users().size()
							+ " user(s) currently logged in.").getBytes());
				}

			}
		 else if (message.startsWith("&A")) {
			// admin control requests
		} else if (message.startsWith("&ULR")) {
			// user list request
			m_server.userListRequest(this);
		} else {
			m_server.broadcast(this,
					"<span style=\"color:" + m_color + "\"><b>" + m_name + "</b></span>" + ": " + message);
		}
	}

	public void packetSent(NIOSocket socket, Object tag) {
		// No need to handle this case.
	}

	public void sendBroadcast(byte[] bytesToSend) {
		// Only send broadcast to users logged in.
		if (!m_name.equals("")) {
			m_socket.write(bytesToSend);
		}

	}

	// deser data and md5 check + admin check
	private boolean loginCheck(String login, String password) {
		try {
			StringBuffer hexString = new StringBuffer();
			MessageDigest md5 = MessageDigest.getInstance("md5");
			md5.reset();
			md5.update((login+password).getBytes());
			byte[] theDigest = md5.digest();
			for (int i = 0; i < theDigest.length; i++) {
				hexString.append(Integer.toHexString((0xF0 & theDigest[i]) >> 4));
				hexString.append(Integer.toHexString(0x0F & theDigest[i]));
			}
			
			for (Users user : m_server.users) {
				if (user.getMd5().equals(hexString.toString())) {
					if (user.isAdmin()) isAdmin = true;
					return true;
				} 
			}

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return false;
	}
}
