package com.example.tasknotify.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.tasknotify.fragment.FragmentListTodo;
import com.example.tasknotify.fragment.FragmentUserAdd;
import com.example.tasknotify.fragment.FragmentManageAdd;

public class AdapterFragment extends FragmentPagerAdapter {
    int numTab = 2;
    boolean isManager;
    public AdapterFragment(@NonNull FragmentManager fm, int behavior, boolean isManager) {
        super(fm, behavior);
        this.isManager=isManager;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                    return new FragmentListTodo();

            case 1:
                if(isManager){
                    return new FragmentManageAdd();
                }else{
                    return  new FragmentUserAdd();
                }
        }
        return  null;
    }

    @Override
    public int getCount() {
        return numTab;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Danh sách";
            case 1:
                    if(isManager){
                    return "Quản lý ";
                    }else{
                    return "Cá nhân";
                }
        }
        return null;
    }
}
