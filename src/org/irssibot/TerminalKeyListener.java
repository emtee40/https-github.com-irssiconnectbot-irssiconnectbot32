package org.irssibot;

import android.view.KeyEvent;
import android.view.View;
import de.mud.terminal.VT320;
import org.irssibot.transport.Transport;

import static org.irssibot.util.LogHelper.DEBUG;

class TerminalKeyListener implements View.OnKeyListener {

	private final Transport transport;
	private TerminalView terminalView;
	private VT320 buffer;

	public TerminalKeyListener(TerminalView terminalView, Transport transport, VT320 buffer) {
		this.terminalView = terminalView;
		this.transport = transport;
		this.buffer = buffer;
	}

	public boolean onKey(View v, int keyCode, KeyEvent event) {
		int key = terminalView.keymap.get(keyCode, event.getMetaState());

		DEBUG("Got key data:", keyCode, event.getCharacters(), key);

		if (event.getCharacters() != null) {
			transport.write(event.getCharacters().getBytes());
			
			return true;
		}
		
		if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
		
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				buffer.keyPressed(VT320.KEY_LEFT, ' ', 0);
				return true;
			
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				buffer.keyPressed(VT320.KEY_RIGHT, ' ', 0);
				return true;

			case KeyEvent.KEYCODE_ENTER:
//				buffer.keyPressed(VT320.KEY_ENTER, ' ', 0);
				buffer.keyTyped(VT320.KEY_ENTER, ' ', 0);
				return true;
			
			case KeyEvent.KEYCODE_DEL:
				buffer.keyPressed(VT320.KEY_BACK_SPACE, ' ', 0);
				
				return true;
		}

		transport.write(new String(Character.toChars(key)).getBytes());

		return false;
	}
}
