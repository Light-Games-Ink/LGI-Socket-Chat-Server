/**
 * 
 */
package ru.lgi.main;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * @author LaughingMaan
 *
 */
class ConnectionHandler implements CompletionHandler<AsynchronousSocketChannel, Attachment> {

	@Override
	public void completed(AsynchronousSocketChannel client, Attachment attach) {
		try {
			SocketAddress clientAddr = client.getRemoteAddress();
			System.out.format("Accepted a connection from %s%n", clientAddr);
			attach.server.accept(attach, this);
			ReadWriteHandler rwHandler = new ReadWriteHandler();
			Attachment newAttach = new Attachment();
			newAttach.server = attach.server;
			newAttach.client = client;
			newAttach.buffer = ByteBuffer.allocate(2048);
			newAttach.isRead = true;
			newAttach.clientAddr = clientAddr;
			client.read(newAttach.buffer, newAttach, rwHandler);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void failed(Throwable exc, Attachment attachment) {
		System.out.println("Failed to accept a connection");
		exc.printStackTrace();
	}

}
