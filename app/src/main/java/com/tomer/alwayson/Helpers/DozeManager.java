package com.tomer.alwayson.Helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class DozeManager {

    private static final String TAG = DozeManager.class.getSimpleName();

    public DozeManager(Context context) {
        if (!isDumpPermissionGranted(context))
            executeCommand("pm grant com.tomer.alwayson android.permission.DUMP");
        if (!isDevicePowerPermissionGranted(context))
            executeCommand("pm grant com.tomer.alwayson android.permission.DEVICE_POWER");
    }

    public void enterDoze() {
        if (!getDeviceIdleState().equals("IDLE")) {
            Log.i(TAG, "Entering Doze");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                executeCommand("dumpsys deviceidle force-idle deep");
            } else {
                executeCommand("dumpsys deviceidle force-idle");
            }
        } else {
            Log.i(TAG, "enterDoze() received but skipping because device is already Dozing");
        }
    }

    public void exitDoze() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            executeCommand("dumpsys deviceidle unforce");
        } else {
            executeCommand("dumpsys deviceidle step");
        }
    }

    public static void executeCommand(final String command) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                List<String> output = Shell.SH.run(command);
                if (output == null)
                    Log.i(TAG, "Error occurred while executing command (" + command + ")");
            }
        });
    }

    private String getDeviceIdleState() {
        String state = "";
        List<String> output = Shell.SH.run("dumpsys deviceidle");
        String outputString = TextUtils.join(", ", output);
        if (outputString.contains("mState=ACTIVE")) {
            state = "ACTIVE";
        } else if (outputString.contains("mState=INACTIVE")) {
            state = "INACTIVE";
        } else if (outputString.contains("mState=IDLE_PENDING")) {
            state = "IDLE_PENDING";
        } else if (outputString.contains("mState=SENSING")) {
            state = "SENSING";
        } else if (outputString.contains("mState=LOCATING")) {
            state = "LOCATING";
        } else if (outputString.contains("mState=IDLE")) {
            state = "IDLE";
        } else if (outputString.contains("mState=IDLE_MAINTENANCE")) {
            state = "IDLE_MAINTENANCE";
        }
        return state;
    }

    public static boolean isDevicePowerPermissionGranted(Context context) {
        return context.checkCallingOrSelfPermission("android.permission.DEVICE_POWER") == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isDumpPermissionGranted(Context context) {
        return context.checkCallingOrSelfPermission(Manifest.permission.DUMP) == PackageManager.PERMISSION_GRANTED;
    }
}