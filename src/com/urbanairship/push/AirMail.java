package com.urbanairship.push;


import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.util.Log;

public class AirMail {
    // Singleton
    private static AirMail instance = null;

    // Static
    private static final String AIRMAIL_PACKAGE = "com.urbanairship.airmail";
    private static final int POLL_INTERVAL = 5000;   // 5 seconds

    // Member vars
    protected PushReceiver pushReceiverClass;
    protected APIDReceiver apidReceiverClass;
    private boolean receiverCreated = false;
    
    
    public static synchronized AirMail getInstance() {
        if(instance == null) {
            instance = new AirMail();
        }
        return instance;
    }

    protected AirMail() {
        // defeats instantiation.
    }

    /**
    * Register an application by sending a broadcast to AirMail
    * and setup broadcast receiver with <code>apidReceiver</code> to accept
    * apid.
    * 
    * @param ctx  Application Context
    * @param apidReceiver Callback that accepts apid
    */
    public void register(Context ctx, final APIDReceiver apidReceiver) {
        Log.d(UA.LOG_TAG, "Registering");
        apidReceiverClass = apidReceiver;
      
        /* Ensure AirMail is installed and if not, prompt user for install */
        AirMailInstaller ami = new AirMailInstaller(ctx);
        if (ami.controlCenterInstalled()) {
            ami.finishRegister();
        } else {
            ami.promptForDownload();
        }
    }

    /**
    * Set up callback that receives incoming push notifications
    * 
    * @param ctx  Application Context
    * @param pushReceiver PushReceiver that accepts incoming push notification
    * @see PushReceiver#onReceive
    */
    public void acceptPush(Context ctx, PushReceiver pushReceiver){
        pushReceiverClass = pushReceiver;
    }

    private class AirMailInstaller {
        private Timer timer = null;
        private Context ctx = null;
        
        public AirMailInstaller(Context ctx) {
            this.ctx = ctx;
        }

        
        // After "More Info" is read, go back to displaying the 
        // main install dialog.
        OnClickListener infoOnClick = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                promptForDownload();
            }
            
        };
        
        OnClickListener btnOnClick = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + AIRMAIL_PACKAGE));
                    ctx.startActivity(marketIntent);
                    startPollingForInstall();
                } else if(which == DialogInterface.BUTTON_NEGATIVE){
                    apidReceiverClass.onAirMailInstallRefusal();
                } else if(which == DialogInterface.BUTTON_NEUTRAL) {
                    // More Info
                    new AlertDialog.Builder(ctx).setTitle("About AirMail")
                        .setMessage(
                                "AirMail Control Panel enables push notifications " +
                                "from the apps you have installed on your Android " +
                                "device.\n\n Once installed, AirMail Control Panel " +
                                "gives you complete control over how you experience " +
                                "notifications or alerts. AirMail is a hub where  " +
                                "you can customize how you receive alerts from  " +
                                "each app with AirMail Push enabled."
                                 ).setPositiveButton("OK", infoOnClick).create().show();
                }
            }
        };

        
        // @TODO: We should only nag the user once (per session?)
        public void promptForDownload() {

            AlertDialog promptInstall = new AlertDialog.Builder(ctx).setTitle("Push Notifications")
                    .setMessage(
                            "This application requires AirMail to "
                                    + "enable real-time notifications.")
                    .setPositiveButton("OK", btnOnClick).setNegativeButton(
                            "Cancel", btnOnClick).setNeutralButton("More Info",
                            btnOnClick).create();
            // @TODO why does this throw a NullPointerException?
            // Button defaultButton = promptInstall.getButton(DialogInterface.BUTTON_POSITIVE);
            // defaultButton.requestFocus();
            promptInstall.show();
        }

        /**
         * Polls every few seconds until Notification center has been installed.
         */
        private void startPollingForInstall() {

            class AirMailInstallPoller extends TimerTask {
                @Override
                public void run() {
                    Log.d(UA.LOG_TAG, "Poll for AirMail Install");
                    if (controlCenterInstalled()) {
                        timer.cancel();
                        Log.d(UA.LOG_TAG, "AirMail Control Center Installed!");
                        finishRegister();
                    }
                }
            }

            timer = new Timer();
            timer.schedule(new AirMailInstallPoller(), 0, POLL_INTERVAL);
        }        
        
        /**
         * Pulls package information about Notification center from package manager.
         */
        private boolean controlCenterInstalled() {
            boolean installed = false;
            try {
                @SuppressWarnings("unused")
                PackageInfo info = ctx.getPackageManager().getPackageInfo(
                        AIRMAIL_PACKAGE, 0);
                installed = true;
            } catch (NameNotFoundException e) {}
            return installed;
        }
        
        
        private void finishRegister() {

            /* Setup incoming filter and receiver */
            if(!receiverCreated){
                Log.d(UA.LOG_TAG, "Registering IntentReceiver");
                IntentFilter filter = new IntentFilter();
                filter.addAction(UA.ACTION_ACCEPT_PUSH);
                filter.addAction(UA.ACTION_END_REGISTER);
                filter.addAction(UA.ACTION_NOTIFY);
                ctx.registerReceiver(new IntentReceiver(), filter);
                receiverCreated = true;
            }

            /* Properties gotten from res/raw/ua.properties */
            Properties props = new Properties(this.ctx);

            /* Create outgoing START_REGISTER intent */
            Intent intent = new Intent(UA.ACTION_START_REGISTER);
            intent.setClassName(UA.AIRMAIL_PACKAGE, UA.AIRMAIL_RECEIVER);

            /* Create incoming END_REGISTER intent  */
            Intent localIntent = new Intent(UA.ACTION_END_REGISTER);

            /* Add appKey and incoming intent as payload for outgoing intent */
            intent.putExtra("appKey", props.appKey());
            intent.putExtra("app", PendingIntent.getBroadcast(ctx, 0, localIntent, 0));

            ctx.sendBroadcast(intent);

        }
    }
    

}
