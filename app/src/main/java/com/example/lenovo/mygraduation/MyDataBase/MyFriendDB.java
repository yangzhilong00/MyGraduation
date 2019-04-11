package com.example.lenovo.mygraduation.MyDataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.lenovo.mygraduation.Bean.MyFriend;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 2019/3/2.
 */

public class MyFriendDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MyGraduation.db";
    private static final String TABLE_NAME = "friend";
    private static final int DB_VERSION = 1;

    public MyFriendDB(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
        SQLiteDatabase db = getWritableDatabase();
        String sql = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"("+
                "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                "owner_id VARCHAR(10) NOT NULL,"+
                "friend_id VARCHAR(10) NOT NULL)";
        db.execSQL(sql);//执行sql语句
        db.close();
        System.out.println("MyFriendDB");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        System.out.println("MyFriendDB:onCreate");
        //创建联系人信息表
//        String sql = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"("+
//                "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
//                "owner_id VARCHAR(10) NOT NULL,"+
//                "friend_id VARCHAR(10) NOT NULL)";
//        db.execSQL(sql);//执行sql语句
//        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // To Do
    }

    public void insert(MyFriend myFriend){
        insert(myFriend.owner_id, myFriend.friend_id);
    }

    public void delete(MyFriend myFriend){
        delete(myFriend.owner_id,myFriend.friend_id);
    }

    public void insert(String owner_id, String friend_id){
        //先检查原来有没有该条数据
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

    public boolean exists(String owner_id, String friend_id){
        SQLiteDatabase db = getReadableDatabase();
        String query_sql = "SELECT * " +
                "FROM "+TABLE_NAME +
                " WHERE owner_id = '"+owner_id+"' AND friend_id = '"+friend_id+"'";
        Cursor cursor = db.rawQuery(query_sql,null);
        if(cursor.moveToFirst()){
            cursor.close();
            db.close();
            return true;
        }
        return false;
    }

    public void delete(String owner_id, String friend_id){
        SQLiteDatabase db = getWritableDatabase();
        String delete_sql = String.format("DELETE FROM %s " +
                "WHERE owner_id = '%s' AND friend_id = '%s'", TABLE_NAME, owner_id, friend_id);
        db.execSQL(delete_sql);
        db.close();
    }

    public List<MyFriend> getAllFriend(String owner_id){
        List<MyFriend> allFriend = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query_sql = "SELECT * " +
                "FROM "+TABLE_NAME +
                " WHERE owner_id = '"+owner_id+"'";
        Cursor cursor = db.rawQuery(query_sql,null);
        if (cursor.moveToFirst()){
            do{
                MyFriend temp = new MyFriend(cursor.getString(1), cursor.getString(2));
                allFriend.add(temp);
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return allFriend;
    }


    public void clear(){
        SQLiteDatabase db = getWritableDatabase();
        String delete_sql = "DELETE FROM "+TABLE_NAME;
        db.execSQL(delete_sql);
        db.close();
    }
}
