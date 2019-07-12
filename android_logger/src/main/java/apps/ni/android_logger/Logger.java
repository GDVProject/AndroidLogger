package apps.ni.android_logger;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Logger {

    public static class Initializer {

        private Logger instance;
        private Context context;

        public Initializer(Context context, String appTag) {
            instance = new Logger(appTag);
            this.context = context;
        }

        public Initializer writeToConsole(boolean write) {
            instance.writeToConsole = write;
            return this;
        }

        public Initializer writeToFile(boolean write) {
            instance.setWriteToFile(context, write);
            return this;
        }

        public void initialize() {
            instance.startLogging();
        }

    }

    private static Logger logger;
    private static final String LOG_PATH = "logs";
    private static final String LOG_FILE_NAME_CURRENT = "current.log";
    private static final String LOG_FILE_NAME_PREVIOUS = "previous.log";
    private static final String LOG_FILE_NAME_ZIP = "log.zip";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS", Locale.US);

    private final Semaphore semaphore = new Semaphore(1, true);

    private File logFile;
    private String appTag;
    private boolean writeToConsole;
    private boolean writeToFile;
    private String previousLogPath;
    private String currentLogPath;
    private String zipLogPath;

    private Logger(String appTag) {
        this.appTag = appTag;
    }

    private void setWriteToFile(Context context, boolean write) {
        this.writeToFile = write;
        if (this.writeToFile) {
            try {
                File directory = new File(context.getFilesDir(), LOG_PATH);
                if (!directory.exists()) {
                    directory.mkdir();
                }
                File zip = new File(directory, LOG_FILE_NAME_ZIP);
                if (zip.exists()) {
                    zip.delete();
                }
                zipLogPath = zip.getAbsolutePath();
                File previous = new File(directory, LOG_FILE_NAME_PREVIOUS);
                if (previous.exists()) {
                    previous.delete();
                }
                logFile = new File(directory, LOG_FILE_NAME_CURRENT);
                if (logFile.exists()) {
                    logFile.renameTo(previous);
                    previousLogPath = previous.getAbsolutePath();
                    logFile.delete();
                }
                logFile.createNewFile();
                currentLogPath = logFile.getPath();
            } catch (IOException e) {
                this.writeToFile = false;
            }
        }
    }

    public static Initializer initializeLogger(Context context, String tag) {
        return new Initializer(context, tag);
    }

    private static Logger getLogger() {
        if (logger != null) {
            return logger;
        } else {
            throw new LoggerNotInitializedException();
        }
    }

    public static void log(String message) {
        getLogger().logMessage(message);
    }

    public static void log(Object context, String message) {
        getLogger().logMessage(context, message);
    }

    public static void log(String description, Bundle bundle) {
        getLogger().logMessage(description, bundle);
    }

    public static void log(String description, Intent intent) {
        getLogger().logMessage(description, intent);
    }

    public static void log(String description, Throwable t) {
        getLogger().logMessage(description, t);
    }

    public static void shareLog(Activity context, String title) {
        shareLog(context, title, true);
    }

    public static void shareLog(Activity context, String title, boolean zip) {
        Intent intent;
        if (zip) {
            intent = shareZippedLog(context);
        } else {
            intent = shareRawLogs(context);
        }
        if (intent != null) {
            Intent chooserIntent = Intent.createChooser(intent, title);
            if (chooserIntent != null) {
                context.startActivityForResult(chooserIntent, 123);
                return;
            }
        }
        Toast.makeText(context, R.string.log_sharing_failed, Toast.LENGTH_LONG).show();
    }

    private static Intent shareZippedLog(Context context) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        String filePath = getLogger().zipLog();
        if (filePath != null) {
            Uri uri = getFileUri(context, filePath);
            if (uri != null) {
                intent.setType("application/zip");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
            }
            return intent;
        }
        return null;
    }

    private static Intent shareRawLogs(Context context) {
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        Logger logger = getLogger();
        ArrayList<Uri> uris = new ArrayList<>();
        String[] files = new String[]{logger.previousLogPath, logger.currentLogPath};
        for (String filePath : files) {
            Uri uri = getFileUri(context, filePath);
            if (uri != null) {
                uris.add(uri);
            }
        }
        if (uris.size() > 0) {
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_STREAM, uris);
            return intent;
        }
        return null;
    }

    private static Uri getFileUri(Context context, String path) {
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                return FileProvider.getUriForFile(context, "apps.ni.android_logger", file);
            }
        }
        return null;
    }

    public static String getCurrentLogFileName() {
        return getLogger().currentLogPath;
    }

    public static String getPreviousLogFileName() {
        return getLogger().previousLogPath;
    }

    public static String getCurrentLog() {
        return getLogger().getLog(getLogger().currentLogPath);
    }

    public static String getPreviousLog() {
        return getLogger().getLog(getLogger().previousLogPath);
    }

    public static String getLogZip() {
        return getLogger().zipLog();
    }

    private void startLogging() {
        logger = this;
        final Thread.UncaughtExceptionHandler regularHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.UncaughtExceptionHandler logHandler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                logMessage("Uncaught exception", e);
                regularHandler.uncaughtException(t, e);
            }
        };
        Thread.setDefaultUncaughtExceptionHandler(logHandler);
        log(String.format(Locale.US, "%s (SDK %d)", Build.MODEL, Build.VERSION.SDK_INT));
    }

    private String getLog(String fileName) {
        if (fileName != null) {
            StringBuilder stringBuilder = new StringBuilder();
            try (FileInputStream inputStream = new FileInputStream(fileName);
                 InputStreamReader reader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(reader)) {
                String s;
                while ((s = bufferedReader.readLine()) != null) {
                    stringBuilder.append("\n");
                    stringBuilder.append(s);
                }
                return stringBuilder.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        } else {
            return "";
        }
    }

    private String zipLog() {
        try (FileOutputStream fileOutputStream = new FileOutputStream(zipLogPath);
             ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
            addLogToZip(zipOutputStream, getLog(previousLogPath), LOG_FILE_NAME_PREVIOUS);
            addLogToZip(zipOutputStream, getLog(currentLogPath), LOG_FILE_NAME_CURRENT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return zipLogPath;
    }

    private void addLogToZip(ZipOutputStream zipOutputStream, String log, String fileName) {
        try {
            if (!TextUtils.isEmpty(log)) {
                zipOutputStream.putNextEntry(new ZipEntry(fileName));
                zipOutputStream.write(log.getBytes(StandardCharsets.UTF_8));
                zipOutputStream.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logMessage(String description, Bundle bundle) {
        log(getBundleString(description, bundle));
    }

    private void logMessage(String description, Intent intent) {
        log(getIntentString(description, intent));
    }

    private void logMessage(Object context, String message) {
        String m = String.format("%s: %s", getComponentName(context), message);
        logMessage(m);
    }

    private void logMessage(String description, Throwable e) {
        logMessage(String.format("%s:\n%s", description, getExceptionString(e)));
    }

    private void logMessage(String message) {
        if (writeToConsole) {
            logToConsole(message);
        }
        if (writeToFile) {
            logToFile(message);
        }
    }

    private void logToConsole(String message) {
        Log.i(appTag, message);
    }

    private void logToFile(final String message) {
        final String text = String.format("%s - %s", dateFormat.format(new Date()), message);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    semaphore.acquire();
                    appendToFile(text);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphore.release();
                }
            }
        }).start();
    }

    synchronized private void appendToFile(String message) {
        try (
                FileOutputStream fos = new FileOutputStream(logFile, true);
                OutputStreamWriter osw = new OutputStreamWriter(fos);
                BufferedWriter bw = new BufferedWriter(osw)) {
            bw.newLine();
            bw.append(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getBundleString(String description, Bundle bundle) {
        StringBuilder stringBuilder = new StringBuilder();
        if (bundle != null) {
            Set<String> keys = bundle.keySet();
            stringBuilder.append(String.format("\t%s (%s items)", description, String.valueOf(keys.size())));
            for (String key : keys) {
                stringBuilder.append(String.format("\n\t%s = %s", key, String.valueOf(bundle.get(key))));
            }
        } else {
            stringBuilder.append(String.format("\t%s: null", description));
        }
        return stringBuilder.toString();
    }

    private String getIntentString(String description, Intent intent) {
        StringBuilder stringBuilder = new StringBuilder();
        if (intent != null) {
            stringBuilder.append(description);
            stringBuilder.append(String.format("\nACTION: %s", String.valueOf(intent.getAction())));
            stringBuilder.append(String.format("\nCOMPONENT NAME: %s", getComponentName(intent.getComponent())));
            stringBuilder.append(String.format("\n%s", getBundleString("EXTRAS", intent.getExtras())));
        } else {
            stringBuilder.append(String.format("\t%s: null", description));
        }
        return stringBuilder.toString();
    }

    private String getComponentName(Object component) {
        if (component != null) {
            return component.getClass().getSimpleName();
        }
        return "null";
    }

    private String getExceptionString(Throwable e){
        Throwable throwable = e;
        StringBuilder stringBuilder = new StringBuilder();
        do {
            stringBuilder.append(String.format("%s: %s", getComponentName(throwable), throwable.getMessage()));
            for (StackTraceElement element : throwable.getStackTrace()) {
                stringBuilder.append("\n\t");
                stringBuilder.append(element.toString());
            }
            throwable = throwable.getCause();
            if (throwable != null){
                stringBuilder.append("\nCaused by: ");
            }
        } while (throwable != null);
        return stringBuilder.toString();
    }

}
