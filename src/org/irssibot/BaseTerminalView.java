package org.irssibot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyCharacterMap;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import de.mud.terminal.VDUBuffer;
import de.mud.terminal.VDUDisplay;
import de.mud.terminal.VT320;
import org.irssibot.transport.Transport;
import org.irssibot.util.Colors;

import static org.irssibot.util.LogHelper.DEBUG;

public class BaseTerminalView extends View implements VDUDisplay {
	final protected VT320     buffer;
	final protected Transport transport;
	final protected Context   context;

	final private Handler dataHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			String data = (String) msg.obj;

			DEBUG("Got data:", data);

			buffer.putString(data);

			invalidate();
		}
	};
	protected Paint  defaultPaint;
	protected int    charWidth;
	protected int    charHeight;
	protected Canvas terminalCanvas;
	private   Bitmap terminalBitmap;
	private boolean fullRedraw = false;
	KeyCharacterMap keymap = KeyCharacterMap.load(KeyCharacterMap.BUILT_IN_KEYBOARD);
	private   int                charTop;
	protected InputMethodManager inputManager;

	public BaseTerminalView(Context context, Transport transport) {
		super(context);

		this.transport = transport;
		this.context = context;

		this.buffer = new VT320View();

		this.transport.setDataHandler(dataHandler);

		buffer.showCursor(true);
		buffer.setDisplay(this);

		defaultPaint = new Paint();
		terminalCanvas = new Canvas();
		inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

		defaultPaint.setAntiAlias(true);
		defaultPaint.setTypeface(Typeface.MONOSPACE);
		defaultPaint.setFakeBoldText(true);

		setFontSize(10);
	}

	@Override
	protected void onSizeChanged(int width, int height, int oldw, int oldh) {
		super.onSizeChanged(width, height, oldw, oldh);

		DEBUG("Screen size changed:", width, height, oldw, oldh);

		if (terminalBitmap != null) {
			terminalBitmap.recycle();
		}

		terminalBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

		terminalCanvas = new Canvas(terminalBitmap);

		int terminalWidth = (int) Math.ceil(width / charWidth);
		int terminalHeight = (int) Math.ceil(height / charHeight);

		transport.resize(terminalWidth, terminalHeight, width, height);

		buffer.setScreenSize(terminalWidth, terminalHeight, true);

		redraw();
	}

	@Override
	protected void onDraw(Canvas canvas) {

		DEBUG("Draw screen.");

//		super.onDraw(terminalCanvas);

		synchronized (buffer) {

			boolean entireDirty = buffer.update[0] || fullRedraw;

			int bg, fg, fgColor, bgColor;

			for (int y = 0; y < buffer.height; y++) {

				if (!buffer.update[y + 1] && !entireDirty) {
					continue;
				}

				buffer.update[y + 1] = false;

				for (int x = 0; x < buffer.width; x++) {

					int addr = 0;
					int curAttr = buffer.charAttributes[buffer.windowBase + y][x];

					while (x + addr < buffer.width &&
						   buffer.charAttributes[buffer.windowBase + y][x + addr] == curAttr) {
						addr++;
					}

					if ((curAttr & VDUBuffer.COLOR_FG) != 0) {
						fgColor = ((curAttr & VDUBuffer.COLOR_FG) >> VDUBuffer.COLOR_FG_SHIFT) - 1;
					} else {
						fgColor = 15;

					}

					if (fgColor < 8 && (curAttr & VDUBuffer.BOLD) != 0) {
						fg = Colors.defaults[fgColor + 8];
					} else {
						fg = Colors.defaults[fgColor];
					}

					// check if background color attribute is set
					if ((curAttr & VDUBuffer.COLOR_BG) != 0) {
						bg = Colors.defaults[((curAttr & VDUBuffer.COLOR_BG) >> VDUBuffer.COLOR_BG_SHIFT) - 1];
					} else {
						bg = Colors.defaults[0];
					}

					defaultPaint.setColor(bg);

					terminalCanvas.save(Canvas.CLIP_SAVE_FLAG);

					terminalCanvas.clipRect(
						x * charWidth, y * charHeight,
						(x + addr) * charWidth, (y + 1) * charHeight

					);

					terminalCanvas.drawPaint(defaultPaint);

					defaultPaint.setColor(fg);

					terminalCanvas.drawText(
						buffer.charArray[buffer.windowBase + y],
						x,
						addr,
						x * charWidth,
						(y * charHeight) - charTop,
						defaultPaint);

					terminalCanvas.restore();

					x += addr - 1;
				}

			}

			buffer.update[0] = false;

			int cursorColumn = buffer.getCursorColumn();
			int cursorRow = buffer.getCursorRow();

			defaultPaint.setColor(0x3FFFFFFF);

			terminalCanvas.save(Canvas.CLIP_SAVE_FLAG);

			terminalCanvas.clipRect(
				cursorColumn * charWidth, cursorRow * charHeight,
				(cursorColumn + 1) * charWidth, (cursorRow + 1) * charHeight
			);

			terminalCanvas.drawPaint(defaultPaint);

			terminalCanvas.restore();

		}

		fullRedraw = false;

		canvas.drawBitmap(terminalBitmap, 0, 0, null);
	}

	public void setFontSize(float size) {
		defaultPaint.setTextSize(size);

		Paint.FontMetrics fm = defaultPaint.getFontMetrics();

		float[] widths = new float[1];
		defaultPaint.getTextWidths("X", widths);

		charWidth = (int) Math.ceil(widths[0]);
		charHeight = (int) Math.ceil(fm.descent - fm.top);

		charTop = (int) Math.ceil(fm.top);

		redraw();
	}

	public void redraw() {
		invalidate();
		fullRedraw = true;
	}

	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
		outAttrs.imeOptions = EditorInfo.IME_ACTION_SEND;
//		outAttrs.inputType = EditorInfo.TYPE_CLASS_TEXT;

		InputConnection ic = new BaseInputConnection(this, false) {

			@Override
			public int getCursorCapsMode(int reqModes) {

				return TextUtils.getCapsMode(
					new String(buffer.charArray[buffer.windowBase + buffer.getCursorRow()]),
					buffer.getCursorColumn(),
					reqModes);
			}

			@Override
			public boolean reportFullscreenMode(boolean enabled) {
				DEBUG("Fullscreen?", enabled);

				return super.reportFullscreenMode(enabled);

			}
		};

		return null;
	}

	public VDUBuffer getVDUBuffer() {
		return null;
	}

	public void updateScrollBar() {
	}

	public void setVDUBuffer(VDUBuffer buffer) {
	}

	public void setColor(int index, int red, int green, int blue) {
	}

	public void resetColors() {
	}

	protected class VT320View extends VT320 {

		@Override
		public void debug(String notice) {
			DEBUG(notice);
		}

		@Override
		public void write(byte[] b) {
			transport.write(b);
		}

		@Override
		public void write(int b) {
			transport.write(b);
		}
	}
}
