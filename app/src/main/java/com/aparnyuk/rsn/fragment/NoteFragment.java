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
import com.aparnyuk.rsn.model.Note;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseRecyclerViewAdapter;

import java.util.Date;

public class NoteFragment extends AbstractTabFragment {
    FirebaseRecyclerViewAdapter mAdapter;

    public static NoteFragment getInstance(Context context) {
        Bundle args = new Bundle();
        NoteFragment fragment = new NoteFragment();
        fragment.setArguments(args);
        fragment.setContext(context);
        fragment.setTitle(context.getString(R.string.tab_note));
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_note, container, false);
        //   view.setBackgroundColor(getResources().getColor(R.color.colorTabFrag4));

        initFab();
        RecyclerView recycler = (RecyclerView) view.findViewById(R.id.noteRecyclerView);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Use Firebase to populate the list.
        Firebase.setAndroidContext(getContext());
        Firebase base = new Firebase(Constants.FIREBASE_URL).child("note");

        mAdapter = new FirebaseRecyclerViewAdapter <Note, NoteListViewHolder>(Note.class, R.layout.list_item_for_note, NoteListViewHolder.class, base) {
            @Override
            public void populateViewHolder(NoteListViewHolder noteListViewHolder, Note note) {
                noteListViewHolder.noteText.setText(note.getText());
                noteListViewHolder.dateText.setText(note.getDate().toString());
            }
        };

        recycler.setAdapter(mAdapter);


        return view;
    }

    private void initFab() {
        final EditText text = (EditText) view.findViewById(R.id.noteText);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.note_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//!!
                                /*ТЕСТОВЫЙ ВВОД ДАННЫХ */
                // создать диалоговые окна ввода данных о заметках
                // перенести этот код в диалоговое окно и добавить ввод остальных данных через сеттеры
                Note note = new Note(text.getText().toString(), new Date());
                new Firebase(Constants.FIREBASE_URL)
                        .child("note")
                        .push()
                        .setValue(note);
            }
//!!
        });
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public static class NoteListViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView notePhoneNum;
        TextView noteText;
        TextView dateText;

        public NoteListViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.note_cv);
            noteText = (TextView) itemView.findViewById(R.id.note_text);
            dateText = (TextView) itemView.findViewById(R.id.note_date);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
    }
}
