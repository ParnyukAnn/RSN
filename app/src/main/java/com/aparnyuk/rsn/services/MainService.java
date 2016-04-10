package com.aparnyuk.rsn.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;

import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainService extends Service {

    //   private final int NOTIFICATION_ID = 73;

    public final static int STATUS_CANCEL = 200;
    public final static int STATUS_ACTION = 100;
    public final static String PARAM_RESULT = "result";
    public final static String PARAM_STATUS = "status";
    public final static String BROADCAST_ACTION = "com.aparnyuk.receiver.BROADCAST";


    public static boolean state = true;
    //!! delete this when save password in pref or offline auth
    public static boolean no_data = true;

    SharedPreferences sp;

    final String TAG = "Annet";

    private Firebase sms_ref;
    private Firebase call_ref;
    private Firebase remind_ref;
    private ChildEventListener smsListener;
    private ChildEventListener callListener;
    private ChildEventListener remindListener;
    private Firebase ref;

    private Timer mTimer;
    private int taskID;

    HashMap<String, TimerTask> tasksMap;
    Map<String, TimerTask> syncTasksMap;
    // HashMap<Integer, TimerTask> tasks;
    // Map<Integer, TimerTask> syncTasks;

    NotificationManager nm;
    BroadcastReceiver br;

    public MainService() {

    }

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        createBroadcast();
        state = false;

        mTimer = new Timer();
        taskID = 0;
        //tasks = new HashMap<>();
        //syncTasks = Collections.synchronizedMap(tasks);
        tasksMap = new HashMap<>();
        syncTasksMap = Collections.synchronizedMap(tasksMap);
        nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

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
            no_data = false;
        } else {
            Log.d(TAG, "no auth user");
            //!!!!!!
            no_data = true;
        }
        return Service.START_STICKY;
    }

    ChildEventListener listenFirebase(Firebase ref, final int taskType) {
        ChildEventListener listener = new ChildEventListener() {
            // Retrieve new tasks as they are added to the database
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                createTask(snapshot);
            }

            // Get the data on a tasks that has changed
            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildKey) {
                Log.d(TAG, "Change data in database.");
                // createTimer();
                deleteTask(snapshot);
                createTask(snapshot);

            }

            // Get the data on a tasks  that has been removed
            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                Log.d(TAG, "Remove data from database");
                // createTimer();
                deleteTask(snapshot);
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

    private void deleteTask(DataSnapshot snapshot) {
        if (syncTasksMap.containsKey(snapshot.getKey())) {
// !! delete this section
            switch (snapshot.getRef().getParent().getKey()) {
                case "sms": {
                    SmsTimerTask sms = (SmsTimerTask) syncTasksMap.get(snapshot.getKey());
                    Log.d(TAG, "Delete Sms TimerTask. Task ID = " + sms.id);
                    break;
                }
                case "call": {
                    CallTimerTask call = (CallTimerTask) syncTasksMap.get(snapshot.getKey());
                    Log.d(TAG, "Delete Call TimerTask. Task ID = " + call.id);
                    break;
                }
                case "remind": {
                    RemindTimerTask remind = (RemindTimerTask) syncTasksMap.get(snapshot.getKey());
                    Log.d(TAG, "Delete Remind TimerTask. Task ID = " + remind.id);
                    break;
                }
            }
// !! delete this section

            String key = snapshot.getKey();
            // удалить эл. из базы или установить isOpen=false
            // tasks.remove(this);

            //remind_ref.child(key).removeValue();
            syncTasksMap.get(key).cancel();
            mTimer.purge();
            syncTasksMap.remove(key);
            // remind_ref.child(key).child("open").setValue(false);
        }
    }

    // create new Timer Task and add it into Timer schedule
    private void createTask(DataSnapshot snapshot) {
        if (taskID < 32767) { //2147483647
            switch (snapshot.getRef().getParent().getKey()) {
                case "sms": {
                    Sms sms = snapshot.getValue(Sms.class);
                    if (sms.isOpen()) {
                        //  tasks.put(taskID, new SmsTimerTask(sms));
                        syncTasksMap.put(snapshot.getKey(), new SmsTimerTask(sms, taskID));
                        mTimer.schedule(syncTasksMap.get(snapshot.getKey()), sms.getDate());
                        //  mTimer.schedule(tasks.get(taskID), sms.getDate());
                        Log.d(TAG, "Add Sms TimerTask. Task ID = " + taskID);
                        taskID++;
                    }
                    break;
                }
                case "call": {
                    Calls call = snapshot.getValue(Calls.class);
                    if (call.isOpen()) {
                        //    tasks.put(taskID, new CallTimerTask(call));
                        syncTasksMap.put(snapshot.getKey(), new CallTimerTask(call, taskID));
                        mTimer.schedule(syncTasksMap.get(snapshot.getKey()), call.getDate());
                        // mTimer.schedule(tasks.get(taskID), call.getDate());
                        Log.d(TAG, "Add Call TimerTask. Task ID = " + taskID);
                        taskID++;
                    }
                    break;
                }
                case "remind": {
                    Remind remind = snapshot.getValue(Remind.class);
                    if (remind.isOpen()) {
                        //tasks.put(taskID, new RemindTimerTask(remind,snapshot.getKey()));
                        syncTasksMap.put(snapshot.getKey(), new RemindTimerTask(remind, snapshot.getKey(), taskID));
                        if (remind.getRepeatPeriod() == 0) {
                            mTimer.schedule(syncTasksMap.get(snapshot.getKey()), remind.getDate());
                            // mTimer.schedule(tasks.get(taskID), remind.getDate());
                        } else {
                            mTimer.schedule(syncTasksMap.get(snapshot.getKey()), remind.getDate(), remind.getRepeatPeriod());
                            //mTimer.schedule(tasks.get(taskID), remind.getDate(), remind.getRepeatPeriod());
                        }
                        Log.d(TAG, "Add Remind TimerTask. Task ID = " + taskID);
                        taskID++;
                    }
                    break;
                }
            }

        } else {
            createTimer();
        }
    }

    // create new Timer and rewrite TimerTask here
    private void createTimer() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer();
        taskID = 0;
        //tasks.clear();
        tasksMap.clear();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    Log.d(TAG, "key " + child.getKey());
                    for (DataSnapshot childTask : child.getChildren()) {
                        createTask(childTask);
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    class SmsTimerTask extends TimerTask {
        private Sms sms;
        int id;

        public SmsTimerTask(Sms smsTask, int id) {
            this.sms = smsTask;
            this.id = id;
        }

        @Override
        public void run() {
            Log.d(TAG, "Start SmsTimerTask. Task ID =  " + id);
        }
    }

    class CallTimerTask extends TimerTask {
        private Calls call;
        int id;

        public CallTimerTask(Calls callTask, int id) {
            this.call = callTask;
            this.id = id;
        }

        @Override
        public void run() {
            Log.d(TAG, "Start Call TimerTask. Task ID =  " + id);
        }
    }

    class RemindTimerTask extends TimerTask {
        private Remind remind;
        int id;
        int count;
        int stop;
        String key;

        public RemindTimerTask(Remind remindTask, String key, int id) {
            this.remind = remindTask;
            this.count = 0;
            this.stop = remind.getRepeatCount();
            this.key = key;
            this.id = id;
        }

        @Override
        public void run() {

            Log.d(TAG, "Start Remind TimerTask. Task ID =  " + id + " " + remind.getText() + " count - " + count + " stop when - " + (stop));
            remindNotification(remind.getText() + "Task id " + id + " " + " repeat count - " + count + " stop when - " + (stop), id);

            if (count == stop) {
                Log.d(TAG, "Stop Remind TimerTask. Task ID =  " + id);
                // удалить эл. из базы или установить isOpen=false
                // tasks.remove(this);
               /* syncTasksMap.remove(key);*/
                //remind_ref.child(key).removeValue();

                remind_ref.child(key).child("open").setValue(false);


               /* this.cancel();
                mTimer.purge();*/
                // nm.cancel(id);
            } else {
                count++;
            }
        }
    }

    public void remindNotification(String text, int id) {
        Intent cancelIntent = new Intent(MainService.BROADCAST_ACTION);
        cancelIntent.putExtra(MainService.PARAM_STATUS, MainService.STATUS_CANCEL);
        cancelIntent.putExtra(MainService.PARAM_RESULT, 10);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);



       /* Intent actionIntent = new Intent(MainService.BROADCAST_ACTION);
        actionIntent.putExtra(MainService.PARAM_STATUS, MainService.STATUS_ACTION);
        actionIntent.putExtra(MainService.PARAM_RESULT, 5);
        PendingIntent actionPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
*/
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        Intent appIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //Intent Intent = new Intent(getApplicationContext(), MainActivity.class);

        builder
                .setContentIntent(appPendingIntent)
                .setSmallIcon(R.drawable.telegram)
//                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher))
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_alarm_check_grey600_48dp))
                .setTicker(getString(R.string.notification_ticker))
                .setWhen(System.currentTimeMillis()) // время уведомления - текущее
                .setAutoCancel(true) // для автоматического закрытия
                .setContentTitle(getString(R.string.app_name) + getString(R.string.notification_text))
                .setContentText(text)
                        //.setPriority(Notification.PRIORITY_HIGH)
                        //.setCategory(Notification.CATEGORY_ALARM)
                        //.setContentText(getString(R.string.notification_text))
                .addAction(R.drawable.ic_delete_grey600_18dp, "Cancel", cancelPendingIntent)
                //.addAction(R.drawable.ic_alarm_grey600_18dp, "Later", actionPendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        //.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.notification_long_text)));
        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
        notification.flags = notification.flags | Notification.FLAG_INSISTENT;
        nm.notify(id, notification);
    }


    public void onDestroy() {

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (!tasksMap.isEmpty()) {
            tasksMap.clear();
        }
        //ref.removeEventListener();
        if (!no_data) {
            sms_ref.removeEventListener(smsListener);
            call_ref.removeEventListener(callListener);
            remind_ref.removeEventListener(remindListener);
        }
        Log.d(TAG, "Destroy service");
        this.unregisterReceiver(br);
        super.onDestroy();
        state = true;

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void createBroadcast() {

        br = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int status = intent.getIntExtra(PARAM_STATUS, 0);
                //int result = intent.getIntExtra(PARAM_RESULT, -1);
                Log.d(TAG, "on receiver");
                if (status == STATUS_CANCEL) {
                    Toast.makeText(getApplicationContext(), "CANCEL", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "on receiver - cancel");
                    createTimer();
                }
                if (status == STATUS_ACTION) {
                    Toast.makeText(getApplicationContext(), "ACTION", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "on receiver - action");
                }
            }
        };
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        this.registerReceiver(br, intFilt);
    }
}


