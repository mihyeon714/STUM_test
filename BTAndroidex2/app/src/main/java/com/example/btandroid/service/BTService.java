package com.example.btandroid.service;

import java.util.Timer;

import com.example.btandroid.Constants;
import com.example.btandroid.bluetooth.BluetoothManager;
import com.example.btandroid.bluetooth.ConnectionInfo;
import com.example.btandroid.bluetooth.TransactionBuilder;
import com.example.btandroid.bluetooth.TransactionReceiver;
import com.parse.ParseObject;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class BTService  extends Service {
	private static final String TAG = "BTService";
	
	// Context, System
	private Context mContext = null;
	private static Handler mActivityHandler = null; //핸들러가 MainActivity.java에 있음
	private ServiceHandler mServiceHandler = new ServiceHandler(); //핸들러가 여기있음
	private final IBinder mBinder = new BTServiceBinder(); //BTServiceBinder()란 클래스가 아래있음
	
	// Bluetooth
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothManager mBtManager = null; //만들어준거
	private ConnectionInfo mConnectionInfo = null; //만들어준거
	
	private TransactionBuilder mTransactionBuilder = null; //만들어준거 //보내는애?
	private TransactionReceiver mTransactionReceiver = null; //만들어준거 //받는애?
	
	// Auto-refresh timer
	private Timer mRefreshTimer = null;
	private Timer mDeleteTimer = null;

    //////////////////////////////////////////////////////MHS
    public static final float PI = 3.141592f;
    boolean transFlag=false;//D바로 다음의 N을 위한 flag임
    //////////////////////////////////////////////////////MHE


	/*****************************************************
	 * 
	 *	Overrided methods
	 *
	 ******************************************************/
	@Override
	public void onCreate() {
		Log.d(TAG, "# Service - onCreate() starts here");
		
		mContext = getApplicationContext();
		initialize();//아래에있지..
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "# Service - onStartCommand() starts here");
		
		// If service returns START_STICKY, android restarts service automatically after forced close.
		// At this time, onStartCommand() method in service must handle null intent.
		return Service.START_STICKY;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		// This prevents reload after configuration changes
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "# Service - onBind()");
		return mBinder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "# Service - onUnbind()");
		return true;
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "# Service - onDestroy()");
		finalizeService();
	}
	
	@Override
	public void onLowMemory (){
		Log.d(TAG, "# Service - onLowMemory()");
		// onDestroy is not always called when applications are finished by Android system.
		finalizeService();
	}

	
	/*****************************************************
	 * 
	 *	Private methods
	 *
	 ******************************************************/
	private void initialize() {
		Log.d(TAG, "# Service : initialize ---");
		
		// Get connection info instance
		mConnectionInfo = ConnectionInfo.getInstance(mContext);
		
		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
			return;
		}
		
		if (!mBluetoothAdapter.isEnabled()) {
			// BT is not on, need to turn on manually.
			// Activity will do this.
		} else {
			if(mBtManager == null) {
				setupBT();//블루투스 아답터있는거 확인했으니까 블루투스세팅해줘야지 //걍 핸들러랑 연결해줌
			}
		}
	}

	private void sendMessageToDevice(String message) { //기기로메세지보내기..
		if(message == null || message.length() < 1)
			return;
		
		TransactionBuilder.Transaction transaction = mTransactionBuilder.makeTransaction();
		transaction.begin();
		transaction.setMessage(message);
		transaction.settingFinished();
		transaction.sendTransaction();
	}
	
	
	/*****************************************************
	 * 
	 *	Public methods
	 *
	 ******************************************************/
	public void finalizeService() {  //서비스끝내기전에 다 해제해줘야지
		Log.d(TAG, "# Service : finalize ---");
		
		mBluetoothAdapter = null;
		// Stop the bluetooth session
		if (mBtManager != null)
			mBtManager.stop();
		mBtManager = null;
		
		// Stop the timer
		if(mRefreshTimer != null) {
			mRefreshTimer.cancel();
			mRefreshTimer = null;
		}
		if(mDeleteTimer != null) {
			mDeleteTimer.cancel();
			mDeleteTimer = null;
		}
		
	}
	
	public void setupService(Handler h) { //서비스 설정 //메인엑티비티에서초기화할때 호출됨
		mActivityHandler = h;
		
		// Double check BT manager instance
		if(mBtManager == null)  //만약에 블루투스가 서비스핸들러랑 연결안되있으면 연결해주고
			setupBT();
		
		// Initialize transaction builder & receiver //보내고 받는거를 위한 초기화과정~//액티비티핸들러와 연결해주기
		if(mTransactionBuilder == null)
			mTransactionBuilder = new TransactionBuilder(mBtManager, mActivityHandler);
		if(mTransactionReceiver == null)
			mTransactionReceiver = new TransactionReceiver(mActivityHandler);
		
		// If ConnectionInfo holds previous connection info,
		// try to connect using it.
		if(mConnectionInfo.getDeviceAddress() != null && mConnectionInfo.getDeviceName() != null) {
			connectDevice(mConnectionInfo.getDeviceAddress()); //기기와 연결하기
		} 
		// or wait in listening mode
		else {
			if (mBtManager.getState() == BluetoothManager.STATE_NONE) {
				// Start the bluetooth services
				mBtManager.start();
			}
		}
	}
	
    /**
     * Setup and initialize BT manager
     */
	public void setupBT() {
        Log.d(TAG, "Service - setupBT()");

        // Initialize the BluetoothManager to perform bluetooth connections
        if(mBtManager == null)
        	mBtManager = new BluetoothManager(this, mServiceHandler);
    }
	
    /**
     * Check bluetooth is enabled or not.
     */
	public boolean isBluetoothEnabled() {
		if(mBluetoothAdapter==null) {
			Log.e(TAG, "# Service - cannot find bluetooth adapter. Restart app.");
			return false;
		}
		return mBluetoothAdapter.isEnabled();
	}
	
	/**
	 * Get scan mode //나보여주기임//메인엑티비티에서 부름
	 */
	public int getBluetoothScanMode() {
		int scanMode = -1;
		if(mBluetoothAdapter != null)
			scanMode = mBluetoothAdapter.getScanMode();
		
		return scanMode;
	}

    /**
     * Initiate a connection to a remote device.
     * @param address  Device's MAC address to connect
     */
	public void connectDevice(String address) {
		Log.d(TAG, "Service - connect to " + address);
		
		// Get the BluetoothDevice object
		if(mBluetoothAdapter != null) {
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
			
			if(device != null && mBtManager != null) {
				mBtManager.connect(device);
			}
		}
	}
	
    /**
     * Connect to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    /* 안쓰는거같아서 주석처리함..위에있음 똑같은 함수
	public void connectDevice(BluetoothDevice device) {
		if(device != null && mBtManager != null) {
			mBtManager.connect(device);
		}
	}
    */
	/**
	 * Get connected device name
	 */
	public String getDeviceName() {
		return mConnectionInfo.getDeviceName();
	}

	/**
	 * Send message to remote device using Bluetooth
	 */
    /* 위에있음..
	public void sendMessageToRemote(String message) {
		sendMessageToDevice(message);
	}
	*/
	
	
	/*****************************************************
	 * 
	 *	Handler, Listener, Timer, Sub classes
	 *
	 ******************************************************/
	public class BTServiceBinder extends Binder {
		public BTService getService() {
			return BTService.this;
		}
	}
	
    /**
     * Receives messages from bluetooth manager
     */
	class ServiceHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg) {
			
			switch(msg.what) {
			case BluetoothManager.MESSAGE_STATE_CHANGE:
				// Bluetooth state Changed
				Log.d(TAG, "Service - MESSAGE_STATE_CHANGE: " + msg.arg1);
				
				switch (msg.arg1) {
				case BluetoothManager.STATE_NONE:
					mActivityHandler.obtainMessage(Constants.MESSAGE_BT_STATE_INITIALIZED).sendToTarget();
					if(mRefreshTimer != null) {
						mRefreshTimer.cancel();
						mRefreshTimer = null;
					}
					break;
					
				case BluetoothManager.STATE_LISTEN:
					mActivityHandler.obtainMessage(Constants.MESSAGE_BT_STATE_LISTENING).sendToTarget();
					break;
					
				case BluetoothManager.STATE_CONNECTING:
					mActivityHandler.obtainMessage(Constants.MESSAGE_BT_STATE_CONNECTING).sendToTarget();
					break;
					
				case BluetoothManager.STATE_CONNECTED:
					mActivityHandler.obtainMessage(Constants.MESSAGE_BT_STATE_CONNECTED).sendToTarget();
					break;
				}
				break;

            //////////////////////////////////////////////////////////여기가 중요한듯 싶다//데이터전송과 받기
			case BluetoothManager.MESSAGE_WRITE:
				Log.d(TAG, "Service - MESSAGE_WRITE: ");
                /////////////////////////////////////////////////////////////////////////MHS
                //암튼 시간을 보내줘야하는데..휴
               // if(mTransactionBuilder != null)
                //    mTransactionBuilder.makeTransaction();//이게 맞나 모르겄다
                ////////////////////////////////////////////////////////////////////////MHE

				break;


			case BluetoothManager.MESSAGE_READ:
				Log.d(TAG, "Service - MESSAGE_READ: ");
				
				byte[] readBuf = (byte[]) msg.obj;
				// construct commands from the valid bytes in the buffer //원래코드는 주석되있었음
				if(mTransactionReceiver != null) {
                    mTransactionReceiver.setByteArray(readBuf);
                    String readMessage = new String(readBuf);
                    Log.d("여긴서비스부분 리드메세지",readMessage);//여기도 쓰레기값이 있군요.
                }

                //여기서 파스에 보내줬었는데 안저장되서 트랜잭션리시버에 넣어줬다.

               /* ////////////////////////////////////////////////////////////////////////////MHS
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf);
                Log.d("어디한번보자",readMessage);
                String str[]=readMessage.split(",");


                //읽어온 애를 스트링으로 바꿨으니까 스트링을 쪼개서 자료형을 바꾼다음에 파스디비에 저장하는 것으로 하자
                //아두이노에서 순서에 맞게 보내는것이 중요하다 그래야지 바꾸는것도..휴
                //그리고 드렁크워터는 부피로 바꿔야되는거 참고하기..


                //쪼개기
                String drinkflag=str[0];
                int year=Integer.parseInt(str[1]);
                int month=Integer.parseInt(str[2]);
                int day=Integer.parseInt(str[3]);
                int hour=Integer.parseInt(str[4]);
                int min=Integer.parseInt(str[5]);
                int sec=Integer.parseInt(str[6]);
                int watercm=Integer.parseInt(str[7]);//지금은 인체와의 거리가 전송되고있음
                float watertemp=Float.parseFloat(str[8]);
                Log.d("tag0", str[0]);
                Log.d("tag1",str[1]);
                Log.d("tag2",str[2]);
                Log.d("tag3",str[3]);
                Log.d("tag4",str[4]);
                Log.d("tag5",str[5]);
                Log.d("tag6",str[6]);
                Log.d("tag7",str[7]);
                Log.d("tag7",str[8]);

                float watervolume= PI*8*8*watercm;

                if(drinkflag.equals("D")) {
                    ParseObject testDBdata = new ParseObject("dataTestMH");
                    testDBdata.put("drinkflag", drinkflag);
                    testDBdata.put("year", year);
                    testDBdata.put("month", month);
                    testDBdata.put("day", day);
                    testDBdata.put("hour", hour);
                    testDBdata.put("min", min);
                    testDBdata.put("sec", sec);
                    testDBdata.put("watervolume", watervolume);
                    testDBdata.put("watertemp", watertemp);
                    testDBdata.saveInBackground();
                    transFlag=true;
                }else if(transFlag==true && drinkflag.equals("N")){
                    ParseObject testDBdata = new ParseObject("dataTestMH");
                    testDBdata.put("drinkflag", drinkflag);
                    testDBdata.put("year", year);
                    testDBdata.put("month", month);
                    testDBdata.put("day", day);
                    testDBdata.put("hour", hour);
                    testDBdata.put("min", min);
                    testDBdata.put("sec", sec);
                    testDBdata.put("watervolume", watervolume);
                    testDBdata.put("watertemp", watertemp);
                    testDBdata.saveInBackground();
                    transFlag=false;
                }else if(sec==0) { //1분단위로 보낼라고 ㅋㅋㅋㅋㅋㅋ //사실 D랑 N 구분해서 보내야함..D다음에 N있으면 둘다 보내야됨
                    ///파스에저장
                    ParseObject testDBdata = new ParseObject("dataTestMH");
                    testDBdata.put("drinkflag", drinkflag);
                    testDBdata.put("year", year);
                    testDBdata.put("month", month);
                    testDBdata.put("day", day);
                    testDBdata.put("hour", hour);
                    testDBdata.put("min", min);
                    testDBdata.put("sec", sec);
                    testDBdata.put("watervolume", watervolume);
                    testDBdata.put("watertemp", watertemp);
                    testDBdata.saveInBackground();
                }

                ///////////////////////////////////////////////////////////////////////////////////MHE
                */

				break;

                //////////////////////////////////////////////////////////여기가 중요한듯 싶다 //데이터전송과받기끝

				
			case BluetoothManager.MESSAGE_DEVICE_NAME:
				Log.d(TAG, "Service - MESSAGE_DEVICE_NAME: ");
				
				// save connected device's name and notify using toast
				String deviceAddress = msg.getData().getString(Constants.SERVICE_HANDLER_MSG_KEY_DEVICE_ADDRESS);
				String deviceName = msg.getData().getString(Constants.SERVICE_HANDLER_MSG_KEY_DEVICE_NAME);
				
				if(deviceName != null && deviceAddress != null) {
					// Remember device's address and name
					mConnectionInfo.setDeviceAddress(deviceAddress);
					mConnectionInfo.setDeviceName(deviceName);
					
					Toast.makeText(getApplicationContext(), 
							"Connected to " + deviceName, Toast.LENGTH_SHORT).show();
				}
				break;
				
			case BluetoothManager.MESSAGE_TOAST:
				Log.d(TAG, "Service - MESSAGE_TOAST: ");
				
				Toast.makeText(getApplicationContext(), 
						msg.getData().getString(Constants.SERVICE_HANDLER_MSG_KEY_TOAST), 
						Toast.LENGTH_SHORT).show();
				break;
				
			}	// End of switch(msg.what)
			
			super.handleMessage(msg);
		}
	}	// End of class MainHandler
	
	
	
}
