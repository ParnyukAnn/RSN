package com.aparnyuk.rsn.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;

import com.firebase.ui.FirebaseRecyclerAdapter;

public class AbstractTabFragment extends Fragment {
    private String title;
    Context context;
    View view;
    public FirebaseRecyclerAdapter mAdapter;

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
