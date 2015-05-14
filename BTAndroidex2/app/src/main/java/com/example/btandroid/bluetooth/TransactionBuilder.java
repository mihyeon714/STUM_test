/*
 * Copyright (C) 2014 The Retro Watch - Open source smart watch project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.btandroid.bluetooth;

import java.util.Calendar;

import com.example.btandroid.Constants;

import android.os.Handler;
import android.util.Log;

public class TransactionBuilder {
	private static final String TAG = "TransactionBuilder";
	
	private BluetoothManager mBTManager = null;
	private Handler mHandler = null;
	
	public TransactionBuilder(BluetoothManager bm, Handler errorHandler) {
		mBTManager = bm;
		mHandler = errorHandler;
	}
	
	public Transaction makeTransaction() {
		return new Transaction();
	}
	
	public class Transaction {
		
		public static final int MAX_MESSAGE_LENGTH = 16;
		
		// Command types
//		public static final int COMMAND_TYPE_NONE = 0x00;

		// Transaction instance status
		private static final int STATE_NONE = 0;		// Instance created
		private static final int STATE_BEGIN = 1;		// Initialize transaction
		private static final int STATE_SETTING_FINISHED = 2;	// End of setting parameters 
		private static final int STATE_TRANSFERED = 3;	// End of sending transaction data
		private static final int STATE_ERROR = -1;		// Error occurred
		
		// Transaction parameters
		private int mState = STATE_NONE;
		private byte[] mBuffer = null;
		private String mMsg = null;
		
		
		public void begin() {
			mState = STATE_BEGIN;
			mMsg = null;
			mBuffer = null;
		}
		
		/**
		 * Set string message to send
		 * @param \\id	Identifier - WARNING: use lower 1 byte only
		 * @param msg	String to send
		 */
		public void setMessage(String msg) {
			mMsg = msg;
		}
		
		public void settingFinished() {
			mState = STATE_SETTING_FINISHED;
			mBuffer = mMsg.getBytes();
		}
		
		public boolean sendTransaction() {
			if(mBuffer == null || mBuffer.length < 1) {
				Log.e(TAG, "##### Ooooooops!! No sending buffer!! Check command!!");
				return false;
			}
			
			// For debug
			if(mBuffer.length > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append("Message : ");
				
				for(int i=0; i<mBuffer.length; i++) {
					sb.append(String.format("%02X, ", mBuffer[i]));
				}
				
				Log.d(TAG, " ");
				Log.d(TAG, sb.toString());
			}
			
			if(mState == STATE_SETTING_FINISHED) {
				if(mBTManager != null) {
					// Check that we're actually connected before trying anything
					if (mBTManager.getState() == BluetoothManager.STATE_CONNECTED) {
						// Check that there's actually something to send
						if (mBuffer.length > 0) {
							// Get the message bytes and tell the BluetoothChatService to write
							mBTManager.write(mBuffer);
							
							mState = STATE_TRANSFERED;
							return true;
						}
						mState = STATE_ERROR;
					}
					mHandler.obtainMessage(Constants.MESSAGE_CMD_ERROR_NOT_CONNECTED).sendToTarget();
				}
			}
			return false;
		}
		
		public byte[] getPacket() {
			if(mState == STATE_SETTING_FINISHED) {
				return mBuffer;
			}
			return null;
		}
		
	}	// End of class Transaction

}
