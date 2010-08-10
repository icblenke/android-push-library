package com.urbanairship.push;

public interface APIDReceiver {
    void onReceive(String APID, boolean valid);
    void onAirMailInstallRefusal();
}
