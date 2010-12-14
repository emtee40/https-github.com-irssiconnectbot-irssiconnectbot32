package org.irssibot;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import org.irssibot.util.URIHelper;
import org.irssibot.util.URITest;

import static org.irssibot.util.LogHelper.INFO;

public class MainActivity extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		((TextView) findViewById(R.id.hello)).setText(URIHelper.find(URITest.INPUT).toString());

		INFO("Meaning of life:", 42);
    }
}
