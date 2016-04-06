package com.aparnyuk.rsn.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;

import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.aparnyuk.rsn.R;
import com.aparnyuk.rsn.Utils.Constants;
import com.aparnyuk.rsn.activity.MainActivity;
import com.aparnyuk.rsn.model.Calls;
import com.aparnyuk.rsn.model.Remind;
import com.aparnyuk.rsn.model.Sms;
import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

public class MainService extends Service {
    private NotificationManager nm;
    private final int NOTIFICATION_ID = 73;
    public static boolean state = true;

    SharedPreferences sp;

    private Timer mTimer;

    final String TAG = "Annet";

    private Firebase sms_ref;
    private Firebase call_ref;
    private Firebase remind_ref;
    private ChildEventListener smsListener;
    private ChildEventListener callListener;
    private ChildEventListener remindListener;
    private Firebase ref;

    public MainService() {

    }

    public void onCreate() {
        super.onCreate();
        state = false;
        nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mTimer = new Timer();
        Log.d(TAG, "onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        Firebase.setAndroidContext(this);
        ref = new Firebase(Constants.FIREBASE_URL);
        AuthData authData = ref.getAuth();
        if (authData != null) {
            ref = ref.child(authData.getUid());
            sms_ref = ref.child("sms");
            call_ref = ref.child("call");
            remind_ref = ref.child("remind");
            smsListener = listenFirebase(sms_ref, 0);
            callListener = listenFirebase(call_ref, 1);
            remindListener = listenFirebase(remind_ref, 2);
        } else {
            Log.d(TAG, "no auth user");
            //!!!!!!
        }

        // test input
        /*Date date = new Date(System.currentTimeMillis());
        for (int i = 0; i < 10; i++) {
            int s = (1 + (int) (Math.random() * 10)) * 10000;
            date.setTime(System.currentTimeMillis() + s);
            aList.add(date);
            mTimer.schedule(new CallTimerTask("lkj"), date);
            Log.d(TAG, date.toString());
        }*/
        return Service.START_STICKY;
    }

    public void updateData() {
        // 1. изменить запись в базе (если повтор 3 раза, то теперь 2)
        // 2. добавить задание в рассписание - createTimer()
    }

    public void deleteData() {
        // удалить запись из базы (если без повтора или оставался один повтор)
    }


    ChildEventListener listenFirebase(Firebase ref, final int taskType) {
        ChildEventListener listener = new ChildEventListener() {
            // Retrieve new tasks as they are added to the database
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                switch (taskType) {
                    case 0: {
                        Sms newSms = snapshot.getValue(Sms.class);
                        mTimer.schedule(new SmsTimerTask(newSms.getText()), newSms.getDate());
                        Log.d(TAG, "Add SmsTimerTask");
                        break;
                    }
                    case 1: {
                        Calls newCall = snapshot.getValue(Calls.class);
                        mTimer.schedule(new CallTimerTask(newCall.getText()), newCall.getDate());
                        Log.d(TAG, "Add CallTimerTask");
                        break;
                    }
                    case 2: {
                        Remind newRemind = snapshot.getValue(Remind.class);
                        mTimer.schedule(new RemindTimerTask(newRemind.getText()), newRemind.getDate());
                        Log.d(TAG, "Add RemindTimerTask");
                        break;
                    }
                }
            }

            // Get the data on a tasks that has changed
            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildKey) {
                Log.d(TAG, "Change TimerTask");
                createTimer();
            }

            // Get the data on a tasks  that has been removed
            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                Log.d(TAG, "Remove TimerTask");
                createTimer();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                // doing nothing
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d(TAG, "some error happened");
            }
        };
        ref.addChildEventListener(listener);
        return listener;
    }

    //create new Timer and rewrite TimerTask here
    private void createTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = new Timer();
        }
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    //   Log.d(TAG, "Children " + snapshot.toString());
                    Log.d(TAG, "key " + child.getKey());
                    for (DataSnapshot childTask : child.getChildren()) {
                        //   Log.d(TAG, "Children " + snapshot.toString());
                        switch (child.getKey()) {
                            case "sms": {
                                Sms newSms = childTask.getValue(Sms.class);
                                mTimer.schedule(new SmsTimerTask(newSms.getText()), newSms.getDate());
                                Log.d(TAG, "Add SmsTimerTask = ");
                                break;
                            }
                            case "call": {
                                Calls newCall = childTask.getValue(Calls.class);
                                mTimer.schedule(new CallTimerTask(newCall.getText()), newCall.getDate());
                                Log.d(TAG, "Add CallTimerTask= ");
                                break;
                            }
                            case "remind": {
                                Remind newRemind = childTask.getValue(Remind.class);
                                mTimer.schedule(new RemindTimerTask(newRemind.getText()), newRemind.getDate());
                                Log.d(TAG, "Add RemindTimerTask= ");
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    class SmsTimerTask extends TimerTask {
        private String taskText;
        long id;

        public SmsTimerTask(String text/*,long taskID*/) {
            this.taskText = text;
            //this.id = taskID;
        }

        @Override
        public void run() {
            //  Log.d(TAG, "Start SmsTimerTask");

        }
    }

    class CallTimerTask extends TimerTask {
        private String taskText;

        public CallTimerTask(String text) {
            this.taskText = text;
        }

        @Override
        public void run() {
            //  Log.d(TAG, "Start CallTimerTask ");
        }
    }

    class RemindTimerTask extends TimerTask {
        private String taskText;

        public RemindTimerTask(String text) {
            this.taskText = text;
        }

        @Override
        public void run() {
            //    Log.d(TAG, "Start RemindTimerTask");
        }
    }


    public void onDestroy() {
        nm.cancel(NOTIFICATION_ID);
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
        //ref.removeEventListener();
        sms_ref.removeEventListener(smsListener);
        call_ref.removeEventListener(callListener);
        remind_ref.removeEventListener(remindListener);
        Log.d(TAG, "Destroy service");
        super.onDestroy();
        state = true;
    }

    public void remind() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        Intent appIntent = new Intent(getApplicationContext(), MainActivity.class);
        //   Intent downloadIntent = new Intent(getApplicationContext(), UpdateService.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //   PendingIntent downloadPendingIntent = PendingIntent.getService(getApplicationContext(), 0, downloadIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder
                .setContentIntent(appPendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher))
                .setTicker(getString(R.string.notification_ticker))
                .setWhen(System.currentTimeMillis()) // время уведомления - текущее
                .setAutoCancel(true) // для автоматического закрытия
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_text))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.notification_long_text)));
        // .addAction(R.drawable.ic_account_black_18dp, getString(R.string.notification_update_button), downloadPendingIntent);
        Notification notification = builder.build();
        nm.notify(NOTIFICATION_ID, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }
}


