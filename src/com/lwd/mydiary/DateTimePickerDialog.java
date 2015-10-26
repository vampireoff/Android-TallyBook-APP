package com.lwd.mydiary;
import java.util.Calendar;

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
public class DateTimePickerDialog extends AlertDialog implements OnClickListener
{
	private DateTimePicker mDateTimePicker;
	private Calendar mDate = Calendar.getInstance();
	private OnDateTimeSetListener mOnDateTimeSetListener;
	/**
	 * 
	 * @param context
	 * @param date
	 * @param is 为true的时候只显示年月
	 */
	public DateTimePickerDialog(Context context, long date, boolean is) 
	{
		super(context);
		mDateTimePicker = new DateTimePicker(context, is, 0);
		setView(mDateTimePicker, 0, 0, 0, 0);
		mDateTimePicker.setOnDateTimeChangedListener(new OnDateTimeChangedListener()
		{
			@Override
			public void onDateTimeChanged(DateTimePicker view, int year, int month, int day)
			{
				mDate.set(Calendar.YEAR, year);
				mDate.set(Calendar.MONTH, month);
				mDate.set(Calendar.DAY_OF_MONTH, day);
			}
		});
		
		mDateTimePicker.setOnClickListenered(new OnClickListenered() {
			
			@Override
			public void OnButtonClickListener(View v) {
				// TODO Auto-generated method stub
				if (mOnDateTimeSetListener != null) 
				{
					mOnDateTimeSetListener.OnDateTimeSet(DateTimePickerDialog.this, mDate.getTimeInMillis());
				}
			}
		});
		
		mDate.setTimeInMillis(date);
	}
	
	public interface OnDateTimeSetListener 
	{
		void OnDateTimeSet(AlertDialog dialog, long date);
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
