package com.lwd.mydiary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lwd.mydiary.DateTimePickerDialog.OnDateTimeSetListener;
import com.lwd.mydiary.MyDialog.IDialogOnclickInterface;

/**
 * 账单详细界面
 * @author Administrator
 *
 */
public class InfoActivity extends BaseActivity implements OnClickListener, IDialogOnclickInterface{
	
	private LinearLayout returnView, saveView;
	private ListView listView;
	private Context mcontext = InfoActivity.this;
	private Myadapter aMyadapter;
	private List<Map<String, String>> list = new ArrayList<Map<String,String>>();
	private Map<String, String> map = new HashMap<String, String>();
	private TextView begin, end, totalView, yeView;
	private EditText oEditText, omEditText, bmEditText;
	private Button oButton, aButton, yButton, nButton;
	private int mtotal = 0;
	private ToDoDB toDoDB;
	private SimpleDateFormat format;
	private Intent intent, bIntent;
	private boolean isNew, isEnable, isSave = true;
	private String idString = "";
	private AlertDialog alertDialog;
	private String[] strings = {ToDoDB.FIELD_begin, ToDoDB.FIELD_end, ToDoDB.FIELD_tmoney, 
			ToDoDB.FIELD_total, ToDoDB.FIELD_ymoney, ToDoDB.FIELD_bool};
	private int longClickPosition;
	private MyDialog myDialog;
	private View currentItemView;
	public static String localpath = Environment.getExternalStorageDirectory() + "/MyDiary";
	public static String txtname = "db.txt";
	private File file;
	private boolean dbool;
	
	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.info_activity);
		
		ExitApplication.getInstance().addActivity(this);
		file = new File(localpath);
		if (!file.exists()) {
			file.mkdirs();
		}
		setNeedBackGesture(true);
		myDialog = new MyDialog(mcontext);
		bIntent = new Intent("refresh");
		intent = getIntent();
		idString = intent.getStringExtra("id");
		format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		returnView = (LinearLayout)findViewById(R.id.return_view);
		saveView = (LinearLayout)findViewById(R.id.save_btn);
		listView = (ListView)findViewById(R.id.infolist);
		begin = (TextView)findViewById(R.id.begin);
		end = (TextView)findViewById(R.id.end);
		totalView = (TextView)findViewById(R.id.total);
		yeView = (TextView)findViewById(R.id.ye);
		oEditText = (EditText)findViewById(R.id.edit_object);
		omEditText = (EditText)findViewById(R.id.edit_objectmoney);
		bmEditText = (EditText)findViewById(R.id.edit_bmoney);
		oButton = (Button)findViewById(R.id.object_btn);
		aButton = (Button)findViewById(R.id.alter_btn);
		
		toDoDB = new ToDoDB(mcontext);
		if (intent.getStringExtra("flag").equals("new")) {
			isNew = true;
		}else {
			aButton.setVisibility(View.VISIBLE);
			begin.setText(intent.getStringExtra("begin"));
			end.setText(intent.getStringExtra("end"));
			bmEditText.setText(intent.getStringExtra("total"));
			totalView.setText(intent.getStringExtra("tmoney"));
			yeView.setText(intent.getStringExtra("ymoney"));
			mtotal = stringtoint(intent.getStringExtra("tmoney"));
			bmEditText.setEnabled(false);
			list = toDoDB.query(ToDoDB.TABLE_OBJECT, idString);
		}
		if (list == null) {
			list = new ArrayList<Map<String,String>>();
		}
		aMyadapter = new Myadapter(list);
		listView.setAdapter(aMyadapter);
		listView.setOnItemLongClickListener(longClickListener);
		listView.setOnItemClickListener(itemclick);
		
		returnView.setOnClickListener(this);
		saveView.setOnClickListener(this);
		begin.setOnClickListener(this);
		end.setOnClickListener(this);
		oButton.setOnClickListener(this);
		aButton.setOnClickListener(this);
		
		myDialog.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				currentItemView.setBackgroundResource(R.drawable.infolistback);

			}
		});
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	OnItemClickListener itemclick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			Toast.makeText(mcontext, list.get(arg2).get("time"), Toast.LENGTH_SHORT).show();
		}
	};
	
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
			currentItemView = view;
			longClickPosition = position;
			view.setBackgroundColor(getResources().getColor(R.color.tran_blue));
			DisplayMetrics displayMetrics = new DisplayMetrics();
			Display display = InfoActivity.this.getWindowManager().getDefaultDisplay();
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
		if (!list.get(longClickPosition).get("name").contains("欠")) {
			mtotal -= stringtoint(list.get(longClickPosition).get("price"));
			totalView.setText(inttostring(mtotal));
			yeView.setText(inttostring(TextUtils.isEmpty(bmEditText.getText()) ? 0 : 
				stringtoint(bmEditText.getText().toString()) - mtotal));
		}
		dbool = false;
		list.remove(longClickPosition);
		currentItemView.setBackgroundResource(R.drawable.infolistback);
		aMyadapter.notifyDataSetChanged();
		isSave = false;
		setNeedBackGesture(false);
	}
	
	public class Myadapter extends BaseAdapter{
		
		List<Map<String, String>> adList = new ArrayList<Map<String,String>>();
		LayoutInflater inflater = null;
		
		@SuppressWarnings("static-access")
		public Myadapter(List<Map<String, String>> mList){
			this.adList = mList;
			inflater = inflater.from(mcontext);
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return adList.size();
		}
		
		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHoder holder = null;
			if (convertView == null) {
				holder = new ViewHoder();
				convertView = inflater.inflate(R.layout.infolist_item, null);
				holder.nView = (TextView)convertView.findViewById(R.id.oname);
				holder.mView = (TextView)convertView.findViewById(R.id.ominus);
				holder.aView = (TextView)convertView.findViewById(R.id.oadd);
				holder.pView = (TextView)convertView.findViewById(R.id.oprice);
				holder.imageView = (ImageView)convertView.findViewById(R.id.star);
				convertView.setTag(holder);
			}else {
				holder = (ViewHoder) convertView.getTag();
			}
			
			if (adList.get(position).get("name").contains("欠")) {
				dbool = true;
				holder.imageView.setVisibility(View.VISIBLE);
				if (adList.get(position).get("name").contains("被")) {
					holder.imageView.setImageResource(R.drawable.bq);
				}else {
					holder.imageView.setImageResource(R.drawable.q);
				}
			}else {
				holder.imageView.setVisibility(View.GONE);
			}
			holder.nView.setText(adList.get(position).get("name"));
			holder.pView.setText(adList.get(position).get("price"));
			holder.mView.setOnClickListener(new mViewclick(position));
			holder.aView.setOnClickListener(new aViewclick(position));
			return convertView;
		}
	}
	
	public class mViewclick implements OnClickListener{
		int mp;
		
		public mViewclick(int p){
			this.mp = p;
		}
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			ShowDialog(0, true, mp);
		}
		
	}
	
	public class aViewclick implements OnClickListener{
		int mp;
		
		public aViewclick(int p){
			this.mp = p;
		}
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			ShowDialog(1, true, mp);
			
		}
		
	}
	
	public static class ViewHoder{
		TextView nView, mView, aView, pView;
		ImageView imageView;
	}
	
	/**
	 * 显示日期选择对话框
	 * @param m
	 */
	public void ShowDialog(int m, boolean flag, int p)
	{
		if (flag) {
			NumberPickerDialog dialog = new NumberPickerDialog(mcontext, true, m);
			dialog.setOnDateTimeSetListener(new listener(m, p));
			dialog.show();
		}else {
			DateTimePickerDialog dialog = new DateTimePickerDialog(mcontext, System.currentTimeMillis(), false);
			dialog.setOnDateTimeSetListener(new listener2(m));
			dialog.show();
		}
	}
	
	/**
	 * 日期选择对话框确定键监听事件
	 * @author Administrator
	 *
	 */
	public class listener2 implements OnDateTimeSetListener{
		int n;
		
		public listener2(int m){
			this.n = m;
		}
		
		@Override
		public void OnDateTimeSet(AlertDialog dialog, long date) {
			// TODO Auto-generated method stub
			if (n == 0) {
				begin.setText(getStringDate(date));
				dialog.dismiss();
			}else {
				if (compare(getStringDate(date), begin.getText().toString())) {
					Toast.makeText(mcontext, "结束日期不能小于开始日期", Toast.LENGTH_SHORT).show();
				}else {
					end.setText(getStringDate(date));
					dialog.dismiss();
				}
			}
			isSave = false;
			setNeedBackGesture(false);
		}
	};
	
	/**
	 * 比较两个日期
	 * @param date
	 * @param string
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public boolean compare(String string1, String string){
		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		try {
			calendar1.setTime(format.parse(string1));
			calendar2.setTime(format.parse(string));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return calendar1.compareTo(calendar2) < 0;
	}
	
	/**
	 * 加减金额按钮监听
	 * @author Administrator
	 *
	 */
	public class listener implements com.lwd.mydiary.NumberPickerDialog.OnDateTimeSetListener{
		int n, p;
		
		public listener(int m, int pp){
			this.n = m;
			this.p = pp;
		}
		
		@Override
		public void OnDateTimeSet(AlertDialog dialog, int num) {
			// TODO Auto-generated method stub
			if (n == 0) {
				int a = stringtoint(list.get(p).get("price"));
				int b = num;
				if (a - b >= 0 && a - b < 10) {
					list.get(p).put("price", "0" + inttostring(a - b));
				}else if (a - b < 0) {
					list.get(p).put("price", "00");
				}
				else {
					list.get(p).put("price", inttostring(a - b));
				}
				list.get(p).put("time", format.format(new Date()));
				aMyadapter.notifyDataSetChanged();
				
				int ln = 0;
				for (Map<String, String> mmap : list) {
					if (!mmap.get("name").contains("欠")) {
						ln += stringtoint(mmap.get("price"));
					}
				}
				mtotal = ln;
				
				totalView.setText(inttostring(mtotal));
				
				if (!TextUtils.isEmpty(bmEditText.getText())) {
					yeView.setText(inttostring(
							stringtoint(bmEditText.getText().toString()) - mtotal));
				}else {
					yeView.setText(mtotal == 0? "0" : "-" + mtotal);
				}
			}else {
				int a = stringtoint(list.get(p).get("price"));
				int b = num;
				if (a + b >= 0 && a + b < 10) {
					list.get(p).put("price", "0" + inttostring(a + b));
				}else {
					list.get(p).put("price", inttostring(a + b));
				}
				list.get(p).put("time", format.format(new Date()));
				aMyadapter.notifyDataSetChanged();
				
				if (!list.get(p).get("name").contains("欠")) {
					
					mtotal += num;
					totalView.setText(inttostring(mtotal));
					
					if (!TextUtils.isEmpty(bmEditText.getText())) {
						yeView.setText(inttostring(
								stringtoint(bmEditText.getText().toString()) - mtotal));
					}else {
						yeView.setText("-" + mtotal);
					}
				}
			}
			isSave = false;
			setNeedBackGesture(false);
			dialog.dismiss();
		}
		
	};
	/**
	 * 将长时间格式字符串转换为时间 yyyy-MM-dd
	 *
	 */
	@SuppressLint("SimpleDateFormat")
	public static String getStringDate(Long date) 
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = formatter.format(date);
		
		return dateString;
	}
	
	/**
	 * 把string转成int
	 * @param string
	 * @return
	 */
	public int stringtoint(String string){
		return Integer.parseInt(string);
	}
	
	/**
	 * 把string转成int
	 * @param string
	 * @return
	 */
	public String inttostring(int n){
		return String.valueOf(n);
	}
	
	/**
	 * 保存初始金额，计算余额
	 */
	private void savebm(){
		isEnable = false;
		bmEditText.setEnabled(false);
		aButton.setText("修改");
		if (TextUtils.isEmpty(bmEditText.getText().toString())) {
			yeView.setText(mtotal == 0? "0" : "-" + mtotal);
		}else {
			yeView.setText(inttostring(stringtoint(bmEditText.getText().toString()) - mtotal));
		}
	}
	
	/**
	 * 弹出是否保存对话框
	 */
	private void showsavedialog(){
		View view = LayoutInflater.from(mcontext).inflate(R.layout.exit_view, null);
		yButton = (Button)view.findViewById(R.id.y_button);
		nButton = (Button)view.findViewById(R.id.n_button);
		
		alertDialog = new AlertDialog.Builder(mcontext).create();
		alertDialog.setView(view, 0, 0, 0, 0);
		alertDialog.show();
		
		yButton.setOnClickListener(this);
		nButton.setOnClickListener(this);
	}
	
	/**
	 * 保存数据到数据库
	 * @return
	 */
	private boolean Savedata(){
		
		if (begin.getText().toString().contains("日期")) {
			Toast.makeText(mcontext, "请先设置开始日期", Toast.LENGTH_SHORT).show();
		}else if (end.getText().toString().contains("日期")) {
			Toast.makeText(mcontext, "请先设置结束日期", Toast.LENGTH_SHORT).show();
		}else if (isNew && toDoDB.isNewHasDate(begin.getText().toString(), 
				end.getText().toString())) {
			Toast.makeText(mcontext, "日期已存在，请修改日期！", Toast.LENGTH_SHORT).show();
		}else if (!isNew && toDoDB.isHasDate(begin.getText().toString(), 
				end.getText().toString(), idString)) {
			Toast.makeText(mcontext, "日期已存在，请修改日期！", Toast.LENGTH_SHORT).show();
		}else if (list == null || list.size() == 0) {
			Toast.makeText(mcontext, "一条账单信息都没有呢~", Toast.LENGTH_SHORT).show();
		}else {
			Map<String, String> iMap = new HashMap<String, String>();
			if (isNew) {
				iMap.put("begin", begin.getText().toString());
				iMap.put("end", end.getText().toString());
				iMap.put("total", TextUtils.isEmpty(bmEditText.getText()) ? 
						"0" : bmEditText.getText().toString());
				iMap.put("tmoney", totalView.getText().toString());
				iMap.put("ymoney", yeView.getText().toString());
				iMap.put("bool", String.valueOf(dbool));
				try {
					idString = toDoDB.insert(ToDoDB.TABLE_DATE, "", iMap, false);
					for (Map<String, String> iMap2 : list) {
						toDoDB.insert(ToDoDB.TABLE_OBJECT, idString, iMap2, false);
					}
					Toast.makeText(mcontext, "保存成功", Toast.LENGTH_SHORT).show();
					this.sendBroadcast(bIntent);
					isSave = true;
					setNeedBackGesture(true);
					savetotext();
					
					return true;
					
				} catch (Exception e) {
					// TODO: handle exception
					Toast.makeText(mcontext, "保存失败", Toast.LENGTH_SHORT).show();
					Log.i("save_fail", "error:" + e.getMessage().toString());
				}
			}else {
				if (isEnable) {
					savebm();
				}
				List<Map<String, String>> iList = new ArrayList<Map<String,String>>();
				String[] strings2 ={begin.getText().toString(), end.getText().toString(), 
						totalView.getText().toString(), bmEditText.getText().toString(), 
						yeView.getText().toString(), String.valueOf(dbool)};
				
				for (int i = 0; i < strings.length; i++) {
					iMap = new HashMap<String, String>();
					iMap.put("key", strings[i]);
					iMap.put("value", strings2[i]);
					iList.add(iMap);
				}
				try {
					toDoDB.update(ToDoDB.TABLE_DATE, idString, iList);
					toDoDB.deleteo(idString, ToDoDB.TABLE_OBJECT);
					for (Map<String, String> iMap2 : list) {
						toDoDB.insert(ToDoDB.TABLE_OBJECT, idString, iMap2, false);
					}
					Toast.makeText(mcontext, "保存成功", Toast.LENGTH_SHORT).show();
					this.sendBroadcast(bIntent);
					isSave = true;
					setNeedBackGesture(true);
					savetotext();
					
					return true;
				} catch (Exception e) {
					// TODO: handle exception
					Toast.makeText(mcontext, "保存失败", Toast.LENGTH_SHORT).show();
					Log.i("save_fail2", "error:" + e.getMessage().toString());
				}
			}
		}
		return false;
	}
	
	/**
	 * 保存数据到txt文件
	 * @throws IOException 
	 */
	public void savetotext(){
		File mfile = new File(localpath, txtname);
		if (mfile.exists()) {
			mfile.delete();
		}
		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(mfile);
			String dbdata = toDoDB.queryall(ToDoDB.TABLE_OBJECT) + "&&" + toDoDB.queryall(ToDoDB.TABLE_DATE);
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
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isSave) {
				this.finish();
			}else {
				showsavedialog();
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.return_view:
			if (isSave) {
				this.finish();
			}else {
				showsavedialog();
			}
			break;
		case R.id.y_button:
			alertDialog.dismiss();
			if (Savedata()) {
				this.finish();
			}
			break;
		case R.id.n_button:
			alertDialog.dismiss();
			this.finish();
			break;
		case R.id.alter_btn:
			if (isEnable) {
				savebm();
				Toast.makeText(mcontext, "修改成功", Toast.LENGTH_SHORT).show();
			}else {
				isEnable = true;
				bmEditText.setEnabled(true);
				aButton.setText("确定");
			}
			isSave = false;
			setNeedBackGesture(false);
			break;
		case R.id.save_btn:
			if (Savedata()) {
				if (isNew) {
					isNew = false;
					bmEditText.setEnabled(false);
					aButton.setVisibility(View.VISIBLE);
				}
			}
			break;
		case R.id.begin:
			ShowDialog(0, false, 0);
			break;
		case R.id.end:
			ShowDialog(1, false, 0);
			break;
		case R.id.object_btn:
			if (TextUtils.isEmpty(oEditText.getText())) {
				Toast.makeText(mcontext, "消费对象不能为空", Toast.LENGTH_SHORT).show();
			}else if (TextUtils.isEmpty(omEditText.getText())) {
				Toast.makeText(mcontext, "金额不能为空", Toast.LENGTH_SHORT).show();
			}else {
				if (oEditText.getText().toString().contains("\n")) {
					String[] mstrings = oEditText.getText().toString().split("\n");
					for (int i = 0; i < mstrings.length; i++) {
						if (mstrings[i].contains(":")) {
							oEditText.setText(mstrings[i].split(":")[0]);
							omEditText.setText(mstrings[i].split(":")[1].trim());
						}else if (mstrings[i].contains("：")) {
							oEditText.setText(mstrings[i].split("：")[0]);
							omEditText.setText(mstrings[i].split("：")[1].trim());
						}
						addone();
					}
				}else {
					addone();
				}
			}
			break;
			
		default:
			break;
		}
	}
	
	/**
	 * 新增一条消费记录
	 */
	private void addone(){
		try {
			
			boolean ishas = false;
			Map<String, String> nameMap;
			for (int i = 0; i < list.size(); i++){
				nameMap = list.get(i);
				if (nameMap.get("name").equals(oEditText.getText().toString())) {
					ishas = true;
					if (stringtoint(nameMap.get("price")) 
							+ stringtoint(omEditText.getText().toString()) < 10) {
						nameMap.put("price", "0" + inttostring(stringtoint(nameMap.get("price")) 
								+ stringtoint(omEditText.getText().toString())));
					}else {
						nameMap.put("price", inttostring(stringtoint(nameMap.get("price")) 
								+ stringtoint(omEditText.getText().toString())));
					}
					nameMap.put("time", format.format(new Date()));
				}
			}
			if (!ishas){
				map = new HashMap<String, String>();
				map.put("name", oEditText.getText().toString());
				map.put("time", format.format(new Date()));
				if (stringtoint(omEditText.getText().toString()) >= 0 && 
						stringtoint(omEditText.getText().toString()) < 10) {
					map.put("price", "0" + omEditText.getText().toString());
				}else {
					map.put("price", omEditText.getText().toString());
				}
				list.add(0, map);
			}
			
			aMyadapter.notifyDataSetChanged();
			
			if (!oEditText.getText().toString().contains("欠")) {
				
				mtotal += stringtoint(omEditText.getText().toString());
				totalView.setText(inttostring(mtotal));
				
				if (!TextUtils.isEmpty(bmEditText.getText())) {
					yeView.setText(inttostring(
							stringtoint(bmEditText.getText().toString()) - mtotal));
				}else {
					yeView.setText("-" + mtotal);
				}
			}
			
			isSave = false;
			setNeedBackGesture(false);
			oEditText.setText("");
			omEditText.setText("");
			oEditText.setFocusable(true);
			oEditText.requestFocus();
		} catch (Exception e) {
			// TODO: handle exception
			Toast.makeText(mcontext, "导入数据格式有误", Toast.LENGTH_LONG).show();
		}
	}
	
}
