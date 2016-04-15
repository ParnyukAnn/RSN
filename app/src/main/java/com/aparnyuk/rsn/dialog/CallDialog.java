package com.aparnyuk.rsn.dialog;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
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

import com.aparnyuk.rsn.Utils.Contact;
import com.aparnyuk.rsn.Utils.Constants;
import com.aparnyuk.rsn.activity.MultipleContactPickerActivity;
import com.aparnyuk.rsn.R;
import com.aparnyuk.rsn.model.Calls;
import com.aparnyuk.rsn.model.Sim;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class CallDialog extends DialogFragment {
    private EditText nameView, dateView, timeView, callNumberText;
    private Spinner repeatSpinner, quantitySpinner;
    private int day, month, year, hour, minute;
    private static TimePickerDialog timePicker;
    private int day_x, month_x, year_x, hour_x, minute_x;
    private static DatePickerDialog datePicker;
    private static Button okButton, cancelButton;
    private Button buttonContacts;
    private final int CONTACT_PICKER_REQUEST = 135;
    private final int PICK_CONTACT = 1325;
    public String callPosition = null;
    Firebase base = new Firebase(Constants.FIREBASE_URL);


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            callPosition = getArguments().getString("callPosition");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.call_dialog, null);

        nameView = (EditText) view.findViewById(R.id.call_dialog_text);
        nameView.addTextChangedListener(titleWatcher);

        dateView = (EditText) view.findViewById(R.id.event_date);
        timeView = (EditText) view.findViewById(R.id.event_time);
        buttonContacts = (Button) view.findViewById(R.id.buttonContacts);
        callNumberText = (EditText) view.findViewById(R.id.call_number_text);

        buttonContacts.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent contactPickerIntent = new Intent(getActivity(), MultipleContactPickerActivity.class);
                getActivity().startActivityForResult(contactPickerIntent, CONTACT_PICKER_REQUEST);
            }
        });

        Date date = new Date();
        TimeZone timeZone = TimeZone.getDefault();
        int offset = timeZone.getOffset(date.getTime()) / (60 * 60 * 1000);

        DateTime dateTime = new DateTime(date);
        LocalDateTime localDateTime = dateTime.toLocalDateTime();

        day = localDateTime.getDayOfMonth();
        month = localDateTime.getMonthOfYear();
        year = localDateTime.getYear();
        hour = localDateTime.getHourOfDay() + offset;
        minute = localDateTime.getMinuteOfHour();

        datePicker = new DatePickerDialog(getActivity(), myDateListener, year, month, day);
        timePicker = new TimePickerDialog(getActivity(), myTimeListener, hour, minute, true);
        showDate(year, month, day);
        showTime(hour, minute);

        dateView.setOnClickListener(new View.OnClickListener() {

            public void setDatePicker(DatePickerDialog mDatePicker) {
                final Calendar calendar = Calendar.getInstance();
                mDatePicker = datePicker;
                mDatePicker.getDatePicker().setMinDate(calendar.getTimeInMillis());
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

        repeatSpinner = (Spinner) view.findViewById(R.id.repeatCallSpinner);
        ArrayAdapter<CharSequence> arrayRepeatAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.repeat, android.R.layout.simple_spinner_dropdown_item);
        arrayRepeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeatSpinner.setAdapter(arrayRepeatAdapter);


        builder.setTitle(R.string.dialog_create_call)
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
                        ArrayList<String> phoneNumbers = new ArrayList<>();
                        phoneNumbers.add("8947839534");
                        phoneNumbers.add("5487983721");
                        Sim sim = new Sim("sim 1", "phone 2");
                        Calls call = new Calls(phoneNumbers, sim, date1);
                        Firebase base = new Firebase(Constants.FIREBASE_URL);
                        AuthData authData = base.getAuth();
                        if (authData != null) {
                            base = base.child(authData.getUid());
                        }
                        if (callPosition == null){
                            base.child("call").push().setValue(call);
                        } else {
                            Firebase resf = base.child("call").child(callPosition);
                            resf.setValue(call);
                        }
                        dialog.dismiss();
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

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int day, int month, int year) {
            showDate(day, month, year);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ArrayList<Contact> contacts = new ArrayList<Contact>();
        contacts = data.getParcelableArrayListExtra("contacts");
        StringBuilder stringBuilder = new StringBuilder();
        for (Contact c : contacts) {
            stringBuilder.append(c.getNumber() + "; ");
        }
        callNumberText.setText(stringBuilder);

    }
}