package com.applurk.plugin;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.PowerManager;

import android.util.Log;
import android.provider.Settings;
import android.widget.Toast;

import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.app.KeyguardManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Build;

public class ScreenLocker extends CordovaPlugin {
    public static final String TAG = "ScreenLocker";
    public static final String ACTION_UNLOCK = "unlock";
    public static final String ACTION_LOCK = "lock";
    public static PowerManager powerManager;
    public static PowerManager.WakeLock wakeLock;

    /**
     * Constructor.
     */
    public ScreenLocker() {
    }

    /**
     * Sets the context of the Command. This can then be used to do things like
     * get file paths associated with the Activity.
     *
     * @param cordova The context of the main Activity.
     * @param webView The CordovaWebView Cordova is running in.
     */
    
    @SuppressLint("InvalidWakeLockTag")
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        powerManager = (PowerManager) cordova.getActivity().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock((PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE), "TAG");

        Log.v(TAG, "Init ScreenLocker");
    }

    @Override
    public boolean execute(final String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        boolean result = false;
        Log.v(TAG, "ScreenLocker received:" + action);

        try {
            if (ACTION_LOCK.equals(action)) {
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Window window = cordova.getActivity().getWindow();
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                        window.clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

                        if(wakeLock.isHeld()) {
                            wakeLock.release();
                        }

                        Log.v(TAG, "ScreenLocker received SUCCESS:" + action);
                        callbackContext.success();
                    }
                });
                result = true;
            } else if (ACTION_UNLOCK.equals(action)) {
                Log.v(TAG, "ScreenLocker received ACTION_UNLOCK");
                JSONObject arg_object = args.getJSONObject(0);
                final int  acquireTime = arg_object.getInt("timeout");

                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {

                            cordova.getActivity().setShowWhenLocked(true);
                            cordova.getActivity().setTurnScreenOn(true);

                            KeyguardManager keyguardManager = (KeyguardManager) cordova.getActivity().getSystemService(Context.KEYGUARD_SERVICE);
                            keyguardManager.requestDismissKeyguard(cordova.getActivity(), null);

                            if(wakeLock.isHeld()) {
                                wakeLock.release();
                            }
                            if(acquireTime > 0) {
                                wakeLock.acquire(acquireTime);
                            }
                            else
                            {
                                wakeLock.acquire();
                            }
                            Log.v(TAG, "ScreenLocker received SUCCESS:" + action);
                            callbackContext.success();
                        }
                        else{

                            Window window = cordova.getActivity().getWindow();
                            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

                            if(wakeLock.isHeld()) {
                                wakeLock.release();
                            }
                            if(acquireTime > 0) {
                                wakeLock.acquire(acquireTime);
                            }
                            else
                            {
                                wakeLock.acquire();
                            }
                            Log.v(TAG, "ScreenLocker received SUCCESS:" + action);
                            callbackContext.success();
                        }
                    }
                });

                result = true;
            } else {
                callbackContext.error("Invalid action");
                result = false;
            }
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
            result = false;
        }

        return result;
    }
}