package com.aparnyuk.rsn.fragment.dialog;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.aparnyuk.rsn.R;
import com.aparnyuk.rsn.Utils.Constants;
import com.aparnyuk.rsn.activity.MultipleContactPickerActivity;
import com.aparnyuk.rsn.model.Sim;
import com.aparnyuk.rsn.model.Sms;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class SmsDialog extends DialogFragment {


    private EditText nameView, dateView, timeView, smsNumberText;
    private Spinner repeatSpinner, noticeSpinner;
    private int day, month, year, hour, minute;
    private static TimePickerDialog timePicker;
    private static DatePickerDialog datePicker;
    private static Button okButton, cancelButton;
    private Button buttonContacts;
    //    static final int SELECT_CONTACT_SUCCESS_RESULT = 101;
    private final int CONTACT_PICKER_REQUEST = 1350;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.sms_dialog, null);

        nameView = (EditText) view.findViewById(R.id.sms_dialog_text);
        nameView.addTextChangedListener(titleWatcher);

        dateView = (EditText) view.findViewById(R.id.event_date);
        timeView = (EditText) view.findViewById(R.id.event_time);
        buttonContacts = (Button) view.findViewById(R.id.buttonContacts);
        smsNumberText = (EditText) view.findViewById(R.id.sms_number_text);

        buttonContacts.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                // Perform action on click
//
//                // Create a new intent for choosing a contact
//                // http://stackoverflow.com/questions/9496350/pick-a-number-and-name-from-contacts-list-in-android-app
//                Intent contactPickerIntent = new Intent (Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
//                contactPickerIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE); //(Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
//                // Start the contact picker expecting a result with the resultCode '101'
//                //StartActivityForResult (contactPickerIntent, SELECT_CONTACT_SUCCESS_RESULT);
//                startActivityForResult(contactPickerIntent, SELECT_CONTACT_SUCCESS_RESULT);

                //1 Contact
//                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
//                Intent intent1 = new Intent();
//                startActivityForResult(intent, PICK_CONTACT);

                Intent contactPickerIntent = new Intent(getContext(), MultipleContactPickerActivity.class);
                startActivityForResult(contactPickerIntent, CONTACT_PICKER_REQUEST);
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

        repeatSpinner = (Spinner) view.findViewById(R.id.repeatSmsSpinner);
        ArrayAdapter<CharSequence> arrayRepeatAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.repeat, android.R.layout.simple_spinner_dropdown_item);
        arrayRepeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeatSpinner.setAdapter(arrayRepeatAdapter);

        noticeSpinner = (Spinner) view.findViewById(R.id.noticeSmsSpinner);
        ArrayAdapter<CharSequence> arrayRepeatAdapter1 = ArrayAdapter.createFromResource(getActivity(),
                R.array.repeat, android.R.layout.simple_spinner_dropdown_item);
        arrayRepeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        noticeSpinner.setAdapter(arrayRepeatAdapter1);


        builder.setTitle(R.string.dialog_create_sms)
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
                        ArrayList<String> phoneNumbers = new ArrayList<>();
                        phoneNumbers.add("8947839534");
                        phoneNumbers.add("5487983721");
                        Sim sim = new Sim("sim 1", "phone 2");
                        Sms sms = new Sms(phoneNumbers, sim, "dfasf", new Date());
                        Firebase base = new Firebase(Constants.FIREBASE_URL);
                        AuthData authData = base.getAuth();
                        if (authData != null) {
                            base = base.child(authData.getUid());
                        }
                        base.child("sms").push().setValue(sms);
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
        if (requestCode == CONTACT_PICKER_REQUEST) {
//            if (resultCode == AppCompatActivity.RESULT_OK) {

            ArrayList<Contact> contacts = new ArrayList<Contact>();
            contacts = data.getParcelableArrayListExtra("contacts");
            StringBuilder stringBuilder = new StringBuilder();
            for (Contact c : contacts) {
                stringBuilder.append(c.getNumber() + "; ");
//                    Log.d("Selected contact = ", c.getNumber());
//                    smsNumberText.setText(c.getName());
//                    smsNumberText.setText("XYI");
            }
            smsNumberText.setText(stringBuilder);

        }
    }
}
