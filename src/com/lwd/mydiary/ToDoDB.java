package com.lwd.mydiary;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ToDoDB extends SQLiteOpenHelper {
	
	private final static String DATABASE_NAME = "Diary_db";
	private final static int DATABASE_VERSION = 1;
	public final static String TABLE_OBJECT = "table_Object";
	public final static String TABLE_DATE = "table_Date";
	
	public final static String FIELD_id = "_id";
	public final static String FIELD_mid = "id";
	
	public final static String FIELD_object = "object";
	public final static String FIELD_price = "price";
	public final static String FIELD_time = "time";
	
	public final static String FIELD_begin = "begin";
	public final static String FIELD_end = "end";
	public final static String FIELD_total = "total";
	public final static String FIELD_tmoney = "tmoney";
	public final static String FIELD_ymoney = "ymoney";
	public final static String FIELD_bool = "bool";
	
	private SimpleDateFormat format;
	
	String sql = "CREATE TABLE " + TABLE_OBJECT + " (" + FIELD_id
			+ " INTEGER primary key autoincrement, " 
			+ FIELD_mid + " varchar(10)," 
			+ FIELD_object + " text," 
			+ FIELD_price + " varchar(10)," 
			+ FIELD_time + " TIMESTAMP)";
	
	String sql2 = "CREATE TABLE " + TABLE_DATE + " (" + FIELD_id
			+ " INTEGER primary key autoincrement, " 
			+ FIELD_begin + " varchar(10)," 
			+ FIELD_end + " varchar(10)," 
			+ FIELD_total + " varchar(10)," 
			+ FIELD_tmoney + " varchar(10)," 
			+ FIELD_ymoney + " varchar(10)," 
			+ FIELD_bool + " varchar(5)," 
			+ FIELD_time + " TIMESTAMP)";

	public String pathString;
	
	@SuppressLint("SimpleDateFormat")
	public ToDoDB(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(sql);
		db.execSQL(sql2);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = "DROP TABLE IF EXISTS " + TABLE_OBJECT;
		db.execSQL(sql);
		onCreate(db);
	}

	
	
	/**
	 * 查询数据
	 * @param name
	 * @return
	 */
	public List<Map<String, String>> query(String name, String id) {
		List<Map<String, String>> list = new ArrayList<Map<String,String>>();
		Map<String, String> map = null;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = null;
		if (name.equals(TABLE_OBJECT)) {
			cursor = db.rawQuery("select * from " + name + " where " + FIELD_mid + "='" + 
					id + "' order by " + FIELD_time + " desc",  null);
		}else {
			cursor = db.rawQuery("select * from " + name + " order by " + FIELD_time + " desc", null);
		}
		if (cursor.getCount() != 0) {
			while (cursor.moveToNext()) {
				map = new HashMap<String, String>();
				if (name.equals(TABLE_OBJECT)) {
					map.put("name", cursor.getString(cursor.getColumnIndex(FIELD_object)));
					map.put("price", cursor.getString(cursor.getColumnIndex(FIELD_price)));
					map.put("time", cursor.getString(cursor.getColumnIndex(FIELD_time)));
				}else {
					map.put("begin", cursor.getString(cursor.getColumnIndex(FIELD_begin)));
					map.put("end", cursor.getString(cursor.getColumnIndex(FIELD_end)));
					map.put("total", cursor.getString(cursor.getColumnIndex(FIELD_total)));
					map.put("tmoney", cursor.getString(cursor.getColumnIndex(FIELD_tmoney)));
					map.put("ymoney", cursor.getString(cursor.getColumnIndex(FIELD_ymoney)));
					map.put("id", cursor.getString(cursor.getColumnIndex(FIELD_id)));
					map.put("time", cursor.getString(cursor.getColumnIndex(FIELD_time)));
					map.put("bool", cursor.getString(cursor.getColumnIndex(FIELD_bool)));
				}
				list.add(map);
			}
			db.close();
			cursor.close();
			return list;
		}else {
			db.close();
			cursor.close();
			return null;
		}
	}

	/**
	 * 查询全部数据
	 * @param name
	 * @return
	 */
	public String queryall(String name) {
		List<Map<String, String>> list = new ArrayList<Map<String,String>>();
		List<String> lStrings = new ArrayList<String>();
		Map<String, String> map = null;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = null;
		cursor = db.rawQuery("select * from " + name, null);
		if (cursor.getCount() != 0) {
			while (cursor.moveToNext()) {
				map = new HashMap<String, String>();
				if (name.equals(TABLE_OBJECT)) {
					map.put("id", cursor.getString(cursor.getColumnIndex(FIELD_id)));
					map.put("mid", cursor.getString(cursor.getColumnIndex(FIELD_mid)));
					map.put("name", cursor.getString(cursor.getColumnIndex(FIELD_object)));
					map.put("price", cursor.getString(cursor.getColumnIndex(FIELD_price)));
					map.put("time", cursor.getString(cursor.getColumnIndex(FIELD_time)));
				}else{
					map.put("begin", cursor.getString(cursor.getColumnIndex(FIELD_begin)));
					map.put("end", cursor.getString(cursor.getColumnIndex(FIELD_end)));
					map.put("total", cursor.getString(cursor.getColumnIndex(FIELD_total)));
					map.put("tmoney", cursor.getString(cursor.getColumnIndex(FIELD_tmoney)));
					map.put("ymoney", cursor.getString(cursor.getColumnIndex(FIELD_ymoney)));
					map.put("id", cursor.getString(cursor.getColumnIndex(FIELD_id)));
					map.put("time", cursor.getString(cursor.getColumnIndex(FIELD_time)));
					map.put("bool", cursor.getString(cursor.getColumnIndex(FIELD_bool)));
			}
				list.add(map);
					
			}
			db.close();
			cursor.close();
			JSONObject object = null;
			for (Map<String, String> map2 : list) {
				object = new JSONObject(map2);
				lStrings.add(object.toString());
			}
			return lStrings.toString();
		}else {
			db.close();
			cursor.close();
			return "";
		}
	}
	
	/**
	 * 判断新建文件日期是否重复
	 * @param begin
	 * @param end
	 * @return
	 */
	public boolean isNewHasDate(String begin, String end){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from " + TABLE_DATE + " where " + FIELD_begin + 
				"='" + begin + "' and " + FIELD_end + "='" + end + "'", null);
		if (cursor.moveToFirst()) {
			db.close();
			cursor.close();
			return true;
		}else {
			db.close();
			cursor.close();
			return false;
		}
	}
	/**
	 * 判断非新建文件日期是否重复
	 * @param begin
	 * @param end
	 * @return
	 */
	public boolean isHasDate(String begin, String end, String id){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from " + TABLE_DATE + " where " + FIELD_begin + 
				"='" + begin + "' and " + FIELD_end + "='" + end + "' and (" + FIELD_begin + 
				" <> (select " + FIELD_begin + " from " + TABLE_DATE + " where " + FIELD_id + 
				"='" + id + "') or " + FIELD_end + " <> (select " + FIELD_end + " from " + 
				TABLE_DATE + " where " + FIELD_id + "='" + id + "'))", null);
		if (cursor.moveToFirst()) {
			db.close();
			cursor.close();
			return true;
		}else {
			db.close();
			cursor.close();
			return false;
		}
	}
	
	/**
	 * 插入数据
	 * @param name
	 * @param map
	 * @return
	 */
	public String insert(String name, String id, Map<String, String> map, boolean bool) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		
		if (name.equals(TABLE_OBJECT)) {
			cv.put(FIELD_object, map.get("name"));
			cv.put(FIELD_price, map.get("price"));
			cv.put(FIELD_time, map.get("time"));
			if (bool) {
				cv.put(FIELD_id, map.get("id"));
				cv.put(FIELD_mid, map.get("mid"));
			}else {
				cv.put(FIELD_mid, id);
			}
		}else {
			cv.put(FIELD_begin, map.get("begin"));
			cv.put(FIELD_end, map.get("end"));
			cv.put(FIELD_total, map.get("total"));
			cv.put(FIELD_tmoney, map.get("tmoney"));
			cv.put(FIELD_ymoney, map.get("ymoney"));
			cv.put(FIELD_bool, map.get("bool"));
			if (bool) {
				cv.put(FIELD_time, map.get("time"));
				cv.put(FIELD_id, map.get("id"));
			}else {
				cv.put(FIELD_time, format.format(new Date()));
			}
		}
		db.insert(name, null, cv);
		
		Cursor cursor = db.rawQuery("select LAST_INSERT_ROWID() ",null);
		if (cursor.moveToFirst()) {
			db.close();
			return String.valueOf(cursor.getInt(0));
		}else {
			db.close();
			cursor.close();
			return "";
		}
	}

	/**
	 * 删除date表数据
	 * @param id
	 * @param name
	 */
	public void deleted(String id, String name) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = FIELD_id + " = ?";
		String[] whereValue = { id };
		db.delete(name, where, whereValue);
		db.close();
	}
	/**
	 * 删除object表数据
	 * @param id
	 * @param name
	 */
	public void deleteo(String id, String name) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = FIELD_mid + " = ?";
		String[] whereValue = { id };
		db.delete(name, where, whereValue);
		db.close();
	}

	/**
	 * 更新date表数据
	 * @param id
	 * @param text
	 */
	public void update(String tname, String id, List<Map<String, String>> list) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = FIELD_id + " = ?";
		String[] whereValue = { id };
		ContentValues cv = new ContentValues();
		for (int i = 0; i < list.size(); i++) {
			cv.put(list.get(i).get("key"), list.get(i).get("value"));
		}
		cv.put(FIELD_time, format.format(new Date()));
		db.update(tname, cv, where, whereValue);
		db.close();
	}
}
