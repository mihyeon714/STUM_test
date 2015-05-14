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

import java.util.ArrayList;

import android.os.Handler;
import android.util.Log;

import com.parse.ParseObject;

public class TransactionReceiver {
	private static final String TAG = "TransactionReceiver";
	
	private static final int PARSE_MODE_ERROR = 0;
	private static final int PARSE_MODE_WAIT_START_BYTE = 1;
	private static final int PARSE_MODE_WAIT_COMMAND = 2;
	private static final int PARSE_MODE_WAIT_DATA = 3;
	private static final int PARSE_MODE_WAIT_END_BYTE = 4;
	
	private Handler mHandler = null;
	private ArrayList<Transaction> mTransactionQueue = new ArrayList<Transaction>();
	
	private int mParseMode = PARSE_MODE_WAIT_START_BYTE;
	private int mCommand = Transaction.COMMAND_TYPE_NONE;
	private Transaction mTransaction = null;


    //////////////////////////////////////////////////////MHS
    public static final float PI = 3.141592f;
    boolean transFlag=false;//D바로 다음의 N을 위한 flag임
    //////////////////////////////////////////////////////MHE


	public TransactionReceiver(Handler h) {
		mHandler = h;
		mParseMode = PARSE_MODE_WAIT_START_BYTE;
	}
	
	public void setByteArray(byte[] buffer) {
		parseStream(buffer);
	}

	public void popTransaction() {
		// TODO:
	}

	private void parseStream(byte[] buffer) {
		if(buffer != null && buffer.length > 0) {
			for(int i=0; i<buffer.length; i++) {
				
				switch(mParseMode) {
				case PARSE_MODE_WAIT_START_BYTE:
					parseStartByte(buffer[i]);
					break;
				case PARSE_MODE_WAIT_COMMAND:
					parseCommand(buffer[i]);
					break;
				case PARSE_MODE_WAIT_DATA:
					parseData(buffer[i]);
					break;
				case PARSE_MODE_WAIT_END_BYTE:
					parseEndByte(buffer[i]);
					break;
				}
			}	// End of for loop
            ////////////////////////////////////////////////////////////////////////////MHS
            // construct a string from the valid bytes in the buffer
            String readMessage = new String(buffer);
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
            Log.d("tag8",str[8]);

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



        }	// End of if()
	}

	private void parseStartByte(byte packet) {
		if(packet == Transaction.TRANSACTION_START_BYTE) {
			mParseMode = PARSE_MODE_WAIT_COMMAND;
			mTransaction = new Transaction();
		}
	}
	
	private void parseCommand(byte cmd) {
		mCommand = cmd;
		switch(mCommand) {
		case Transaction.COMMAND_TYPE_PING:
			mParseMode = PARSE_MODE_WAIT_END_BYTE;
			break;
			
		// TODO:
			
		default:
			break;
		}	// End of switch()
	}	// End of parseCommand()
	
	private void parseData(byte packet) {
		if(packet == Transaction.TRANSACTION_END_BYTE) {
			mParseMode = PARSE_MODE_WAIT_START_BYTE;
			pushTransaction();
		}
		
		// TODO: 
	}
	
	private void parseEndByte(byte packet) {
		if(packet == Transaction.TRANSACTION_END_BYTE) {
			mParseMode = PARSE_MODE_WAIT_START_BYTE;
			pushTransaction();
		}
	}
	
	private void pushTransaction() {
		if(mTransaction != null) {
			mTransactionQueue.add(mTransaction);
			mTransaction = null;
		}
	}


	public class Transaction {
		private static final byte TRANSACTION_START_BYTE = (byte)0xfc;
		private static final byte TRANSACTION_END_BYTE = (byte)0xfd;
		
		public static final int COMMAND_TYPE_NONE = 0x00;
		public static final int COMMAND_TYPE_PING = 0x01;
		
	}

}
