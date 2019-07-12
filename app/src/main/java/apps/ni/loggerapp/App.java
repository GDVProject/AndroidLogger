package apps.ni.loggerapp;

import android.app.Application;

import apps.ni.android_logger.Logger;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.initializeLogger(this, "TEST").writeToConsole(true).writeToFile(true).initialize();
    }
}
