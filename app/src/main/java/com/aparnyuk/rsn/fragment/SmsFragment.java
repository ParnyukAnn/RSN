package com.aparnyuk.rsn.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aparnyuk.rsn.R;

public class SmsFragment extends AbstractTabFragment {

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
     // view.setBackgroundColor(getResources().getColor(R.color.colorTabFrag1));
        return view;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}
