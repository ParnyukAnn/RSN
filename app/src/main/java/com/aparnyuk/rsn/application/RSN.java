package com.aparnyuk.rsn.application;

        //import com.crashlytics.android.Crashlytics;
        import com.firebase.client.Firebase;
        //import io.fabric.sdk.android.Fabric;

public class RSN extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
       // Fabric.with(this, new Crashlytics());

        Firebase.setAndroidContext(this);
        // enable disk persistence
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
    }
}

