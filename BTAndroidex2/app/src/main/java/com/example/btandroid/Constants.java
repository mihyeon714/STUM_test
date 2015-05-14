package com.example.btandroid;

public class Constants {

	// Service handler message key
	public static final String SERVICE_HANDLER_MSG_KEY_DEVICE_NAME = "device_name";
	public static final String SERVICE_HANDLER_MSG_KEY_DEVICE_ADDRESS = "device_address";
	public static final String SERVICE_HANDLER_MSG_KEY_TOAST = "toast";
    
    // Preference
	public static final String PREFERENCE_NAME = "BTAndroidPref";
	
	public static final String PREFERENCE_CONN_INFO_ADDRESS = "device_address";
	public static final String PREFERENCE_CONN_INFO_NAME = "device_name";
	
	public static final String PREFERENCE_KEY_LAST_LAST_INIT_TIME = "LastInitData";
	public static final String PREFERENCE_KEY_IS_FIRST_EXEC = "IsFirstExec";
	
    // Message types sent from Service to Activity
    public static final int MESSAGE_CMD_ERROR_NOT_CONNECTED = -50;
    
    public static final int MESSAGE_BT_STATE_INITIALIZED = 1;
    public static final int MESSAGE_BT_STATE_LISTENING = 2;
    public static final int MESSAGE_BT_STATE_CONNECTING = 3;
    public static final int MESSAGE_BT_STATE_CONNECTED = 4;
    public static final int MESSAGE_BT_STATE_ERROR = 10;
    
    public static final int MESSAGE_ADD_NOTIFICATION = 101;
    public static final int MESSAGE_DELETE_NOTIFICATION = 105;
    public static final int MESSAGE_GMAIL_UPDATED = 111;
    public static final int MESSAGE_SMS_RECEIVED = 121;
    public static final int MESSAGE_CALL_STATE_RECEIVED = 131;
    public static final int MESSAGE_RF_STATE_RECEIVED = 141;
    public static final int MESSAGE_FEED_UPDATED = 151;
    
    public static final int MESSAGE_READ_FROM_DEVICE = 201;
    
    public static final int RESPONSE_ADD_FILTER_FAILED = -1;
    public static final int RESPONSE_EDIT_FILTER_FAILED = -1;
    public static final int RESPONSE_DELETE_FILTER_FAILED = -1;
    
    public static final int RESPONSE_ADD_RSS_FAILED = -1;
    public static final int RESPONSE_EDIT_RSS_FAILED = -1;
    public static final int RESPONSE_DELETE_RSS_FAILED = -1;
	
	// Intent request codes
	public static final int REQUEST_CONNECT_DEVICE = 1;
	public static final int REQUEST_ENABLE_BT = 2;




}
