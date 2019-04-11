package com.example.lenovo.mygraduation.MyDataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 2019/3/23.
 */

public class MyNewFriendDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MyGraduation.db";
    private static final String TABLE_NAME = "newfriend";
    private static final int DB_VERSION = 1;

    public MyNewFriendDB(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
        SQLiteDatabase db = getWritableDatabase();
        String sql = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"("+
                "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                "owner_id VARCHAR(10) NOT NULL," +
                "friend_id VARCHAR(10) NOT NULL)";
        db.execSQL(sql);//执行sql语句
        db.close();
        System.out.println("MyNewFriendDB");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        System.out.println("MyNewFriendDB:onCreate");
        //创建好友请求信息表
//        String sql = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"("+
//                "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
//                "owner_id VARCHAR(10) NOT NULL," +
//                "friend_id VARCHAR(10) NOT NULL)";
//        db.execSQL(sql);//执行sql语句
//        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insert(String owner_id, String friend_id){
        SQLiteDatabase db = getReadableDatabase();
        String query_sql = "SELECT * " +
                "FROM "+TABLE_NAME +
                " WHERE owner_id = '"+owner_id+"' AND friend_id = '"+friend_id+"'";
        Cursor cursor = db.rawQuery(query_sql,null);
        if(cursor.moveToFirst()){
            cursor.close();
            db.close();
            return ;
        }
        else{
            cursor.close();
            db.close();
            db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("owner_id", owner_id);
            values.put("friend_id", friend_id);
            db.insert(TABLE_NAME, null, values);
            db.close();
        }
    }

    public void delete(String owner_id, String friend_id){
        SQLiteDatabase db = getWritableDatabase();
        String delete_sql = "DELETE FROM "+TABLE_NAME+" " +
                "WHERE owner_id='"+owner_id+"' AND " +
                "friend_id='"+friend_id+"'";
        db.execSQL(delete_sql);
        db.close();
    }

    public List<String> getAllNewFriend(String owner_id){
        List<String> result = new ArrayList<String>();
        SQLiteDatabase db = getReadableDatabase();
        String query_sql = "SELECT friend_id " +
                "FROM "+TABLE_NAME+" " +
                "WHERE owner_id='"+owner_id+"'";
        Cursor cursor = db.rawQuery(query_sql,null);
        if (cursor.moveToFirst()){
            do{
                String temp = cursor.getString(0);
                result.add(temp);
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return result;
    }

    public void clear(){
        SQLiteDatabase db = getWritableDatabase();
        String delete_sql = "DELETE FROM "+TABLE_NAME;
        db.execSQL(delete_sql);
        db.close();
    }
}
