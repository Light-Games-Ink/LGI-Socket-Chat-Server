/**
 * 
 */
package ru.lgi.main;

import java.io.IOException;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;

/**
 * @author LaughingMaan
 *
 */
class ReadWriteHandler implements CompletionHandler<Integer, Attachment> {

	@Override
	public void completed(Integer result, Attachment attach) {
		if (result == -1) {
			try {
				attach.client.close();
				System.out.format("Stopped listening to the client %s%n", attach.clientAddr);

			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}

		if (attach.isRead) {
			attach.buffer.flip();
			int limits = attach.buffer.limit();
			byte[] bytes = new byte[limits];
			attach.buffer.get(bytes, 0, limits);
			Charset cs = Charset.forName("UTF-8");
			String msg = new String(bytes,cs);
			System.out.format("Client at %s says: %s%n", attach.clientAddr, msg);
			attach.isRead = false;
			attach.buffer.rewind();
		} else {
			attach.client.write(attach.buffer, attach, this);
			attach.isRead = true;
			attach.buffer.clear();
			attach.client.read(attach.buffer, attach, this);
		}
	}

	@Override
	public void failed(Throwable exc, Attachment attach) {
		exc.printStackTrace();
	}

}
