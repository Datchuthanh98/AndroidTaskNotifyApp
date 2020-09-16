package com.example.tasknotify.Interface;

import com.example.tasknotify.model.User;

import java.util.List;

public interface UserConnectFirebase {
    void onPlayerLoadedFromFireBase(List<User> users);
}
