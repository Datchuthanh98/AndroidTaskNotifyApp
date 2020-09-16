package com.example.tasknotify.Interface;

import com.example.tasknotify.model.Todo;

public interface OnFirebaseUpdate {
    void updateSqlite(Todo todo);
}
