package com.urbanairship.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class IntentReceiver extends BroadcastReceiver {
    private Intent intent;
    private String message;
    private String payload;
    private AirMail airmail;

    public IntentReceiver() {
        airmail = AirMail.getInstance();
    }

    @Override
    public void onReceive(Context ctx, Intent in) {
        intent = in;
        message = intent.getStringExtra("message");
        payload = intent.getStringExtra("payload");

        final String action = intent.getAction();

        if(action.equals(UA.ACTION_END_REGISTER))
            endRegister();
        else if (action.equals(UA.ACTION_ACCEPT_PUSH))
            acceptPush();
        else if (action.startsWith(UA.ACTION_NOTIFY))
            actionNotify();

    }
    
    private void endRegister(){
        if(airmail.apidReceiverClass == null){
            Log.e(UA.LOG_TAG, "No APIDReceiver callback!");
            return;
        }
        final String apid = intent.getStringExtra("apid");
        final boolean valid = intent.getBooleanExtra("valid", false);
        if(valid) UA.setApid(apid);
        airmail.apidReceiverClass.onReceive(apid, valid);
    }
    
    private boolean checkPushReceiver(){
        if(airmail.pushReceiverClass == null){
            Log.e(UA.LOG_TAG, "No PushReceiver callback!");
            return false;
        } else {
            return true;
        }
    }
    
    private void acceptPush() {
        if(checkPushReceiver())
            airmail.pushReceiverClass.onReceive(message, payload);
    }
    
    private void actionNotify(){
        if(checkPushReceiver())
            airmail.pushReceiverClass.onClick(message, payload);
    }
}
