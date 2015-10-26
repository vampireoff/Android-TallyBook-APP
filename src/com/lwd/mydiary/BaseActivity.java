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
 * 全部activity继承此类，实现手势监听和自动锁屏
 * @author Administrator
 *
 */
public class BaseActivity extends Activity {
	
	private MyUtility utility = new MyUtility(BaseActivity.this);
	/** 手势监听 */
	GestureDetector mGestureDetector;
	/** 是否需要监听手势关闭功能 */
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
			//app 进入后台
			
			//全局变量 记录当前已经进入后台
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
		//app 从后台唤醒，进入前台,锁定屏幕
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
	 * 初始化关闭界面的手势监听
	 */
	private void initGestureDetector() {
		if (mGestureDetector == null) {
			mGestureDetector = new GestureDetector(getApplicationContext(),
					new BackGestureListener(this));
		}
	}
	
	/**
	 * 判断是否开启了手势监听
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
	 * 设置是否进行手势监听
	 */
	public void setNeedBackGesture(boolean mNeedBackGesture){
		this.mNeedBackGesture = mNeedBackGesture;
	}
	
	/*
	 * 返回
	 */
	public void doBack(View view) {
		onBackPressed();
	}
	
}
