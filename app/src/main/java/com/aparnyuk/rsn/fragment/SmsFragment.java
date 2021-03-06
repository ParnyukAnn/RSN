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
import com.aparnyuk.rsn.adapter.SmsListAdapter;
//import com.aparnyuk.rsn.dialog.SmsDialog;
import com.aparnyuk.rsn.dialog.SmsDialog;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;


public class SmsFragment extends AbstractTabFragment {
    Toolbar toolbar;
    // SmsDialog smsDialog;
    public SmsListAdapter smsAdapter;
    MainActivity act;

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

        // init recycler view
        RecyclerView recycler = (RecyclerView) view.findViewById(R.id.smsRecyclerView);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Use Firebase to populate the list.
        Firebase.setAndroidContext(getContext());
        Firebase base = new Firebase(Constants.FIREBASE_URL);
        AuthData authData = base.getAuth();
        if (authData != null) {
            base = base.child(authData.getUid()).child("sms");
        } else {
            base = base.child("sms");
        }
        smsAdapter = new SmsListAdapter(base);
        recycler.setAdapter(smsAdapter);
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

        if (getActivity() != null) {
            act = (MainActivity) getActivity();
        }
        // normal mode: one click - open sms, long - set delete mode
        // on delete mode: one click - check/uncheck sms to delete, long click - the same
        smsAdapter.setOnItemClickListener(
                new SmsListAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position, boolean deleteMode, boolean changeMode) {
                        if (!deleteMode) {
                            if (changeMode) {
                                act.setNormalModeInterface();
                            } else {
                                final Firebase ref = smsAdapter.getRef(position);
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot) {
                                        String smsPosition = snapshot.getKey();
                                        SmsDialog smsDialog = new SmsDialog();
                                        Bundle bundle = new Bundle();
                                        bundle.putString("smsPosition", smsPosition);
                                        smsDialog.setArguments(bundle);
                                        smsDialog.show(getFragmentManager(), "CreateDialog4");
                                    }

                                    @Override
                                    public void onCancelled(FirebaseError firebaseError) {
                                    }
                                });
                            }
                        } else {
                            act.setTitle("" + SmsListAdapter.getDeleteItemSet().size());
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
                            act.setTitle("" + SmsListAdapter.getDeleteItemSet().size());
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
        if (SmsListAdapter.isDeleteMode()) {
            if (delete) {
                Log.d("sms", "delete in sms fragment");
                for (int i : SmsListAdapter.getDeleteItemSet()) {
                    smsAdapter.getRef(i).addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                snapshot.getRef().removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                        }
                    });

                    /*if (smsAdapter.getRef(i) != null) {
                        smsAdapter.getRef(i).removeValue();
                    }*/
                }
            }
            smsAdapter.clearDeleteMode();
            smsAdapter.notifyDataSetChanged();
            act.setNormalModeInterface();
        }
    }


//    public void onDestroy() {
//        super.onDestroy();
//        mAdapter.cleanup();
//    }
}
