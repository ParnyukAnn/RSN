package com.aparnyuk.rsn.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;
import com.aparnyuk.rsn.activity.MainActivity;

public abstract class AbstractTabFragment extends Fragment implements MainActivity.onDeleteClickListener {
    private String title;
    Context context;
    View view;

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

}
