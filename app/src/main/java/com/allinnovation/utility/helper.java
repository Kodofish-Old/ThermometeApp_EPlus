package com.allinnovation.utility;

import java.text.NumberFormat;

import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;

public class helper {

	/*
	 * �]�wTextView�����u
	 * http://stackoverflow.com/questions/2394935/can-i-underline-text-in-an-android-layout
	 * @param view :�]�tTextView��View
	 * @param textviewId�GTextView Id
	 * @param textcontent�GTextView�ҭn�������r
	 */
	public static void setTextViewWithUnderLine(TextView textView, String textcontent)
	{
		if (textView == null) return;
		//TextView textView = (TextView) industrialMode.findViewById(textviewId);
		SpannableString content = new SpannableString(textcontent);
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		textView.setText(content);
		
	}
	
	public static void setTextViewWithUnderLine(TextView textView, double value, String Unit)
	{
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);
		setTextViewWithUnderLine(textView, nf.format(value) + Unit);
	}
	
	public static void setTextViewWithNumberFormat(TextView textView, double value, String Unit)
	{
		if (textView == null) return;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);
		textView.setText(nf.format(value) + Unit);
	}
	
	public static double getValueWithNumberFormat(double value, int digits)
	{
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(digits);
		return Double.parseDouble(nf.format(value));
		
	}
	
	/*
	 * �]�wTextView�����u
	 * http://stackoverflow.com/questions/2394935/can-i-underline-text
	 * -in-an-android-layout
	 */
	protected void setTextViewWithUnderLine(View view, int textviewId, String textcontent) {
		TextView textView = (TextView) view.findViewById(textviewId);
		SpannableString content = new SpannableString(textcontent);
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		textView.setText(content);

	}
	
}
