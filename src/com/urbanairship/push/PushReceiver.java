package com.urbanairship.push;

public interface PushReceiver {
    void onReceive(String message, String payload);
    void onClick(String message, String payload);
}
