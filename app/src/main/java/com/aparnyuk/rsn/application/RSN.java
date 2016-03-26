package com.aparnyuk.rsn.application;

import com.firebase.client.Firebase;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;


public class RSN extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        Firebase.setAndroidContext(this);
        // enable disk persistence
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
    }
}

