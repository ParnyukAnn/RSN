package com.aparnyuk.rsn.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aparnyuk.rsn.R;

public class CallFragment extends AbstractTabFragment {

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
        return view;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}
