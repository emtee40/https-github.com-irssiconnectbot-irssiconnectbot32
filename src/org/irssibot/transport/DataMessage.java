package org.irssibot.transport;

public class DataMessage {
	public char[] array;
	public int length;

	public DataMessage(char[] array, int length) {
		this.array = array;
		this.length = length;
	}
}
