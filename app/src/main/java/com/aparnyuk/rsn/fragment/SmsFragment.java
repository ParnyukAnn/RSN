package com.aparnyuk.rsn.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.aparnyuk.rsn.Constants;
import com.aparnyuk.rsn.R;
import com.aparnyuk.rsn.model.Sim;
import com.aparnyuk.rsn.model.Sms;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Date;

public class SmsFragment extends AbstractTabFragment {
    FirebaseRecyclerViewAdapter mAdapter;

    public static SmsFragment getInstance(Context context) {
        Bundle args = new Bundle();
        SmsFragment fragment = new SmsFragment();
        fragment.setArguments(args);
        fragment.setContext(context);
        fragment.setTitle(context.getString(R.string.tab_sms));
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_sms, container, false);
        // view.setBackgroundColor(getResources().getColor(R.color.colorTabFrag1));

        RecyclerView recycler = (RecyclerView) view.findViewById(R.id.smsRecyclerView);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Use Firebase to populate the list.
        Firebase.setAndroidContext(getContext());
        Firebase base = new Firebase(Constants.FIREBASE_URL).child("sms");

        mAdapter = new FirebaseRecyclerViewAdapter<Sms, SmsListViewHolder>(Sms.class, R.layout.list_item_for_sms, SmsListViewHolder.class, base) {

            @Override
            public void populateViewHolder(SmsListViewHolder smsListViewHolder, Sms sms) {
                smsListViewHolder.smsPhoneNum.setText(sms.getNumbers().get(0));
                smsListViewHolder.smsText.setText(sms.getText());
                smsListViewHolder.dateText.setText(sms.getDate().toString());
            }
        };

        recycler.setAdapter(mAdapter);
/*
        // Create a new Adapter
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, android.R.id.text1);

        // Assign adapter to ListView
        listView.setAdapter(adapter);


        base.addChildEventListener(new ChildEventListener() {
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                adapter.add((String) dataSnapshot.child("note").getValue());
            }

            public void onChildRemoved(DataSnapshot dataSnapshot) {
                adapter.remove((String) dataSnapshot.child("note").getValue());
            }

            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            public void onCancelled(FirebaseError firebaseError) {
            }
        });
*/
        //!!

        initFab();
        return view;
    }

    private void initFab() {
        final EditText text = (EditText) view.findViewById(R.id.smsText);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.sms_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//!!
                                /*ТЕСТОВЫЙ ВВОД ДАННЫХ */
                // создать диалоговые окна ввода данных смс
                // перенести этот код в диалоговое окно и добавить ввод остальных данных через сеттеры
                ArrayList<String> phoneNumbers = new ArrayList<>();
                phoneNumbers.add("8947839534");
                phoneNumbers.add("5487983721");
                Sim sim = new Sim("sim 1", "phone 2");
                Sms sms = new Sms(phoneNumbers, sim, text.getText().toString(), new Date());
                new Firebase(Constants.FIREBASE_URL)
                        .child("sms")
                        .push()
                        .setValue(sms);
//!!
            }
        });
    }

    public static class SmsListViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView smsPhoneNum;
        TextView smsText;
        TextView dateText;

        public SmsListViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.sms_cv);
            smsPhoneNum = (TextView) itemView.findViewById(R.id.sms_phone_num);
            smsText = (TextView) itemView.findViewById(R.id.sms_text);
            dateText = (TextView) itemView.findViewById(R.id.sms_date);
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        mAdapter.cleanup();
//    }
}
