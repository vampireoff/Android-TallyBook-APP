package com.lwd.mydiary;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.lwd.gesture.MyUtility;
import com.lwd.gesture.UnlockGesturePasswordActivity;
/**
 * ȫ��activity�̳д��࣬ʵ�����Ƽ������Զ�����
 * @author Administrator
 *
 */
public class BaseActivity extends Activity {
	
	private MyUtility utility = new MyUtility(BaseActivity.this);
	/** ���Ƽ��� */
	GestureDetector mGestureDetector;
	/** �Ƿ���Ҫ�������ƹرչ��� */
	private boolean mNeedBackGesture = false;
	private SharedPreferences onoffShared;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		onoffShared = getSharedPreferences("onoff", Context.MODE_PRIVATE);
		registerScreenActionReceiver();
		initGestureDetector();
	}
	
	private void registerScreenActionReceiver(){    
	    final IntentFilter filter = new IntentFilter();    
	    filter.addAction(Intent.ACTION_SCREEN_OFF);    
//	    filter.addAction(Intent.ACTION_SCREEN_ON);    
	    registerReceiver(receiver, filter);    
	}    
	    
	private final BroadcastReceiver receiver = new BroadcastReceiver(){    
	    
	    @Override    
	    public void onReceive(final Context context, final Intent intent) {    
	        // Do your action here    
	    	MyConfig.isActive = false;
	    }    
	    
	};    
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		initGestureDetector();
		if (!utility.isAppOnForeground()) {
			//app �����̨
			
			//ȫ�ֱ��� ��¼��ǰ�Ѿ������̨
			MyConfig.isActive = false;
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MyConfig.isOnOff = onoffShared.getBoolean("onoff", false);
		if(App.getInstance().getLockPatternUtils().savedPatternExists()){
			MyConfig.isGesture = true;
		}else {
			MyConfig.isGesture = false;
		}
		//app �Ӻ�̨���ѣ�����ǰ̨,������Ļ
		if (!MyConfig.isActive) {
			MyConfig.isActive = true;
			if(MyConfig.isGesture == true && MyConfig.isOnOff == true && 
					MyConfig.isUnlockShow == false){
				Intent intent = new Intent(BaseActivity.this, UnlockGesturePasswordActivity.class);
				intent.putExtra("back", "back");
				startActivity(intent);
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		this.unregisterReceiver(receiver);
	}
	
	/**
	 * ��ʼ���رս�������Ƽ���
	 */
	private void initGestureDetector() {
		if (mGestureDetector == null) {
			mGestureDetector = new GestureDetector(getApplicationContext(),
					new BackGestureListener(this));
		}
	}
	
	/**
	 * �ж��Ƿ��������Ƽ���
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		if(mNeedBackGesture){
			return mGestureDetector.onTouchEvent(ev) || super.dispatchTouchEvent(ev);
		}
		return super.dispatchTouchEvent(ev);
	}
	
	/*
	 * �����Ƿ�������Ƽ���
	 */
	public void setNeedBackGesture(boolean mNeedBackGesture){
		this.mNeedBackGesture = mNeedBackGesture;
	}
	
	/*
	 * ����
	 */
	public void doBack(View view) {
		onBackPressed();
	}
	
}
