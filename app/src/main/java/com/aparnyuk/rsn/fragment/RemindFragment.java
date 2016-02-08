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
import com.aparnyuk.rsn.model.Remind;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseRecyclerViewAdapter;

import java.util.Date;

public class RemindFragment extends AbstractTabFragment {
    FirebaseRecyclerViewAdapter mAdapter;

    public static RemindFragment getInstance(Context context) {
        Bundle args = new Bundle();
        RemindFragment fragment = new RemindFragment();
        fragment.setArguments(args);
        fragment.setContext(context);
        fragment.setTitle(context.getString(R.string.tab_remind));
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_remind, container, false);
        // view.setBackgroundColor(getResources().getColor(R.color.colorTabFrag3));

        initFab();

        RecyclerView recycler = (RecyclerView) view.findViewById(R.id.remindRecyclerView);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Use Firebase to populate the list.
        Firebase.setAndroidContext(getContext());
        Firebase base = new Firebase(Constants.FIREBASE_URL).child("remind");

        mAdapter = new FirebaseRecyclerViewAdapter<Remind, RemindListViewHolder>(Remind.class, R.layout.list_item_for_remind, RemindListViewHolder.class, base) {

            @Override
            public void populateViewHolder(RemindListViewHolder smsListViewHolder, Remind remind) {
                smsListViewHolder.remindText.setText(remind.getText());
                smsListViewHolder.dateText.setText(remind.getDate().toString());
            }
        };

        recycler.setAdapter(mAdapter);

        return view;
    }

    private void initFab() {
        //Firebase.setAndroidContext(getContext());
        final EditText text = (EditText) view.findViewById(R.id.remindText);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.remind_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//!!
                                /*ТЕСТОВЫЙ ВВОД ДАННЫХ */
                // создать диалоговые окна ввода данных о напоминании
                // перенести этот код в диалоговое окно и добавить ввод остальных данных через сеттеры
                Remind remind = new Remind(text.getText().toString(), new Date());
                new Firebase(Constants.FIREBASE_URL)
                        .child("remind")
                        .push()
                        .setValue(remind);
            }
//!!
        });
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public static class RemindListViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView remindText;
        TextView dateText;

        public RemindListViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.remind_cv);
            remindText = (TextView) itemView.findViewById(R.id.remind_text);
            dateText = (TextView) itemView.findViewById(R.id.remind_date);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
    }
}
