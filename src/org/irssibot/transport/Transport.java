package org.irssibot.transport;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: parkerkane
 * Date: 13.12.2010
 * Time: 18:14
 */
public abstract class Transport {

	protected final AtomicReference<DataListener>        dataListener        = new AtomicReference<DataListener>();
	protected final AtomicReference<InteractionListener> interactionListener = new AtomicReference<InteractionListener>();

	private final Relay  relay       = new Relay(this);
	private       Thread relayThread = null;

	public abstract int read(byte[] buffer, int offset, int length);

	public abstract void write(byte[] buffer);

	public abstract void write(int c);

	public abstract void connect();

	public abstract void disconnect();

	public void setDataListener(DataListener listener) {
		synchronized (dataListener) {
			dataListener.set(listener);
		}
	}

	public void setInteractionListener(InteractionListener listener) {
		synchronized (interactionListener) {
			interactionListener.set(listener);
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

	protected void showMessage(String message) {
		synchronized (interactionListener) {
			if (interactionListener.get() != null) {

				interactionListener.get().onMessage(message);
			}
		}
	}

	public boolean inputBoolean(String message) {
		boolean val = false;

		synchronized (interactionListener) {
			if (interactionListener.get() != null) {
				val = interactionListener.get().onBoolean(message);
			}
		}
		return val;
	}

	public String inputPassword(String message) {
		String val = null;

		synchronized (interactionListener) {
			if (interactionListener.get() != null) {
				val = interactionListener.get().onPassword(message);
			}
		}
		return val;
	}

	public String inputString(String message) {
		String val = null;

		synchronized (interactionListener) {
			if (interactionListener.get() != null) {
				val = interactionListener.get().onInput(message);
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

						synchronized (transport.dataListener) {
							if (transport.dataListener.get() != null) {
								transport.dataListener.get().onData(charArray, offset);
							}
						}

					}
				} catch (Exception e) {
					e.printStackTrace();

					break;
				}

			}

		}
	}
}
