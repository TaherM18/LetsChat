package com.example.wechat.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.wechat.CallsFragment;
import com.example.wechat.ChatsFragment;
import com.example.wechat.StoriesFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private final int TAB_COUNT = 3;
    private final String[] TAB_NAMES = new String[] {"Chats", "Status", "Calls"};
    private Context context;

    public ViewPagerAdapter(@NonNull FragmentManager fm, Context context) {
        super(fm);
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ChatsFragment();
            case 1:
                return new StoriesFragment();
            default:
                return new CallsFragment();
        }
    }

    @Override
    public int getCount() {
        return TAB_COUNT;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return TAB_NAMES[position];
    }
}
