package com.allinnovation.flexthermometeapp;

import com.allinnovation.device.DeviceActivity;
import com.allinnovation.device.Thermomete;
import com.allinnovation.utility.helper;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IndustrialMode extends DeviceActivity implements OnClickListener {

	// public static View rootView;
	int mode = 0;
	protected static TextView txt_Temperature;
	protected static TextView txt_PeakTemperature;
	protected static TextView txt_MaxTemperature;
	protected static TextView txt_MinTemperature;
	protected static TextView txt_MaxMinTemperature;
	protected static TextView txt_AvgTemperature;
	
	/*
	 * // public IndustrialMode() // { // // } // // public static final
	 * IndustrialMode newInstance() { // IndustrialMode f = new
	 * IndustrialMode(); // return f; // }
	 * 
	 * // public static final IndustrialMode newInstance(int mode) { //
	 * IndustrialMode f = IndustrialMode.newInstance(); // // // Supply index
	 * input as an argument. // Bundle args = new Bundle(); //
	 * args.putInt("mode", mode); // f.setArguments(args); // // return f; // }
	 * 
	 * // public int getMode() { // if (getArguments() == null) return 0; // if
	 * (!getArguments().containsKey("mode")) return 0; // return
	 * getArguments().getInt("mode", 0); // }
	 * 
	 * @Override public void onCreate(Bundle savedInstanceState) { // TODO
	 * Auto-generated method stub super.onCreate(savedInstanceState); //
	 * System.out.println("IndustrialMode--onCreate"); }
	 * 
	 * @Override public View onCreateView( Bundle savedInstanceState) { // if
	 * (container == null) return null;
	 * 
	 * 
	 * if (mode == 0) rootView =
	 * inflater.inflate(R.layout.activity_industrial_mode, container, false);
	 * else rootView = inflater.inflate(R.layout.activity_industrial_mode2,
	 * container, false); InitComponent();
	 * System.out.println("IndustrialMode--onCreateView"); return rootView; }
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		InitHandler();

		// 客製化Title bar
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_industrial_mode);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.industrial_title_bar);

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
						mVibrator.vibrate(100);
						/*
						 * // Bundle args = new Bundle(); // args.putInt("mode",
						 * 0); // try { // // setData(args); //
						 * FragmentTran(this.getClass(), getFragmentManager(),
						 * // R.id.pager, args, 2); // } catch
						 * (java.lang.InstantiationException e) { // // TODO
						 * 自動產生的 catch 區塊 // e.printStackTrace(); // }
						 */
						setContentView(R.layout.activity_industrial_mode);
						InitComponent();
						return;
					}
				});

		((ImageButton) this.findViewById(R.id.ibtnpmmode))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mode = 1;
						mVibrator.vibrate(100);
						/*
						 * // Bundle args = new Bundle(); // args.putInt("mode",
						 * 1); // // try { // // setData(args); //
						 * FragmentTran(this.getClass(), getFragmentManager(),
						 * // R.id.pager, args, 1); // // FragmentChangeView();
						 * // } catch (java.lang.InstantiationException e) { //
						 * // TODO 自動產生的 catch 區塊 // e.printStackTrace(); // }
						 */
						setContentView(R.layout.activity_industrial_mode2);
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
				txt_Temperature, "00.0" + Unit);
		
//		drawGraphic();
		drawGraphicScrollable();
	}


	/*
	 * // public void setData(Bundle args) { // this.setArguments(args);
	 * 
	 * // getActivity().setContentView(getView()); // FragmentTransaction ft =
	 * null; // // The reload fragment code here ! // if (this.isDetached()) {
	 * // ft = getFragmentManager().beginTransaction(); // ft.detach(this); //
	 * // ft.attach(this).commit(); // } // }
	 * 
	 * // public void FragmentChangeView() // { // FragmentManager
	 * fragmentManager2 = getFragmentManager(); // FragmentTransaction
	 * fragmentTransaction2 = fragmentManager2.beginTransaction(); //
	 * IndustrialMode fragment2 = new IndustrialMode(); //
	 * fragmentTransaction2.addToBackStack("abc"); //
	 * fragmentTransaction2.hide(IndustrialMode.this); //
	 * fragmentTransaction2.add(R.id.pager, fragment2); //
	 * fragmentTransaction2.commit(); // }
	 * 
	 * // public static void FragmentTran(Class fragment, FragmentManager fm,
	 * int LayoutId, Bundle bundle, int mode) throws
	 * java.lang.InstantiationException // { // FragmentTransaction ft =
	 * fm.beginTransaction(); // try { // Fragment newFragment = (Fragment)
	 * IndustrialMode.newInstance(mode); // // if (bundle != null) //
	 * newFragment.setArguments(bundle); // // ft.replace(LayoutId,
	 * IndustrialMode.newInstance(mode)); ////
	 * ft.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK); //
	 * ft.addToBackStack(fragment.getName());
	 * //此為讓popBackStack可以回到指定fragment的關鍵。 // // ft.commit(); // } catch
	 * (InstantiationException e1) { // // TODO Auto-generated catch block //
	 * e1.printStackTrace(); //// } catch (IllegalAccessException e1) { //// //
	 * TODO Auto-generated catch block //// e1.printStackTrace(); // } // // }
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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

				if (deviceoperation.getReadThreadGoing() == false) {
					System.out
							.println("deviceoperation.getReadThreadGoing() == false");
					return;
				}
				
				if (item == null) {
//					ShowToast("無法取得溫度資料。");
					return;
				}
				
				TemperatureCondition(item);

				com.allinnovation.utility.helper.setTextViewWithUnderLine(
						txt_Temperature, item.getTemperature(), Unit);

				com.allinnovation.utility.helper.setTextViewWithNumberFormat(
						txt_PeakTemperature,
						deviceoperation.getMaxTemperture(), Unit);

				com.allinnovation.utility.helper.setTextViewWithNumberFormat(
						txt_AvgTemperature, deviceoperation.getAvgTemperture(),
						Unit);
				com.allinnovation.utility.helper.setTextViewWithNumberFormat(
						txt_MaxMinTemperature,
						deviceoperation.getMaxMinTemperture(), Unit);
				com.allinnovation.utility.helper.setTextViewWithNumberFormat(
						txt_MaxTemperature, deviceoperation.getMaxTemperture(),
						Unit);
				com.allinnovation.utility.helper.setTextViewWithNumberFormat(
						txt_MinTemperature, deviceoperation.getMinTemperture(),
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

		mVibrator.vibrate(100);
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
