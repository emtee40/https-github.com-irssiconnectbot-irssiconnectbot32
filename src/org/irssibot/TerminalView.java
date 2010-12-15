package org.irssibot;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.widget.EditText;
import android.widget.Toast;
import de.mud.terminal.VDUBuffer;
import de.mud.terminal.VDUDisplay;
import org.irssibot.transport.PromptMessage;
import org.irssibot.transport.Transport;

import static org.irssibot.util.LogHelper.DEBUG;

/**
 * User: parkerkane
 * Date: 13.12.2010
 * Time: 9:54
 */
public class TerminalView extends BaseTerminalView implements VDUDisplay {

	private Paint defaultPaint;
	private int   charWidth;
	private int   charHeight;

	KeyCharacterMap keymap = KeyCharacterMap.load(KeyCharacterMap.BUILT_IN_KEYBOARD);
	
	public TerminalView(Context context, final Transport transport) {

		super(context, transport);

		setLayoutParams(
			new WindowManager.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

		buffer.setDisplay(this);

		transport.setPromptHandler(new PromptHandler());

		transport.connect();

		defaultPaint = new Paint();

		defaultPaint.setAntiAlias(true);
		defaultPaint.setTypeface(Typeface.MONOSPACE);
		defaultPaint.setFakeBoldText(true);

		setFontSize(10);

		setFocusable(true);
		setFocusableInTouchMode(true);

		setOnKeyListener(new OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				int key = keymap.get(keyCode, event.getMetaState());
				
				DEBUG("Got key data:", keyCode, event.getCharacters(), key);
				
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
//					buffer.putString(new String(Character.toChars(key)));
					
//					transport.write(key);
					
					transport.write(new String(Character.toChars(key)).getBytes());
				}
				
				return false;
			}
		});
		
		requestFocus();
	}

	@Override
	protected void onDraw(Canvas canvas) {

		DEBUG("Draw screen.");

		super.onDraw(canvas);

		canvas.drawARGB(255, 0, 0, 0);

		synchronized (buffer) {

			for (int y = 0; y < buffer.height; y++) {

				if (!buffer.update[y + 1]) {
					continue;
				}

				for (int x = 0; x < buffer.width; x++) {

//					defaultPaint.setColor(0xFF000000);
//
//					canvas.clipRect(
//						x * charWidth, y * charHeight,
//						(x + 1) * charWidth, (y + 1) * charHeight
//
//					);

					defaultPaint.setColor(0xFFFFFFFF);

					canvas.drawText(
						buffer.charArray[buffer.windowBase + y],
						x,
						1,
						x * charWidth,
						y * charHeight,
						defaultPaint);

//					canvas.restore();

				}
			}
		}

	}

	public void setFontSize(float size) {
		defaultPaint.setTextSize(size);

		Paint.FontMetrics fm = defaultPaint.getFontMetrics();

		float[] widths = new float[1];
		defaultPaint.getTextWidths("X", widths);

		charWidth = (int) Math.ceil(widths[0]);
		charHeight = (int) Math.ceil(fm.descent - fm.top);

		invalidate();
	}

	public VDUBuffer getVDUBuffer() {
		return null;
	}

	public void redraw() {
		invalidate();
	}

	public void updateScrollBar() {
	}

	public void setVDUBuffer(VDUBuffer buffer) {
	}

	public void setColor(int index, int red, int green, int blue) {
	}

	public void resetColors() {
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
