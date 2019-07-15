package apps.ni.loggerapp;

import android.app.Application;

import apps.ni.android_logger.Logger;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.initializeLogger(this).writeToConsole(true).setConsoleTag("TEST").writeToFile(true).initialize();
    }
}
