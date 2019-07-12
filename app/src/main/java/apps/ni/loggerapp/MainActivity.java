package apps.ni.loggerapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import apps.ni.android_logger.LogActivity;
import apps.ni.android_logger.Logger;


public class MainActivity extends LogActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextView) findViewById(R.id.textView)).setText(Logger.getLogZip());
            }
        });
        final Activity activity = this;
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.shareLog(activity, "Send logs", false);
            }
        });
        int a = 10 / 0;
    }
}
