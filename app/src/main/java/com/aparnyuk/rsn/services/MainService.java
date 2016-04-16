package com.aparnyuk.rsn.services;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;

import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainService extends Service {

    // constants for sending from notification and reading in broadcast receiver
    public final static int STATUS_SEND = 100;
    public final static int STATUS_CALL = 200;
    public final static int STATUS_LATER = 300;
    public final static int STATUS_SMS_CANCEL = 101;
    public final static int STATUS_CALL_CANCEL = 201;
    public final static int STATUS_REMIND_CANCEL = 301;

    String SEND_SMS_FLAG = "SEND_SMS";
    String DELIVER_SMS_FLAG = "DELIVER_SMS";

    public final static String PARAM_STATUS = "status";
    public final static String PARAM_ID = "id";
    public final static String PARAM_SMS_TEXT = "sms_text";
    public final static String PARAM_TASK_KEY = "key";
    public final static String PARAM_PHONE_NUM = "num";
    public final static String BROADCAST_ACTION = "com.aparnyuk.rsn.receiver.BROADCAST";

    // check if service is running (need for stop/start service)
    public static boolean state = true;

    //!! delete this when save password in pref or organize some offline auth
    public static boolean no_data = true;

    // not use for now
    SharedPreferences sp;

    final String TAG = "Annet";

    // short reference to node for more easy reading/changing info in firebse
    private Firebase sms_ref;
    private Firebase call_ref;
    private Firebase remind_ref;
    private Firebase ref;

    private ChildEventListener smsListener;
    private ChildEventListener callListener;
    private ChildEventListener remindListener;

    private Timer mTimer;

    //need for getting id from task to notification
    private int taskID;

    // set a unique id for every timerTask
    // needs for canceling it outside this task
    HashMap<String, TimerTask> tasksMap;
    Map<String, TimerTask> syncTasksMap;

    NotificationManager nm;
    BroadcastReceiver notificationReceiver;
    BroadcastReceiver deliverReceiver;
    BroadcastReceiver sendReceiver;

    public MainService() {
    }

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        notificationBroadcastReceiver();
        sendBroadcastReceiver();
        deliverBroadcastReceiver();

        state = false;

        mTimer = new Timer();
        taskID = 0;

        tasksMap = new HashMap<>();
        syncTasksMap = Collections.synchronizedMap(tasksMap);

        nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

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
            //!!
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
                deleteTask(snapshot);
                createTask(snapshot);
            }

            // Get the data on a tasks  that has been removed
            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                Log.d(TAG, "Remove data from database");
                deleteTask(snapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                // doing nothing
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d(TAG, "Some error happened");
                createTimer();
            }
        };
        ref.addChildEventListener(listener);
        return listener;
    }

    private void deleteTask(DataSnapshot snapshot) {
        // delete notification if task not finished, but deleted
        if (syncTasksMap.containsKey(snapshot.getKey())) {
            switch (snapshot.getRef().getParent().getKey()) {
                case "sms": {
                    SmsTimerTask sms = (SmsTimerTask) syncTasksMap.get(snapshot.getKey());
                    if (sms.count != sms.stop) {
                        nm.cancel(sms.id);
                    }
                    Log.d(TAG, "Delete Sms TimerTask. Task ID = " + sms.id);
                    break;
                }
                case "call": {
                    CallTimerTask call = (CallTimerTask) syncTasksMap.get(snapshot.getKey());
                    Log.d(TAG, "Delete Call TimerTask. Task ID = " + call.id);
                    if (call.count != call.stop) {
                        nm.cancel(call.id);
                    }
                    break;
                }
                case "remind": {
                    RemindTimerTask remind = (RemindTimerTask) syncTasksMap.get(snapshot.getKey());
                    if (remind.count != remind.stop) {
                        nm.cancel(remind.id);
                    }
                    Log.d(TAG, "Delete Remind TimerTask. Task ID = " + remind.id);
                    break;
                }
            }

            String key = snapshot.getKey();

            // delete task from Timer and array of active tasks
            syncTasksMap.get(key).cancel();
            mTimer.purge();
            syncTasksMap.remove(key);
        }
    }

    // create new Timer Task and add it into Timer schedule
    private void createTask(DataSnapshot snapshot) {
        if (taskID < 32767) { //2147483647
            switch (snapshot.getRef().getParent().getKey()) {
                case "sms": {
                    Sms sms = snapshot.getValue(Sms.class);
                    if (sms.isOpen()) {
                        String logs = "";
                        // создаём новую задачу и помещаем её в массив активних задач
                        syncTasksMap.put(snapshot.getKey(), new SmsTimerTask(sms, snapshot.getKey(), taskID));
                        // если есть уведомление перед отравкой, создаётся задача со временем на notificationTime раньше
                        // из временем повторения - notificationTime
                        // задача отработает два раза: 1-й - уведомление, второй - отправка смс.
                        // в случае если задача повторяющаяся - созздастся новая задача
                        // путём изменения в базе значения repeatCount на repeatCount - 1.
                        // !! поверка корректности заданых Date, notificationTime и repeatCount должна просиходить при вводе
                        if (sms.isNotificationBefore()) {
                            logs = logs + " with notification before.";
                            Date notifDate = sms.getDate();
                            notifDate.setTime(notifDate.getTime() - sms.getNotificationTime());
                            mTimer.schedule(syncTasksMap.get(snapshot.getKey()), notifDate, sms.getNotificationTime());
                        } else {
                            if (sms.getRepeatPeriod() == 0) {
                                mTimer.schedule(syncTasksMap.get(snapshot.getKey()), sms.getDate());

                            } else {
                                mTimer.schedule(syncTasksMap.get(snapshot.getKey()), sms.getDate(), sms.getRepeatPeriod());
                            }
                        }
                        Log.d(TAG, "Add Sms TimerTask. " + logs + " Task ID = " + taskID);
                        taskID++;
                    }
                    break;
                }
                case "call": {
                    Calls call = snapshot.getValue(Calls.class);
                    if (call.isOpen()) {
                        //    tasks.put(taskID, new CallTimerTask(call));
                        syncTasksMap.put(snapshot.getKey(), new CallTimerTask(call, snapshot.getKey(), taskID));
                        if (call.getRepeatPeriod() == 0) {
                            mTimer.schedule(syncTasksMap.get(snapshot.getKey()), call.getDate());
                            // mTimer.schedule(tasks.get(taskID), remind.getDate());
                        } else {
                            mTimer.schedule(syncTasksMap.get(snapshot.getKey()), call.getDate(), call.getRepeatPeriod());
                            //mTimer.schedule(tasks.get(taskID), remind.getDate(), remind.getRepeatPeriod());
                        }
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

    /******************************
     * CREATE TIMER TASKS
     ********************************************/
    class SmsTimerTask extends TimerTask {
        private Sms sms;
        String key;
        int id;
        int count;
        int stop;
        boolean runWithNotify;

        public Sms getSms() {
            return this.sms;
        }

        public SmsTimerTask(Sms smsTask, String key, int id) {
            this.sms = smsTask;
            this.key = key;
            this.id = id;
            this.runWithNotify = sms.isNotificationBefore();
            this.count = 0;
            this.stop = sms.getRepeatCount();
        }

        @Override
        public void run() {
            String logs = " Task ID =  " + id + " " + " count - " + count + " stop when - " + (stop);//+ " Text: " + sms.getText();
            // Log.d(TAG, "Start Sms TimerTask. " + logs);
            // Если необходимо сделать уведомление до отправки
            if (sms.isNotificationBefore()) {// correct data/time must be checked in create dialog.
                Log.d(TAG, "Start Sms TimerTask. Show notification. " + logs);
                if (runWithNotify) {
                    int delay = sms.getNotificationTime() / 1000; // /60
                    String notifText = "This sms [ " + sms.getText() + " ] will be sent in " + delay + " sec.";
                    smsNotificationBefore(notifText, key, id);
                    runWithNotify = false;
                } else {
                    Log.d(TAG, "Start Sms TimerTask. Sending sms. " + logs);
                    sendSms(this.sms, this.id);

                    if (count == stop) {
                        Log.d(TAG, "Stop Sms TimerTask. Task ID =  " + id);
                        // удалить эл. из базы или установить isOpen=false
                        // tasks.remove(this);
                        sms_ref.child(key).child("open").setValue(false);
                    } else {
                        this.sms.setRepeatCount(this.sms.getRepeatCount() - 1);
                        Date newDate = new Date(this.sms.getDate().getTime() + this.sms.getRepeatPeriod());
                        this.sms.setDate(newDate);
                        sms_ref.child(key).setValue(this.sms);
                    }
                }
            } else {
                Log.d(TAG, "Start Sms TimerTask. Without notification. " + logs);
                sendSms(this.sms, this.id);
                //smsNotification(, key, id);
                if (count == stop) {
                    Log.d(TAG, "Stop Sms TimerTask. Task ID =  " + id);
                    // удалить эл. из базы или установить isOpen=false
                    // tasks.remove(this);
                    sms_ref.child(key).child("open").setValue(false);
                } else {
                    count++;
                }
            }
        }
    }

    class CallTimerTask extends TimerTask {
        private Calls call;
        int id;
        int count;
        int stop;
        String key;

        public CallTimerTask(Calls callTask, String key, int id) {
            this.call = callTask;
            this.count = 0;
            this.stop = call.getRepeatCount();
            this.key = key;
            this.id = id;
        }

        public Calls getCall() {
            return call;
        }

        @Override
        public void run() {
            Log.d(TAG, "Start Call TimerTask. Task ID =  " + id + " " + call.getText() + " count - " + count + " stop when - " + (stop));
            callNotification(call.getText() + "Task id " + id + " " + " repeat count - " + count + " stop when - " + (stop), key, id);
            if (count == stop) {
                Log.d(TAG, "Stop CallTimerTask. Task ID =  " + id);
                // удалить эл. из базы или установить isOpen=false
                // tasks.remove(this);
               /* syncTasksMap.remove(key);*/
                //remind_ref.child(key).removeValue();
                call_ref.child(key).child("open").setValue(false);
            } else {
                count++;
            }
        }
    }

    class RemindTimerTask extends TimerTask {
        private Remind remind;
        String key;
        int id;
        int count;
        int stop;

        public RemindTimerTask(Remind remindTask, String key, int id) {
            this.remind = remindTask;
            this.key = key;
            this.id = id;
            this.count = 0;
            this.stop = remind.getRepeatCount();
        }

        public Remind getRemind() {
            return remind;
        }

        @Override
        public void run() {
            Log.d(TAG, "Start Remind TimerTask. Task ID =  " + id + " " + remind.getText() + " count - " + count + " stop when - " + (stop));
            remindNotification(remind.getText() + "Task id " + id + " " + " repeat count - " + count + " stop when - " + (stop), key, id);

            if (count == stop) {
                Log.d(TAG, "Stop Remind TimerTask. Task ID =  " + id);
                // удалить эл. из базы или установить isOpen=false
               /* syncTasksMap.remove(key);*/
                //remind_ref.child(key).removeValue();
                remind_ref.child(key).child("open").setValue(false);
               /* this.cancel();
                mTimer.purge();*/
                //nm.cancel(id);
            } else {
                count++;
            }
        }

    }

    /**********************
     * CREATE AND REGISTER BROADCAST RECEIVERS
     ********************************/
    private void notificationBroadcastReceiver() {
        notificationReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int status = intent.getIntExtra(PARAM_STATUS, 0);
                int id = intent.getIntExtra(PARAM_ID, -1);
                String key = intent.getStringExtra(PARAM_TASK_KEY);
                //int result = intent.getIntExtra(PARAM_RESULT, -1);

                if (status == STATUS_SMS_CANCEL) {
                    //sms_ref.child(key).removeValue();
                    if (syncTasksMap.containsKey(key)) {
                        sms_ref.child(key).child("open").setValue(false);
                    }
                    nm.cancel(id);
                    Toast.makeText(getApplicationContext(), getString(R.string.cancel_notification_send), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "in receiver - cancel sms");
                }
                if (status == STATUS_CALL_CANCEL) {
                    //sms_ref.child(key).removeValue();
                    if (syncTasksMap.containsKey(key)) {
                        call_ref.child(key).child("open").setValue(false);
                    }
                    nm.cancel(id);
                    Toast.makeText(getApplicationContext(), getString(R.string.cancel_notification_call), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "in receiver - cancel call");
                }
                if (status == STATUS_REMIND_CANCEL) {
                    //sms_ref.child(key).removeValue();
                    if (syncTasksMap.containsKey(key)) {
                        remind_ref.child(key).child("open").setValue(false);
                    }
                    nm.cancel(id);
                    Toast.makeText(getApplicationContext(), getString(R.string.cancel_notification_remind), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "in receiver - cancel remind");
                }
                if (status == STATUS_SEND) {
                    Toast.makeText(getApplicationContext(), getString(R.string.send_notification_button), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "in receiver - action send for sms");
                    //SmsTimerTask smsTimerTask = (SmsTimerTask) syncTasksMap.get(key);
                    //sendSms(smsTimerTask.getSms());
                    // sms_ref.child(key).child("date").setValue(new Date());

                    SmsTimerTask task = (SmsTimerTask) syncTasksMap.get(key);
                    Log.d(TAG, "Start Sms TimerTask. Sending sms. ");
                    sendSms(task.getSms(), id);
                    if (task.getSms().getRepeatCount() == 0) {
                        Log.d(TAG, "Stop Sms TimerTask. Task ID =  " + id);
                        // удалить эл. из базы или установить isOpen=false
                        // tasks.remove(this);
                        sms_ref.child(key).child("open").setValue(false);
                    } else {
                        task.getSms().setRepeatCount(task.getSms().getRepeatCount() - 1);
                        Date newDate = new Date(task.getSms().getDate().getTime() + task.getSms().getRepeatPeriod());
                        task.getSms().setDate(newDate);
                        sms_ref.child(key).setValue(task.getSms());
                    }


                }
                if (status == STATUS_CALL) {
                    Toast.makeText(getApplicationContext(), getString(R.string.call_notification_button), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "in receiver - action call for call");
                    // CallTimerTask callTimerTask = (CallTimerTask)syncTasksMap.get(key);
                    String oneNum = intent.getStringExtra(PARAM_PHONE_NUM);
                    Log.d(TAG, " Numb:" + oneNum);
                    Intent actionDialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + oneNum));
                    actionDialIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(actionDialIntent);
                    Log.d(TAG, "open call");
                    if (syncTasksMap.containsKey(key)) {
                        call_ref.child(key).child("open").setValue(false);
                    }
                    nm.cancel(id);

                }
                if (status == STATUS_LATER) {
                    Toast.makeText(getApplicationContext(), getString(R.string.later_notification_button), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "in receiver - action later for remind");
                    nm.cancel(id);
                    // !!! просто закрыть уведомление, остальное напомниться автоматически
                }
            }
        };
        this.registerReceiver(notificationReceiver, new IntentFilter(BROADCAST_ACTION));
    }

    private void sendBroadcastReceiver() {
        sendReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                String smsText = intent.getStringExtra(PARAM_SMS_TEXT);
                int id = intent.getIntExtra(PARAM_ID, -1);

                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        if (smsText != null) {
                            smsNotificationInfo(smsText + getString(R.string.was_sent_successful), id, false);
                            Log.d(TAG, smsText + getString(R.string.was_sent_successful));
                        }
                        break;
                    //Something went wrong and there's no way to tell what, why or how
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        smsNotificationInfo(getString(R.string.some_error_happened) + smsText + getString(R.string.wasnt_sent), id, false);
                        Log.d(TAG, "GENERIC FAILURE");
                        break;
                    //Failed because service is currently unavailable
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        smsNotificationInfo(getString(R.string.service_is_unavailable) + smsText + getString(R.string.wasnt_sent), id, false);
                        Log.d(TAG, "NO SERVICE");
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        smsNotificationInfo(getString(R.string.some_error_happened) + smsText + getString(R.string.wasnt_sent), id, false);
                        Log.d(TAG, "Null PDU");
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        smsNotificationInfo(getString(R.string.phone_module_is_off) + smsText + getString(R.string.wasnt_sent), id, false);
                        Log.d(TAG, "Radio Off");
                        break;
                    default:
                        // sent SMS message failed
                        smsNotificationInfo(getString(R.string.some_error_happened) + smsText + getString(R.string.wasnt_sent), id, false);
                        Log.d(TAG, "Something went wrong during sms was sending!");
                        break;
                }
            }
            // показать уведомление об успехе если есть в настройках и о неудачи
            // о неудачи предложить отменить отправку, выбрать новое время??, или отправить сейчас.
            // не удалять задачу
            // как выбрать новое время = открыть активити и сразу запустить диалог??
        };
        registerReceiver(sendReceiver, new IntentFilter(SEND_SMS_FLAG));
    }

    private void deliverBroadcastReceiver() {
        deliverReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent in) {
                // SMS delivered actions
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        // sent SMS message successfully;
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Сообщение доставлено!", Toast.LENGTH_SHORT);
                        toast.show();
                        Log.d(TAG, "Сообщение доставлено!");
                        break;
                    default:
                        // sent SMS message failed
                        Log.d(TAG, "Something wrong!");
                        break;
                }
            }
        };
        registerReceiver(deliverReceiver, new IntentFilter(DELIVER_SMS_FLAG));
    }


    /******************************
     * CREATE NOTIFICATION
     *******************************************/
    public void remindNotification(String text, String key, int id) {

        Intent cancelIntent = new Intent(MainService.BROADCAST_ACTION)
                .putExtra(MainService.PARAM_STATUS, MainService.STATUS_REMIND_CANCEL)
                .putExtra(MainService.PARAM_ID, id)
                .putExtra(MainService.PARAM_TASK_KEY, key);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent actionIntent = new Intent(MainService.BROADCAST_ACTION)
                .putExtra(MainService.PARAM_STATUS, MainService.STATUS_LATER)
                .putExtra(MainService.PARAM_ID, id)
                .putExtra(MainService.PARAM_TASK_KEY, key);
        PendingIntent actionPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 2, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent appIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());

        builder
                .setContentIntent(appPendingIntent)
                .setSmallIcon(R.drawable.telegram)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_alarm_check_grey600_48dp))
                .setTicker(getString(R.string.remind_notification_ticker))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.app_name) + getString(R.string.remind_notification_text))
                .setContentText(text)
                .setPriority(Notification.PRIORITY_HIGH)
                .setFullScreenIntent(appPendingIntent, true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text));

        RemindTimerTask remindTimerTask = (RemindTimerTask) syncTasksMap.get(key);
        if (remindTimerTask.getRemind().getRepeatCount() > 0) {
            builder
                    .addAction(R.drawable.ic_close_grey600_18dp, getString(R.string.cancel_notification_button), cancelPendingIntent)
                    .addAction(R.drawable.ic_alarm_grey600_18dp, getString(R.string.later_notification_button), actionPendingIntent);
        }

        Notification notification = builder.build();

        notification.vibrate = new long[]{1000, 1000, 1000, 1000, 1000, 1000};

        nm.notify(id, notification);
    }

    public void smsNotificationBefore(String text, String key, int id) {

        Intent scancelIntent = new Intent(MainService.BROADCAST_ACTION)
                .putExtra(MainService.PARAM_STATUS, MainService.STATUS_SMS_CANCEL)
                .putExtra(MainService.PARAM_ID, id)
                .putExtra(MainService.PARAM_TASK_KEY, key);
        PendingIntent scancelPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 3, scancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent sactionIntent = new Intent(MainService.BROADCAST_ACTION)
                .putExtra(MainService.PARAM_STATUS, MainService.STATUS_SEND)
                .putExtra(MainService.PARAM_ID, id)
                .putExtra(MainService.PARAM_TASK_KEY, key);
        PendingIntent sactionPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 4, sactionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent appIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());

        builder
                .setContentIntent(appPendingIntent)
                .setSmallIcon(R.drawable.telegram)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_email_outline_grey600_48dp))
                .setTicker(getString(R.string.sms_notification_ticker_will))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.app_name) + getString(R.string.sms_notification_text))
                .setContentText(text)
                .addAction(R.drawable.ic_close_grey600_18dp, getString(R.string.cancel_notification_button), scancelPendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setFullScreenIntent(appPendingIntent, false)
                .addAction(R.drawable.ic_send_grey600_18dp, getString(R.string.send_notification_button), sactionPendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        Notification notification = builder.build();

        notification.vibrate = new long[]{1000, 1000, 1000, 1000, 1000, 1000 };

        nm.notify(id, notification);
    }

    public void smsNotificationInfo(String text, int id, boolean delivered) {

        Intent appIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());

        builder
                .setContentIntent(appPendingIntent)
                .setSmallIcon(R.drawable.telegram)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_send_grey600_48dp))
                .setTicker(getString(R.string.sms_notification_ticker_send))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.app_name) + getString(R.string.sms_notification_text))
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        if (delivered) {
            builder
                    .setTicker(getString(R.string.sms_notification_ticker_delivered))
                    .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_email_outline_grey600_48dp));
        }
        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
        notification.flags = notification.flags | Notification.FLAG_INSISTENT;
        nm.notify(id, notification);
    }

    public void callNotification(String text, String key, int id) {
        Intent cancelIntent = new Intent(MainService.BROADCAST_ACTION)
                .putExtra(MainService.PARAM_STATUS, MainService.STATUS_CALL_CANCEL)
                .putExtra(MainService.PARAM_ID, id)
                .putExtra(MainService.PARAM_TASK_KEY, key);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 5, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        CallTimerTask callTask = (CallTimerTask) syncTasksMap.get(key);
        String num = callTask.getCall().getNumbers().get(0);
        Intent actionIntent = new Intent(MainService.BROADCAST_ACTION)
                .putExtra(MainService.PARAM_STATUS, MainService.STATUS_CALL)
                .putExtra(MainService.PARAM_ID, id)
                .putExtra(MainService.PARAM_TASK_KEY, key)
                .putExtra(MainService.PARAM_PHONE_NUM, num);
        PendingIntent actionPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 6, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent appIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder
                .setContentIntent(appPendingIntent)
                .setSmallIcon(R.drawable.telegram)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_phone_grey600_48dp))
                .setTicker(getString(R.string.call_notification_ticker))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.app_name) + getString(R.string.call_notification_text))
                .setContentText(text)
                .setPriority(Notification.PRIORITY_HIGH)
                .setFullScreenIntent(appPendingIntent, true)
                .addAction(R.drawable.ic_close_grey600_18dp, getString(R.string.cancel_notification_button), cancelPendingIntent)
                .addAction(R.drawable.ic_phone_grey600_18dp, getString(R.string.call_notification_button), actionPendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        Notification notification = builder.build();

        notification.vibrate = new long[]{1000, 1000, 1000, 1000, 1000, 1000};

        nm.notify(id, notification);
    }


    /****************************************
     * SEND SMS
     *******************************************/
    private void sendSms(Sms sms, int id) {
        String notificationText = getString(R.string.sms_to_numbers);
        for (String num : sms.getNumbers()) {
            notificationText = notificationText + num + " ";
        }
        notificationText = notificationText + getString(R.string.with_text) + sms.getText() + getString(R.string.end_of_with_text);
        Intent sendIntent = new Intent(SEND_SMS_FLAG)
                .putExtra(MainService.PARAM_SMS_TEXT, notificationText)
                .putExtra(MainService.PARAM_ID, id);
        PendingIntent sendPendingIntent = PendingIntent.getBroadcast(this, 77,
                sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent deliverIntent = new Intent(SEND_SMS_FLAG);
        PendingIntent deliverPendingIntent = PendingIntent.getBroadcast(this, 88,
                deliverIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        SmsManager smsManager = SmsManager.getDefault();
        for (String num : sms.getNumbers()) {
            //!! проверка номера телефона
            // разбивка длинного сообщения
            Log.d(TAG, "Send sms on number " + num);
            if (textLength(sms.getText()) < 140) {
                smsManager.sendTextMessage(num, null, sms.getText(), sendPendingIntent, deliverPendingIntent);
            } else {
                Log.d(TAG, "Long message");
                ArrayList<String> messageArray = smsManager.divideMessage(sms.getText());
                ArrayList<PendingIntent> sendIntents = new ArrayList<>();
                for (int i = 0; i < messageArray.size(); i++) {
                    Log.d(TAG, "part" + i);
                    sendIntents.add(sendPendingIntent);
                }
                smsManager.sendMultipartTextMessage(num, null, messageArray, sendIntents, null);
            }

        }
    }

    private int textLength(String str) {
        byte[] utf8;
        int byteCount = 0;
        try {
            utf8 = str.getBytes("UTF-8");
            byteCount = utf8.length;
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        return byteCount;
    }

    /*************************************
     * OTHER
     **************************************************/
    public void onDestroy() {

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (!tasksMap.isEmpty()) {
            tasksMap.clear();
        }

        if (!no_data) {
            sms_ref.removeEventListener(smsListener);
            call_ref.removeEventListener(callListener);
            remind_ref.removeEventListener(remindListener);
        }
        Log.d(TAG, "Destroy service");
        this.unregisterReceiver(notificationReceiver);
        this.unregisterReceiver(sendReceiver);
        this.unregisterReceiver(deliverReceiver);
        super.onDestroy();
        state = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}



