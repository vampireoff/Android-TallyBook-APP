package com.lwd.mydiary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lwd.gesture.UnlockGesturePasswordActivity;
import com.lwd.mydiary.MyDialog.IDialogOnclickInterface;

public class MainActivity extends BaseActivity implements OnClickListener, IDialogOnclickInterface{

	private Context mcontext = MainActivity.this;
	private TextView topTextView, maintext;
	private ListView listView;
	private Myadapter myadapter;
	private List<Map<String, String>> list = new ArrayList<Map<String,String>>();
	private ToDoDB doDB;
	private boolean isexit;
	private int longClickPosition;
	private MyDialog myDialog;
	private RelativeLayout currentItemView;
	private File file;
	private AlertDialog alertDialog;
	private Button yButton, nButton;
	private SharedPreferences shared;
	private SharedPreferences.Editor editor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		shared = getSharedPreferences("onoff", Context.MODE_PRIVATE);
		editor = shared.edit();
		setNeedBackGesture(false);
		ExitApplication.getInstance().addActivity(this);
		file = new File(InfoActivity.localpath, InfoActivity.txtname);
		IntentFilter filter = new IntentFilter("refresh");
		this.registerReceiver(receiver, filter);
		
		myDialog = new MyDialog(mcontext);
		topTextView = (TextView)findViewById(R.id.top_add);
		maintext = (TextView)findViewById(R.id.maintext);
		listView = (ListView)findViewById(R.id.mainlist);
		doDB = new ToDoDB(mcontext);

		list = doDB.query(ToDoDB.TABLE_DATE, "");
		if (list == null) {
			if (file.exists()) {
				View view = LayoutInflater.from(mcontext).inflate(R.layout.exit_view, null);
				yButton = (Button)view.findViewById(R.id.y_button);
				nButton = (Button)view.findViewById(R.id.n_button);
				TextView textView = (TextView)view.findViewById(R.id.tip_text);
				textView.setText("发现有原先保存的数据，是否同步？");
				yButton.setText("是");
				nButton.setText("否");
				
				alertDialog = new AlertDialog.Builder(mcontext).create();
				alertDialog.setView(view, 0, 0, 0, 0);
				alertDialog.show();
				alertDialog.setCanceledOnTouchOutside(false);
				
				yButton.setOnClickListener(this);
				nButton.setOnClickListener(this);
			}
			list = new ArrayList<Map<String,String>>();
			maintext.setVisibility(View.VISIBLE);
		}else {
			listView.setVisibility(View.VISIBLE);
		}
		myadapter = new Myadapter(list);
		listView.setAdapter(myadapter);
		listView.setOnItemClickListener(new itemlisten());
		listView.setOnItemLongClickListener(longClickListener);
		
		topTextView.setOnClickListener(this);
		maintext.setOnClickListener(this);
		
		myDialog.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				currentItemView.setBackgroundResource(R.drawable.listitem_selector);

			}
		});
	}
	
	OnItemLongClickListener longClickListener = new OnItemLongClickListener() {
		
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View view,
				int position, long arg3) {
			// TODO Auto-generated method stub
			//使用振动器
			Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(100);  //振动1毫秒
			int[] location = new int[2];
			view.getLocationOnScreen(location);
			currentItemView = (RelativeLayout)view.findViewById(R.id.itemview);
			longClickPosition = position;
			currentItemView.setBackgroundResource(R.drawable.listitem_press);
			DisplayMetrics displayMetrics = new DisplayMetrics();
			Display display = MainActivity.this.getWindowManager().getDefaultDisplay();
			display.getMetrics(displayMetrics);
			WindowManager.LayoutParams params = myDialog.getWindow().getAttributes();
			params.gravity = Gravity.BOTTOM;
			params.y =displayMetrics.heightPixels - location[1];
			myDialog.getWindow().setAttributes(params);
			myDialog.setCanceledOnTouchOutside(true);
			myDialog.show();
			return false;
		}
	};
	
	
	@Override
	public void rightOnclick() {
		// TODO Auto-generated method stub
		myDialog.dismiss();
		doDB.deleted(list.get(longClickPosition).get("id"), ToDoDB.TABLE_DATE);
		doDB.deleteo(list.get(longClickPosition).get("id"), ToDoDB.TABLE_OBJECT);
		list.remove(longClickPosition);
		currentItemView.setBackgroundResource(R.drawable.listitem_selector);
		myadapter.notifyDataSetChanged();
		savetotext();
		Toast.makeText(mcontext, "删除成功", Toast.LENGTH_SHORT).show();
		if (list.size() == 0) {
			maintext.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
			if (file.exists()) {
				file.delete();
			}
		}
	}
	
	/**
	 * 保存数据到txt文件
	 * @throws IOException 
	 */
	public void savetotext(){
		
		if (file.exists()) {
			file.delete();
		}
		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(file);
			String dbdata = doDB.queryall(ToDoDB.TABLE_OBJECT) + "&&" + doDB.queryall(ToDoDB.TABLE_DATE);
			if (!dbdata.equals("&&")) {
				byte[] b = dbdata.getBytes();
				outputStream.write(b);
				outputStream.close();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	BroadcastReceiver receiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			maintext.setVisibility(View.GONE);
			list.clear();
			list = doDB.query(ToDoDB.TABLE_DATE, "");
			myadapter.refresh(list);
			listView.setVisibility(View.VISIBLE);
		}
	};
	
	private class itemlisten implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(mcontext, InfoActivity.class);
			intent.putExtra("flag", "item");
			intent.putExtra("id", list.get(position).get("id"));
			intent.putExtra("total", list.get(position).get("total"));
			intent.putExtra("tmoney", list.get(position).get("tmoney"));
			intent.putExtra("ymoney", list.get(position).get("ymoney"));
			intent.putExtra("begin", list.get(position).get("begin"));
			intent.putExtra("end", list.get(position).get("end"));
			startActivity(intent);
		}
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.top_add:
			Intent intent = new Intent(mcontext, InfoActivity.class);
			intent.putExtra("flag", "new");
			startActivity(intent);
			break;
		case R.id.maintext:
			Intent intent2 = new Intent(mcontext, InfoActivity.class);
			intent2.putExtra("flag", "new");
			startActivity(intent2);
			break;
		case R.id.y_button:
			try {
				FileInputStream inputStream = new FileInputStream(file);
				int len = inputStream.available();
				if (len != 0) {
					byte[] b = new byte[len];
					inputStream.read(b);
					String[] strings = EncodingUtils.getString(b, "UTF-8").split("&&");
					JSONArray array = new JSONArray(strings[0]); 
					JSONArray array2 = new JSONArray(strings[1]); 
					JSONObject object = null;
					Map<String, String> jmap = null;
					for (int i = 0; i < array.length(); i++) {
						object = new JSONObject(array.get(i).toString());
						jmap = new HashMap<String, String>();
						jmap.put("id", object.getString("id"));
						jmap.put("mid", object.getString("mid"));
						jmap.put("name", object.getString("name"));
						jmap.put("price", object.getString("price"));
						jmap.put("time", object.getString("time"));
						doDB.insert(ToDoDB.TABLE_OBJECT, "", jmap, true);
					}
					for (int i = 0; i < array2.length(); i++) {
						object = new JSONObject(array2.get(i).toString());
						jmap = new HashMap<String, String>();
						jmap.put("id", object.getString("id"));
						jmap.put("begin", object.getString("begin"));
						jmap.put("end", object.getString("end"));
						jmap.put("total", object.getString("total"));
						jmap.put("tmoney", object.getString("tmoney"));
						jmap.put("ymoney", object.getString("ymoney"));
						jmap.put("time", object.getString("time"));
						jmap.put("bool", object.getString("bool"));
						doDB.insert(ToDoDB.TABLE_DATE, "", jmap, true);
					}
					list = doDB.query(ToDoDB.TABLE_DATE, "");
					myadapter.refresh(list);
					maintext.setVisibility(View.GONE);
					listView.setVisibility(View.VISIBLE);
				}
				inputStream.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			alertDialog.dismiss();
			break;
		case R.id.n_button:
			alertDialog.dismiss();
			break;

		default:
			break;
		}
	}
	
	public static class ViewHolder{
		TextView date, total, time, round;
		ImageView imageView;
	}
	
	private class Myadapter extends BaseAdapter {
		
		List<Map<String, String>> adaList = new ArrayList<Map<String,String>>();
		ViewHolder holder = null;
		LayoutInflater inflater;
		
		@SuppressWarnings("static-access")
		public Myadapter(List<Map<String, String>> list){
			this.adaList = list;
			inflater = inflater.from(MainActivity.this);
		}
		
		public void refresh(List<Map<String, String>> list){
			this.adaList = list;
			notifyDataSetChanged();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.mainlist_item, null);
				holder.date = (TextView)convertView.findViewById(R.id.item_date);
				holder.total = (TextView)convertView.findViewById(R.id.item_total);
				holder.time = (TextView)convertView.findViewById(R.id.item_time);
				holder.round = (TextView)convertView.findViewById(R.id.item_delete);
				holder.imageView = (ImageView)convertView.findViewById(R.id.mstar);
				convertView.setTag(holder);
			}else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			if (adaList.get(position).get("bool").equals("true")) {
				holder.imageView.setVisibility(View.VISIBLE);
				holder.round.setVisibility(View.GONE);
			}else {
				holder.imageView.setVisibility(View.GONE);
				holder.round.setVisibility(View.VISIBLE);
			}
			holder.date.setText(adaList.get(position).get("begin") + " ~ " + 
								adaList.get(position).get("end"));
			holder.total.setText("初：" + adaList.get(position).get("total") + "    总：" + 
								adaList.get(position).get("tmoney") + "    余：" + 
								adaList.get(position).get("ymoney"));
			holder.time.setText(adaList.get(position).get("time"));
			return convertView;
		}
		
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}
		
		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return adaList.size();
		}
	}; 
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
		if (MyConfig.isGesture) {
			menu.add(0, 1, 0, "修改手势密码");
			if (MyConfig.isOnOff) {
				menu.add(0, 2, 0, "手势密码已打开");
			}else {
				menu.add(0, 2, 0, "手势密码已关闭");
			}
		}else {
			menu.add(0, 1, 0, "创建手势密码");
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == 1) {
			Intent intent = new Intent(mcontext, UnlockGesturePasswordActivity.class);
			intent.putExtra("type", "alter");
			startActivity(intent);
			invalidateOptionsMenu();
			return true;
		}else if (id == 2) {
			if (MyConfig.isOnOff) {
				MyConfig.isOnOff = false;
				editor.putBoolean("onoff", false);
				Toast.makeText(mcontext, "手势密码已关闭", Toast.LENGTH_SHORT).show();
			}else {
				MyConfig.isFirst = false;
				MyConfig.isOnOff = true;
				editor.putBoolean("onoff", true);
				Toast.makeText(mcontext, "手势密码已打开", Toast.LENGTH_SHORT).show();
			}
			editor.commit();
			invalidateOptionsMenu();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@SuppressLint("ShowToast")
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			
			if(!isexit){
				isexit = true;
				Toast.makeText(MainActivity.this, "再按一次退出记账神器", 2000).show();
				new Handler().postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						isexit = false;
					}
				}, 2000);
				return false;
			}else {
				this.finish();
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		this.unregisterReceiver(receiver);
		MyConfig.isFirst = true;
	}

}
