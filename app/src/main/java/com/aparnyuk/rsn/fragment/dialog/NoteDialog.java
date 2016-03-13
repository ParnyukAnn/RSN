package com.aparnyuk.rsn.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.aparnyuk.rsn.Utils.Constants;
import com.aparnyuk.rsn.R;
import com.aparnyuk.rsn.model.Note;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;

import java.util.Date;

public class NoteDialog extends DialogFragment {

    private EditText nameView;
    private static Button okButton, cancelButton;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.note_dialog, null);

        nameView = (EditText) view.findViewById(R.id.event_name_note);
        nameView.addTextChangedListener(titleWatcher);

        builder.setTitle(R.string.dialog_create_note)
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
                        Note note = new Note(nameView.getText().toString(), new Date());

                        Firebase base = new Firebase(Constants.FIREBASE_URL);
                        AuthData authData = base.getAuth();
                        if (authData != null) {
                            base =base.child(authData.getUid());
                        }
                        base.child("note").push().setValue(note);

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

}
