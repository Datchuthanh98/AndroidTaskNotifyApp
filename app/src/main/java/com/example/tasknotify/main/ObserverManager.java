package com.example.tasknotify.main;

import androidx.lifecycle.MutableLiveData;

public class ObserverManager {
    private static ObserverManager instance = new ObserverManager();

    private ObserverManager(){
    };
    public static ObserverManager getInstance(){
        return instance;
    }
    public MutableLiveData<Integer> data=new MutableLiveData<>();
    public MutableLiveData<String> nameSearh=new MutableLiveData<>();


}
