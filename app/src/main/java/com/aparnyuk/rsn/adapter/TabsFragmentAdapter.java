package com.aparnyuk.rsn.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.aparnyuk.rsn.Constants;
import com.aparnyuk.rsn.fragment.AbstractTabFragment;
import com.aparnyuk.rsn.fragment.CallFragment;
import com.aparnyuk.rsn.fragment.NoteFragment;
import com.aparnyuk.rsn.fragment.RemindFragment;
import com.aparnyuk.rsn.fragment.SmsFragment;

import java.util.HashMap;
import java.util.Map;

public class TabsFragmentAdapter extends FragmentPagerAdapter {
    // код(номер)/фрагмент
    private Map<Integer, AbstractTabFragment> tabs;
    private Context context;

    public TabsFragmentAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
        initTabMap();
    }

    @Override
    public int getItemPosition(Object object) {
        return  POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs.get(position).getTitle();
    }

    @Override
    public int getCount() {
        return tabs.size();
    }

    @Override
    public AbstractTabFragment getItem(int position) {
        return tabs.get(position);
    }

    private void initTabMap() {
        tabs = new HashMap<>();
        tabs.put(Constants.TAB_ONE_SMS, SmsFragment.getInstance(context));
        tabs.put(Constants.TAB_TWO_CALL, CallFragment.getInstance(context));
        tabs.put(Constants.TAB_THREE_REMIND, RemindFragment.getInstance(context));
        tabs.put(Constants.TAB_FOUR_NOTE, NoteFragment.getInstance(context));
    }

    public void updateData(){
//        getItem(0).mAdapter.notifyDataSetChanged();
//        getItem(1).mAdapter.notifyDataSetChanged();
//        getItem(2).mAdapter.notifyDataSetChanged();
//        getItem(3).mAdapter.notifyDataSetChanged();
        notifyDataSetChanged();
    }
}
