package apps.ni.android_logger;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public abstract class LogFragment extends Fragment {

    @Override
    public void onAttach(Context context) {
        Logger.log(this, "Lifecycle: onAttach");
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Logger.log(this, "Lifecycle: onCreate");
        Logger.log("savedInstanceState", savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Logger.log(this, "Lifecycle: onViewCreated");
        Logger.log("savedInstanceState", savedInstanceState);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Logger.log(this, "Lifecycle: onActivityCreated");
        Logger.log("savedInstanceState", savedInstanceState);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        Logger.log(this, "Lifecycle: onViewStateRestored");
        Logger.log("savedInstanceState", savedInstanceState);
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onStart() {
        Logger.log(this, "Lifecycle: onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        Logger.log(this, "Lifecycle: onResume");
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Logger.log(this, "Lifecycle: onSaveInstanceState");
        Logger.log("outState", outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        Logger.log(this, "Lifecycle: onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Logger.log(this, "Lifecycle: onStop");
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        Logger.log(this, "Lifecycle: onLowMemory");
        super.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        Logger.log(this, "Lifecycle: onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Logger.log(this, "Lifecycle: onDestroy");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Logger.log(this, "Lifecycle: onDetach");
        super.onDetach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.log(this, String.format(Locale.US, "ActivityResult received, requestCode = %d, resultCode = %d", requestCode, resultCode));
        Logger.log("Data", data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format(Locale.US, "Permission request result received (request code: %d)", requestCode));
        for (int i = 0; i < Math.min(permissions.length, grantResults.length); i++) {
            stringBuilder.append(String.format("\n\tPermission: %s, Result: %s", permissions[i], grantResults[i] == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "DENIED"));
        }
        Logger.log(this, stringBuilder.toString());
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void startActivity(Intent intent, @Nullable Bundle options) {
        Logger.log(this, "Starting Activity");
        Logger.log("Intent", intent);
        Logger.log("Options", options);
        super.startActivity(intent, options);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        Logger.log(this, String.format(Locale.US, "Starting Activity for result (request code: %d)", requestCode));
        Logger.log("Intent", intent);
        Logger.log("Options", options);
        super.startActivityForResult(intent, requestCode, options);
    }
}
