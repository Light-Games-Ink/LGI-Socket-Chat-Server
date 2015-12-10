/**
 * 
 */
package ru.lgi.main;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;

/**
 * @author LaughingMaan
 *
 */
public class Main {
	static private AsynchronousServerSocketChannel server;
	static String host;
	static int port;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			server = AsynchronousServerSocketChannel.open();
			InetSocketAddress sAddr = new InetSocketAddress(host, port);
			server.bind(sAddr);
			System.out.format("Server is listening at %s%n", sAddr);
			Attachment attach = new Attachment();
			attach.server = server;
			server.accept(attach, new ConnectionHandler());
			Thread.currentThread().join();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
