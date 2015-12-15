/**
 * 
 */
package ru.lgi.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import naga.ConnectionAcceptor;
import naga.NIOServerSocket;
import naga.NIOSocket;
import naga.ServerSocketObserver;
import naga.eventmachine.EventMachine;

/**
 * @author LaughingMaan
 *
 */
public class Main implements ServerSocketObserver {
	static String host;
	static int port;
	static ArrayList<NIOSocket> sockets = new ArrayList<NIOSocket>();
	static ArrayList<Settings> settings = new ArrayList<Settings>();
	static ArrayList<Users> usersLaP = new ArrayList<Users>();
	private final EventMachine m_eventMachine;
	private final List<User> m_users;

	Main(EventMachine machine) {
		m_eventMachine = machine;
		m_users = new ArrayList<User>();
	}

	/**
	 * @param args
	 */
	//@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		try {
			//settings = (ArrayList<Settings>) SerializationManager.deSerializeData("Settings", "ser", "");
			//usersLaP = (ArrayList<Users>) SerializationManager.deSerializeData("Users", "ser", "");
			port = Integer.parseInt(args[0]);
			EventMachine machine = new EventMachine();
			NIOServerSocket socket = machine.getNIOService().openServerSocket(port);
			socket.listen(new Main(machine));
			socket.setConnectionAcceptor(ConnectionAcceptor.ALLOW);
			machine.start();
				
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public EventMachine getEventMachine() {
		return m_eventMachine;
	}

	@Override
	public void acceptFailed(IOException exception) {
		System.out.println("Failed to accept connection: " + exception);

	}

	@Override
	public void serverSocketDied(Exception exception) {
		System.out.println("Server socket died.");
		System.exit(-1);

	}

	@Override
	public void newConnection(NIOSocket nioSocket) {
		System.out.println("New user connected from " + nioSocket.getIp() + ".");
		getM_users().add(new User(this, nioSocket));
		

	}

	void removeUser(User user) {
		System.out.println("Removing user " + user + ".");
		getM_users().remove(user);
	}

	public void broadcast(User sender, String string) {
		// We convert the packet, then send it to all users except the sender.
		byte[] bytesToSend = string.getBytes();
		for (User user : getM_users()) {
			user.sendBroadcast(bytesToSend);
		}
	}

	public List<User> getM_users() {
		return m_users;
	}
}
