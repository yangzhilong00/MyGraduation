package com.example.lenovo.mygraduation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.lenovo.mygraduation.Adapter.RVAdapterAddFriend;
import com.example.lenovo.mygraduation.Adapter.RVAdapterNewFunction;
import com.example.lenovo.mygraduation.Bean.EventMessage;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

public class NewFunctionActivity extends AppCompatActivity implements View.OnClickListener{

    private String user_id;
    private String user_pw;
    private ImageView button_back;
    private LinearLayout layout_busying;
    private RecyclerView recyclerView_newFunction;
    private EditText et_search;
    private Button button_search;
    private MyGlobal myApp;
    private boolean busying;
    private List<String> newFunctionList;
    private RVAdapterNewFunction adapter;

    public class queue_bean{
        public String user_id;
        public int user_generation;
        public queue_bean(String str, int i){
            user_id = str;
            user_generation = i;
        }
    }

    //远程数据库信息
    static private String connectString = "jdbc:mysql://192.168.199.228:3306/openfire"
            + "?autoReconnect=true&useUnicode=true&useSSL=false"
            + "&characterEncoding=utf-8&serverTimezone=UTC";
    static private String  sql_user="root";
    static private String  sql_pwd="123456";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_function);
        Init();
        newFunctionList = new ArrayList<String>();
//        设置布局管理器
        //垂直分布和横向分布  LinearLayoutManager.HORIZONTAL  VERTICAL
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView_newFunction.setLayoutManager(layoutManager);

        adapter = new RVAdapterNewFunction(this, newFunctionList);
        recyclerView_newFunction.setAdapter(adapter);
    }

    private void Init(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("数据库类导入成功");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
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
        recyclerView_newFunction = (RecyclerView)findViewById(R.id.recycleview_addfriend);
        et_search = (EditText)findViewById(R.id.et_search);
        button_search = (Button)findViewById(R.id.button_search);
    }

    public Handler myhandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {      //判断标志位
                case 0:   //对View进行操作
                    adapter.setNewData(newFunctionList);
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
                String search_name = et_search.getText().toString();
                if(!myApp.ifConnect()){
                    myApp.MakeToast("无法连接服务器");
                    return ;
                }
                Set_Busying();
                newFunctionList.clear();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Connection con = DriverManager.getConnection(connectString, sql_user, sql_pwd);
                            Statement stmt = con.createStatement();
                            Queue<queue_bean> queue = new LinkedList<queue_bean>();
                            Set<String> set = new TreeSet<>();
                            queue.offer(new queue_bean(user_id, 0));
                            set.add(user_id);
                            while(!queue.isEmpty()){
                                queue_bean temp_bean = queue.poll();
                                String query_sql = "SELECT user_name " +
                                        "FROM myuser " +
                                        "WHERE user_id = '"+temp_bean.user_id+"';";
                                ResultSet rs = stmt.executeQuery(query_sql);
                                if(rs.next()){
                                    String temp_name = rs.getString(1);
                                    if(temp_name.equals(search_name)){
                                        try {
                                            newFunctionList.addAll(myApp.searchUsers(temp_bean.user_id));
                                        } catch (XMPPException.XMPPErrorException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                rs.close();
                                if(temp_bean.user_generation < 3){ //以3代作为一个参考界限
                                    query_sql = "SELECT friend_id " +
                                            "FROM myfriend " +
                                            "WHERE owner_id = '"+temp_bean.user_id+"';";
                                    rs = stmt.executeQuery(query_sql);
                                    while(rs.next()){
                                        String temp_id = rs.getString(1);
                                        if(!set.contains(temp_id)){
                                            set.add(temp_id);
                                            queue.add(new queue_bean(temp_id, temp_bean.user_generation+1));
                                        }
                                    }
                                }
                            }
                            stmt.close();
                            con.close();
                        } catch (SQLException e) { //连接失败
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
