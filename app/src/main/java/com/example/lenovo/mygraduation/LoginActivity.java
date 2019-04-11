package com.example.lenovo.mygraduation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private MyGlobal myApp;
    private Button Button_login;
    private Button Button_register;
    private EditText edittext_user_id;
    private EditText edittext_user_pw;
    private boolean busying;
    private RelativeLayout layout_busying;
    private SharedPreferences shared;
    private SharedPreferences.Editor editor;

    static private String connectString = "jdbc:mysql://192.168.199.228:3306/openfire"
            + "?autoReconnect=true&useUnicode=true&useSSL=false"
            + "&characterEncoding=utf-8&serverTimezone=UTC";
    static private String  sql_user="root";
    static private String  sql_pwd="123456";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//        SystemBarUI.initSystemBar(LoginActivity.this, R.color.green);
        Init();
        //如果不是第一次登陆，则跳到主界面
        if(shared.getString("user_id", "").length() > 0){
            GotoMainActivity(shared.getString("user_id", ""),shared.getString("user_pw", ""),false);
        }
        else{
            SetListener();
        }
    }

    private void Init() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        myApp = (MyGlobal)getApplication();
        shared = getSharedPreferences("user", MODE_PRIVATE);
        editor = shared.edit();
        Button_login = (Button)findViewById(R.id.button_login);
        Button_register = (Button)findViewById(R.id.button_register);
        edittext_user_id = (EditText)findViewById(R.id.et_user_id);
        edittext_user_pw = (EditText)findViewById(R.id.et_user_pw);
        layout_busying = (RelativeLayout)findViewById(R.id.layout_busying);
        Set_Not_Busying();
    }

    private void SetListener(){
        Button_login.setOnClickListener(this);
        Button_register.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if(busying) return;  //如果正在进行操作，则点击无反应；
        int temp_id = view.getId();
        if(temp_id == R.id.button_login){
            final String str_user_id = edittext_user_id.getText().toString();
            final String str_user_pw = edittext_user_pw.getText().toString();
            if(TextUtils.isEmpty(str_user_id) || TextUtils.isEmpty(str_user_pw)){
                myApp.MakeToast("账号或密码不能为空！");
                return;
            }
            if(str_user_id.length()>10 || str_user_pw.length()>10){
                myApp.MakeToast("账号或密码太长！");
                return;
            }
            Set_Busying();
            new Thread(new Runnable() {
                @Override
                public void run() {  //开始连接
                    try {
                        Connection con = DriverManager.getConnection(connectString, sql_user, sql_pwd);
                        Statement stmt=con.createStatement();
                        String QueryString = "SELECT * FROM myuser WHERE user_id = '"+str_user_id+"';";
                        ResultSet rs = stmt.executeQuery(QueryString);
                        if(rs.next()){
                            String user_pw = rs.getString("user_pw");
                            String user_name = rs.getString("user_name");
                            if(user_pw.equals(str_user_pw)){
                                editor.putString("user_id", str_user_id);
                                editor.putString("user_pw", str_user_pw);
                                editor.putString("user_name", user_name);
                                editor.commit();
                                GotoMainActivity(str_user_id, str_user_pw, true);
                            }
                        }
                        else{
                            myApp.MakeToast("用户名或密码错误");
                        }
                        rs.close();
                        stmt.close();
                        con.close();
                    } catch (SQLException e) { //连接失败
                        myApp.MakeToast("连接失败");
                        e.printStackTrace();
                    } catch (java.sql.SQLException e) {
                        e.printStackTrace();
                        myApp.MakeToast("连接失败");
                    }
                    Set_Not_Busying();
                }
            }).start();
        }
        if(temp_id == R.id.button_register){
            SMSSDK.initSDK(LoginActivity.this, "23857643884d0", "e1632925b81d3ea0b7cf4dcd4f2676dd");
            RegisterPage registerPage = new RegisterPage();
            registerPage.setRegisterCallback(new EventHandler() {
                public void afterEvent(int event, int result, Object data) {
                    if (result == SMSSDK.RESULT_COMPLETE) {
//                        @SuppressWarnings("unchecked")
                        //验证成功
                        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    else{
                        //验证失败
                    }
                }
            });
            registerPage.show(getApplicationContext());
        }
    }

    private void Set_Busying(){
        busying = true;
        layout_busying.setVisibility(View.VISIBLE);
    }

    private void Set_Not_Busying(){
        busying = false;
        layout_busying.setVisibility(View.INVISIBLE);
    }

    //将登陆的用户名、密码以及是否为初次登陆打包送去MainActivity
    void GotoMainActivity(String user_id, String user_pw, boolean first_time){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("user_id", user_id);
        bundle.putString("user_pw", user_pw);
        bundle.putBoolean("first_time", first_time);
        intent.putExtras(bundle);
        //跳到MainActivity时清除掉LoginActivity
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
