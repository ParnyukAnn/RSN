package com.aparnyuk.rsn.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.aparnyuk.rsn.Utils.Constants;
import com.aparnyuk.rsn.R;
import com.aparnyuk.rsn.adapter.NoteListAdapter;
import com.aparnyuk.rsn.fragment.dialog.NoteDialog;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;


public class NoteFragment extends AbstractTabFragment {
    Toolbar toolbar;
    NoteDialog noteDialog;
    public NoteListAdapter noteAdapter;

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

        // from parent class, need for changing toolbar colors
        setActivityElements();

        // init recycler view
        RecyclerView recycler = (RecyclerView) view.findViewById(R.id.noteRecyclerView);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Use Firebase to populate the list.
        Firebase.setAndroidContext(getContext());
        Firebase base = new Firebase(Constants.FIREBASE_URL);
        AuthData authData = base.getAuth();
        if (authData != null) {
            base = base.child(authData.getUid()).child("note");
        } else {
            base = base.child("note");
        }
        noteAdapter = new NoteListAdapter(base);
        recycler.setAdapter(noteAdapter);
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        // normal mode: one click - open note, long - set delete mode
        // on delete mode: one click - check/uncheck note to delete, long click - the same
        noteAdapter.setOnItemClickListener(new NoteListAdapter.OnItemClickListener() {
                                               @Override
                                               public void onItemClick(View view, int position, boolean deleteMode, boolean changeMode) {
                                                   if (!deleteMode) {
                                                       if (changeMode) {
                                                           setNormalModeInterface();
                                                       } else {
                                                           noteDialog = new NoteDialog();
                                                           noteDialog.show(getFragmentManager(), "CreateDialog2");
                                                           //noteAdapter.getRef(position).removeValue();

                                                           //  toolbar.setDisplayHomeAsUpEnabled(true);
                                                       }
                                                   } else {
                                                       getActivity().setTitle("" + noteAdapter.getDeleteItemSet().size());
                                                   }
                                               }

                                               @Override
                                               public void onItemLongClick(View view, int position, boolean deleteMode, boolean changeMode) {
                                                   if (!deleteMode) {
                                                       if (changeMode) {
                                                           setNormalModeInterface();
                                                       }
                                                   } else {
                                                       if (changeMode) {
                                                           setDeleteModeInterface();
                                                       }
                                                       getActivity().setTitle("" + noteAdapter.getDeleteItemSet().size());
                                                   }
                                               }
                                           }

        );
        return view;
    }


    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void onDeleteClick(boolean delete) {
        if (noteAdapter.isDeleteMode()) {
            if (delete) {
                Log.d("Note", "delete in note fragment");
                for (int i : noteAdapter.getDeleteItemSet()) {
                    noteAdapter.getRef(i).removeValue();
                }
            }
            noteAdapter.clearDeleteMode();
            noteAdapter.notifyDataSetChanged();
            setNormalModeInterface();
        }
    }


//    public void onDestroy() {
//        super.onDestroy();
//        mAdapter.cleanup();
//    }
}
