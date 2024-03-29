package com.example.letschat.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.letschat.menu.CallsFragment;
import com.example.letschat.menu.ChatsFragment;
import com.example.letschat.menu.StoriesFragment;

public class ViewPagerStateAdapter extends FragmentStateAdapter {

    private final int TAB_COUNT = 3;

    public ViewPagerStateAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
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
    public int getItemCount() {
        return TAB_COUNT;
    }
}
