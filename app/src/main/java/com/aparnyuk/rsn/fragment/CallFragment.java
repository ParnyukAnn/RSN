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
import com.aparnyuk.rsn.activity.MainActivity;
import com.aparnyuk.rsn.adapter.CallListAdapter;
import com.aparnyuk.rsn.dialog.CallDialog;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;


public class CallFragment extends AbstractTabFragment {
    Toolbar toolbar;
    CallDialog callDialog;
    public CallListAdapter callAdapter;
    MainActivity act;

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

        // init recycler view
        RecyclerView recycler = (RecyclerView) view.findViewById(R.id.callRecyclerView);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Use Firebase to populate the list.
        Firebase.setAndroidContext(getContext());
        Firebase base = new Firebase(Constants.FIREBASE_URL);
        AuthData authData = base.getAuth();
        if (authData != null) {
            base = base.child(authData.getUid()).child("call");
        } else {
            base = base.child("call");
        }
        callAdapter = new CallListAdapter(base);
        recycler.setAdapter(callAdapter);
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

        if (getActivity() != null) {
            act = (MainActivity) getActivity();
        }

        // normal mode: one click - open call, long - set delete mode
        // on delete mode: one click - check/uncheck call to delete, long click - the same
        callAdapter.setOnItemClickListener(
                new CallListAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position, boolean deleteMode, boolean changeMode) {
                        if (!deleteMode) {
                            if (changeMode) {
                                act.setNormalModeInterface();
                            } else {
                                callDialog = new CallDialog();
                                callDialog.show(getFragmentManager(), "CreateDialog2");
                            }
                        } else {
                            act.setTitle("" + CallListAdapter.getDeleteItemSet().size());
                        }
                    }

                    @Override
                    public void onItemLongClick(View view, int position, boolean deleteMode, boolean changeMode) {
                        if (!deleteMode) {
                            if (changeMode) {
                                act.setNormalModeInterface();
                            }
                        } else {
                            if (changeMode) {
                                act.setDeleteModeInterface();
                            }
                            act.setTitle("" + CallListAdapter.getDeleteItemSet().size());
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
        if (CallListAdapter.isDeleteMode()) {
            if (delete) {
                Log.d("Call", "delete in call fragment");
                for (int i : CallListAdapter.getDeleteItemSet()) {
                    if (callAdapter.getRef(i) != null) {
                        callAdapter.getRef(i).removeValue();
                    }
                }
            }
            callAdapter.clearDeleteMode();
            callAdapter.notifyDataSetChanged();
            act.setNormalModeInterface();
        }
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        mAdapter.cleanup();
//    }
}
