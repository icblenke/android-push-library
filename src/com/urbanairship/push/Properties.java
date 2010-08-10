package com.urbanairship.push;

import java.io.IOException;

import android.content.Context;
import android.content.res.Resources.NotFoundException;

public class Properties {

    java.util.Properties prop = new java.util.Properties();

    Properties(Context ctx) {
        try {
            int ua = ctx.getResources().getIdentifier("ua", "raw",
                ctx.getPackageName());
            prop.load(ctx.getResources().openRawResource(ua));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Boolean isDebug() {
        String debug = prop.getProperty("debug", "false");
        if (debug.compareTo("true") == 0) {
            return true;
        }
        return false;
    }

    public String appKey() {
        if (isDebug() == true) {
            return prop.getProperty("debug.app_key", "");
        }
        return prop.getProperty("production.app_key", "");
    }
}
