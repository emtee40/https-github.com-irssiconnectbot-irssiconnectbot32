package org.irssibot.transport;

import com.jcraft.jsch.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * User: parkerkane
 * Date: 13.12.2010
 * Time: 17:14
 */
public class SSH extends Transport {

	private JSch    ssh;
	private Session session;
	private Channel channel;
	private boolean connected = false;

	private InputStream  inputStream;
	private OutputStream outputStream;

	public SSH(String username, String host, int port)
		throws JSchException {

		ssh = new JSch();

		session = ssh.getSession(username, host, port);
		session.setUserInfo(new UserInfo() {

			private String password;
			private String passphrase;

			public String getPassphrase() {
				return passphrase;
			}

			public String getPassword() {
				return password;
			}

			public boolean promptPassword(String message) {

				password = SSH.this.inputPassword(message);

				return password != null;
			}

			public boolean promptPassphrase(String message) {

				passphrase = SSH.this.inputPassword(message);

				return passphrase != null;
			}

			public boolean promptYesNo(String message) {

				return SSH.this.inputBoolean(message);
			}

			public void showMessage(String message) {
				SSH.this.showMessage(message);
			}
		});
	}

	public boolean isConnected() {
		return connected;
	}

	@Override
	public int read(byte[] buffer, int offset, int length) {

		int ret = 0;
		try {
			ret = inputStream.read(buffer, offset, length);
		} catch (IOException e) {
			e.printStackTrace();

			showMessage("Error while reading stream.");

			this.disconnect();
		}

		return ret;
	}

	@Override
	public void write(byte[] buffer) {

		try {
			outputStream.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();

			showMessage("Error while writing to stream.");

			disconnect();
		}
	}

	@Override
	public void write(int c) {

	}

	@Override
	public void connect() {

		try {
			session.connect();
			channel = session.openChannel("shell");

			inputStream = channel.getInputStream();
			outputStream = channel.getOutputStream();

		} catch (JSchException e) {
			e.printStackTrace();
			showMessage("Error while connecting to server.");
			
			disconnect();
		} catch (IOException e) {
			e.printStackTrace();
			showMessage("Error while getting streams.");
			
			disconnect();
		} finally {
			connected = true;

			startRelay();
		}

	}

	@Override
	public void disconnect() {

		connected = false;

		stopRelay();

		if (channel != null) {
			channel.disconnect();
		}

		if (session != null) {
			session.disconnect();
		}
	}
}
