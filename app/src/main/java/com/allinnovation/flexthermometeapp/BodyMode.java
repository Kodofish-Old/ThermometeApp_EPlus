package com.allinnovation.flexthermometeapp;

import com.allinnovation.device.DeviceActivity;
import com.allinnovation.device.Thermomete;
import com.allinnovation.utility.helper;
import com.jjoe64.graphview.GraphView.GraphViewData;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BodyMode extends DeviceActivity implements OnClickListener {

	// public static View rootView;
	int mode = 0;
	protected static TextView txt_Temperature;
	protected static TextView txt_PeakTemperature;
	protected static TextView txt_MaxTemperature;
	protected static TextView txt_MinTemperature;
	protected static TextView txt_MaxMinTemperature;
	protected static TextView txt_AvgTemperature;
	
	/*
	 * @Override public View onCreateView(LayoutInflater inflater, ViewGroup
	 * container, Bundle savedInstanceState) {
	 * 
	 * if (savedInstanceState != null) { mode =
	 * savedInstanceState.getInt("data", 0); }
	 * 
	 * if (mode == 0) rootView = inflater.inflate(R.layout.activity_body_mode,
	 * container, false); else rootView =
	 * inflater.inflate(R.layout.activity_body_mode2, container, false); //
	 * Bundle args = getArguments(); InitComponent(); return rootView; }
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		InitHandler();
		
		// 客製化Title bar
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_body_mode);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.body_title_bar);

		InitComponent();
	}

	protected void InitComponent() {
		// 設定點選畫面時開始讀取
				LinearLayout screenView = (LinearLayout) this
						.findViewById(R.id.screenframe);
				screenView.setOnClickListener(this);
				MonitorModeEnd();
				deviceoperation.TempertureClear();
				
		((ImageButton) this.findViewById(R.id.ibtnmmode))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mode = 0;
						setContentView(R.layout.activity_body_mode);
						InitComponent();
						return;
					}
				});

		((ImageButton) this.findViewById(R.id.ibtnpmmode))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mode = 1;
						setContentView(R.layout.activity_body_mode2);
						InitComponent();
						return;
					}
				});

		txt_PeakTemperature = (TextView) this
				.findViewById(R.id.txt_PeakTemperature);
		txt_Temperature = (TextView) this.findViewById(R.id.txt_Temperature);
		txt_MaxTemperature = (TextView) this
				.findViewById(R.id.txt_MaxTemperature);
		txt_MinTemperature = (TextView) this
				.findViewById(R.id.txt_MinTemperature);
		txt_MaxMinTemperature = (TextView) this
				.findViewById(R.id.txt_MaxMinTemperature);
		txt_AvgTemperature = (TextView) this
				.findViewById(R.id.txt_AvgTemperature);
		
		com.allinnovation.utility.helper.setTextViewWithUnderLine(
				(TextView) this.findViewById(R.id.txt_Temperature), "00.0" + Unit);
		drawGraphicScrollable();
	}

	/*
	 * public static void FragmentTran(Class fragment, FragmentManager fm, int
	 * LayoutId, Bundle bundle) throws java.lang.InstantiationException {
	 * FragmentTransaction ft = fm.beginTransaction(); try { Fragment
	 * newFragment = (Fragment) fragment.newInstance(); if (bundle != null)
	 * newFragment.setArguments(bundle);
	 * 
	 * ft.replace(LayoutId, newFragment, fragment.getName());
	 * ft.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
	 * ft.addToBackStack(fragment.getName());
	 * //此為讓popBackStack可以回到指定fragment的關鍵。 ft.commit(); } catch
	 * (InstantiationException e1) { // TODO Auto-generated catch block
	 * e1.printStackTrace(); } catch (IllegalAccessException e1) { // TODO
	 * Auto-generated catch block e1.printStackTrace(); }
	 * 
	 * }
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent();
		switch (item.getItemId()) {
		case R.id.action_industrial_mode:
			intent.setClass(this, IndustrialMode.class);
			break;
		case R.id.action_body_mode:
			intent.setClass(this, BodyMode.class);
			break;
		default:
			intent = null;
			finish();
			break;
		}
		if (intent != null) {
			MonitorModeEnd();
			startActivity(intent);
		}
		return true;
	}

	private void InitHandler() {
		DeviceActivity.mhandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Thermomete item = deviceoperation.getThermomete();

				if (item == null) {
					ShowToast("無法取得溫度資料。");
					return;
				}

				if (deviceoperation.getReadThreadGoing() == false) {
					System.out
							.println("deviceoperation.getReadThreadGoing() == false");
					return;
				}

				com.allinnovation.utility.helper.setTextViewWithUnderLine(
						txt_Temperature, item.getBodyTemperature(), Unit);

				com.allinnovation.utility.helper.setTextViewWithNumberFormat(
						txt_PeakTemperature,
						deviceoperation.getMaxBodyTemperture(), Unit);

				com.allinnovation.utility.helper.setTextViewWithNumberFormat(
						txt_AvgTemperature, deviceoperation.getAvgBodyTemperture(),
						Unit);
				com.allinnovation.utility.helper.setTextViewWithNumberFormat(
						txt_MaxMinTemperature,
						deviceoperation.getMaxMinBodyTemperture(), Unit);
				com.allinnovation.utility.helper.setTextViewWithNumberFormat(
						txt_MaxTemperature, deviceoperation.getMaxBodyTemperture(),
						Unit);
				com.allinnovation.utility.helper.setTextViewWithNumberFormat(
						txt_MinTemperature, deviceoperation.getMinBodyTemperture(),
						Unit);

				if (graph2LastXValue == 0)
				{
					graph2LastXValue += 1d;
					
					TemperatureSerial.resetData(new GraphViewData[] {
						      new GraphViewData(graph2LastXValue, helper.getValueWithNumberFormat(item.getTemperature(), 0))
					});
				}
				else
				{
					graph2LastXValue += 1d;
					TemperatureSerial.appendData(new GraphViewData(graph2LastXValue, helper.getValueWithNumberFormat(item.getTemperature(), 0)), true, 10);
				}
				
				String mode = msg.getData().getString("mode");
				if (mode == "Industrial_Pressed_Measure_Mode"
						|| mode == "Body_One_Touch_Mode")
					deviceoperation.EnableRead();

			}
		};

	}

	@Override
	public void onClick(View v) {
		switch (mode) {
		case 0:
//			ShowToast("Monitor start");
			MonitorModeExecution();
			break;
		case 1:
//			ShowToast("Press Start");
			PressedModeStart();
			break;
		default:
			return;
		}
		
	}

}
