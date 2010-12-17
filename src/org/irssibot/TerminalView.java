package org.irssibot;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
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

	private boolean isSoftKeyVisible = false;
	private boolean checkSoftKey     = false;

	Handler  softKeyCheckHandler = new Handler();
	Runnable softKeyCheckRunner  = new Runnable() {

		public void run() {

			Handler handle = new Handler();

			ResultReceiver rr = new ResultReceiver(handle) {

				@Override
				protected void onReceiveResult(int resultCode, Bundle resultData) {
					if (resultCode == InputMethodManager.RESULT_SHOWN) {
						checkSoftKey = false;
						inputManager.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
					}

					super.onReceiveResult(resultCode, resultData);

					boolean oldVis = isSoftKeyVisible;

					isSoftKeyVisible = resultCode == InputMethodManager.RESULT_UNCHANGED_SHOWN;

					if (oldVis != isSoftKeyVisible) {
						resizeScreen();
					}
				}
			};

			if (checkSoftKey) {
				inputManager.showSoftInput(TerminalView.this, InputMethodManager.SHOW_IMPLICIT, rr);
			}

			softKeyCheckHandler.postDelayed(this, 1000);
		}
	};

	private void startTimer() {

		this.halt();
		softKeyCheckHandler.postDelayed(softKeyCheckRunner, 1000);
	}

	public void resume() {

		this.startTimer();
	}

	public void halt() {

		softKeyCheckHandler.removeCallbacks(softKeyCheckRunner);
	}

	public TerminalView(Context context, final Transport transport) {

		super(context, transport);

		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp.height = WindowManager.LayoutParams.MATCH_PARENT;
		setLayoutParams(lp);

		transport.setPromptHandler(new PromptHandler());

		transport.connect();

		setFocusable(true);
		setFocusableInTouchMode(true);

		setOnKeyListener(new TerminalKeyListener(this, transport, buffer));

		requestFocus();

		setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				inputManager.showSoftInput(TerminalView.this, InputMethodManager.SHOW_FORCED);
				checkSoftKey = true;
				isSoftKeyVisible = true;
				resizeScreen();
			}
		});
	}

	private void resizeScreen() {
		DEBUG("Is soft key visible?", isSoftKeyVisible);

		this.onSizeChanged(getWidth(), getHeight(), getWidth(), getHeight());
	}

	@Override
	protected void onSizeChanged(int width, int height, int oldw, int oldh) {

		// TODO Needs user configured height for portrait and landscape
		
		if (isSoftKeyVisible) {
			if (MainActivity.isPortrait) {
				height = 27 * charHeight;
			} else {
				height = 12 * charHeight;
			}
		}

		super.onSizeChanged(width, height, oldw, oldh);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		checkSoftKey = true;

		startTimer();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		halt();
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

					// TODO: Need to be done!

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
