package com.example.tasknotify.model;

import java.io.Serializable;

public class Todo implements Serializable {
    private String id;
    private String userID;
    private String name;
    private long dateTask;
    private long priority;
    private String urlImagePreview;
    private int requestCode;

    public Todo() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDateTask() {
        return dateTask;
    }

    public void setDateTask(long dateTask) {
        this.dateTask = dateTask;
    }

    public long getPriority() {
        return priority;
    }

    public void setPriority(long priority) {
        this.priority = priority;
    }

    public String getUrlImagePreview() {
        return urlImagePreview;
    }

    public void setUrlImagePreview(String urlImagePreview) {
        this.urlImagePreview = urlImagePreview;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    public Todo(String id, String name, long dateTask, long priority, String urlImagePreview, int requestCode,String userID) {
        this.id = id;
        this.name = name;
        this.dateTask = dateTask;
        this.priority = priority;
        this.urlImagePreview = urlImagePreview;
        this.requestCode = requestCode;
        this.userID=userID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
