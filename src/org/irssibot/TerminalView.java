package org.irssibot;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import org.irssibot.transport.PromptMessage;
import org.irssibot.transport.Transport;

import static org.irssibot.util.LogHelper.DEBUG;

/**
 * User: parkerkane
 * Date: 13.12.2010
 * Time: 9:54
 */
public class TerminalView extends BaseTerminalView {

	public TerminalView(Context context, Transport transport) {

		super(context, transport);

		transport.setPromptHandler(new PromptHandler());
		
		transport.connect();
	}

	private class PromptHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			final PromptMessage data = (PromptMessage) msg.obj;

			DEBUG("Got prompt message:", data.type);

			switch (data.type) {
				case Message:
					Toast.makeText(TerminalView.this.context, data.message, Toast.LENGTH_LONG).show();
					data.release();
					break;

				case Boolean:

					AlertDialog.Builder builder = new AlertDialog.Builder(TerminalView.this.context)
						.setCancelable(false)
						.setTitle("SSH")
						.setMessage(data.message)
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int which) {
								data.sendResponse(true);
							}
						})
						.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int which) {
								data.sendResponse(false);
							}
						});
					
					builder.create().show();
					
					break;

				case String:
					data.release();
					break;

				case Password:

					final Dialog dialog = new Dialog(TerminalView.this.context);
					
					dialog.setTitle(data.message);
					dialog.setContentView(R.layout.password);
					
					final EditText password = (EditText) dialog.findViewById(R.id.password);
					
					password.setText("");
					password.setOnKeyListener(new OnKeyListener() {

						public boolean onKey(View v, int keyCode, KeyEvent event) {
							
							if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
								data.sendResponse(password.getText().toString());
								
								dialog.dismiss();
								return true;
							}
							
							return false;
						}
					});
					
					dialog.show();
					break;

			}
		}
	}
}
