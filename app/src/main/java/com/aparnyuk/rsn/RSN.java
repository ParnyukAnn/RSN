package com.aparnyuk.rsn;

import com.firebase.client.Firebase;

public class RSN extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
