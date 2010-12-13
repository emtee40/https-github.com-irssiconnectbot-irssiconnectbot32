package org.irssibot;

import org.irssibot.transport.Transport;

/**
 * User: treunanen
 * Date: 13.12.2010
 * Time: 18:22
 */
public class Relay implements Runnable {

	private Transport transport;
	
	public Relay(Transport transport) {
		
		this.transport = transport;

	}

	public void run() {

	}
}
