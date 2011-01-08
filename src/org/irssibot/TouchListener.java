package org.irssibot;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class TouchListener implements OnTouchListener {
	private GestureDetector detect;
	
	public TouchListener (GestureDetector detect) {
		this.detect = detect;
	}
	
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {

		if(detect.onTouchEvent(arg1)) {
			return true;
		}
		
		return false;
	}
		
}