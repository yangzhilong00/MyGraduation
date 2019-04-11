package com.example.lenovo.mygraduation.MyDataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.lenovo.mygraduation.Bean.MyChat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 2019/3/2.
 */

public class MyChatDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MyGraduation.db";
    private static final String TABLE_NAME = "chat";
    private static final int DB_VERSION = 1;

    public MyChatDB(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
        //创建聊天內容表
        SQLiteDatabase db = getWritableDatabase();
        String sql = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"("+
                "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                "sender_id VARCHAR(10) NOT NULL,"+
                "receiver_id VARCHAR(10) NOT NULL,"+
                "chat_content TEXT,"+
                "chat_type INTEGER,"+
                "chat_date TEXT,"+
                "chat_state INTEGER);";
        db.execSQL(sql);//执行sql语句
        db.close();
        System.out.println("MyChatDB");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        System.out.println("MyChatDB:onCreate");
        //创建聊天內容表
//        String sql = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"("+
//                "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
//                "sender_id VARCHAR(10) NOT NULL,"+
//                "receiver_id VARCHAR(10) NOT NULL,"+
//                "chat_content TEXT,"+
//                "chat_type INTEGER,"+
//                "chat_date TEXT,"+
//                "chat_state INTEGER);";
//        db.execSQL(sql);//执行sql语句
//        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // To Do
    }

    public void insert(MyChat myChat){
        insert(myChat.sender_id, myChat.receiver_id, myChat.chat_content, myChat.chat_type, myChat.chat_date, myChat.chat_state);
    }

    public void delete(int chat_id){
        SQLiteDatabase db = getWritableDatabase();
        String delete_sql = "DELETE FROM "+TABLE_NAME+" " +
                "WHERE chat_id = '"+String.valueOf(chat_id)+"'";
        db.execSQL(delete_sql);
        db.close();
    }

    public void insert(String sender_id, String receiver_id, String chat_content, int chat_type, String chat_date, int chat_state){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("sender_id", sender_id);
        values.put("receiver_id", receiver_id);
        values.put("chat_content", chat_content);
        values.put("chat_type", chat_type);
        values.put("chat_date", chat_date);
        values.put("chat_state", chat_state);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public List<MyChat> getAllChat(String user_id, String friend_id){
        List<MyChat> allChat = new ArrayList<MyChat>();
        SQLiteDatabase db = getReadableDatabase();
        String query_sql = String.format("SELECT * " +
                                        "FROM %s " +
                                        "WHERE id IN (SELECT id " +
                                                    "FROM %s " +
                                                    "WHERE sender_id='%s' AND receiver_id='%s' " +
                                                    "UNION " +
                                                    "SELECT id " +
                                                    "FROM %s " +
                                                    "WHERE sender_id='%s' AND receiver_id='%s') " +
                                        "ORDER BY chat_date ASC", TABLE_NAME, TABLE_NAME, user_id, friend_id, TABLE_NAME, friend_id, user_id);
        Cursor cursor = db.rawQuery(query_sql,null);
        if (cursor.moveToFirst()){
            do{
                allChat.add(new MyChat(cursor.getInt(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getInt(4),cursor.getString(5),cursor.getInt(6)));
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return allChat;
    }

    public void clear(){
        SQLiteDatabase db = getWritableDatabase();
        String delete_sql = "DELETE FROM "+TABLE_NAME;
        db.execSQL(delete_sql);
        db.close();
    }
}
