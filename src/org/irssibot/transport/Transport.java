package org.irssibot.transport;

import android.os.Handler;
import android.os.Message;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import static org.irssibot.util.LogHelper.DEBUG;
import static org.irssibot.util.LogHelper.ERROR;

/**
 * User: parkerkane
 * Date: 13.12.2010
 * Time: 18:14
 */
public abstract class Transport {

	protected final AtomicReference<Handler> dataHandler   = new AtomicReference<Handler>();
	protected final AtomicReference<Handler> promptHandler = new AtomicReference<Handler>();

	private final        Semaphore responseLock   = new Semaphore(0);
	private final static Semaphore responseUILock = new Semaphore(1);

	private final Relay  relay       = new Relay(this);
	private       Thread relayThread = null;

	public abstract int read(byte[] buffer, int offset, int length);

	public abstract void write(byte[] buffer);

	public abstract void write(int c);

	public abstract void connect();

	public abstract void disconnect();

	public void setDataHandler(Handler handler) {
		synchronized (dataHandler) {
			dataHandler.set(handler);
		}
	}

	public void setPromptHandler(Handler handler) {
		synchronized (promptHandler) {
			promptHandler.set(handler);
		}
	}

	public Charset getCharset() {
		return relay.getCharset();
	}

	public void setCharset(String encoding) {
		relay.setCharset(encoding);
	}

	public void setCharset(Charset charset) {
		relay.setCharset(charset);
	}

	public void startRelay() {
		if (relayThread != null) {
			return;
		}

		relayThread = new Thread(relay);
		relayThread.setDaemon(true);
		relayThread.setName("Relay");
		relayThread.start();
	}

	public void stopRelay() {
		if (relayThread != null) {
			relayThread.interrupt();
		}

		relayThread = null;
	}

	protected void showMessage(String message)
		throws InterruptedException {

		synchronized (promptHandler) {
			if (promptHandler.get() == null) {
				return;
			}

			responseUILock.acquire();

			try {

				PromptMessage msg = new PromptMessage(PromptMessage.Type.Message, responseLock, message);

				Message.obtain(promptHandler.get(), -1, msg).sendToTarget();

				responseLock.acquire();
			} finally {
				responseUILock.release();
			}
		}

	}

	public boolean promptBoolean(String message)
		throws InterruptedException {

		boolean val = false;

		synchronized (promptHandler) {
			if (promptHandler.get() == null) {
				return val;
			}

			responseUILock.acquire();

			try {

				PromptMessage msg = new PromptMessage(PromptMessage.Type.Boolean, responseLock, message);

				DEBUG("Sending boolean message");
				Message.obtain(promptHandler.get(), -1, msg).sendToTarget();

				DEBUG("Waiting for response.");
				responseLock.acquire();

				val = msg.responseBoolean.get();

				DEBUG("Got response:", val);
			} finally {
				responseUILock.release();
			}
		}

		return val;
	}

	public String promptPassword(String message)
		throws InterruptedException {

		String val = null;

		synchronized (promptHandler) {
			if (promptHandler.get() == null) {
				return val;
			}

			responseUILock.acquire();

			try {
				PromptMessage msg = new PromptMessage(PromptMessage.Type.Password, responseLock, message);

				DEBUG("Sending password message");
				Message.obtain(promptHandler.get(), -1, msg).sendToTarget();

				DEBUG("Waiting for response");
				responseLock.acquire();

				val = msg.responseString.get();

				DEBUG("Got response:", "xxxxxxxxxx");
			} finally {
				responseUILock.release();
			}
		}

		return val;
	}

	public String promptString(String message)
		throws InterruptedException {

		String val = null;

		synchronized (promptHandler) {
			if (promptHandler.get() == null) {
				return val;
			}

			responseUILock.acquire();

			try {
				PromptMessage msg = new PromptMessage(PromptMessage.Type.Message, responseLock, message);

				Message.obtain(promptHandler.get(), -1, msg).sendToTarget();

				responseLock.acquire();

				val = msg.responseString.get();
			} finally {
				responseUILock.release();
			}
		}
		return val;
	}

	private static class Relay implements Runnable {
		private static final int BUFFER_SIZE = 4096;

		private Transport      transport;
		private Charset        charset;
		private CharsetDecoder decoder;

		public Relay(Transport transport) {
			this.transport = transport;

			setCharset("utf-8");
		}

		public Charset getCharset() {
			return charset;
		}

		public void setCharset(String encoding) {
			setCharset(Charset.forName(encoding));
		}

		public void setCharset(Charset charset) {
			this.charset = charset;

			CharsetDecoder newDecoder;

			newDecoder = charset.newDecoder();
			newDecoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
			newDecoder.onMalformedInput(CodingErrorAction.REPLACE);

			decoder = newDecoder;
		}

		public void run() {
			ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
			CharBuffer charBuffer = CharBuffer.allocate(BUFFER_SIZE);

			byte[] byteArray = byteBuffer.array();
			char[] charArray = charBuffer.array();

			byteBuffer.limit(0);

			while (true) {

				try {
					int bytesToRead = byteBuffer.capacity() - byteBuffer.limit();
					int offset = byteBuffer.arrayOffset() + byteBuffer.limit();
					int bytesRead = transport.read(byteArray, offset, bytesToRead);

					if (bytesRead > 0) {
						byteBuffer.limit(byteBuffer.limit() + bytesRead);

						CoderResult result = decoder.decode(byteBuffer, charBuffer, false);

						if (result.isUnderflow() && byteBuffer.limit() == byteBuffer.capacity()) {
							byteBuffer.compact();
							byteBuffer.limit(byteBuffer.position());
							byteBuffer.position(0);
						}

						offset = charBuffer.position();

						DEBUG("Got data. Sending message:", offset, "bytes.");

						synchronized (transport.dataHandler) {
							if (transport.dataHandler.get() != null) {
								Message.obtain(
									transport.dataHandler.get(),
									-1,
									new String(charArray, 0, offset)
								).sendToTarget();
							}
						}

						charBuffer.clear();

					} else {
						Thread.sleep(100);
					}
				} catch (InternalError e) {
					return;
				} catch (Exception e) {
					ERROR("Exception:", e.toString());
					e.printStackTrace();

					break;
				}

			}

		}
	}

}
