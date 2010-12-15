package org.irssibot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import de.mud.terminal.VT320;
import org.irssibot.transport.Transport;

import static org.irssibot.util.LogHelper.DEBUG;

public class BaseTerminalView extends View {
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

	public BaseTerminalView(Context context, Transport transport) {
		super(context);

		this.transport = transport;
		this.context = context;

		this.buffer = new VT320View();

		this.transport.setDataHandler(dataHandler);
	}

	protected class VT320View extends VT320 {

		@Override
		public void debug(String notice) {
			DEBUG(notice);
		}

		@Override
		public void write(byte[] b) {

		}

		@Override
		public void write(int b) {

		}
	}
}
