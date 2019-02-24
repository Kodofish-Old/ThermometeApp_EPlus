package com.allinnovation.flexthermometeapp;

import java.util.ArrayList;

import com.allinnovation.device.DeviceOperation;
import com.allinnovation.device.Thermomete;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.method.ScrollingMovementMethod;
//import android.support.v4.app.NavUtils;
//import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
//import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
		ActionBar.OnNavigationListener {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current dropdown position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	static DeviceOperation deviceoperation;
	static readThread read_thread;
	final boolean isDebug = true;
	static TextView viewTitle;
	static TextView txt_Temperature;
	static final int sleepsec = 1000;
	static InterruptedException lastException;
	public static final String ARG_SECTION_NUMBER = "section_number";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		deviceoperation = new DeviceOperation(this);
		deviceoperation.createDeviceList();
		// deviceoperation.handler = mhandler;
		// Toast.makeText(this, "Devcount=" + String.valueOf(
		// deviceoperation.DevCount) , Toast.LENGTH_LONG).show();

		setContentView(R.layout.activity_main);

		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(
		// Specify a SpinnerAdapter to populate the dropdown list.
				new ArrayAdapter<String>(getActionBarThemedContextCompat(),
						android.R.layout.simple_list_item_1,
						android.R.id.text1, new String[] {
								getString(R.string.title_section1),
								getString(R.string.title_section2),
								getString(R.string.title_section3)}), this);
	}

	/**
	 * Backward-compatible version of {@link ActionBar#getThemedContext()} that
	 * simply returns the {@link android.app.Activity} if
	 * <code>getThemedContext</code> is unavailable.
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private Context getActionBarThemedContextCompat() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return getActionBar().getThemedContext();
		} else {
			return this;
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		ShowEvent("onRestoreInstanceState");
		// Restore the previously serialized current dropdown position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		ShowEvent("onSaveInstanceState");
		// Serialize the current dropdown position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
				.getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		ShowEvent("onCreateOptionMenu");
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		ShowEvent("onNavigationItemSelected");
		// When the given dropdown item is selected, show its contents in the
		// container view.
		
		Fragment fragment = new DummySectionFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, position + 1);
		fragment.setArguments(args);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, fragment).commit();

		return true;
	}

	@Override
	public void onStart() {
		super.onStart();
		ShowEvent("onStart");
//		deviceoperation.createDeviceList();
	}
	
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
	    deviceoperation.disconnectDevice();
	    
	    if (lastException != null)
	    	ShowDebugMsg(lastException.getMessage());
	}
	
	@Override
	public void onStop() {
		super.onStop();
		ShowEvent("onStop");
		interruptallthread();
	}

	void interruptallthread()
	{
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
	}

	

	public void notifyUSBDeviceAttach() {
		ShowEvent("notifyUSBDeviceAttach");
		deviceoperation.createDeviceList();
		ShowToast("裝置己連接");
	}

	public void notifyUSBDeviceDetach() {
		ShowEvent("notifyUSBDeviceDetach");
		deviceoperation.disconnectDevice();
		ShowToast("裝置卸載");
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	@SuppressLint("ValidFragment")
	public class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			View viewMode = null;
			String title = "";
			switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
			case 1:
				viewMode = inflater
						.inflate(R.layout.view_industrial_mode, null);
				title = this.getString(R.string.title_section1);
				txt_Temperature = ((TextView) viewMode
						.findViewById(R.id.txt_Temperature));
				Button btn_mmode = ((Button) viewMode
						.findViewById(R.id.btn_mmode));
				if (deviceoperation.getEnableRead() == 1)
					btn_mmode.setText(R.string.btn_mmode_open);
				else
					btn_mmode.setText(R.string.btn_mmode);

				((Button) viewMode.findViewById(R.id.btn_mmode))
						.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								Button btn_mmode = ((Button) v
										.findViewById(R.id.btn_mmode));
								
								deviceoperation.StartRead();

								connectFunction(CaptureMode.Industrial_Monitoring_Mode);
								
								if (deviceoperation.getEnableRead() == 1) {
									btn_mmode.setText(R.string.btn_mmode_open);
//									btn_pmmode.setVisibility(View.INVISIBLE);
								} else {
									deviceoperation.stopRead();
									btn_mmode.setText(R.string.btn_mmode);
									interruptallthread();
//									btn_pmmode.setVisibility(View.VISIBLE);
								}
							}
						});

				((Button) viewMode.findViewById(R.id.btn_pmmode))
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {

								deviceoperation.StartRead();
								connectFunction(CaptureMode.Industrial_Pressed_Measure_Mode);
							}
						});

				break;

			case 2:
				viewMode = inflater.inflate(R.layout.view_body_mode, null);
				title = this.getString(R.string.title_section2);
				txt_Temperature = ((TextView) viewMode
						.findViewById(R.id.txt_Temperature));
				((Button) viewMode.findViewById(R.id.btn_mmode))
						.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								Button btn_mmode = ((Button) v
										.findViewById(R.id.btn_mmode));
								
//								Button btn_otmmode = ((Button) v.findViewById(R.id.btn_otmmode));
								deviceoperation.StartRead();
								
								connectFunction(CaptureMode.Body_Monitoring_Mode);

								if (deviceoperation.getEnableRead() == 1) {
									btn_mmode.setText(R.string.btn_mmode_open);
//									btn_otmmode.setVisibility(View.INVISIBLE);
								} else {
									deviceoperation.stopRead();
									btn_mmode.setText(R.string.btn_mmode);
//									btn_otmmode.setVisibility(View.VISIBLE);
								}
							}
						});

				((Button) viewMode.findViewById(R.id.btn_otmmode))
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								deviceoperation.StartRead();
								connectFunction(CaptureMode.Body_One_Touch_Mode);
							}
						});
				break;
			case 3:
				viewMode = inflater.inflate(R.layout.view_engineer_mode, null);
				title = this.getString(R.string.title_section2);
				((Button) viewMode.findViewById(R.id.btn_mmode)).setOnClickListener(new monitor_mode_button());
				((Button) viewMode.findViewById(R.id.btn_clear)).setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						viewTitle.setText("");
					}
				});
				break;
			default:
				break;
			}

			viewTitle = (TextView) viewMode.findViewById(R.id.section_label);
			viewTitle.setText(title + "\n");
			viewTitle.setMovementMethod(new ScrollingMovementMethod());

			return viewMode;
		}
	}
	
	public class monitor_mode_button implements View.OnClickListener {
		public void onClick(View v) {
			Button btn_mmode = ((Button) v.findViewById(R.id.btn_mmode));
			
			deviceoperation.StartRead();
			
			connectFunction(CaptureMode.Enginner_Mode);
	
			if (deviceoperation.getEnableRead() == 1) {
				btn_mmode.setText(R.string.btn_mmode_open);
			} else {
				deviceoperation.stopRead();
				btn_mmode.setText(R.string.btn_mmode);
			}
		}
	}

	public void connectFunction(CaptureMode mode) {
		if (mode == null) {
			ShowDebugMsg("mode is null");
			return;
		}

//		ShowDebugMsg("in connectFunction");
//		ShowDebugMsg("deviceoperation.bReadThreadGoing = "
//				+ String.valueOf(deviceoperation.getReadThreadGoing()));

		if (deviceoperation.IsSimulate()) {
			connectAction(mode);
			return;
		}

		if (deviceoperation.IsFTDeviceNull() == true) {
			ShowDebugMsg("ftDev is null");
			return;
		}

		// ShowToast("deviceoperation.ftDev.isOpen() = " +
		// String.valueOf(deviceoperation.ftDev.isOpen()));
		if (false == deviceoperation.getReadThreadGoing()
				&& deviceoperation.IsFTDeviceOpen()) {
			ShowDebugMsg("stand by go to connectAction");
			connectAction(mode);
			return;
		}
	}

	public void connectAction(CaptureMode mode) {
		switch (mode) {
		case Industrial_Monitoring_Mode:
		case Body_Monitoring_Mode:
//			ShowDebugMsg("go to createReadThread()");
			createReadThread(mode);
			break;
		case Industrial_Pressed_Measure_Mode:
		case Body_One_Touch_Mode:
//			ShowDebugMsg("goto getvalueOnce()");
			createReadThread(mode);
//			deviceoperation.stopRead();
//			deviceoperation.disconnectFunction();
			break;
		case Enginner_Mode:
			createReadThread(mode);
			break;
		default:
			break;

		}
	}

	/*
	 * 建立Thread來進行資料讀取
	 */
	public void createReadThread(CaptureMode mode) {
		if (mhandler == null)
		{
			ShowDebugMsg("CreateReadThread handler is null!!");
			return;
		}

		if (deviceoperation == null)
		{
			ShowDebugMsg("CreateReadThread deviceoperation is null!!");
			return;
		}
		
		read_thread = new readThread(mhandler, deviceoperation, mode);// ,
		deviceoperation.setReadThreadGoing(true);
		read_thread.start();

		ShowDebugMsg("己建立執行緒讀取資料");
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

			if (mdeviceOperation == null || mHandler == null || mMode == null) 
				{
				//TODO 評估是否要中斷Thread
				return;
				}
			
			
			// 在thread中不能使用任何操作介面的事，會造成程式Crash
			while (deviceoperation.getReadThreadGoing() == true) {
				try {
					Thread.sleep(sleepsec);
					mdeviceOperation.readdata();

					if (mdeviceOperation.getAllThermomete() != null) {
						if (mdeviceOperation.getAllThermomete().size() > 0)
						{
							Bundle modeBundle = new Bundle();
							modeBundle.putString("mode", mMode.name());
		     
		                    Message msg = new Message();
		                    msg.setData(modeBundle);
		     
		                    mHandler.sendMessage(msg);
		                    
		                    if (mMode == CaptureMode.Body_One_Touch_Mode || mMode == CaptureMode.Industrial_Pressed_Measure_Mode)
		                    {
		                    	Thread.sleep(3000);
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

	
	@SuppressLint("HandlerLeak")
	public final Handler mhandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
				Thermomete item = deviceoperation.getThermomete();
				
				if (item == null) 
				{
					ShowToast("無法取得溫度資料。");
					return;
				}
				
				if (deviceoperation.getReadThreadGoing() == false) return;

				String mode = msg.getData().getString("mode");
				
				if (viewTitle != null && mode == "Enginner_Mode")
				{
					viewTitle.append(item.toString() + "\n");
					return;
				}

				
				if (item.getTemperature() < (float)1000 && item.getTemperature() > (float)8)
				{
					ShowDebugMsg("handle Message process.");
					if (txt_Temperature != null)
					{
//							deviceoperation.sethasValue(true);
						txt_Temperature.setText(String.valueOf(item.getTemperature()) + "℃");
					}
//						
					
					
//						ShowDebugMsg("mode = " + mode);
					if (mode == "Industrial_Pressed_Measure_Mode" || mode == "Body_One_Touch_Mode")
					{
						deviceoperation.EnableRead();
						ShowDebugMsg("一鍵模式結束");
					}
					
			
		
				}
		}	
	};

		
	protected void ShowToast(String text)
	{
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
	
	/*
	 * 顯示除錯誤息
	 */
	protected void ShowDebugMsg(String text) {
		if (isDebug) {
			Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
//			viewTitle.append(text + "\n");
		}

	}
	
	protected void ShowEvent(String text)
	{
		if (isDebug) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
		}
	}
	
}
