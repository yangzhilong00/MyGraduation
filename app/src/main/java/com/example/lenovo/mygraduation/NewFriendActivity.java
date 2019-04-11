package com.example.lenovo.mygraduation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.lenovo.mygraduation.Adapter.RVAdapterNewFriend;
import com.example.lenovo.mygraduation.Bean.EventMessage;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Presence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.fromId;

public class NewFriendActivity extends AppCompatActivity implements View.OnClickListener{

    private String user_id;
    private String user_pw;
    private ImageView button_back;
    private RecyclerView recyclerView_newfriend;
    private LinearLayout layout_nothing;
    private MyGlobal myApp;
    private RVAdapterNewFriend adapter;
    private List<String> newFriendList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friend);

        Init();
        updateRecycleView();
    }

    private void Init(){
        setNotNewMarkFriend();
        FindView();
        myApp = (MyGlobal)getApplication();
        Bundle bundle = getIntent().getExtras();
        user_id = bundle.getString("user_id");
        user_pw = bundle.getString("user_pw");
        button_back.setOnClickListener(this);
        newFriendList = myApp.myNewFriendDB.getAllNewFriend(user_id);
        // 设置布局管理器
        //垂直分布和横向分布  LinearLayoutManager.HORIZONTAL  VERTICAL
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView_newfriend.setLayoutManager(layoutManager);

        adapter = new RVAdapterNewFriend(this,newFriendList);
        recyclerView_newfriend.setAdapter(adapter);
    }

    private void FindView(){
        button_back = (ImageView) findViewById(R.id.button_back);
        recyclerView_newfriend = (RecyclerView)findViewById(R.id.recycleview_newfriend);
        layout_nothing = (LinearLayout)findViewById(R.id.layout_nothing);
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.button_back){
            finish();
        }
    }

    //同意好友申请
    public void agreeFriend(int position){
        String friend_id = newFriendList.get(position);
        if(myApp.agreeNewFriend(user_id,friend_id)){ //如果成功同意请求
            //更新newFriend列表
            newFriendList.remove(position);
            myApp.myNewFriendDB.delete(user_id, friend_id);
            updateRecycleView();
            //更新Friend列表
            myApp.myFriendDB.insert(user_id,friend_id);
            myApp.SetListFromFriendDB(user_id);
            updateFriend();
            myApp.MakeToast("已同意");
        }
        else{
            myApp.MakeToast("操作失败");
        }
    }

    //拒绝好友申请
    public void refuseFriend(int position){
        String friend_id = newFriendList.get(position);
        if(myApp.refuseNewFriend(friend_id)){ //如果成功拒绝请求
            //更新newFriend列表
            newFriendList.remove(position);
            myApp.myNewFriendDB.delete(user_id, friend_id);
            updateRecycleView();
            myApp.MakeToast("已拒绝");
            //Friend列表不必更新

            //远程数据库更新
            myApp.mysql_delete_myfriend(friend_id, user_id);
        }
        else{
            myApp.MakeToast("操作失败");
        }
    }

    //更新FriendFragment的数据列表
    public void updateFriend(){
        EventMessage eventMessage = new EventMessage();
        eventMessage.setWhich(10);
        eventMessage.setMyFriendList(myApp.myFriendList);
        EventBus.getDefault().post(eventMessage);
    }

    //设置Friend的new标志
    public void setNewMarkFriend(){
        EventMessage eventMessage = new EventMessage();
        eventMessage.setWhich(11);
        EventBus.getDefault().post(eventMessage);
    }

    //取消Friend的new标志
    public void setNotNewMarkFriend(){
        EventMessage eventMessage = new EventMessage();
        eventMessage.setWhich(12);
        EventBus.getDefault().post(eventMessage);
    }

    //更新列表
    public void updateRecycleView(){
        adapter.notifyDataSetChanged();
        if(newFriendList.size()==0){
            layout_nothing.setVisibility(View.VISIBLE);
        }
        else{
            layout_nothing.setVisibility(View.GONE);
        }
    }
}
