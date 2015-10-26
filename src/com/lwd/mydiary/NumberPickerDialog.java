package com.lwd.mydiary;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;

import com.lwd.mydiary.DateTimePicker.OnClickListenered;
import com.lwd.mydiary.DateTimePicker.OnDateTimeChangedListener;
/**
 * 日期选择对话框
 * @author Administrator
 *
 */
@SuppressLint("SimpleDateFormat")
public class NumberPickerDialog extends AlertDialog implements OnClickListener
{
	private DateTimePicker mDateTimePicker;
	private OnDateTimeSetListener mOnDateTimeSetListener;
	private int num;
	/**
	 * 
	 * @param context
	 * @param date
	 * @param is 为true的时候只显示年月
	 */
	public NumberPickerDialog(Context context, boolean is, int n) 
	{
		super(context);
		mDateTimePicker = new DateTimePicker(context, is, n);
		setView(mDateTimePicker, 0, 0, 0, 0);
		mDateTimePicker.setOnDateTimeChangedListener(new OnDateTimeChangedListener()
		{
			@Override
			public void onDateTimeChanged(DateTimePicker view, int year, int month, int day)
			{
				num = year;
			}
		});
		
		mDateTimePicker.setOnClickListenered(new OnClickListenered() {
			
			@Override
			public void OnButtonClickListener(View v) {
				// TODO Auto-generated method stub
				if (mOnDateTimeSetListener != null) 
				{
					mOnDateTimeSetListener.OnDateTimeSet(NumberPickerDialog.this, num);
				}
			}
		});
		
		num = 2;
	}
	
	public interface OnDateTimeSetListener 
	{
		void OnDateTimeSet(AlertDialog dialog, int num);
	}
	
	public void setOnDateTimeSetListener(OnDateTimeSetListener callBack)
	{
		mOnDateTimeSetListener = callBack;
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		
	}
	
}
