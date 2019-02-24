package com.allinnovation.device;

public class ThermometeOffSetTable {

	private double mTP = 0;
	private double mTS = 0;
	private double mTemperature = 0;

	public ThermometeOffSetTable() {

	}

	public ThermometeOffSetTable(double TP, double TS, double Temperature) {
		mTP = TP;
		mTS = TS;
		mTemperature = Temperature;
	}

	public double getTP() {
		return mTP;
	}

	public double getTS() {
		return mTS;
	}

	public double getTemperature() {
		return mTemperature;
	}

}
