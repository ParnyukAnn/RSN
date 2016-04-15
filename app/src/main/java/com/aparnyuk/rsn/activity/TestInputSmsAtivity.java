package com.aparnyuk.rsn.activity;

import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.aparnyuk.rsn.R;
import com.aparnyuk.rsn.Utils.Constants;
import com.aparnyuk.rsn.model.Sim;
import com.aparnyuk.rsn.model.Sms;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;

import java.util.ArrayList;
import java.util.Date;

public class TestInputSmsAtivity extends AppCompatActivity {
    ArrayList<String> phoneNumbers = new ArrayList<>();
    EditText delayBeforeStart, repeatCount, notifyBefore, phoneNum, repeatPeriod, text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_input_sms_ativity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        repeatCount = (EditText) findViewById(R.id.test_repeat_count);
        delayBeforeStart = (EditText) findViewById(R.id.test_delay_before_start);
        notifyBefore = (EditText) findViewById(R.id.test_notify_before);
        phoneNum = (EditText) findViewById(R.id.test_phone_num);
        repeatPeriod = (EditText) findViewById(R.id.test_repeat_period);
        text = (EditText) findViewById(R.id.test_sms_text);

        phoneNumbers.add(phoneNum.getText().toString());
        Button add = (Button) findViewById(R.id.add_sms_button);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phoneNumbers.add(phoneNum.getText().toString());
            }
        });
        Button infoText = (Button) findViewById(R.id.test_text_info);
        infoText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String txt = "Numbers:";
                for (String num : phoneNumbers) {
                    txt = txt + num + " ";
                }
                txt = txt + ".Dealay:" + delayBeforeStart.getText().toString()
                        + ".Repeat:" + repeatCount.getText().toString()
                        + ".Interval:" + repeatPeriod.getText().toString()
                        + "sec.Notif.before:" + notifyBefore.getText().toString();
                text.setText(txt);
            }
        });
        Button longText = (Button) findViewById(R.id.test_add_long_text);
        longText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                text.setText("Проверка отправки длинного сообщения. В этом сообщении 79 символов. Удачи мне!!");
            }
        });
        Button send = (Button) findViewById(R.id.send_button);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Date oldDate = new Date(); // oldDate == current time
                Date newDate = new Date(oldDate.getTime() + Integer.parseInt(delayBeforeStart.getText().toString()) * 1000 + Integer.parseInt(notifyBefore.getText().toString()) * 1000);

                //  phoneNumbers.add("0989580367");
                //phoneNumbers.add("0964460071");

                Sim sim = new Sim("sim 1", "phone 2");
                Sms sms = new Sms(phoneNumbers, sim, text.getText().toString(), newDate);

                sms.setRepeatCount(Integer.parseInt(repeatCount.getText().toString()));
                sms.setRepeatPeriod(Integer.parseInt(repeatPeriod.getText().toString()) * 1000);

                if (Integer.parseInt(notifyBefore.getText().toString()) == 0) {
                    sms.setNotificationBefore(false);
                } else {
                    sms.setNotificationBefore(true);
                    sms.setNotificationTime(Integer.parseInt(notifyBefore.getText().toString()) * 1000);
                }


                if (sms.getRepeatPeriod() > sms.getNotificationTime()) {
                    Firebase base = new Firebase(Constants.FIREBASE_URL);
                    AuthData authData = base.getAuth();
                    if (authData != null) {
                        base = base.child(authData.getUid());
                    }
                    base.child("sms").push().setValue(sms);

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Промежуток между отправкой смс должен быть больше времени предварительного уведомления", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
