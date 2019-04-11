package com.example.lenovo.mygraduation.MyDataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.lenovo.mygraduation.Bean.MyMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 2019/3/2.
 */

public class MyMessageDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MyGraduation.db";
    private static final String TABLE_NAME = "message";
    private static final int DB_VERSION = 1;

    public MyMessageDB(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
        //创建最近信息表
        SQLiteDatabase db = getWritableDatabase();
        String sql = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"("+
                "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                "owner_id VARCHAR(10) NOT NULL,"+
                "friend_id VARCHAR(10) NOT NULL,"+
                "message_type INTEGER,"+
                "message_content TEXT,"+
                "message_date TEXT);";
        db.execSQL(sql);//执行sql语句
        db.close();
        System.out.println("MyMessageDB");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        System.out.println("MyMessageDB:onCreate");
        //创建最近信息表
//        String sql = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"("+
//                "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
//                "owner_id VARCHAR(10) NOT NULL,"+
//                "friend_id VARCHAR(10) NOT NULL,"+
//                "message_type INTEGER,"+
//                "message_content TEXT,"+
//                "message_date TEXT);";
//        db.execSQL(sql);//执行sql语句
//        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // To Do
    }

    public void insert(MyMessage myMessage){
        insert(myMessage.owner_id, myMessage.friend_id, myMessage.message_type, myMessage.message_content, myMessage.message_date);
    }

    public void update(MyMessage myMessage){
        update(myMessage.owner_id, myMessage.friend_id, myMessage.message_type, myMessage.message_content, myMessage.message_date);
    }

    public void delete(MyMessage myMessage){
        delete(myMessage.owner_id, myMessage.friend_id);
    }

    public List<MyMessage> getAllMessage(String owner_id){
        List<MyMessage> allMessage = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query_sql = "SELECT * " +
                "FROM "+TABLE_NAME +
                " WHERE owner_id = '"+owner_id+"' ORDER BY message_date DESC";
        Cursor cursor = db.rawQuery(query_sql,null);
        if (cursor.moveToFirst()){
            do{
                MyMessage temp = new MyMessage(cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getString(4), cursor.getString(5));
                allMessage.add(temp);
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return allMessage;
    }

    public void clear(){
        SQLiteDatabase db = getWritableDatabase();
        String delete_sql = "DELETE FROM "+TABLE_NAME;
        db.execSQL(delete_sql);
        db.close();
    }

    public void update(String owner_id, String friend_id, int message_type, String message_content, String message_date){
        SQLiteDatabase db = getWritableDatabase();
        String update_sql = String.format("UPDATE %s " +
                "SET message_type=%d,message_content='%s',message_date='%s' " +
                "WHERE owner_id='%s' AND friend_id='%s'",TABLE_NAME, message_type, message_content, message_date,owner_id, friend_id);
        db.execSQL(update_sql);
        db.close();
    }

    public void insert(String owner_id, String friend_id, int message_type, String message_content, String message_date){
        //先检查原来有没有该条数据
        SQLiteDatabase db = getReadableDatabase();
        String query_sql = "SELECT * " +
                "FROM "+TABLE_NAME +
                " WHERE owner_id = '"+owner_id+"' AND friend_id = '"+friend_id+"'";
        Cursor cursor = db.rawQuery(query_sql,null);
        if(cursor.moveToFirst()){
            update(owner_id, friend_id, message_type, message_content, message_date);
        }
        else{
            db.close();
            db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("owner_id", owner_id);
            values.put("friend_id", friend_id);
            values.put("message_type", message_type);
            values.put("message_content", message_content);
            values.put("message_date", message_date);
            db.insert(TABLE_NAME, null, values);
        }
        cursor.close();
        db.close();
    }

    public void delete(String owner_id, String friend_id){
        SQLiteDatabase db = getWritableDatabase();
        String delete_sql = String.format("DELETE FROM %s " +
                "WHERE owner_id = '%s' AND friend_id = '%s'", TABLE_NAME, owner_id, friend_id);
        db.execSQL(delete_sql);
        db.close();
    }
}
