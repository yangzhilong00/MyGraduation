package com.example.lenovo.mygraduation.MyDataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 2019/3/2.
 */

public class MyUserDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MyGraduation.db";
    private static final String TABLE_NAME = "user";
    private static final int DB_VERSION = 1;
    private static final int COLUMN_COUNT = 8;

    public MyUserDB(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建用户注册信息表
        String sql = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"("+
                "id VARCHAR(10) PRIMARY KEY,"+
                "name VARCHAR(15),"+
                "password VARCHAR(15),"+
                "picture TEXT,"+
                "status VARCHAR(2),"+
                "mood VARCHAR(20),"+
                "email VARCHAR(15),"+
                "phone VARCHAR(11));";
        db.execSQL(sql);//执行sql语句
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // To Do
    }

    public void insert(String id, String name, String password){
        insert(id, name, password, "", "", "", "", "");
    }

    public void insert(String id, String name, String password, String picture, String status, String mood, String email, String phone){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("name", name);
        values.put("password",password);
        values.put("picture", picture);
        values.put("status", status);
        values.put("mood", mood);
        values.put("email",email);
        values.put("phone", phone);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void update(String id, String name, String password, String picture, String status, String mood, String email, String phone){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        String whereClause = "id = ?";
        String[] whereArgs = {id};
        values.put("id", id);
        values.put("name", name);
        values.put("password",password);
        values.put("picture", picture);
        values.put("status", status);
        values.put("mood", mood);
        values.put("email",email);
        values.put("phone", phone);
        db.update(TABLE_NAME, values, whereClause, whereArgs);
        db.close();
    }

    public void delete(String id){
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = "id = ?"; // 主键列名 = ?
        String[] whereArgs = {id}; // 主键的值
        db.delete(TABLE_NAME, whereClause, whereArgs);
        db.close();
    }

    public String[] getUser(String id){
        SQLiteDatabase db = getReadableDatabase();
        String query_sql = "SELECT * FROM "+TABLE_NAME+" WHERE id = "+id+";";
        Cursor cursor = db.rawQuery(query_sql,null);
        db.close();
        String [] user = null;
        if(cursor.moveToFirst()){
            user = new String[cursor.getColumnCount()];
            for(int i=0; i<cursor.getColumnCount(); i++){
                user[i] = cursor.getString(i);
            }
        }
        cursor.close();
        return user;
    }

}
