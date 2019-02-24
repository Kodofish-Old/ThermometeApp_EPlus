package com.allinnovation.device;

import java.util.ArrayList;
import java.util.Random;

import com.allinnovation.flexthermometeapp.CaptureMode;
import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/*
 * DeviceOperation class
 */
public class DeviceOperation {// extends FragmentActivity {

	// original ///////////////////////////////
	static Context DeviceUARTContext;
	D2xxManager ftdid2xx;
	FT_Device ftDev = null;

	int DevCount = -1;
	int currentIndex = -1;
	int openIndex = 0;

	/* graphical objects */
	TextView TemperatureText;
	TextView Hex;
	/* 取得資料button */
	Button CheckValueButton;
	/* 寫入資料button */
	Button WriteButton;
	ArrayAdapter<CharSequence> portAdapter;
	/* 是否為可讀取資料flag */
	int iEnableReadFlag = 0;

	// public Handler handler;
	EditText writeText;

	/* local variables */
	int baudRate; /* baud rate */
	byte stopBit; /* 1:1stop bits, 2:2 stop bits */
	byte dataBit; /* 8:8bit, 7: 7bit */
	byte parity; /* 0: none, 1: odd, 2: even, 3: mark, 4: space */
	byte flowControl; /* 0:none, 1: flow control(CTS,RTS) */
	int portNumber; /* port number */
	CaptureMode mCapturemode;
	ArrayList<CharSequence> portNumberList;
	ArrayList<Thermomete> thermometeList;
	
	static final int readLength = 512;
	int readcount = 0;
	int iavailable = 0;

	byte[] readData;
	char[] readDataToText;
	/* 用以判斷是否能進行資料讀取的flag */
	boolean bReadThreadGoing = false;

	// public readThread read_thread;

	boolean uart_configured = false;

	final static String TAG = DeviceOperation.class.getSimpleName();
	final static boolean isDebug = false;
	final boolean bSimulate = true;

	boolean mhasValue = false;
	
	public boolean IshasValue()
	{
		return mhasValue == true;
	}
	
	public void sethasValue(boolean v)
	{
		mhasValue = v;
		
	}
	/*
	 * 初始化參數
	 */
	private void InitVar() {
		readData = new byte[readLength];
		readDataToText = new char[readLength];
		thermometeList = new ArrayList<Thermomete>();
		
		/* by default it is 9600 */
		baudRate = 9600;

		/* default is stop bit 1 */
		stopBit = 1;

		/* default data bit is 8 bit */
		dataBit = 8;

		/* default is none */
		parity = 0;

		/* default flow control is is none */
		flowControl = 0;

		portNumber = 1;

		try {
			ftdid2xx = D2xxManager.getInstance(DeviceUARTContext);
			ShowToast("ftdid2xx get Instance");
		} catch (D2xxManager.D2xxException ex) {
			ex.printStackTrace();
			ShowToast(ex.getMessage());
		}
	}

	// Empty Constructor
	public DeviceOperation(Context parentContext) {
		DeviceUARTContext = parentContext;
		InitVar();
	}


	public boolean IsFTDeviceNull() {
		return (ftDev == null);
	}

	public boolean IsFTDeviceOpen() {
		if (this.IsFTDeviceNull() == true)
			return false;
		return ftDev.isOpen();
	}

	public boolean IsSimulate() {
		return bSimulate;
	}

	/*
	 * 取得目前是否可持續讀取資料
	 * 
	 * @return 
	 * 	true:目前正讀取資料中
	 * 	false:目前無讀取資料
	 */
	public boolean getReadThreadGoing() {
		return bReadThreadGoing;
	}

	/*
	 * 設定ReadThreadGoing
	 */
	public void setReadThreadGoing(boolean v) {
		bReadThreadGoing = v;
	}

	/*
	 * 取得可取得資料byte數
	 * 
	 * @return 回傳byte數
	 */
	public int getiavailable() {
		return iavailable;
	}

	/*
	 * 是否目前為讀取資料中
	 * 
	 * @return 
	 * 	0=為可讀取資料；
	 * 	1=停止讀取資料
	 */
	public int getEnableRead() {
		return iEnableReadFlag;
	}
	
	public void setEnableRead(int v)
	{
		iEnableReadFlag = v;
	}

	/*
	 * 停止讀取資料
	 */
	public void stopRead() {
		bReadThreadGoing = false;
	}

	/**
	 * Hot plug for plug in solution This is workaround before android 4.2 .
	 * Because BroadcastReceiver can not receive ACTION_USB_DEVICE_ATTACHED
	 * broadcast
	 */

	public void onResume() {
		DevCount = 0;
		createDeviceList();
		ShowToast(TAG + " onResume");
		ShowToast("DevCount=" + String.valueOf(DevCount));
		if (DevCount > 0) {
			connectDevice();
			SetConfig(baudRate, dataBit, stopBit, parity, flowControl);
		}
	}

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
	 * menu; this adds items to the action bar if it is present. //
	 * getMenuInflater().inflate(R.menu.main, menu); return true; }
	 * 
	 * @Override public boolean onOptionsItemSelected(MenuItem item) {
	 * 
	 * // switch (item.getItemId()) { // case R.id.action_writedata: //
	 * if(DevCount <= 0 || ftDev == null) // { //
	 * Toast.makeText(DeviceUARTContext, "Device not open yet...",
	 * Toast.LENGTH_SHORT).show(); // } // else if( uart_configured == false) //
	 * { // Toast.makeText(DeviceUARTContext, "UART not configure yet...",
	 * Toast.LENGTH_SHORT).show(); // } // else // { // // SendMessage(); // }
	 * // break; // case R.id.action_exit: // finish(); // break; // }
	 * 
	 * return super.onOptionsItemSelected(item);
	 * 
	 * }
	 */

	/*
	 * 準備進行Device資料讀取
	 */
	public void StartRead() {
		ShowToast(TAG + " StartRead");
		ShowToast("己連接裝置數：" + String.valueOf(DevCount));
		if (IsSimulate() == false)
		{
		if (DevCount <= 0 || ftDev == null) {
			ShowToast("Device not open yet...");
			return;
		}

		if (uart_configured == false) {
			ShowToast("UART not configure yet...");
			return;
		}
		}

		EnableRead();
		connectDevice();
	}

	/*
	 * USB裝置連接
	 */
	public void notifyUSBDeviceAttach() {
		createDeviceList();
	}

	/*
	 * USB裝置解除
	 */
	public void notifyUSBDeviceDetach() {
		disconnectDevice();
	}

	/*
	 * 建立Device清單
	 */
	public void createDeviceList() {
		// ShowToast("CreateDeviceList");
		int tempDevCount = ftdid2xx.createDeviceInfoList(DeviceUARTContext);
		ShowToast("找到裝置 " + tempDevCount + " 個裝置");

		if (bSimulate)
			tempDevCount = 1;

		if (tempDevCount > 0) {
			if (DevCount != tempDevCount) {
				DevCount = tempDevCount;
				updatePortNumberSelector();
			}
		} else {
			DevCount = -1;
			currentIndex = -1;
		}
	}

	/*
	 * 顯示Debug訊息
	 */
	protected void debug(String msg) {
		if (isDebug)
			Log.i(TAG, msg);
	}

	/*
	 * 顯示Toast訊息
	 */
	protected void ShowToast(String text) {
		if (isDebug) {
			Toast.makeText(DeviceUARTContext, text, Toast.LENGTH_SHORT).show();
		}

	}

	/*
	 * 與Device連線，並啟動一個Thread進行持續讀值
	 */
	public void connectDevice() {

		int tmpProtNumber = openIndex + 1;

		if (bSimulate) {
			currentIndex = openIndex;
			return;
		}

		if (currentIndex != openIndex) {
			if (null == ftDev) {
				ftDev = ftdid2xx.openByIndex(DeviceUARTContext, openIndex);
			} else {
				synchronized (ftDev) {
					ftDev = ftdid2xx.openByIndex(DeviceUARTContext, openIndex);
				}
			}
			uart_configured = false;
		} else {
			ShowToast("Device port " + tmpProtNumber + " is already opened");
			// return;
		}

		if (ftDev == null) {
			ShowToast("open device port(" + tmpProtNumber
					+ ") NG, ftDev == null");
			return;
		}

		if (ftDev.isOpen()) {
			currentIndex = openIndex;
			ShowToast("open device port(" + tmpProtNumber + ") OK");

		} else {
			ShowToast("open device port(" + tmpProtNumber + ") NG");
		}
	}
	
	/*
	 * 與Device中斷連線
	 */
	public void disconnectDevice() {
		DevCount = -1;
		currentIndex = -1;
		bReadThreadGoing = false;
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (ftDev != null) {
			synchronized (ftDev) {
				if (ftDev.isOpen()) {
					ftDev.close();
				}
			}
		}
	}

	/*
	 * 啟動讀取
	 */
	public void EnableRead() {
		iEnableReadFlag = (iEnableReadFlag + 1) % 2;

		if (iEnableReadFlag == 1) {
			if (IsSimulate() == false)
			{
				ftDev.purge((byte) (D2xxManager.FT_PURGE_TX));
				ftDev.restartInTask();
				ShowToast("裝置準備工作完成");
			}
//			ShowToast("Read Enabled");
		} else {
			ShowToast("裝置己停止工作");
			if (IsSimulate() == false) ftDev.stopInTask();
		}
		
	}

	

	/* 
	 * 建立模擬資料
	 */
	public byte[] createSimulateData()
	{
			iavailable = 9;
			Random dice = new Random();
			byte[] simulateData = new byte[9];
			simulateData[0] = (byte)0xaa;
			simulateData[1] = (byte)0xaa;
			simulateData[2] = (byte)0x1;
			simulateData[3] = (byte)0x4;
			
			simulateData[4] = (byte)0x1;
			simulateData[4] = Byte.parseByte(String.valueOf(dice.nextInt(16)), 16);// Integer.parseInt("1", 16);
			simulateData[5] = (byte)0x53;
			simulateData[5] = Byte.parseByte(String.valueOf(dice.nextInt(16)), 16);
			simulateData[6] = (byte)0xff;
			simulateData[7] = (byte)0x9c;
			simulateData[8] = (byte)0x48;
 			
			addThermometeToList(simulateData);
			
			return simulateData;
	}
	
	public void readdata() {

		byte[] simulateData = new byte[9];
		
		thermometeList.clear();
		if (bSimulate) {
			
			simulateData = createSimulateData();
			
			return;
		}

		int i;
		synchronized (ftDev) {
			iavailable = ftDev.getQueueStatus();
			if (iavailable > 0) {

				if (iavailable > readLength) {
					iavailable = readLength;
				}

				ftDev.read(readData, iavailable);
				for (i = 0; i < iavailable; i++) {
					if (readData[i] == (byte)0xaa && readData[i+1] == (byte)0xaa)
					{
						if (i+9 <= readData.length)
						{
							int j = 0;
							for (j = 0; j < 9; j++)
							{
								simulateData[j] = readData[i+j];
							}
							
							addThermometeToList(simulateData);
							
							i=i+8;
						}
					}
					
				}
			}
		}
	}
	
	/*
	 * 將溫度資料加入到ArrayList中
	 */
	protected void addThermometeToList(byte[] simulateData)
	{
		Thermomete item = null;
		try {
			item = new Thermomete(simulateData);
		} catch (Exception e) {
			// TODO 自動產生的 catch 區塊
//			e.printStackTrace();
		}
		if (item != null) thermometeList.add(item);
	}

	/*
	 * private void getValueOnece(Handler h) { Handler mHandler = h; int i;
	 * 
	 * if (true == bReadThreadGoing) { try { Thread.sleep(100); } catch
	 * (InterruptedException e) { }
	 * 
	 * //Toast.makeText(null, "開始取得資料", Toast.LENGTH_SHORT).show();
	 * synchronized(ftDev) { iavailable = ftDev.getQueueStatus(); if (bSimulate)
	 * iavailable = 9; if (iavailable > 0) {
	 * 
	 * if(iavailable > readLength){ iavailable = readLength; }
	 * 
	 * if (bSimulate) { //TODO: 建立readDataTotext 陣列
	 * 
	 * } else { ftDev.read(readData, iavailable); for (i = 0; i < iavailable;
	 * i++) { readDataToText[i] = (char) readData[i]; } } Message msg =
	 * mHandler.obtainMessage(); mHandler.sendMessage(msg); ShowToast("已取得資料:" +
	 * msg); } } } }
	 */

	/*
	 * 嗯..目前用途還不明
	 */
	public void updatePortNumberSelector() {
		ShowToast("updatePortNumberSelector:" + DevCount);

		ShowToast(DevCount + " Port device attached");
		/*
		 * if(DevCount == 2) { portAdapter =
		 * ArrayAdapter.createFromResource(DeviceUARTContext,
		 * R.array.port_list_2, R.layout.my_spinner_textview);
		 * portAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
		 * portSpinner.setAdapter(portAdapter);
		 * portAdapter.notifyDataSetChanged();
		 * ShowToast("2 port device attached");
		 * //portSpinner.setOnItemSelectedListener(new
		 * MyOnPortSelectedListener()); } else if(DevCount == 4) { portAdapter =
		 * ArrayAdapter.createFromResource(DeviceUARTContext,
		 * R.array.port_list_4, R.layout.my_spinner_textview);
		 * portAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
		 * portSpinner.setAdapter(portAdapter);
		 * portAdapter.notifyDataSetChanged();
		 * ShowToast("4 port device attached");
		 * //portSpinner.setOnItemSelectedListener(new
		 * MyOnPortSelectedListener()); } else { portAdapter =
		 * ArrayAdapter.createFromResource(DeviceUARTContext,
		 * R.array.port_list_1, R.layout.my_spinner_textview);
		 * portAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
		 * portSpinner.setAdapter(portAdapter);
		 * portAdapter.notifyDataSetChanged();
		 * ShowToast("1 port device attached");
		 * //portSpinner.setOnItemSelectedListener(new
		 * MyOnPortSelectedListener()); }
		 */

	}

	/*
	 * 設定Device參數
	 */
	public void SetConfig(int baud, byte dataBits, byte stopBits, byte parity,
			byte flowControl) {
		if (bSimulate) {
			uart_configured = true;
			return;
		}

		if (ftDev.isOpen() == false) {
			Log.e("j2xx", "SetConfig: device not open");
			return;
		}

		// configure our port
		// reset to UART mode for 232 devices

		ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);

		ftDev.setBaudRate(baud);

		switch (dataBits) {
		case 7:
			dataBits = D2xxManager.FT_DATA_BITS_7;
			break;
		case 8:
			dataBits = D2xxManager.FT_DATA_BITS_8;
			break;
		default:
			dataBits = D2xxManager.FT_DATA_BITS_8;
			break;
		}

		switch (stopBits) {
		case 1:
			stopBits = D2xxManager.FT_STOP_BITS_1;
			break;
		case 2:
			stopBits = D2xxManager.FT_STOP_BITS_2;
			break;
		default:
			stopBits = D2xxManager.FT_STOP_BITS_1;
			break;
		}

		switch (parity) {
		case 0:
			parity = D2xxManager.FT_PARITY_NONE;
			break;
		case 1:
			parity = D2xxManager.FT_PARITY_ODD;
			break;
		case 2:
			parity = D2xxManager.FT_PARITY_EVEN;
			break;
		case 3:
			parity = D2xxManager.FT_PARITY_MARK;
			break;
		case 4:
			parity = D2xxManager.FT_PARITY_SPACE;
			break;
		default:
			parity = D2xxManager.FT_PARITY_NONE;
			break;
		}

		ftDev.setDataCharacteristics(dataBits, stopBits, parity);

		short flowCtrlSetting;
		switch (flowControl) {
		case 0:
			flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
			break;
		case 1:
			flowCtrlSetting = D2xxManager.FT_FLOW_RTS_CTS;
			break;
		case 2:
			flowCtrlSetting = D2xxManager.FT_FLOW_DTR_DSR;
			break;
		case 3:
			flowCtrlSetting = D2xxManager.FT_FLOW_XON_XOFF;
			break;
		default:
			flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
			break;
		}
		ftDev.setFlowControl(flowCtrlSetting, (byte) 0x0b, (byte) 0x0d);

		uart_configured = true;
		ShowToast("Config done!");

	}

	/*
	 * 傳送資料至Device的記憶體
	 */
	public void SendMessage(String writeData) {
		if (ftDev.isOpen() == false) {
			Log.e("j2xx", "SendMessage: device not open");
			return;
		}

		ShowToast("Start send message to Device");

		ftDev.setLatencyTimer((byte) 16);
		ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));

		byte[] OutData = writeData.getBytes();
		ftDev.write(OutData, writeData.length());

	}

	
	public Thermomete getThermomete()
	{
		if (thermometeList == null) return null;
		
		int size = thermometeList.size(); 
		
		if ( size == 0) return null; 
			
		return thermometeList.get(size - 1);
		
	}
	
	public ArrayList<Thermomete> getAllThermomete()
	{
		return thermometeList;
	}
	
	

}
