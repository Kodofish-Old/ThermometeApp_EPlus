/**
 * 
 */
package com.allinnovation.device;

import java.util.ArrayList;

/**
 * @author KodofishChang
 *
 */
public class Thermomete {
	
	char[] mOrignalData;
	boolean mIsValid = false;
	public Thermomete()
	{}
	
	public Thermomete(char[] data) throws Exception
	{
		if (data.length != 9) throw new Exception("parameter's length error") {

			/**
			 * 
			 */
			private static final long serialVersionUID = -1314525729076352523L;
		};
		
		Init(data);
	}
	
	public Thermomete(byte[] data) throws Exception
	{
		if (data.length != 9) throw new Exception("parameter's length error") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			
		};
		
		char[] chars = new char[9];
		int i =0;
		for (i = 0; i < data.length; i++) {
			chars[i] = (char) data[i];
		}
		
		Init(chars);
	}
	
	/*
	 * ﹍て跑计
	 * 
	 */
	private void Init(char[] data)
	{
		mOrignalData = data;
		mIsValid = validData();
	}
	
	/*
	 * check data is valid data format
	 */
	boolean validData()
	{
		boolean rt = true;
		
		if (mOrignalData.length != 9) return false;
		
		if (mOrignalData[0] != (byte)0xaa) return false;
		
		if (mOrignalData[1] != (byte)0xaa) return false;
		
		if (mOrignalData[2] != (byte)0x1) return false;
		
		if (mOrignalData[3] != (byte)0x4) return false;
		
		
		return rt;
	}

	/*
	 * Get Orignal Thermomete Data
	 */
	public char[] getData()
	{
		return mOrignalData;
	}
	
	/*
	 * 獶 Javadoc
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
//		sb.append("Orignal char data = ").append(mOrignalData).append("\n");
		sb.append("Hex = ");
		for(char c: mOrignalData)
		{
			sb.append(Integer.toHexString((int) c)).append(" ");
		}
		sb.append("\n");
		sb.append("TP = ").append(getTP()).append(", TS = ").append(getTS()).append(", C = ").append(getTemperature()).append(", BT = ").append(getBodyTemperature());
		
		
		return sb.toString();
	}
	
	/*
	 * 眔ヘ夹放
	 */
	public float getTP()
	{
		return computeHexToTemperature(mOrignalData[4], mOrignalData[5]);
	}

	/*
	 * 眔吏挂放
	 */
	public float getTS()
	{
		return computeHexToTemperature(mOrignalData[6], mOrignalData[7]);
	}
	
	/*
	 * 眔砰放
	 * 
	 */
	public String getBodyTemperature()
	{
		String TP = String.valueOf(getTP());
		String TS = String.valueOf(getTS());
		return GetThermomenteOffSetValue(TP, TS);
	}
	
	/*
	 * 眔吏挂放
	 */
	public float getTemperature()
	{
		return getTP();
	}
	
	
	/*
	 * OffSet Table 琩高放
	 * 
	 * @param TP ヘ夹放
	 * 
	 * @param TS 吏挂放
	 * 
	 * @return 顺非放
	 */
	protected String GetThermomenteOffSetValue(String TP, String TS)
			throws NumberFormatException {

		return GetThermomenteOffSetValue(Double.parseDouble(TP),
				Double.parseDouble(TS));
	}

	/*
	 * OffSet Table 琩高放
	 * 
	 * @param TP ヘ夹放
	 * 
	 * @param TS 吏挂放
	 * 
	 * @return 顺非放
	 */
	protected String GetThermomenteOffSetValue(Double TP, Double TS) {
		ArrayList<ThermometeOffSetTable> table = createThermometeOffSetTable();
		ThermometeOffSetTable t1 = new ThermometeOffSetTable();
		ThermometeOffSetTable t2 = new ThermometeOffSetTable();;
		ThermometeOffSetTable t3 = new ThermometeOffSetTable();;
		ThermometeOffSetTable t4 = new ThermometeOffSetTable();;

		for (ThermometeOffSetTable item : table) {
			if (item.getTS() == TS && item.getTP() == TP) return String.valueOf(TP + item.getTemperature());
			
			//TODO: 耞Α惠璶穝喷靡
			if (item.getTP() < TP && item.getTS() < TS &&
					((item.getTP() > t1.getTP() || t1.getTP() == 0) && (item.getTS() > t1.getTS() || t1.getTS() == 0))) t1 = item;
			if (item.getTP() > TP && item.getTS() < TS && 
					((item.getTP() < t2.getTP() || t2.getTP() == 0) && (item.getTS() > t2.getTS() || t2.getTS() == 0))) t2 = item;
			if (item.getTP() < TP && item.getTS() > TS && 
					((item.getTP() > t3.getTP() || t3.getTP() == 0) && (item.getTS() < t3.getTS() || t3.getTS() == 0))) t3 = item;
			if (item.getTP() > TP && item.getTS() > TS &&
					((item.getTP() < t4.getTP() || t4.getTP() == 0) && (item.getTS() < t4.getTS() || t4.getTS() == 0))) t4 = item;
		}
		
		double s1 = (t1.getTemperature() + t2.getTemperature())/2;
		double s2 = (t3.getTemperature() + t4.getTemperature())/2;
		double s = (s1 + s2)/2;
		
		s = Math.floor(s/.01)*.01;
		
		return String.valueOf(TP + s);
	}
		
	/*
	 * 盢ㄢHex value锣传放
	 */
	private float computeHexToTemperature(char v1, char v2) {

		String HexValue = convertcharToHexString(v1) + convertcharToHexString(v2);
		float Decimal = ((float) (Integer.parseInt(HexValue, 16))) / 10;
		return Decimal;
//		return String.format("%.1f", Decimal);

	}
	
	/*
	 * Convert char to Hex String
	 */
	private String convertcharToHexString(char v)
	{
		String Hex = Integer.toHexString((int) v);
		
		if (Hex.length() > 2) Hex = Hex.replace("ff",  "");
		
		return Hex;
	}

	protected String ConvertCharToHex(char[] chars, int length) {
		StringBuffer hex = new StringBuffer();
		for (int i = 0; i < length; i++) {
			hex.append(Integer.toHexString((int) chars[i]) + "\n");
		}
		return hex.toString();
	}

	protected String convertStringToHex(String str) {

		char[] chars = str.toCharArray();

		StringBuffer hex = new StringBuffer();
		for (int i = 0; i < chars.length; i++) {
			hex.append(Integer.toHexString((int) chars[i]));
		}

		return hex.toString();
	}

	protected String convertHexToString(String hex) {

		StringBuilder sb = new StringBuilder();
		StringBuilder temp = new StringBuilder();

		// 49204c6f7665204a617661 split into two characters 49, 20, 4c...
		for (int i = 0; i < hex.length() - 1; i += 2) {

			// grab the hex in pairs
			String output = hex.substring(i, (i + 2));
			// convert hex to decimal
			int decimal = Integer.parseInt(output, 16);
			// convert the decimal to character
			sb.append((char) decimal);

			temp.append(decimal);
		}
		System.out.println("Decimal : " + temp.toString());

		return sb.toString();
	}
	
	private ArrayList<ThermometeOffSetTable> createThermometeOffSetTable() {
		ArrayList<ThermometeOffSetTable> table = new ArrayList<ThermometeOffSetTable>();
		table.add(new ThermometeOffSetTable(37, 10.0, 1.8));
		table.add(new ThermometeOffSetTable(37, 11.0, 1.8));
		table.add(new ThermometeOffSetTable(37, 12.0, 1.7));
		table.add(new ThermometeOffSetTable(37, 13.0, 1.6));
		table.add(new ThermometeOffSetTable(37, 14.0, 1.6));
		table.add(new ThermometeOffSetTable(37, 15.0, 1.5));
		table.add(new ThermometeOffSetTable(37, 16.0, 1.4));
		table.add(new ThermometeOffSetTable(37, 17.0, 1.4));
		table.add(new ThermometeOffSetTable(37, 18.0, 1.3));
		table.add(new ThermometeOffSetTable(37, 19.0, 1.2));
		table.add(new ThermometeOffSetTable(37, 20.0, 1.2));
		table.add(new ThermometeOffSetTable(37, 21.0, 1.1));
		table.add(new ThermometeOffSetTable(37, 22.0, 1.0));
		table.add(new ThermometeOffSetTable(37, 23.0, 1.0));
		table.add(new ThermometeOffSetTable(37, 24.0, 0.9));
		table.add(new ThermometeOffSetTable(37, 25.0, 0.8));
		table.add(new ThermometeOffSetTable(37, 26.0, 0.8));
		table.add(new ThermometeOffSetTable(37, 27.0, 0.7));
		table.add(new ThermometeOffSetTable(37, 28.0, 0.6));
		table.add(new ThermometeOffSetTable(37, 29.0, 0.5));
		table.add(new ThermometeOffSetTable(37, 30.0, 0.5));
		table.add(new ThermometeOffSetTable(37, 31.0, 0.4));
		table.add(new ThermometeOffSetTable(37, 32.0, 0.3));
		table.add(new ThermometeOffSetTable(37, 33.0, 0.3));
		table.add(new ThermometeOffSetTable(37, 34.0, 0.2));
		table.add(new ThermometeOffSetTable(37, 35.0, 0.1));
		table.add(new ThermometeOffSetTable(37, 36.0, 0.1));
		table.add(new ThermometeOffSetTable(37, 37.0, 0.0));
		table.add(new ThermometeOffSetTable(37, 38.0, -0.1));
		table.add(new ThermometeOffSetTable(37, 39.0, -0.1));
		table.add(new ThermometeOffSetTable(37, 40.0, -0.2));
		table.add(new ThermometeOffSetTable(37, 41.0, -0.3));
		table.add(new ThermometeOffSetTable(37, 42.0, -0.3));

		table.add(new ThermometeOffSetTable(38, 10.0, 1.9));
		table.add(new ThermometeOffSetTable(38, 11.0, 1.8));
		table.add(new ThermometeOffSetTable(38, 12.0, 1.8));
		table.add(new ThermometeOffSetTable(38, 13.0, 1.7));
		table.add(new ThermometeOffSetTable(38, 14.0, 1.6));
		table.add(new ThermometeOffSetTable(38, 15.0, 1.6));
		table.add(new ThermometeOffSetTable(38, 16.0, 1.5));
		table.add(new ThermometeOffSetTable(38, 17.0, 1.4));
		table.add(new ThermometeOffSetTable(38, 18.0, 1.4));
		table.add(new ThermometeOffSetTable(38, 19.0, 1.3));
		table.add(new ThermometeOffSetTable(38, 20.0, 1.2));
		table.add(new ThermometeOffSetTable(38, 21.0, 1.2));
		table.add(new ThermometeOffSetTable(38, 22.0, 1.1));
		table.add(new ThermometeOffSetTable(38, 23.0, 1.0));
		table.add(new ThermometeOffSetTable(38, 24.0, 1.0));
		table.add(new ThermometeOffSetTable(38, 25.0, 0.9));
		table.add(new ThermometeOffSetTable(38, 26.0, 0.8));
		table.add(new ThermometeOffSetTable(38, 27.0, 0.8));
		table.add(new ThermometeOffSetTable(38, 28.0, 0.7));
		table.add(new ThermometeOffSetTable(38, 29.0, 0.6));
		table.add(new ThermometeOffSetTable(38, 30.0, 0.5));
		table.add(new ThermometeOffSetTable(38, 31.0, 0.5));
		table.add(new ThermometeOffSetTable(38, 32.0, 0.4));
		table.add(new ThermometeOffSetTable(38, 33.0, 0.3));
		table.add(new ThermometeOffSetTable(38, 34.0, 0.3));
		table.add(new ThermometeOffSetTable(38, 35.0, 0.2));
		table.add(new ThermometeOffSetTable(38, 36.0, 0.1));
		table.add(new ThermometeOffSetTable(38, 37.0, 0.1));
		table.add(new ThermometeOffSetTable(38, 38.0, 0.0));
		table.add(new ThermometeOffSetTable(38, 39.0, -0.1));
		table.add(new ThermometeOffSetTable(38, 40.0, -0.1));
		table.add(new ThermometeOffSetTable(38, 41.0, -0.2));
		table.add(new ThermometeOffSetTable(38, 42.0, -0.3));

		table.add(new ThermometeOffSetTable(39, 10.0, 2.0));
		table.add(new ThermometeOffSetTable(39, 11.0, 1.9));
		table.add(new ThermometeOffSetTable(39, 12.0, 1.8));
		table.add(new ThermometeOffSetTable(39, 13.0, 1.8));
		table.add(new ThermometeOffSetTable(39, 14.0, 1.7));
		table.add(new ThermometeOffSetTable(39, 15.0, 1.6));
		table.add(new ThermometeOffSetTable(39, 16.0, 1.6));
		table.add(new ThermometeOffSetTable(39, 17.0, 1.5));
		table.add(new ThermometeOffSetTable(39, 18.0, 1.4));
		table.add(new ThermometeOffSetTable(39, 19.0, 1.4));
		table.add(new ThermometeOffSetTable(39, 20.0, 1.3));
		table.add(new ThermometeOffSetTable(39, 21.0, 1.2));
		table.add(new ThermometeOffSetTable(39, 22.0, 1.2));
		table.add(new ThermometeOffSetTable(39, 23.0, 1.1));
		table.add(new ThermometeOffSetTable(39, 24.0, 1.0));
		table.add(new ThermometeOffSetTable(39, 25.0, 1.0));
		table.add(new ThermometeOffSetTable(39, 26.0, 0.9));
		table.add(new ThermometeOffSetTable(39, 27.0, 0.8));
		table.add(new ThermometeOffSetTable(39, 28.0, 0.8));
		table.add(new ThermometeOffSetTable(39, 29.0, 0.7));
		table.add(new ThermometeOffSetTable(39, 30.0, 0.6));
		table.add(new ThermometeOffSetTable(39, 31.0, 0.5));
		table.add(new ThermometeOffSetTable(39, 32.0, 0.5));
		table.add(new ThermometeOffSetTable(39, 33.0, 0.4));
		table.add(new ThermometeOffSetTable(39, 34.0, 0.3));
		table.add(new ThermometeOffSetTable(39, 35.0, 0.3));
		table.add(new ThermometeOffSetTable(39, 36.0, 0.2));
		table.add(new ThermometeOffSetTable(39, 37.0, 0.1));
		table.add(new ThermometeOffSetTable(39, 38.0, 0.1));
		table.add(new ThermometeOffSetTable(39, 39.0, 0.0));
		table.add(new ThermometeOffSetTable(39, 40.0, -0.1));
		table.add(new ThermometeOffSetTable(39, 41.0, -0.1));
		table.add(new ThermometeOffSetTable(39, 42.0, -0.2));

		table.add(new ThermometeOffSetTable(40, 10.0, 2.0));
		table.add(new ThermometeOffSetTable(40, 11.0, 2.0));
		table.add(new ThermometeOffSetTable(40, 12.0, 1.9));
		table.add(new ThermometeOffSetTable(40, 13.0, 1.8));
		table.add(new ThermometeOffSetTable(40, 14.0, 1.8));
		table.add(new ThermometeOffSetTable(40, 15.0, 1.7));
		table.add(new ThermometeOffSetTable(40, 16.0, 1.6));
		table.add(new ThermometeOffSetTable(40, 17.0, 1.6));
		table.add(new ThermometeOffSetTable(40, 18.0, 1.5));
		table.add(new ThermometeOffSetTable(40, 19.0, 1.4));
		table.add(new ThermometeOffSetTable(40, 20.0, 1.4));
		table.add(new ThermometeOffSetTable(40, 21.0, 1.3));
		table.add(new ThermometeOffSetTable(40, 22.0, 1.2));
		table.add(new ThermometeOffSetTable(40, 23.0, 1.2));
		table.add(new ThermometeOffSetTable(40, 24.0, 1.1));
		table.add(new ThermometeOffSetTable(40, 25.0, 1.0));
		table.add(new ThermometeOffSetTable(40, 26.0, 1.0));
		table.add(new ThermometeOffSetTable(40, 27.0, 0.9));
		table.add(new ThermometeOffSetTable(40, 28.0, 0.8));
		table.add(new ThermometeOffSetTable(40, 29.0, 0.8));
		table.add(new ThermometeOffSetTable(40, 30.0, 0.7));
		table.add(new ThermometeOffSetTable(40, 31.0, 0.6));
		table.add(new ThermometeOffSetTable(40, 32.0, 0.5));
		table.add(new ThermometeOffSetTable(40, 33.0, 0.5));
		table.add(new ThermometeOffSetTable(40, 34.0, 0.4));
		table.add(new ThermometeOffSetTable(40, 35.0, 0.3));
		table.add(new ThermometeOffSetTable(40, 36.0, 0.3));
		table.add(new ThermometeOffSetTable(40, 37.0, 0.2));
		table.add(new ThermometeOffSetTable(40, 38.0, 0.1));
		table.add(new ThermometeOffSetTable(40, 39.0, 0.1));
		table.add(new ThermometeOffSetTable(40, 40.0, 0.0));
		table.add(new ThermometeOffSetTable(40, 41.0, -0.1));
		table.add(new ThermometeOffSetTable(40, 42.0, -0.1));

		table.add(new ThermometeOffSetTable(41, 10.0, 2.1));
		table.add(new ThermometeOffSetTable(41, 11.0, 2.0));
		table.add(new ThermometeOffSetTable(41, 12.0, 2.0));
		table.add(new ThermometeOffSetTable(41, 13.0, 1.9));
		table.add(new ThermometeOffSetTable(41, 14.0, 1.8));
		table.add(new ThermometeOffSetTable(41, 15.0, 1.8));
		table.add(new ThermometeOffSetTable(41, 16.0, 1.7));
		table.add(new ThermometeOffSetTable(41, 17.0, 1.6));
		table.add(new ThermometeOffSetTable(41, 18.0, 1.6));
		table.add(new ThermometeOffSetTable(41, 19.0, 1.5));
		table.add(new ThermometeOffSetTable(41, 20.0, 1.4));
		table.add(new ThermometeOffSetTable(41, 21.0, 1.4));
		table.add(new ThermometeOffSetTable(41, 22.0, 1.3));
		table.add(new ThermometeOffSetTable(41, 23.0, 1.2));
		table.add(new ThermometeOffSetTable(41, 24.0, 1.2));
		table.add(new ThermometeOffSetTable(41, 25.0, 1.1));
		table.add(new ThermometeOffSetTable(41, 26.0, 1.0));
		table.add(new ThermometeOffSetTable(41, 27.0, 1.0));
		table.add(new ThermometeOffSetTable(41, 28.0, 0.9));
		table.add(new ThermometeOffSetTable(41, 29.0, 0.8));
		table.add(new ThermometeOffSetTable(41, 30.0, 0.8));
		table.add(new ThermometeOffSetTable(41, 31.0, 0.7));
		table.add(new ThermometeOffSetTable(41, 32.0, 0.6));
		table.add(new ThermometeOffSetTable(41, 33.0, 0.5));
		table.add(new ThermometeOffSetTable(41, 34.0, 0.5));
		table.add(new ThermometeOffSetTable(41, 35.0, 0.4));
		table.add(new ThermometeOffSetTable(41, 36.0, 0.3));
		table.add(new ThermometeOffSetTable(41, 37.0, 0.3));
		table.add(new ThermometeOffSetTable(41, 38.0, 0.2));
		table.add(new ThermometeOffSetTable(41, 39.0, 0.1));
		table.add(new ThermometeOffSetTable(41, 40.0, 0.1));
		table.add(new ThermometeOffSetTable(41, 41.0, 0.0));
		table.add(new ThermometeOffSetTable(41, 42.0, -0.1));

		table.add(new ThermometeOffSetTable(42, 10.0, 2.2));
		table.add(new ThermometeOffSetTable(42, 11.0, 2.1));
		table.add(new ThermometeOffSetTable(42, 12.0, 2.0));
		table.add(new ThermometeOffSetTable(42, 13.0, 2.0));
		table.add(new ThermometeOffSetTable(42, 14.0, 1.9));
		table.add(new ThermometeOffSetTable(42, 15.0, 1.8));
		table.add(new ThermometeOffSetTable(42, 16.0, 1.8));
		table.add(new ThermometeOffSetTable(42, 17.0, 1.7));
		table.add(new ThermometeOffSetTable(42, 18.0, 1.6));
		table.add(new ThermometeOffSetTable(42, 19.0, 1.6));
		table.add(new ThermometeOffSetTable(42, 20.0, 1.5));
		table.add(new ThermometeOffSetTable(42, 21.0, 1.4));
		table.add(new ThermometeOffSetTable(42, 22.0, 1.4));
		table.add(new ThermometeOffSetTable(42, 23.0, 1.3));
		table.add(new ThermometeOffSetTable(42, 24.0, 1.2));
		table.add(new ThermometeOffSetTable(42, 25.0, 1.2));
		table.add(new ThermometeOffSetTable(42, 26.0, 1.1));
		table.add(new ThermometeOffSetTable(42, 27.0, 1.0));
		table.add(new ThermometeOffSetTable(42, 28.0, 1.0));
		table.add(new ThermometeOffSetTable(42, 29.0, 0.9));
		table.add(new ThermometeOffSetTable(42, 30.0, 0.8));
		table.add(new ThermometeOffSetTable(42, 31.0, 0.8));
		table.add(new ThermometeOffSetTable(42, 32.0, 0.7));
		table.add(new ThermometeOffSetTable(42, 33.0, 0.6));
		table.add(new ThermometeOffSetTable(42, 34.0, 0.5));
		table.add(new ThermometeOffSetTable(42, 35.0, 0.5));
		table.add(new ThermometeOffSetTable(42, 36.0, 0.4));
		table.add(new ThermometeOffSetTable(42, 37.0, 0.3));
		table.add(new ThermometeOffSetTable(42, 38.0, 0.3));
		table.add(new ThermometeOffSetTable(42, 39.0, 0.2));
		table.add(new ThermometeOffSetTable(42, 40.0, 0.1));
		table.add(new ThermometeOffSetTable(42, 41.0, 0.1));
		table.add(new ThermometeOffSetTable(42, 42.0, 0.0));

		return table;
	}

}
