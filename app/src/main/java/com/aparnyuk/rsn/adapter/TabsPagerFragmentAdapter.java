package com.aparnyuk.rsn.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.aparnyuk.rsn.fragment.CallFragment;
import com.aparnyuk.rsn.fragment.NoteFragment;
import com.aparnyuk.rsn.fragment.RemindFragment;
import com.aparnyuk.rsn.fragment.SmsFragment;

public class TabsPagerFragmentAdapter extends FragmentPagerAdapter {
    private String[] tabs;

    public TabsPagerFragmentAdapter(FragmentManager fm) {
        super(fm);
        tabs = new String[]{"Sms", "Call", "Remind", "Note"};
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return SmsFragment.getInstance();
            case 1:
                return CallFragment.getInstance();
            case 2:
                return RemindFragment.getInstance();
            case 3:
                return NoteFragment.getInstance();
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs[position];
    }

    @Override
    public int getCount() {
        return tabs.length;
    }
}
