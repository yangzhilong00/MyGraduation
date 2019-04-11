package com.example.lenovo.mygraduation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.iqregister.AccountManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;

import static org.jivesoftware.smackx.filetransfer.FileTransfer.Error.connection;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    static private String connectString = "jdbc:mysql://192.168.199.228:3306/openfire"
            + "?autoReconnect=true&useUnicode=true&useSSL=false"
            + "&characterEncoding=utf-8&serverTimezone=UTC";
    static private String  sql_user="root";
    static private String  sql_pwd="123456";

    private MyGlobal myApp;
    private Button Button_register;
    private EditText edittext_user_id;
    private EditText edittext_user_pw;
    private boolean busying;
    private RelativeLayout layout_busying;
    private SharedPreferences shared;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Init();
        SetListener();
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
        Button_register = (Button)findViewById(R.id.button_register);
        edittext_user_id = (EditText)findViewById(R.id.et_user_id);
        edittext_user_pw = (EditText)findViewById(R.id.et_user_pw);
        layout_busying = (RelativeLayout)findViewById(R.id.layout_busying);
        Set_Not_Busying();
    }

    private void SetListener(){
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
        if(temp_id == R.id.button_register){
            final String str_user_id = edittext_user_id.getText().toString();
            final String str_user_pw = edittext_user_pw.getText().toString();
            if(TextUtils.isEmpty(str_user_id) || TextUtils.isEmpty(str_user_pw)){
                myApp.MakeToast("账号和密码不能为空！");
                return;
            }
            if(str_user_id.length()>10 || str_user_pw.length()>10){
                myApp.MakeToast("账号或密码过长！");
                return;
            }
            Set_Busying();
            new Thread(new Runnable() {
                @Override
                public void run() {  //开始连接
                    if(myApp.connect()){   //连接成功

                        HashMap<String, String> attributes =new HashMap<String, String>();//附加信息
                        AccountManager.sensitiveOperationOverInsecureConnectionDefault(true);
                        try {
                            AccountManager.getInstance(myApp.getmConnection()).createAccount(str_user_id,str_user_pw, attributes);//创建一个账户，username和password分别为用户名和密码
                            //更新mysql数据库
                            try {
                                Connection con = DriverManager.getConnection(connectString, sql_user, sql_pwd);
                                Statement stmt=con.createStatement();
                                String InsertString = "insert into myuser(user_id, user_pw, user_name) " +
                                        "values('"+str_user_id+
                                        "', '"+str_user_pw+
                                        "', '"+str_user_id+"');";
                                int i = stmt.executeUpdate(InsertString);
                                stmt.close();
                                con.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
//                            myApp.MakeToast("注册并成功登录");
                            editor.putString("user_id", str_user_id);
                            editor.putString("user_pw", str_user_pw);
                            editor.commit();
                            GotoMainActivity(str_user_id, str_user_pw, true);
                        } catch (SmackException.NoResponseException e) {
                            e.printStackTrace();
                            myApp.MakeToast("注册失败");
                        } catch (XMPPException.XMPPErrorException e) {
                            e.printStackTrace();
                            myApp.MakeToast("注册失败");
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                            myApp.MakeToast("注册失败");
                        }
                    }
                    else{
                        myApp.MakeToast("无法连接服务器");
                    }
                    Set_Not_Busying();
                }
            }).start();
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
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
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
