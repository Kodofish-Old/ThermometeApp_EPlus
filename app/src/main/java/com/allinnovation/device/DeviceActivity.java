package com.allinnovation.device;

import java.util.Timer;
import java.util.TimerTask;

import com.allinnovation.flexthermometeapp.CaptureMode;
import com.allinnovation.flexthermometeapp.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

public class DeviceActivity extends FragmentActivity {

	private String TAG = DeviceActivity.class.getSimpleName();
	
	protected static DeviceOperation deviceoperation;
	protected static readThread read_thread;
	protected final boolean isDebug = false;
	
	protected static final int sleepsec = 200;
	protected static InterruptedException lastException;

	protected GraphViewSeries TemperatureSerial;
	protected double graph2LastXValue;
	
	boolean bDebug = false;
	boolean bSimulate = false;
	public static final String Unit = "℃";
	
	Timer timer = new Timer(true);
	public Vibrator mVibrator ;
	public Context thisActivity;
	
	public float MinTemperature = 20;
	public float MaxTemperature = 80;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		deviceoperation = new DeviceOperation(this, bDebug, bSimulate);
		deviceoperation.createDeviceList();
		thisActivity = this;
		
		//USB Manager
		IntentFilter filter = new IntentFilter();
		filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.setPriority(500);
        registerReceiver(mUsbReceiver, filter); 
        mVibrator = (Vibrator)this.getApplication().getSystemService(Service.VIBRATOR_SERVICE);
        
//        timer.schedule(new timerTask(), 200, 200);
		
	}

	@Override
	public void onStart() {
		super.onStart();
		ShowEvent("onStart");		
        try {
        	deviceoperation.GetDeviceInformation();
//        	deviceoperation.openDevice();
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// deviceoperation.createDeviceList();
	}

	public class timerTask extends TimerTask
    {
      public void run()
      {
    	  deviceoperation.writeData();
      }
    };
    
	@Override
	public void onResume() {
		super.onResume();
		ShowEvent("onResume");
		deviceoperation.onResume();
		lastException = null;
	}

	@Override
	protected void onPause() {
		ShowEvent("onPause");
		super.onPause();
		deviceoperation.closeDevice();

		if (lastException != null)
			ShowDebugMsg(lastException.getMessage());
	}

	@Override
	public void onStop() {
		super.onStop();
		ShowEvent("onStop");
		interruptallthread();
	}

	private void interruptallthread() {
		if (read_thread != null) {
			if (!read_thread.isInterrupted()) {
				read_thread.interrupt();
			}
		}
	}

	@Override
	public void finish() {
		ShowEvent("finish");
		super.finish();
	}

	@Override
	protected void onDestroy() {
		ShowEvent("onDestroy");
		super.onDestroy();
		deviceoperation.closeDevice();
        unregisterReceiver(mUsbReceiver);
	}

	
    
	/***********USB broadcast receiver*******************************************/
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			String action = intent.getAction();
			if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
				Log.i(TAG,"ATTACHED...");
//				try {
					deviceoperation.notifyUSBDeviceAttach();
//					GetDeviceInformation();
//					openDevice();
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
	               
	        } else if(UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)){
				Log.i(TAG,"DETACHED...");
//				readText.setText("");
				
				deviceoperation.notifyUSBDeviceDetach();
				
//				closeDevice();
//				DeviceName.setText("Device Name : No device");
//				DeviceSerialNo.setText("Device Serial Number:");
//				DeviceDescription.setText("Device Description:");
//				readText.setText("");
//				Temperature.setText("");
//				TemperatureTST.setText("");
//				 for(int i=0; i<256; i++) {
//                     rchar[i] = 0;
//                 }
			}
		}	
	};

	private static class stopThread extends Thread {
		
		@Override
		public void run() {
			super.run();
			deviceoperation.stopRead();
		}
	}
	
	private static class readThread extends Thread {
		Handler mHandler;
		DeviceOperation mdeviceOperation;
		CaptureMode mMode;

		readThread(Handler h, DeviceOperation d, CaptureMode mode) {
			mHandler = h;
			mdeviceOperation = d;
			mMode = mode;
			this.setPriority(Thread.MIN_PRIORITY);
		}

		@Override
		public void run() {
			super.run();

			if (mdeviceOperation == null || mHandler == null || mMode == null) {
				// TODO 評估是否要中斷Thread
				return;
			}

			// 在thread中不能使用任何操作介面的事，會造成程式Crash
			while (deviceoperation.getReadThreadGoing() == true) {
				try {
					mdeviceOperation.writeData();
					Thread.sleep(sleepsec);
					mdeviceOperation.readdata();

					if (mdeviceOperation.getAllThermomete() != null) {
						if (mdeviceOperation.getAllThermomete().size() > 0) {
							Bundle modeBundle = new Bundle();
							modeBundle.putString("mode", mMode.name());

							Message msg = new Message();
							msg.setData(modeBundle);

							mHandler.sendMessage(msg);

							if (mMode == CaptureMode.Body_One_Touch_Mode
									|| mMode == CaptureMode.Industrial_Pressed_Measure_Mode) {
								Thread.sleep(1500);
								deviceoperation.setReadThreadGoing(false);
							}
						}
					}
					
					
				} catch (InterruptedException e) {
					lastException = e;
				}
			}
		}

	}

	public static Handler mhandler = null;

	public void MonitorModeStart()
	{
		deviceoperation.StartRead();
		connectFunction(CaptureMode.Industrial_Monitoring_Mode);
	}
	
	public void MonitorModeEnd()
	{
		
		//由於stopRead會執行sleep(3000)的動作，造成操作時會有遲緩現像，改由Thread來執行stopRead()的動作
		stopThread stop_thread = new stopThread();
		stop_thread.start();
		
		//deviceoperation.stopRead();
	}
	
	public void PressedModeStart()
	{
		deviceoperation.StartRead();
		connectFunction(CaptureMode.Body_One_Touch_Mode);
	}
	
	public void MonitorModeExecution()
	{
		if (deviceoperation.getEnableRead() == 0) 
		{
			graph2LastXValue = 0;
			ShowToast("Monitor Start");
			MonitorModeStart();
			}
		else
		{ 
			ShowToast("Monitor Ending");
			MonitorModeEnd(); 
			ShowToast("Monitor End");
			}
	}

	private void connectFunction(CaptureMode mode) {
		if (mode == null) {
			return;
		}

		if (deviceoperation.IsSimulate()) {
//			connectAction(mode);
			createReadThread(mode);
			return;
		}

		if (deviceoperation.IsFTDeviceNull() == true) {
			return;
		}

		// ShowToast("deviceoperation.ftDev.isOpen() = " +
		// String.valueOf(deviceoperation.ftDev.isOpen()));
		if (false == deviceoperation.getReadThreadGoing()
				&& deviceoperation.IsFTDeviceOpen()) {
			// connectAction(mode);
			createReadThread(mode);
			return;
		}
	}

	/*
	 * private void connectAction(CaptureMode mode) { switch (mode) { case
	 * Industrial_Monitoring_Mode: case Body_Monitoring_Mode:
	 * ShowDebugMsg("go to createReadThread()"); createReadThread(mode); break;
	 * case Industrial_Pressed_Measure_Mode: case Body_One_Touch_Mode:
	 * ShowDebugMsg("goto getvalueOnce()"); createReadThread(mode);
	 * deviceoperation.stopRead(); deviceoperation.disconnectFunction(); break;
	 * case Enginner_Mode: createReadThread(mode); break; default: break;
	 * 
	 * } } /*
	  
	 
	 /* 
	  * 建立Thread來進行資料讀取
	  */
	private void createReadThread(CaptureMode mode) {
		if (mhandler == null) {
			return;
		}

		if (deviceoperation == null) {
			return;
		}
//		ShowToast("Start Thread");
		read_thread = new readThread(mhandler, deviceoperation, mode);// ,
		deviceoperation.setReadThreadGoing(true);
		read_thread.start();

	} 
	
	protected void ShowToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	/*
	 * 顯示除錯誤息
	 */
	protected void ShowDebugMsg(String text) {
		if (isDebug) {
			Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
		}

	}

	protected void ShowEvent(String text) {
		if (isDebug) {
			Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
		}
	}
	
	/*
	 * 畫趨勢圖
	 * http://android-graphview.org/#doc_createsimplegraph
	 */
	protected void drawGraphic()
	{
		// init example series data
		GraphViewSeries exampleSeries = new GraphViewSeries(new GraphViewData[] {
		      new GraphViewData(1, 2.0d)
		      , new GraphViewData(2, 1.5d)
		      , new GraphViewData(3, 2.5d)
		      , new GraphViewData(4, 1.0d)
		});
		 
		GraphView graphView = new LineGraphView(
		      this // context
		      , "" // heading
		);
		graphView.addSeries(exampleSeries); // data
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.graph1);
		layout.addView(graphView);
	}
	
	protected void drawGraphicScrollable()
	{
		// draw sin curve
		/*
		int num = 150;
		GraphViewData[] data = new GraphViewData[num];
		double v=0;
		for (int i=0; i<num; i++) {
		   v += 0.2;
		   data[i] = new GraphViewData(i, Math.sin(v));
		}
		*/
		
		TemperatureSerial = new GraphViewSeries(new GraphViewData[] {
			      new GraphViewData(1, 30d)
			});
		GraphView graphView = new LineGraphView(
		      this
		      , ""
		);
		// add data
		graphView.addSeries(TemperatureSerial);
		
		// set view port, start=2, size=40
		graphView.setViewPort(1, 10);
		graphView.setScrollable(true);
		// optional - activate scaling / zooming
		graphView.setScalable(true);
		
		
//		設定Style
		graphView.setBackgroundColor(Color.rgb(80, 30, 30));
		
		graphView.getGraphViewStyle().setTextSize(20);
		graphView.getGraphViewStyle().setNumHorizontalLabels(5);
		graphView.getGraphViewStyle().setNumVerticalLabels(5);
		graphView.getGraphViewStyle().setVerticalLabelsWidth(60);
		
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.graph1);
		layout.addView(graphView);
	}

	/*
	 * 使用RingtoneManager播效內建聲音
	 */
	private void playAlter()
	{
		try {
		    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
		    r.play();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
	}
	
	/*
	 * 播放音效，音效免費下載網址：http://www.soundjay.com/beep-sounds-1.html
	 */
	public void playAudio() {
		
		Thread t = new Thread() {
			public void run() {
				MediaPlayer mMediaPlayer;
		        try {
		                // http://www.soundjay.com/beep-sounds-1.html lots of free beeps here
		            mMediaPlayer = MediaPlayer.create(thisActivity, R.raw.beep07);
		            mMediaPlayer.setLooping(false);
		            Log.e("beep","started0");
		            mMediaPlayer.start();
		 //           Log.e("beep","started1");
//		            mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
//		                        public void onCompletion(MediaPlayer arg0) {
//		                       finish();
//		                }
//		        });
		        } catch (Exception e) {
		            Log.e("beep", "error: " + e.getMessage(), e);
		        }
				
			}
		};
		t.start();
		
		
    }
	
	/*
	 * 當溫度達到條件時播放音效
	 */
	public void TemperatureCondition(Thermomete v)
	{
		if (v == null)
		{
			playAudio();
			return;
		}
		
		if (v.getTemperature() < this.MinTemperature || v.getTemperature() > this.MaxTemperature) playAudio();
		
	}
}
