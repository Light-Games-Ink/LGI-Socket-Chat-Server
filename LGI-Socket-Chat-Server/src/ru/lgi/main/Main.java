/**
 * 
 */
package ru.lgi.main;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import naga.*;

/**
 * @author LaughingMaan
 *
 */
public class Main {
	static private AsynchronousServerSocketChannel server;
	static String host;
	static int port;
	static ArrayList<NIOSocket> sockets = new ArrayList<NIOSocket>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			port = 5676;
			NIOService service = new NIOService();
			NIOServerSocket socket = service.openServerSocket(new InetSocketAddress("localhost",port), 10);
			socket.listen(new ServerSocketObserverAdapter(){
				@Override
				public void newConnection(NIOSocket nioSocket) {
						if(!sockets.contains(socket)){
					    sockets.add(nioSocket);
					    System.out.println(nioSocket.getIp()+":"+nioSocket.getPort());
						}
					nioSocket.listen(new SocketObserverAdapter(){
						@Override
						public void packetReceived(NIOSocket socket, byte[] packet) {
							//Check the packet, then made a broadcast
							Charset cs = Charset.forName("UTF-8");
							String msg = new String(packet,cs);
							System.out.println(msg);
							for (NIOSocket socket2 : sockets) {
								socket2.write(packet);
							}
							
						}
						@Override
						public void connectionBroken(NIOSocket nioSocket, Exception exception)
						{
							System.out.println(exception.getMessage());
							for (NIOSocket socket2 : sockets) {
								if(socket2.equals(nioSocket)){
									sockets.remove(socket2);
								}
							}
							nioSocket.close();
						}
						
					});
				}
				@Override
				public void acceptFailed(IOException exception){
					System.out.println(exception.getMessage());
				}
				@Override
				public void serverSocketDied(Exception e)
				{
					System.out.println(e.getMessage());
				}
			});
			socket.setConnectionAcceptor(ConnectionAcceptor.ALLOW);
			while(true){
				service.selectBlocking();
			}
			/*
			NIOService service = new NIOService();
			NIOServerSocketSSL serverSocket = service.openSSLServerSocket(null, new InetSocketAddress("localhost", 5678), 100);
			serverSocket.setConnectionAcceptor(ConnectionAcceptor.ALLOW);
			serverSocket.listen(null);
			host = "localhost";
			port = 5678;
			server = AsynchronousServerSocketChannel.open();
			InetSocketAddress sAddr = new InetSocketAddress(host, port);
			server.bind(sAddr);
			System.out.format("Server is listening at %s%n", sAddr);
			Attachment attach = new Attachment();
			attach.server = server;
			server.accept(attach, new ConnectionHandler());
			Thread.currentThread().join();
			*/
		} catch (IOException e) {
			e.printStackTrace();
		//} //catch (InterruptedException e) {
			//e.printStackTrace();
		}
	}

}
