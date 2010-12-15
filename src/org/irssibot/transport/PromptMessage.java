package org.irssibot.transport;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class PromptMessage {
	public enum Type {Message, Boolean, String, Password}

	final public Type   type;
	final public String message;

	final private Semaphore lock;

	public final AtomicReference<String> responseString = new AtomicReference<String>(null);
	public final AtomicBoolean responseBoolean = new AtomicBoolean(false);

	public PromptMessage(Type type, Semaphore lock, String message) {
		this.type = type;
		this.lock = lock;
		this.message = message;
	}

	public void sendResponse(String response) {
		sendResponse(response, true);
	}

	public void sendResponse(String response, boolean release) {
		responseString.set(response);

		if (release) {
			release();
		}
	}

	public void sendResponse(boolean response) {
		sendResponse(response, true);
	}

	public void sendResponse(boolean response, boolean release) {
		responseBoolean.set(response);

		if (release) {
			release();
		}
	}

	public void release() {
		lock.release();
	}
}
