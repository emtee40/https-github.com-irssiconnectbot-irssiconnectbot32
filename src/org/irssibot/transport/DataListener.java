package org.irssibot.transport;

/**
* User: parkerkane
* Date: 14.12.2010
* Time: 04:50
*/
public interface DataListener {
	public void onData(char[] data, int length);
}
