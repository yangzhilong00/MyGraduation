package com.example.lenovo.mygraduation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.lenovo.mygraduation.Adapter.RVAdapterAddFriend;
import com.example.lenovo.mygraduation.Bean.EventMessage;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.roster.Roster;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AddFriendActivity extends AppCompatActivity implements View.OnClickListener{

    private String user_id;
    private String user_pw;
    private ImageView button_back;
    private LinearLayout layout_busying;
    private RecyclerView recyclerView_addfriend;
    private EditText et_search;
    private Button button_search;
    private MyGlobal myApp;
    private boolean busying;
    private List<String> addFriendList;
    private RVAdapterAddFriend adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        Init();
        addFriendList = new ArrayList<String>();
//        设置布局管理器
        //垂直分布和横向分布  LinearLayoutManager.HORIZONTAL  VERTICAL
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView_addfriend.setLayoutManager(layoutManager);

        adapter = new RVAdapterAddFriend(this, addFriendList);
        recyclerView_addfriend.setAdapter(adapter);
    }

    private void Init(){
        FindView();
        Set_Not_Busying();
        Bundle bundle = getIntent().getExtras();
        user_id = bundle.getString("user_id");
        user_pw = bundle.getString("user_pw");
        myApp = (MyGlobal)getApplication();
        button_back.setOnClickListener(this);
        button_search.setOnClickListener(this);
    }

    public void FindView(){
        button_back = (ImageView)findViewById(R.id.button_back);
        layout_busying = (LinearLayout) findViewById(R.id.layout_busying);
        recyclerView_addfriend = (RecyclerView)findViewById(R.id.recycleview_addfriend);
        et_search = (EditText)findViewById(R.id.et_search);
        button_search = (Button)findViewById(R.id.button_search);
    }

    public Handler myhandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {      //判断标志位
                case 0:   //对View进行操作
                    adapter.setNewData(addFriendList);
                    adapter.notifyDataSetChanged();
                    Set_Not_Busying();
            }
        }
    };

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.button_back){
            finish();
        }
        if(id == R.id.button_search){
            if(!busying){
                String search_id = et_search.getText().toString();
                if(!myApp.ifConnect()){
                    myApp.MakeToast("无法连接服务器");
                    return ;
                }
                Set_Busying();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            addFriendList = myApp.searchUsers(search_id);
                        } catch (XMPPException.XMPPErrorException e) {
                            myApp.MakeToast("无法连接服务器");
                            e.printStackTrace();
                        }
                        Message msg = new Message();
                        msg.what = 0;
                        myhandler.sendMessage(msg);
                    }
                }).start();
            }
        }
    }

    //更新FriendFragment的数据列表
    public void updateFriend(){
        EventMessage eventMessage = new EventMessage();
        eventMessage.setWhich(10);
        eventMessage.setMyFriendList(myApp.myFriendList);
        EventBus.getDefault().post(eventMessage);
    }

    //从手机存储中获取bitmap
    public Bitmap getImage(String user_id) {
        String sdCardDir = getApplicationContext().getCacheDir() + "/DCIM/";
//        String sdCardDir = Environment.getExternalStorageDirectory()+"/DCIM/";
        String path = sdCardDir + "MyGraduation/" + user_id + ".jpg";
        Bitmap bitmap = null;
        File f = new File(path);
        if (!f.exists()) {
            return bitmap;
        }
        try {
            FileInputStream fis = new FileInputStream(path);
            bitmap = BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private void Set_Busying(){
        busying = true;
        layout_busying.setVisibility(View.VISIBLE);
    }

    private void Set_Not_Busying(){
        busying = false;
        layout_busying.setVisibility(View.INVISIBLE);
    }

    public void addFriend(String friend_id){
        if(friend_id.equals(user_id)){
            myApp.MakeToast("不能添加自己为好友");
            return;
        }
        if(myApp.myFriendDB.exists(user_id, friend_id)){
            myApp.MakeToast("对方已经是你的好友");
            return;
        }
        if(myApp.addFriend(user_id, friend_id)){
            myApp.MakeToast("已发送好友请求");
            myApp.myFriendDB.insert(user_id, friend_id);
            myApp.mysql_insert_myfriend(user_id, friend_id);
            myApp.SetListFromFriendDB(user_id);
            updateFriend();
        }
        else{
            myApp.MakeToast("操作失败");
        }
    }
}
