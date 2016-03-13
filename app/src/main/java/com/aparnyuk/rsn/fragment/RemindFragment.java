package com.aparnyuk.rsn.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.aparnyuk.rsn.Utils.Constants;
import com.aparnyuk.rsn.R;
import com.aparnyuk.rsn.adapter.RemindListAdapter;
import com.aparnyuk.rsn.fragment.dialog.RemindDialog;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;


public class RemindFragment extends AbstractTabFragment {

    RemindDialog remindDialog;
    public RemindListAdapter remindAdapter;

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

        // from parent class, need for changing toolbar colors
        setActivityElements();

        // init recycler view
        RecyclerView recycler = (RecyclerView) view.findViewById(R.id.remindRecyclerView);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Use Firebase to populate the list.
        Firebase.setAndroidContext(getContext());
        Firebase base = new Firebase(Constants.FIREBASE_URL);
        AuthData authData = base.getAuth();
        if (authData != null) {
            base = base.child(authData.getUid()).child("remind");
        } else {
            base = base.child("remind");
        }
        remindAdapter = new RemindListAdapter(base);
        recycler.setAdapter(remindAdapter);

        // normal mode: one click - open remind, long - set delete mode
        // on delete mode: one click - check/uncheck remind to delete, long click - the same
        remindAdapter.setOnItemClickListener(new RemindListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, boolean deleteMode, boolean changeMode) {
                if (!deleteMode) {
                    if (changeMode) {
                        setNormalModeInterface();
                    } else {
                        remindDialog = new RemindDialog();
                        remindDialog.show(getFragmentManager(), "CreateDialog2");
                        //remindAdapter.getRef(position).removeValue();

                        //  toolbar.setDisplayHomeAsUpEnabled(true);
                    }
                } else {
                    getActivity().setTitle("" + remindAdapter.getDeleteItemSet().size());
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
                    getActivity().setTitle("" + remindAdapter.getDeleteItemSet().size());
                }
            }
        });
        return view;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void onDeleteClick(boolean delete) {
        if (remindAdapter.isDeleteMode()) {
            Log.d("remind", "delete in remind fragment");
            if (delete) {
                for (int i : remindAdapter.getDeleteItemSet()) {
                    remindAdapter.getRef(i).removeValue();
                }
            }
            remindAdapter.clearDeleteMode();
            remindAdapter.notifyDataSetChanged();
            setNormalModeInterface();
        }
    }

//    public void onDestroy() {
//        super.onDestroy();
//        mAdapter.cleanup();
//    }
}
