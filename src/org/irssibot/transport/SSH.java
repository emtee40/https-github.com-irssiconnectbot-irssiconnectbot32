package org.irssibot.transport;

import com.jcraft.jsch.*;
import org.irssibot.util.LogHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.irssibot.util.LogHelper.DEBUG;
import static org.irssibot.util.LogHelper.ERROR;
import static org.irssibot.util.LogHelper.INFO;

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
				
				DEBUG(message);

				try {
					password = SSH.this.promptPassword(message);
				} catch (InterruptedException e) {
					ERROR("Exception:", e.toString());
					e.printStackTrace();
					
					password = null;
				}

				return password != null;
			}

			public boolean promptPassphrase(String message) {

				DEBUG(message);
				
				try {
					passphrase = SSH.this.promptPassword(message);
				} catch (InterruptedException e) {
					ERROR("Exception:", e.toString());
					e.printStackTrace();
					passphrase = null;
				}

				return passphrase != null;
			}

			public boolean promptYesNo(String message) {

				DEBUG(message);

				try {
					return SSH.this.promptBoolean(message);
				} catch (InterruptedException e) {
					ERROR("Exception:", e.toString());
					e.printStackTrace();
				}
				
				return false;
			}

			public void showMessage(String message) {

				DEBUG(message);

				try {
					SSH.this.showMessage(message);
				} catch (InterruptedException e) {
					ERROR("Exception:", e.toString());
					e.printStackTrace();
				}
			}
		});
	}

	public boolean isConnected() {
		return connected;
	}

	@Override
	public int read(byte[] buffer, int offset, int length) {

		DEBUG("Reading", buffer.length, offset, length);
		
		int ret = 0;
		try {
			ret = inputStream.read(buffer, offset, length);
		} catch (IOException e) {
			ERROR("Exception:", e.toString());
			e.printStackTrace();

			try {
				showMessage("Error while reading stream.");
				ERROR("Error while reading stream.");
			} catch (InterruptedException e1) {
				ERROR("Exception:", e.toString());
				e1.printStackTrace();
			}

			this.disconnect();
		}

		return ret;
	}

	@Override
	public void write(byte[] buffer) {

		try {
			outputStream.write(buffer);
		} catch (IOException e) {
			ERROR("Exception:", e.toString());
			e.printStackTrace();

			try {
				showMessage("Error while writing to stream.");
			} catch (InterruptedException e1) {
				ERROR("Exception:", e.toString());
				e1.printStackTrace();
			}

			disconnect();
		}
	}

	@Override
	public void write(int c) {

	}

	@Override
	public void connect() {
		new Thread(new ConnectionRunnable()).start();
	}

	private class ConnectionRunnable implements Runnable {
		public void run() {
			INFO("Connecting to server.");
			
			try {
				session.connect();
				channel = session.openChannel("shell");

				inputStream = channel.getInputStream();
				outputStream = channel.getOutputStream();
				
				channel.connect();

			} catch (JSchException e) {
				ERROR("Exception:", e.toString());
				e.printStackTrace();
				try {
					showMessage("Error while connecting to server.");
					ERROR("Error while connecting to server:", e.toString());
				} catch (InterruptedException e1) {
					ERROR("Exception:", e.toString());
					e1.printStackTrace();
				}

				disconnect();
			} catch (IOException e) {
				ERROR("Exception:", e.toString());
				e.printStackTrace();
				try {
					showMessage("Error while getting streams.");
					ERROR("Error while getting streams.");
				} catch (InterruptedException e1) {
					ERROR("Exception:", e.toString());
					e1.printStackTrace();
				}

				disconnect();
			} finally {
				
				DEBUG("Connected to server");
				
				connected = true;

				startRelay();
			}
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
