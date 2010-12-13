package org.irssibot;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import org.irssibot.util.LogHelper;
import org.irssibot.util.URL;
import org.irssibot.util.URLTest;

import static org.irssibot.util.LogHelper.*;

public class MainActivity extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		((TextView)findViewById(R.id.hello)).setText(URL.find(URLTest.INPUT).toString());

		INFO("Meaning of life:", 42);
    }
}
