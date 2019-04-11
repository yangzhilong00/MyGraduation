package com.example.lenovo.mygraduation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lenovo.mygraduation.Adapter.RVAdapterChat;
import com.example.lenovo.mygraduation.Bean.EventMessage;
import com.example.lenovo.mygraduation.Bean.MyChat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.packet.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener{
    private String user_id;
    private String friend_id;
    private ImageView iv_back;
    private TextView tv_user_id;
    private ImageView iv_user;
    private RecyclerView recyclerview_chat;
    private Button button_send;
    private EditText et_chat;
    private List<MyChat> myChatList;
    private MyGlobal myApp;
    private RVAdapterChat adapter;
    private LinearLayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Init();
    }

    void Init(){
        Bundle receive=getIntent().getExtras();
        user_id = receive.getString("owner_id");
        friend_id = receive.getString("friend_id");
        FindView();
        tv_user_id.setText(friend_id);
        iv_back.setOnClickListener(this);
        iv_user.setOnClickListener(this);
        button_send.setOnClickListener(this);
        myApp = (MyGlobal)getApplication();
        myChatList = myApp.myChatDB.getAllChat(user_id, friend_id);
        // 设置布局管理器
        //瀑布流,设置为3列
//        StaggeredGridLayoutManager layoutManager = new
//                StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);

        // 设置布局管理器
        //垂直分布和横向分布  LinearLayoutManager.HORIZONTAL  VERTICAL
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setStackFromEnd(true);
        recyclerview_chat.setLayoutManager(layoutManager);

        adapter = new RVAdapterChat(this, myChatList, user_id);
        recyclerview_chat.setAdapter(adapter);

        //调到最后一行
        smoothMoveToPosition(recyclerview_chat, myChatList.size());

        //注册EventBus
        EventBus.getDefault().register(this);
    }

    void FindView(){
        iv_back = (ImageView)findViewById(R.id.iv_back);
        tv_user_id = (TextView)findViewById(R.id.tv_user_id);
        iv_user = (ImageView)findViewById(R.id.iv_user);
        recyclerview_chat = (RecyclerView)findViewById(R.id.recycleview_chat);
        button_send = (Button)findViewById(R.id.button_send);
        et_chat = (EditText)findViewById(R.id.et_chat);
    }

    public Bitmap getImage(String user_id){
        String sdCardDir = getApplicationContext().getCacheDir()+"/DCIM/";
//        String sdCardDir = Environment.getExternalStorageDirectory()+"/DCIM/";
        String path = sdCardDir+"MyGraduation/"+user_id+".jpg";
        Bitmap bitmap = null;
        File f=new File(path);
        if(!f.exists()){
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
        if(id == R.id.iv_back){
            finish();
        }
        if(id == R.id.button_send){
            if (!myApp.ifConnect()){
                myApp.MakeToast("无网络连接！");
                return ;
            }
            String chat_content = et_chat.getText().toString();
            if(chat_content.length()>0){
                et_chat.setText("");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SendMessage(chat_content);
                    }
                }).start();
            }

        }
    }

    Handler myhandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {      //判断标志位
                case 0://更新列表
                    adapter.notifyDataSetChanged();
                    smoothMoveToPosition(recyclerview_chat, myChatList.size());
                    break;
                case 1:

            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(EventMessage eventMessage)
    {
        if(eventMessage.getWhich()==2){
            myChatList.add(eventMessage.getMyChat());
            //更新列表
            android.os.Message msg2 = new android.os.Message();
            msg2.what = 0;
            myhandler.sendMessage(msg2);
        }
    }

    public void SendMessage(String chat_content){
        myApp = (MyGlobal)getApplication();
        ChatManager chatManager = myApp.getChatManager(); //从连接中得到聊天管理器
        Chat chat= chatManager.createChat(friend_id+"@"+myApp.getXMPP_DOMAIN());//创建一个聊天，username为对方用户名
        Message msg=new Message();
        msg.setBody(chat_content);//消息主体
        try {
            chat.sendMessage(msg);//发送一个文本消息
            myApp.Print("发送消息成功");
            //更新Message数据库
            myApp.myMessageDB.insert(user_id,friend_id, 0, chat_content, myApp.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
            //更新Message界面
            //更新Message界面
            myApp.SetListFromMessageDB(user_id);
            updateMessage();
            //更新Chat数据库
            MyChat myChat = new MyChat(0, user_id, friend_id, chat_content, 0, myApp.getCurrentDate("yyyy-MM-dd HH:mm:ss"), 0);
            myApp.myChatDB.insert(myChat);
            myChatList.add(myChat);
            //更新Chat界面
            android.os.Message msg2 = new android.os.Message();
            msg2.what = 0;
            myhandler.sendMessage(msg2);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            myApp.Print("发送消息失败");
        }
    }

    //更新MessageFragment界面的数据列表
    public void updateMessage(){
        EventMessage eventMessage = new EventMessage();
        eventMessage.setWhich(0);
        eventMessage.setMyMessageList(myApp.myMessageList);
        EventBus.getDefault().post(eventMessage);
    }

    private void smoothMoveToPosition(RecyclerView mRecyclerView, final int position) {
        // 第一个可见位置
        int firstItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(0));
        // 最后一个可见位置
        int lastItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1));

        if (position < firstItem) {
            // 如果跳转位置在第一个可见位置之前，就smoothScrollToPosition可以直接跳转
            mRecyclerView.smoothScrollToPosition(position);
        } else if (position <= lastItem) {
            // 跳转位置在第一个可见项之后，最后一个可见项之前
            // smoothScrollToPosition根本不会动，此时调用smoothScrollBy来滑动到指定位置
            int movePosition = position - firstItem;
            if (movePosition >= 0 && movePosition < mRecyclerView.getChildCount()) {
                int top = mRecyclerView.getChildAt(movePosition).getTop();
                mRecyclerView.smoothScrollBy(0, top);
            }
        } else {
            // 如果要跳转的位置在最后可见项之后，则先调用smoothScrollToPosition将要跳转的位置滚动到可见位置
            // 再通过onScrollStateChanged控制再次调用smoothMoveToPosition，执行上一个判断中的方法
            mRecyclerView.smoothScrollToPosition(position);
        }
    }

    @Override
    protected void onDestroy() {
        if(EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }
}
