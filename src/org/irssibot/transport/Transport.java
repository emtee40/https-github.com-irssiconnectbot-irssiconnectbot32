package org.irssibot.transport;

/**
 * User: treunanen
 * Date: 13.12.2010
 * Time: 18:14
 */
public abstract class Transport {

	public Transport() {

	}

	public abstract void read(byte[] buffer, int offset, int length);

	public abstract void write(byte[] buffer);
	
	public abstract void write(int c);
	
	public abstract void connect();
	
	public abstract void disconnect();
}
