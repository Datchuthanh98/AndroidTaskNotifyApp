package com.example.tasknotify.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.tasknotify.model.Todo;

import java.util.ArrayList;
import java.util.List;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "my_todo_manage";
    private static final int VERSION = 5;

    public SQLiteHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    private final String DROP_TODO = "DROP TABLE IF EXISTS todo";
    private final String CREATE_TODO = "CREATE TABLE todo(" +
            "id  TEXT PRIMARY KEY ," +
            "name TEXT," +
            "dateTodo INTEGER," +
            "priority INTEGER," +
            "urlImagePreview TEXT," +
            "requestCode INTEGER," +
            "userID TEXT"+
            ")";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TODO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TODO);
        db.execSQL(CREATE_TODO);
    }


    public long insertTodo(Todo todo) {
        ContentValues values = new ContentValues();
        values.put("id", todo.getId());
        values.put("name", todo.getName());
        values.put("dateTodo", todo.getDateTask());
        values.put("priority", todo.getPriority());
        values.put("urlImagePreview", todo.getUrlImagePreview());
        values.put("requestCode", todo.getRequestCode());
        values.put("userID",todo.getUserID());
        return getWritableDatabase().insert("todo", null, values);

    }

    public List<Todo> getAllTodos() {
        List<Todo> todos = new ArrayList<>();
        Cursor cursor = getReadableDatabase()
                .query("todo", null, null,
                        null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex("id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                long dateTodo = cursor.getLong(cursor.getColumnIndex("dateTodo"));
                int priority = cursor.getInt(cursor.getColumnIndex("priority"));
                String urlImagePreview = cursor.getString(cursor.getColumnIndex("urlImagePreview"));
                int requestCode = cursor.getInt(cursor.getColumnIndex("requestCode"));
                String userID=cursor.getString(cursor.getColumnIndex("userID"));
                Todo todo = new Todo(id,name,dateTodo,priority,urlImagePreview,requestCode,userID);
                todos.add(todo);
            }
            cursor.close();
        }
        return todos;
    }

    public long updateTodo(Todo todo) {
        ContentValues values = new ContentValues();
        values.put("id", todo.getId());
        values.put("name", todo.getName());
        values.put("dateTodo", todo.getDateTask());
        values.put("priority", todo.getPriority());
        values.put("urlImagePreview", todo.getUrlImagePreview());
        values.put("requestCode", todo.getRequestCode());
        values.put("userID",todo.getUserID());
        String whereClause = "id=?";

        String[] whereArgs = {todo.getId()};
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        return sqLiteDatabase.update("todo", values, whereClause, whereArgs);
    }


    public long deleteTodo(String isbn) {
        String whereClause = "id = ?";
        String[] args = {isbn};
        return getWritableDatabase().delete("todo", whereClause, args);
    }


    public List<Todo> getListTodobyName(String namePlayer){
        List<Todo> todos = new ArrayList<>();
        String where = "name like ?";
        String[] args = {"%"+namePlayer+"%"};
        Cursor cursor = getReadableDatabase().query("todo", null, where,
                args, null, null, null);
        while (cursor != null && cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex("id"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            long dateTask = cursor.getLong(cursor.getColumnIndex("dateTodo"));
            int priority = cursor.getInt(cursor.getColumnIndex("priority"));
            String urlImagePreview = cursor.getString(cursor.getColumnIndex("urlImagePreview"));
            int requestCode = cursor.getInt(cursor.getColumnIndex("requestCode"));
            String userID=cursor.getString(cursor.getColumnIndex("userID"));
            Todo todo = new Todo(id,name,dateTask,priority,urlImagePreview,requestCode,userID);
            todos.add(todo);
        }
        return todos;
    }

    public int getMaxRequestCode(){
      String getMax="SELECT MAX(requestCode) FROM todo";
      Cursor cursor=getReadableDatabase().rawQuery(getMax,null);
        if (cursor != null && cursor.moveToNext()) {
            int maxRequestCode=cursor.getInt(0);
            cursor.close();
            return  maxRequestCode;
        }
       return 0;
    };


    public long resetTableTodo() {
        return getWritableDatabase().delete("todo", null, null);
    }

}
