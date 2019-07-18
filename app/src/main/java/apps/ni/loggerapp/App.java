package apps.ni.loggerapp;

import android.app.Application;

import apps.ni.android_logger.Logger;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.initializeLogger(this)
                .writeToConsole("TEST")
                .writeToFile()
                .writeToServer("http://ps1zllxnph0j.zzz.com.ua/logs/api/", "log.php")
                .initialize();
    }
}
