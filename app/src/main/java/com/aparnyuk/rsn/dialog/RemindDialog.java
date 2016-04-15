package com.aparnyuk.rsn.dialog;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.aparnyuk.rsn.Utils.Constants;
import com.aparnyuk.rsn.R;
import com.aparnyuk.rsn.model.Remind;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class RemindDialog extends DialogFragment {
    private EditText nameView, dateView, timeView;
    private Spinner repeatRemindSpinner, quantityRemindSpinner;
    private int day, month, year, hour, minute;
    private int day_x, month_x, year_x, hour_x, minute_x;
    private static TimePickerDialog timePicker;
    private static DatePickerDialog datePicker;
    private static Button okButton, cancelButton;
    public String remindPosition = null;
    Firebase base = new Firebase(Constants.FIREBASE_URL);

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            remindPosition = getArguments().getString("remindPosition");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.remind_dialog, null);

        nameView = (EditText) view.findViewById(R.id.remind_dialog_text);
        nameView.addTextChangedListener(titleWatcher);

        dateView = (EditText) view.findViewById(R.id.remind_date);
        timeView = (EditText) view.findViewById(R.id.remind_time);

        final Date date = new Date();
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        DateTime dateTime = new DateTime(date);
        LocalDateTime localDateTime = dateTime.toLocalDateTime();

        day = localDateTime.getDayOfMonth();
        month = localDateTime.getMonthOfYear();
        year = localDateTime.getYear();
        hour = localDateTime.getHourOfDay();
        minute = localDateTime.getMinuteOfHour();


        datePicker = new DatePickerDialog(getActivity(), myDateListener, year, month - 1, day);
        timePicker = new TimePickerDialog(getActivity(), myTimeListener, hour, minute, true);
        showDate(year, month, day);
        showTime(hour, minute);


        //Set OnClickListener on Date and Time
        dateView.setOnClickListener(new View.OnClickListener() {
            public void setDatePicker(DatePickerDialog mDatePicker) {
                final Calendar calendar = Calendar.getInstance();
                mDatePicker = datePicker;
                calendar.set(year, month, day);
                mDatePicker.getDatePicker().setCalendarViewShown(false);
                mDatePicker.show();
            }

            @Override
            public void onClick(View v) {
                setDatePicker(datePicker);
            }
        });

        timeView.setOnClickListener(new View.OnClickListener() {
            public void setTimePicker(TimePickerDialog mTimePicker) {
                mTimePicker = timePicker;
                mTimePicker.show();
            }

            @Override
            public void onClick(View v) {
                setTimePicker(timePicker);
            }
        });

        //Show Spinners
        repeatRemindSpinner = (Spinner) view.findViewById(R.id.repeatRemindSpinner);
        ArrayAdapter<CharSequence> arrayRepeatAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.repeat, android.R.layout.simple_spinner_dropdown_item);
        arrayRepeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeatRemindSpinner.setAdapter(arrayRepeatAdapter);

        quantityRemindSpinner = (Spinner) view.findViewById(R.id.quantityRemindSpinner);
        ArrayAdapter<CharSequence> arrayQuantityAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.quantity, android.R.layout.simple_spinner_dropdown_item);
        arrayRepeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        quantityRemindSpinner.setAdapter(arrayQuantityAdapter);


        builder.setTitle(R.string.dialog_create_remind)
                .setView(view)
                .setPositiveButton(R.string.button_ok, null)
                .setNegativeButton(R.string.button_cancel, null);

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                if (nameView.getText().toString().isEmpty()) {
                    okButton.setEnabled(false);
                }
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        GregorianCalendar gregorianCalendar = new GregorianCalendar(year_x, month_x, day_x, hour_x, minute_x);
                        Date date1 = gregorianCalendar.getTime();
                        if (date1.after(date)) {
                            Remind remind = new Remind(nameView.getText().toString(), date1);

                            // !!
                            Firebase base = new Firebase(Constants.FIREBASE_URL);
                            AuthData authData = base.getAuth();
                            if (authData != null) {
                                base = base.child(authData.getUid());
                            }
                            if (remindPosition == null){
                                base.child("remind").push().setValue(remind);
                            } else {
                                Firebase resf = base.child("remind").child(remindPosition);
                                resf.setValue(remind);
                            }

                            // !!

                            dialog.dismiss();
                        } else {
                            Toast.makeText(getContext(), "Choose correct date", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });
        return dialog;
    }


    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int day, int month, int year) {
            showDate(day, month + 1, year);
        }
    };

    private TimePickerDialog.OnTimeSetListener myTimeListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hour, int minute) {
            showTime(hour, minute);
        }
    };

    private void showDate(int day, int month, int year) {
        day_x = year;
        month_x = month - 1;
        year_x = day;
        dateView.setText(new StringBuilder().append(year).append("/")
                .append(month).append("/").append(day));
    }

    public void showTime(int hour, int minute) {
        hour_x = hour;
        minute_x = minute;
        String mHour = "00";
        if (hour < 10) {
            mHour = "0" + hour;
        } else {
            mHour = String.valueOf(hour);
        }

        String mMinute = "00";
        if (minute < 10) {
            mMinute = "0" + minute;
        } else {
            mMinute = String.valueOf(minute);
        }
        timeView.setText(new StringBuilder().append(mHour)
                .append(":").append(mMinute));
    }


    private final TextWatcher titleWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() < 3) {
                if (okButton.isEnabled()) {
                    okButton.setEnabled(false);
                }
            } else {
                if (!okButton.isEnabled()) {
                    okButton.setEnabled(true);
                }
                nameView.setError(null);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
}
