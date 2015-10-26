package com.lwd.mydiary;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;


public class MyDialog extends Dialog implements OnClickListener {
	private TextView rightTextView;
	private IDialogOnclickInterface dialogOnclickInterface;
	private Context context;

	public MyDialog(Context context) {
		super(context, R.style.MyDialogStyle);
		this.context = context;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_dialog);

		rightTextView = (TextView) findViewById(R.id.textview_d);
		rightTextView.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		dialogOnclickInterface = (IDialogOnclickInterface) context;
		switch (v.getId()) {
		case R.id.textview_d:
			dialogOnclickInterface.rightOnclick();
			break;
		default:
			break;
		}
	}

	public interface IDialogOnclickInterface {

		void rightOnclick();
	}
}
