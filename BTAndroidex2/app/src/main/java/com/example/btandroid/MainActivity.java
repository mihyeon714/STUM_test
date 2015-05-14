package com.example.btandroid;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.example.btandroid.service.BTService;
import com.parse.Parse;
import com.parse.ParseObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    // Debugging
    private static final String TAG = "RetroWatchActivity";
    
	// Context, System
	private Context mContext;
	private BTService mService;
	private ActivityHandler mActivityHandler;
	
	// Global
	private boolean mStopService = true;	// If you want to stop background service when exit app, set this true.
	
	// UI stuff
	private ImageView mImageBT = null;
	private TextView mTextStatus = null;
	
	
	/*****************************************************
	 * 
	 *	 Overrided methods
	 *
	 ******************************************************/
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);



        //----- System, Context
		mContext = this;//.getApplicationContext();
		mActivityHandler = new ActivityHandler();
		
		// Do data initialization after service started and binded
		doStartService(); //여기서 이전Main에서 했던 작업을 Service로 옮겨서 하는듯하다.
		
		// Setup views
		setContentView(R.layout.activity_main);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "ZfjR3Gbh9Ly5JJJTop2oHMr3gSg2C9tSD0NNSs8O", "bohAfTs7aO1PXYOcpc1ucvIi30Hhu1B0SNBzky8Y");
		
		mImageBT = (ImageView) findViewById(R.id.status_title);
		mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_invisible));
		mTextStatus = (TextView) findViewById(R.id.status_text);
		mTextStatus.setText(getResources().getString(R.string.bt_state_init));


        /*
        ParseObject testDBdata = new ParseObject("dataTestMH");
        testDBdata.put("drinkflag", "D");
        testDBdata.put("year", 2015);
        testDBdata.put("month", 5);
        testDBdata.put("day", 14);
        testDBdata.put("hour", 12);
        testDBdata.put("min", 50);
        testDBdata.put("sec", 15);
        testDBdata.put("watervolume", 300);
        testDBdata.put("watertemp", 25.5);
        testDBdata.saveInBackground();
        */

    }

	@Override
	public synchronized void onStart() {
		super.onStart();
	}
	
	@Override
	public synchronized void onPause() {
		super.onPause();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		finalizeActivity(); //추가됨
	}
	
	@Override
	public void onLowMemory (){ //추가됨
		super.onLowMemory();
		// onDestroy is not always called when applications are finished by Android system.
		finalizeActivity();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_scan:
			// Launch the DeviceListActivity to see devices and do scan
			doScan(); //아래함수있음//장치검색
			return true;
		case R.id.action_discoverable:
			// Disabled: Ensure this device is discoverable by others
			ensureDiscoverable(); //아래함수있음 //내기기보이기
			return true;
		}
		return false;
	}



	@Override
	public void onBackPressed() { //추가됨
		super.onBackPressed();		// TODO: Disable this line to run below code
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig){ //추가됨
		// This prevents reload after configuration changes
		super.onConfigurationChanged(newConfig);
	}
	
	

	
	/*****************************************************
	 * 
	 *	Private methods
	 *
	 ******************************************************/
	
	/**
	 * Service connection //추가됨
	 */
	private ServiceConnection mServiceConn = new ServiceConnection() {
		
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.d(TAG, "Activity - Service connected");
			
			mService = ((BTService.BTServiceBinder) binder).getService();
			
			// Activity couldn't work with mService until connections are made
			// So initialize parameters and settings here, not while running onCreate()
			initialize();
		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;
		}
	};
	
	private void doStartService() {
		Log.d(TAG, "# Activity - doStartService()");
		startService(new Intent(this, BTService.class));
		bindService(new Intent(this, BTService.class), mServiceConn, Context.BIND_AUTO_CREATE);
	}
	
	private void doStopService() {
		Log.d(TAG, "# Activity - doStopService()");
		mService.finalizeService();
		stopService(new Intent(this, BTService.class));
	}
	
	/**
	 * Initialization / Finalization
	 */
	private void initialize() {
		Log.d(TAG, "# Activity - initialize()");
		mService.setupService(mActivityHandler);//서비스를 액티비티핸들러랑 연결해준다
		
		// If BT is not on, request that it be enabled.
		// BTService.setupBT() will then be called during onActivityResult
		if(!mService.isBluetoothEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
		}
	}
	
	private void finalizeActivity() { //Ondestroy에서 호출함 //서비스끝내주는거..
		Log.d(TAG, "# Activity - finalizeActivity()");
		
		if(mStopService)
			doStopService();
		
		unbindService(mServiceConn);

		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();
	}
	
	/**
	 * Launch the DeviceListActivity to see devices and do scan
	 */
	private void doScan() { //장치검색하기//함수로분리된것뿐
		Intent intent = new Intent(this, DeviceListActivity.class);
		startActivityForResult(intent, Constants.REQUEST_CONNECT_DEVICE);//정보를 같이보내는거지요..
	}
	
	/**
	 * Ensure this device is discoverable by others
	 */
	private void ensureDiscoverable() { //나보여주기
		if (mService.getBluetoothScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(intent);
		}
	}
	
	/**
	 * Call this method to send messages to remote
	 */
	/*BTService에잇어서 MH이 주석처리햇음
	private void sendMessageToRemote(String message) {
		mService.sendMessageToRemote(message);
	}
	*/
	
	/*****************************************************
	 * 
	 *	Public classes
	 *
	 ******************************************************/
	
	/**
	 * Receives result from external activity
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult " + resultCode);
		
		switch(requestCode) {
		case Constants.REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {  //블루투스활성화버튼 OK눌리면!
				// Get the device MAC address
				String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Attempt to connect to the device
				if(address != null && mService != null)
					mService.connectDevice(address);
			}
			break;
			
		case Constants.REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a BT session
				mService.setupBT();
			} else {
				// User did not enable Bluetooth or an error occured
				Log.e(TAG, "BT is not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
			}
			break;
		}	// End of switch(requestCode)
	}
	
	
	
	/*****************************************************
	 * 
	 *	Handler, Callback, Sub-classes
	 *
	 ******************************************************/
	//추가됨
	public class ActivityHandler extends Handler {
		@Override
		public void handleMessage(Message msg) 
		{
			switch(msg.what) {
			// BT state message
			case Constants.MESSAGE_BT_STATE_INITIALIZED:
				mTextStatus.setText(getResources().getString(R.string.bt_title) + ": " + 
						getResources().getString(R.string.bt_state_init));
				mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_invisible));
				break;
			case Constants.MESSAGE_BT_STATE_LISTENING:
				mTextStatus.setText(getResources().getString(R.string.bt_title) + ": " + 
						getResources().getString(R.string.bt_state_wait));
				mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_invisible));
				break;
			case Constants.MESSAGE_BT_STATE_CONNECTING:
				mTextStatus.setText(getResources().getString(R.string.bt_title) + ": " + 
						getResources().getString(R.string.bt_state_connect));
				mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_away));
				break;
			case Constants.MESSAGE_BT_STATE_CONNECTED:
				if(mService != null) {
					String deviceName = mService.getDeviceName();
					if(deviceName != null) {
						mTextStatus.setText(getResources().getString(R.string.bt_title) + ": " + 
								getResources().getString(R.string.bt_state_connected) + " " + deviceName);
						mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_online));
					}
				}
				break;
			case Constants.MESSAGE_BT_STATE_ERROR:
				mTextStatus.setText(getResources().getString(R.string.bt_state_error));
				mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_busy));
				break;
			
			// BT Command status
			case Constants.MESSAGE_CMD_ERROR_NOT_CONNECTED:
				mTextStatus.setText(getResources().getString(R.string.bt_cmd_sending_error));
				mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_busy));
				break;
				
			////////////////////////////////////////////
			// Handle messages here.
			////////////////////////////////////////////
//			case MESSAGE_xxx:
//			{
//				break;
//			}
			
			default:
				break;
			}
			
			super.handleMessage(msg);
		}
	}	// End of class ActivityHandler
	
	
	
}
