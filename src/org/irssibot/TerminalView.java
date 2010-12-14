package org.irssibot;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import de.mud.terminal.VT320;
import org.irssibot.transport.Transport;

/**
 * User: parkerkane
 * Date: 13.12.2010
 * Time: 9:54
 */
public class TerminalView extends View {

	private VT320 buffer;
	private Transport transport;

	public TerminalView(Context context, Transport transport) {
		
		super(context);
		
		this.transport = transport;
		this.buffer = new VT320View();
	}

	@Override
	protected void onDraw(Canvas canvas) {

		super.onDraw(canvas);

	}
	
	private class VT320View extends VT320 {

		@Override
		public void debug(String notice) {

		}

		@Override
		public void write(byte[] b) {

		}

		@Override
		public void write(int b) {

		}
	}
}
