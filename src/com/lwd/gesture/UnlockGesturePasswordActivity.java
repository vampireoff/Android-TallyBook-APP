package com.lwd.gesture;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.lwd.gesture.LockPatternView.Cell;
import com.lwd.mydiary.App;
import com.lwd.mydiary.ExitApplication;
import com.lwd.mydiary.MainActivity;
import com.lwd.mydiary.MyConfig;
import com.lwd.mydiary.R;

/**
 * 手势密码解锁界面
 * @author Administrator
 *
 */
@SuppressLint("HandlerLeak")
public class UnlockGesturePasswordActivity extends Activity {
	private LockPatternView mLockPatternView;
	private int mFailedPatternAttemptsSinceLastTimeout = 0;
	private CountDownTimer mCountdownTimer = null;
	private TextView mHeadTextView;
	private Animation mShakeAnim;
	private Intent intent;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.gesturepassword_unlock);
		
		MyConfig.isUnlockShow = true;
		intent = getIntent();
		ExitApplication.getInstance().addActivity(this);
		mLockPatternView = (LockPatternView) this
				.findViewById(R.id.gesturepwd_unlock_lockview);
		mLockPatternView.setOnPatternListener(mChooseNewLockPatternListener);
		mLockPatternView.setTactileFeedbackEnabled(true);
		mHeadTextView = (TextView) findViewById(R.id.gesturepwd_unlock_text);
		mShakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake_x);
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (!App.getInstance().getLockPatternUtils().savedPatternExists()) {
			startActivity(new Intent(this, CreateGesturePasswordActivity.class));
			finish();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		MyConfig.isUnlockShow = false;
		if (mCountdownTimer != null)
			mCountdownTimer.cancel();
	}
	private Runnable mClearPatternRunnable = new Runnable() {
		public void run() {
			mLockPatternView.clearPattern();
		}
	};
	
	protected LockPatternView.OnPatternListener mChooseNewLockPatternListener = new LockPatternView.OnPatternListener() {
		
		public void onPatternStart() {
			mLockPatternView.removeCallbacks(mClearPatternRunnable);
			patternInProgress();
		}
		
		public void onPatternCleared() {
			mLockPatternView.removeCallbacks(mClearPatternRunnable);
		}
		
		public void onPatternDetected(List<LockPatternView.Cell> pattern) {
			if (pattern == null)
				return;
			if (App.getInstance().getLockPatternUtils().checkPattern(pattern)) {
				mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Correct);
				
				Intent intentb;
				if(intent.getStringExtra("type") != null){
					intentb = new Intent(UnlockGesturePasswordActivity.this,
							CreateGesturePasswordActivity.class);
					// 打开新的Activity
					startActivity(intentb);
					
				}else if (intent.getStringExtra("back") != null) {
					
				}else {  //手势正确，进入主界面
					intentb = new Intent(UnlockGesturePasswordActivity.this, MainActivity.class);
					intentb.putExtra("flag", "unlock");
					startActivity(intentb);
				}
				
				MyConfig.isFirst = false;
				finish();
			} else {
				mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
				if (pattern.size() >= LockPatternUtils.MIN_PATTERN_REGISTER_FAIL) {
					mFailedPatternAttemptsSinceLastTimeout++;
					int retry = LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT
							- mFailedPatternAttemptsSinceLastTimeout;
					if (retry >= 0) {
						if (retry == 0){}
//							Toast.makeText(UnlockGesturePasswordActivity.this, "您已5次输错密码，请重新登录", Toast.LENGTH_SHORT).show();
						mHeadTextView.setText("密码错误，还可以再输入" + retry + "次");
						mHeadTextView.setTextColor(Color.RED);
						mHeadTextView.startAnimation(mShakeAnim);
					}
					
				}else{
					Toast.makeText(UnlockGesturePasswordActivity.this, "输入长度不够，请重试", Toast.LENGTH_SHORT).show();
				}
				
				//五次输错密码
				if (mFailedPatternAttemptsSinceLastTimeout >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT) {
//					mHandler.postDelayed(attemptLockout, 2000);
//					Clear();
//					LockPatternUtils.clearLock();
//					MyConfig.isGesture = false;
//					MyConfig.isLogin = false;
//					Intent fiveIntent = new Intent(UnlockGesturePasswordActivity.this, LoginActivity.class);
//					fiveIntent.putExtra("relogin", "five");
//					startActivity(fiveIntent);
//					UnlockGesturePasswordActivity.this.finish();
					if (intent.getStringExtra("type") != null) {
						UnlockGesturePasswordActivity.this.finish();
					}else {
						ExitApplication.getInstance().exit();
					}
					
				} else {
					mLockPatternView.postDelayed(mClearPatternRunnable, 2000);
				}
			}
		}
		
		public void onPatternCellAdded(List<Cell> pattern) {
			
		}
		
		private void patternInProgress() {
		}
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (MyConfig.isFirst) {
				ExitApplication.getInstance().exit();
			}else if (intent.getStringExtra("type") != null) {
				this.finish();
			}
			else {
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
//	/**
//	 * 清除SharedPreferences里保存的数据
//	 */
//	public void Clear()
//	{
//		SharedPreferences.Editor editor = shared.edit();
//		editor.clear();
//		editor.commit();
//	}
	
}
