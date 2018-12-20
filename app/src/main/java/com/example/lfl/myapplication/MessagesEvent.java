package com.example.lfl.myapplication;

public class MessagesEvent{
    private static final String TAG = "MessagesEvent";
    public final String message;

    public MessagesEvent(String str) {
        this.message = str;
    }

}
