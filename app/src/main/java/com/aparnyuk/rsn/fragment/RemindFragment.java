package com.aparnyuk.rsn.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aparnyuk.rsn.R;

public class RemindFragment extends Fragment {

    public static RemindFragment getInstance() {
        Bundle args = new Bundle();
        RemindFragment fragment = new RemindFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_remind, container, false);
        return view;
    }
}
