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
import com.aparnyuk.rsn.fragment.dialog.CallDialog;
import com.aparnyuk.rsn.model.Calls;
import com.aparnyuk.rsn.model.Sim;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Date;

public class CallFragment extends AbstractTabFragment {
    FirebaseRecyclerViewAdapter mAdapter;
    CallDialog callDialog;

    public static CallFragment getInstance(Context context) {
        Bundle args = new Bundle();
        CallFragment fragment = new CallFragment();
        fragment.setArguments(args);
        fragment.setContext(context);
        fragment.setTitle(context.getString(R.string.tab_call));
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_call, container, false);
        // view.setBackgroundColor(getResources().getColor(R.color.colorTabFrag2));
        initFab();

        RecyclerView recycler = (RecyclerView) view.findViewById(R.id.callRecyclerView);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Use Firebase to populate the list.
        Firebase.setAndroidContext(getContext());
        Firebase base = new Firebase(Constants.FIREBASE_URL).child("call");

        mAdapter = new FirebaseRecyclerViewAdapter<Calls, CallsListViewHolder>(Calls.class, R.layout.list_item_for_calls, CallsListViewHolder.class, base) {

            @Override
            public void populateViewHolder(CallsListViewHolder callsListViewHolder, Calls calls) {
                callsListViewHolder.callPhoneNum.setText(calls.getNumbers().get(0));
                callsListViewHolder.callText.setText(calls.getText());
                callsListViewHolder.dateText.setText(calls.getDate().toString());
            }
        };

        recycler.setAdapter(mAdapter);

        return view;
    }

    private void initFab() {
        final EditText text = (EditText) view.findViewById(R.id.callText);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.call_fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
////!!
//                                /*ТЕСТОВЫЙ ВВОД ДАННЫХ */
//                // создать диалоговые окна ввода данных звонках
//                // перенести этот код в диалоговое окно и добавить ввод остальных данных через сеттеры
//                ArrayList<String> phoneNumbers = new ArrayList<>();
//                phoneNumbers.add("8947839534");
//                phoneNumbers.add("5487983721");
//                Sim sim = new Sim("sim 1", "phone 2");
//                Calls call = new Calls(phoneNumbers, sim, new Date());
//                call.setText(text.getText().toString());
//                new Firebase(Constants.FIREBASE_URL)
//                        .child("call")
//                        .push()
//                        .setValue(call);
                callDialog = new CallDialog();
                callDialog.show(getFragmentManager(), "CreateDialog3");
            }
//!!
        });
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public static class CallsListViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView callText;
        TextView dateText;
        TextView callPhoneNum;

        public CallsListViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.call_cv);
            callPhoneNum = (TextView) itemView.findViewById(R.id.call_phone_num);
            callText = (TextView) itemView.findViewById(R.id.call_text);
            dateText = (TextView) itemView.findViewById(R.id.call_date);
        }
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        mAdapter.cleanup();
//    }
}
