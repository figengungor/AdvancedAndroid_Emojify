package com.example.android.emojify;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by figengungor on 4/4/2018.
 */

public class EmojifyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
