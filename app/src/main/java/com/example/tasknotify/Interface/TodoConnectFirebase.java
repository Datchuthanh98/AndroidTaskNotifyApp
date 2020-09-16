package com.example.tasknotify.Interface;

import com.example.tasknotify.model.Todo;

import java.util.List;

public interface TodoConnectFirebase {
    void onPlayerLoadedFromFireBase(List<Todo> todos);
}
