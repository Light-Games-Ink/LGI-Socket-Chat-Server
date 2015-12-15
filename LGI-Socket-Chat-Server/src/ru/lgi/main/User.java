package ru.lgi.main;

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
	private String m_name,m_color;
	private DelayedEvent m_disconnectEvent;
	private Users m_userCreds;
	
	public String getM_name() {
		return m_name;
	}


	User(Main server, NIOSocket socket) {
		m_server = server;
		m_socket = socket;
		m_socket.setPacketReader(new AsciiLinePacketReader());
		m_socket.setPacketWriter(new AsciiLinePacketWriter());
		m_socket.listen(this);
		m_name = null;
		m_color = null;
		m_userCreds = new Users();
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
	}

	public String toString() {
		return m_name != null ? m_name + "@" + m_socket.getIp() : "anon@" + m_socket.getIp();
	}

	public void connectionBroken(NIOSocket nioSocket, Exception exception) {
		// Inform the other users if the user was logged in.
		if (m_name != null) {
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
		if (message.startsWith("&C")) {
			//color setting
			String temp_message = message.substring(2, 9);
			m_color = temp_message;
			message = message.substring(9, message.length());
		}
		else if(message.startsWith("&L")){
			//login 
			m_userCreds.setLogin(message.substring(3, message.indexOf("&P") + 2));
			m_userCreds.setPassword(message.substring(message.indexOf("&P") + 2));
			System.out.println(m_userCreds.getLogin() + "/n" + m_userCreds.getPassword());
		}
		else if(message.startsWith("&A")){
			//admin control
		}
		else if(message.startsWith("&ULR")){
			//user list request
			m_server.userListRequest(this);
		}
		// Reset inactivity timer.
		scheduleInactivityEvent();

		// In this protocol, the first line entered is the name.
		if (m_name == null) {

			// User joined the chat.
			m_name = message;
			System.out.println(this + " logged in.");
			if (m_color != null) {
				m_server.broadcast(this,
						"<span style=\"color:" + m_color + "\"><b>" + m_name + "</b></span> has joined the chat.");
				m_socket.write(("Welcome " + "<span style=\"color:" + m_color + "\"><b>" + m_name + "</b></span>"
						+ ". There are " + m_server.getM_users().size() + " user(s) currently logged in.").getBytes());

			} else {
				m_server.broadcast(this, "<b>"+ m_name + "</b> has joined the chat.");
				m_socket.write(("Welcome <b>" + m_name + "</b>. There are " + m_server.getM_users().size()
						+ " user(s) currently logged in.").getBytes());
			}
			return;
		}
		if (m_color != null) {
			m_server.broadcast(this, "<span style=\"color:" + m_color + "\"><b>" + m_name + "</b></span>" + ": " + message);
		} else {
			m_server.broadcast(this, "<b>" + m_name + "</b>: " + message);
		}
	}

	public void packetSent(NIOSocket socket, Object tag) {
		// No need to handle this case.
	}

	public void sendBroadcast(byte[] bytesToSend) {
		// Only send broadcast to users logged in.
		if (m_name != null) {
			m_socket.write(bytesToSend);
		}

	}
}
