package org.irssibot;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import org.irssibot.util.URL;
import org.irssibot.util.URLTest;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		((TextView)findViewById(R.id.hello)).setText(URL.findAll(URLTest.INPUT).toString());
    }
}
