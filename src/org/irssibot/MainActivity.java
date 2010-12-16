package org.irssibot;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.VideoView;
import com.jcraft.jsch.JSchException;
import org.irssibot.transport.SSH;
import org.irssibot.transport.Transport;
import org.irssibot.util.LogHelper;

public class MainActivity extends Activity {

	private Transport transport;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		LogHelper.register(this);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		final Dialog dialog = new Dialog(this);

		dialog.setTitle("Enter server");
		dialog.setContentView(R.layout.connect);

		final EditText server = (EditText) dialog.findViewById(R.id.host);

		server.setText("");
		server.setOnKeyListener(new View.OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {

					boolean res = connectToServer(server.getText().toString());

					if (res) dialog.dismiss();

					return res;
				}

				return false;
			}
		});
		
		dialog.show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		transport.disconnect();
	}

	private boolean connectToServer(final String uri) {
		if (!uri.contains("@")) {
			return false;
		}

		String[] tmp = uri.split("@");
		String username = tmp[0];
		String host = tmp[1];

		transport = null;

		try {
			transport = new SSH(username, host, 22);
		} catch (JSchException e) {
			e.printStackTrace();
		}

		if (transport != null) {
			TerminalView tv = new TerminalView(this, transport);
			
			setContentView(tv);
		}

		return true;
	}

}
