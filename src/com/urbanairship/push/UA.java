package com.urbanairship.push;

/**
 * Generic global storage class
 * <p> 
 * Stores apid, accessible via getApid()
 *
 * @see #getApid()
 */
public class UA {
    private static String apid;

    final static String LOG_TAG = "UA.push";
    final static String AIRMAIL_PACKAGE = "com.urbanairship.airmail";
    final static String AIRMAIL_RECEIVER = AIRMAIL_PACKAGE + ".CoreReceiver";
    
    /* Incoming intents */
    final static String ACTION_END_REGISTER = AIRMAIL_PACKAGE + ".END_REGISTER";
    final static String ACTION_ACCEPT_PUSH = AIRMAIL_PACKAGE + ".ACCEPT_PUSH";
    final static String ACTION_NOTIFY = AIRMAIL_PACKAGE + ".NOTIFY";
    
    /* Outgoing intents */
    final static String ACTION_START_REGISTER = AIRMAIL_PACKAGE + ".START_REGISTER";

    /**
     * @param apid
     */
    public static void setApid(String apid) {
        UA.apid = apid;
    }

    /**
     * @return  String apid
     */
    public static String getApid() {
        return apid;
    }
}
