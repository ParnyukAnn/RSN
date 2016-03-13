package com.aparnyuk.rsn.fragment;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.aparnyuk.rsn.activity.MainActivity;
import com.aparnyuk.rsn.R;
import com.aparnyuk.rsn.adapter.CallListAdapter;
import com.aparnyuk.rsn.adapter.NoteListAdapter;
import com.aparnyuk.rsn.adapter.RemindListAdapter;
import com.aparnyuk.rsn.adapter.SmsListAdapter;

public abstract class AbstractTabFragment extends Fragment implements MainActivity.onDeleteClickListener {
    private String title;
    Context context;
    View view;

    private Toolbar toolbar;
    private TabLayout tabs;
    private FloatingActionButton fb;

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setActivityElements() {
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        tabs = (TabLayout) getActivity().findViewById(R.id.tabLayout);
        fb = (FloatingActionButton) getActivity().findViewById(R.id.fab);
    }

    protected void setDeleteModeInterface() {
        // add try!
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setBackgroundColor(getResources().getColor(R.color.deleteMode));
        tabs.setBackgroundColor(getResources().getColor(R.color.deleteMode));
        tabs.setSelectedTabIndicatorColor(getResources().getColor(R.color.white));
        tabs.setTabTextColors(getResources().getColor(R.color.light_grey), getResources().getColor(R.color.white));
        fb.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.deleteMode)));
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.deleteModeDark));
        }
        getActivity().invalidateOptionsMenu();
    }

    protected void setNormalModeInterface() {
        if (!checkDeleteMode()) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getActivity().setTitle(R.string.app_name);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            tabs.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            tabs.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorFab4));
            tabs.setTabTextColors(getResources().getColor(R.color.colorPrimaryLight), getResources().getColor(R.color.colorTabLine4));
            fb.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getActivity().getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            }
            getActivity().invalidateOptionsMenu();
        }
    }

    public boolean checkDeleteMode() {
        return (NoteListAdapter.isDeleteMode()) || (SmsListAdapter.isDeleteMode()) || (CallListAdapter.isDeleteMode()) || (RemindListAdapter.isDeleteMode());
    }
}
