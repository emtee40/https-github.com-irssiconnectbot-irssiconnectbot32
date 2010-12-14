package org.irssibot.transport;

/**
* User: parkerkane
* Date: 14.12.2010
* Time: 04:50
*/
public interface InteractionListener {
	public String onInput(String message);

	public String onPassword(String message);

	public boolean onBoolean(String message);

	public void onMessage(String message);
}
