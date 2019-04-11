package com.example.lenovo.mygraduation;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovo.mygraduation.Bean.DataSerializable;
import com.example.lenovo.mygraduation.Bean.EventMessage;
import com.example.lenovo.mygraduation.Bean.MyChat;
import com.example.lenovo.mygraduation.Bean.MyFriend;
import com.example.lenovo.mygraduation.Bean.MyMessage;
import com.example.lenovo.mygraduation.MyDataBase.MyFriendDB;
import com.example.lenovo.mygraduation.MyDataBase.MyMessageDB;
import com.example.lenovo.mygraduation.MyFragment.FriendsFragment;
import com.example.lenovo.mygraduation.MyFragment.FunctionFragment;
import com.example.lenovo.mygraduation.MyFragment.MessageFragment;
import com.example.lenovo.mygraduation.MyFragment.MineFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;


import com.example.lenovo.mygraduation.Adapter.MainFragmentStatePagerAdapter;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

// implements ViewPager.OnPageChangeListener
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Toolbar main_head;
    private MyGlobal myApp;
    private ImageView image_message;
    private ImageView image_friends;
    private ImageView image_function;
    private ImageView image_mine;
    private TextView text_message;
    private TextView text_friends;
    private TextView text_function;
    private TextView text_mine;
    private View overview_tab;
    //四个页面的代表数字
    private final int PAGE_MESSAGE = 0;
    private final int PAGE_FRIENDS = 1;
    private final int PAGE_FUNCTION = 2;
    private final int PAGE_MINE = 3;
    private int old_page = 0;
    private int new_page = 0;
    //handle中关于view的设置操作码
    private final int SETVIEW = 0;
    private ViewPager main_viewpager;
    private List<Fragment> fragmentList;
    //共享参数，用于存储用户账号密码
    private SharedPreferences shared;
    private SharedPreferences.Editor editor;
    //用户账号与密码
    private String user_id;
    private String user_pw;
    private String user_name;
    //是否为初次登陆
    private boolean first_time;
    //从LoginActivity中传来的Bundle
    private Bundle bundle_login;

    //屏幕宽度
    private int screenWidth;
    private int screenHight;
    //繁忙中的图标
    private LinearLayout layout_busying;
    private boolean busying;
    //主页面
    private LinearLayout layout_main;

    //序列化数据
    private DataSerializable serializable;

    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Init();
        Set_Busying();
        InitialFragment();
        SetToolBar();
        SetListener();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(myApp.connect()){   //连接成功
                    if(myApp.login(user_id,user_pw)){  //登录成功
                        myApp.MakeToast("登录成功");
                    }
                    else{  //登录失败
                        myApp.MakeToast("登录失败");
                    }
                }
                else{
                    myApp.MakeToast("无法连接服务器");
                }
//                  接收好友请求
                ReceiveNewFriend();
//                    接收消息
                ReceiveMessage();
                if(first_time){ //如果是初次登陆
                    //从服务器中读取好友列表等数据并存储在本地数据库以及List列表中
                    GetFriendFromServer();
                    //保存自己的头像
                    try {
                        myApp.getUserHead(user_id);
                    } catch (XMPPException.XMPPErrorException e) {
                        myApp.MakeToast("无法连接服务器");
                        e.printStackTrace();
                    }
                }
                //如果不是首次登陆，从本地数据库中读取好友列表到List中
                else {
                    myApp.SetListFromFriendDB(user_id);
                }
                //从数据库中读取最近的消息列表到List中
                myApp.SetListFromMessageDB(user_id);
//                myApp.myMessageList.add(new MyMessage("user1","user2",1,0,"你好","12:00"));
                android.os.Message ms = new android.os.Message();
                ms.what = SETVIEW;
                myhandler.sendMessage(ms);
            }
        }).start();
    }

    public Handler myhandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {      //判断标志位
                case SETVIEW:
                    updateMessage();
                    updateFriend();
                    updateMineFragment();
                    Set_Not_Busying();
            }
        }
    };

    private void Init(){
        //处理活动传入的数据
        bundle_login = getIntent().getExtras();
        user_id = bundle_login.getString("user_id");
        user_pw = bundle_login.getString("user_pw");
        first_time = bundle_login.getBoolean("first_time");
        //全局变量
        myApp = (MyGlobal)getApplication();

        //屏幕信息
        screenHight = getResources().getDisplayMetrics().heightPixels;
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        //共享参数
        shared = getSharedPreferences("user", MODE_PRIVATE);
        editor = shared.edit();
        user_id = shared.getString("user_id","");
        user_pw = shared.getString("user_pw","");
        user_name = shared.getString("user_name", "");
        busying = false;
        FindView();
    }
    //设置点击事件
    @Override
    public void onClick(View view) {
        int id = view.getId();
        //如果点击了聊天图标
        if(id == R.id.image_message || id == R.id.text_message){
            Change_Page(PAGE_MESSAGE);
        }
        //如果点击了好友图标
        if(id == R.id.image_friends || id == R.id.text_friends){
            Change_Page(PAGE_FRIENDS);
        }
        //如果点击了功能图标
        if(id == R.id.image_function || id == R.id.text_function) {
            Change_Page(PAGE_FUNCTION);
        }
        //如果点击了个人图标
        if(id == R.id.image_mine || id == R.id.text_mine){
            Change_Page(PAGE_MINE);
        }
    }

    //初始化控件变量
    void FindView(){
        layout_main = (LinearLayout)findViewById(R.id.layout_main);
        image_message = (ImageView)findViewById(R.id.image_message);
        image_friends = (ImageView)findViewById(R.id.image_friends);
        image_function = (ImageView)findViewById(R.id.image_function);
        image_mine = (ImageView)findViewById(R.id.image_mine);
        text_message = (TextView)findViewById(R.id.text_message);
        text_friends = (TextView)findViewById(R.id.text_friends);
        text_function = (TextView)findViewById(R.id.text_function);
        text_mine = (TextView)findViewById(R.id.text_mine);
        main_viewpager = (ViewPager)findViewById(R.id.main_viewpager);
        overview_tab = findViewById(R.id.overview_tab);
        layout_busying = (LinearLayout)findViewById(R.id.layout_busying);
    }

    void GetFriendFromServer() {
        myApp = (MyGlobal) getApplication();
        Roster roster = Roster.getInstanceFor(myApp.getmConnection());
        if (!roster.isLoaded()){
            try {
                roster.reloadAndWait();
            } catch (SmackException.NotLoggedInException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set entries = roster.getEntries();
        System.out.println("getAllFriend");
        for (Object object:entries) {
            System.out.println("catch a friend");
            RosterEntry entry = (RosterEntry)object;
//            entry.getType();
//            entry.getName();//好友昵称
//            entry.getGroups();//好友所在的组
//            entry.getJid().getDomain();//好友域名
//            entry.getJid().getLocalpartOrNull();//好友名字
//            entry.getUser();//(废弃)好友完整名称（包括域名）
//            myApp.Print(entry.getType().toString());
//            myApp.Print(entry.getName());
//            myApp.Print(entry.getGroups().toString());
//            myApp.Print(entry.getJid().getDomain().toString());
//            myApp.Print(entry.getJid().getLocalpartOrNull().toString());
//            myApp.Print(entry.getUser());
            String friend_id = entry.getName();
            if(friend_id == null){
                continue;
            }
            try {
                myApp.getUserHead(friend_id);
                MyFriend myFriend = new MyFriend(user_id, friend_id);
                myApp = (MyGlobal)getApplication();
                myApp.myFriendDB.insert(myFriend);
                myApp.myFriendList.add(myFriend);
            } catch (XMPPException.XMPPErrorException e) {
                myApp.MakeToast("无法连接服务器");
                e.printStackTrace();
            }
        }
//        }
    }

    void SetListener(){
        image_message.setOnClickListener(this);
        image_friends.setOnClickListener(this);
        image_function.setOnClickListener(this);
        image_mine.setOnClickListener(this);
        text_message.setOnClickListener(this);
        text_friends.setOnClickListener(this);
        text_function.setOnClickListener(this);
        text_mine.setOnClickListener(this);
        //菜单点击监听器
        main_head.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                String msg = "";
                switch (menuItem.getItemId()) {
                    case R.id.search:
                        msg += "search";
                        break;
                    case R.id.groupchat:
                        msg += "groupchat";
                        break;
                    case R.id.addfriend:
                        GotoAddFriendActivity();
                        break;
                    case R.id.scan:
                        msg += "scan";
                        break;
                    case R.id.suggest:
                        msg += "suggest";
                        break;
                }
                if(!msg.equals("")) {
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        //点击监听器，该函数要放到setSupportActionBar之后，不然不起作用
        main_head.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"点击返回！",Toast.LENGTH_SHORT).show();
            }
        });
    }

    //设置工具栏
    void SetToolBar(){
        //获取工具栏
        main_head = (Toolbar)findViewById(R.id.main_head);
        //设置返回图标
//        main_head.setNavigationIcon(R.mipmap.back);
        //设置标题文本
        main_head.setTitle(R.string.app_name);
        //标题文本颜色
        main_head.setTitleTextColor(Color.WHITE);
        //设置logo图标
//        main_head.setLogo(R.mipmap.wechat);
        //副标题
//        main_head.setSubtitle("副标题");
        //工具栏背景
        main_head.setBackgroundColor(getResources().getColor(R.color.toolbar_background));
        //使用main_head替换系统自带的ActionBar
        setSupportActionBar(main_head);
    }

    //绑定菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 绑定toobar跟menu
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    //要重写onPrepareOptionsPanel方法才可以显示出图标
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (menu != null) {
            if (menu.getClass() == MenuBuilder.class) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onPrepareOptionsPanel(view, menu);
    }

    //换ViewPager时
    void Change_Page(int page_number){
        main_viewpager.setCurrentItem(page_number);
        ChangeTab();
    }

    //改变下方标签栏的状态
    public void ChangeTab(){
        new_page = main_viewpager.getCurrentItem();
        //将旧的选择页面恢复状态
        switch (old_page){
            case PAGE_MESSAGE:
                image_message.setSelected(false);
                text_message.setSelected(false);
                break;
            case PAGE_FRIENDS:
                image_friends.setSelected(false);
                text_friends.setSelected(false);
                break;
            case PAGE_FUNCTION:
                image_function.setSelected(false);
                text_function.setSelected(false);
                break;
            case PAGE_MINE:
                image_mine.setSelected(false);
                text_mine.setSelected(false);
                break;
        }
        //给新的选择页面设定状态
        switch (new_page){
            case PAGE_MESSAGE:
                image_message.setSelected(true);
                text_message.setSelected(true);
                break;
            case PAGE_FRIENDS:
                image_friends.setSelected(true);
                text_friends.setSelected(true);
                break;
            case PAGE_FUNCTION:
                image_function.setSelected(true);
                text_function.setSelected(true);
                break;
            case PAGE_MINE:
                image_mine.setSelected(true);
                text_mine.setSelected(true);
                break;
        }
        //增加一个覆盖图片的动画
        if(old_page != new_page){
            TranslateAnimation translateAnimation=new TranslateAnimation(old_page*(screenWidth/4),new_page*(screenWidth/4), 0, 0);
            translateAnimation.setFillAfter(true);
            translateAnimation.setDuration(200);
            overview_tab.startAnimation(translateAnimation);
        }
        old_page = new_page;
    }

    private void InitialFragment(){
        Bundle bundle;
        //需要传输到fragment的序列化数据
        myApp = (MyGlobal)getApplication();
        fragmentList = new ArrayList<Fragment>();
        //消息页面
        MessageFragment messageFragment = new MessageFragment();
        fragmentList.add(messageFragment);
        //好友页面
        FriendsFragment friendsFragment = new FriendsFragment();
        fragmentList.add(friendsFragment);
        //功能页面
        fragmentList.add(new FunctionFragment());
        //个人信息页面
        MineFragment mineFragment = new MineFragment();
        bundle = new Bundle();
        bundle.putString("user_id", user_id);
        mineFragment.setArguments(bundle);
        fragmentList.add(mineFragment);
//
        main_viewpager.setAdapter(new MainFragmentStatePagerAdapter(this, getSupportFragmentManager(),fragmentList));
        Change_Page(PAGE_MESSAGE);//设置当前页是第一页
        //最多可以缓存4页
        main_viewpager .setOffscreenPageLimit(4);
    }

    private void Set_Busying(){
        busying = true;
        layout_busying.setVisibility(View.VISIBLE);
    }

    private void Set_Not_Busying(){
        busying = false;
        layout_busying.setVisibility(View.INVISIBLE);
    }

    public void SaveImage(Bitmap image, String user_id){
        //照片通常存在DCIM文件夹中
        String sdCardDir = getApplicationContext().getCacheDir()+"/DCIM/";
//        String sdCardDir = Environment.getExternalStorageDirectory()+"/DCIM/";
        //为APP创建一个文件夹来存储图片
        File appDir = new File(sdCardDir, "MyGraduation");
        if(!appDir.exists()){
            appDir.mkdirs();
        }
        //图片名称
        String fileName = user_id+".jpg";
        //设置图片路径
        File imageDir = new File(appDir, fileName);
        FileOutputStream fos = null;
        System.out.println(imageDir.toString());
        try {
            fos = new FileOutputStream(imageDir);
            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            System.out.println(imageDir.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void removeFriend(String friend_id){
        Roster roster = Roster.getInstanceFor(myApp.getmConnection());
        RosterEntry entry = roster.getEntry(friend_id+"@"+myApp.getXMPP_DOMAIN());
        try {
            roster.removeEntry(entry);
            //更新Friend列表
            myApp.myFriendDB.delete(user_id,friend_id);
            myApp.SetListFromFriendDB(user_id);
            updateFriend();
            //更新Message列表
            myApp.myMessageDB.delete(user_id, friend_id);
            myApp.SetListFromMessageDB(user_id);
            updateMessage();
            //更新远程数据库
            myApp.mysql_delete_myfriend(user_id, friend_id);
            myApp.mysql_delete_myfriend(friend_id, user_id);
            myApp.MakeToast("删除成功");
        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

    }

    public void ReceiveMessage(){
        ChatManager chatManager = myApp.getChatManager();
        if (chatManager != null){
            chatManager.addChatListener(new ChatManagerListener() {
                /**
                 * @param chat
                 * @param b    消息是否来自本地用户
                 */
                @Override
                public void chatCreated(Chat chat, boolean b) {
                    if (!b) {
                        chat.addMessageListener(new ChatMessageListener() {
                            @Override
                            public void processMessage(Chat chat2, final Message message) {
                                System.out.println("ReceiveMessage");
                                String msg=message.getBody();
                                //在此处可以处理接收到的消息
                                //………
                                //如果在发送的时候有自定义消息体，比如我们发送的时候添加了一个名为“textColor”的消息体，在这里就可以根据名称取出
//                            String color=message.getBody("textColor");
                                if (msg != null) {
//                                String friend_id = from.getLocalpart().toString();
                                    String friend_id = myApp.get_from_message(message);
                                    String message_content = msg;
                                    //数据库更新
                                    myApp.myMessageDB.insert(user_id,friend_id, 0, message_content, myApp.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
                                    myApp.myChatDB.insert(friend_id, user_id, message_content, 0, myApp.getCurrentDate("yyyy-MM-dd HH:mm:ss"), 0);
                                    //更新Message界面
                                    myApp.SetListFromMessageDB(user_id);
                                    updateMessage();
                                    //更新Chat界面
                                    updateChat(new MyChat(0, friend_id, user_id, message_content, 0, myApp.getCurrentDate("yyyy-MM-dd HH:mm:ss"), 0));
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void ReceiveNewFriend(){
        myApp = (MyGlobal)getApplication();
        //条件过滤器
        AndFilter filter = new AndFilter(new StanzaTypeFilter(Presence.class));
        //packet监听器
        StanzaListener packetListener = new StanzaListener() {
            @Override
            public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                if (packet instanceof Presence) {
                    Presence presence = (Presence) packet;
                    String fromId = presence.getFrom();
                    String from = presence.getFrom().split("@")[0];//我这里只为了打印去掉了后缀
                    String str = "";
                    String str_toast = "";
                    if (presence.getType().equals(Presence.Type.subscribe)) {
                        str = from + "请求添加好友";
//                        if(myApp.myFriendDB.exists(user_id, from)){
//                            myApp.agreeNewFriend(user_id, from);
//                        }
//                        else{
                            str_toast = from + "请求添加好友";
                            myApp.myNewFriendDB.insert(user_id, from);
                            setNewMarkFriend();
//                        }
                    } else if (presence.getType().equals(Presence.Type.subscribed)) {//对方同意订阅
                        str = from + "同意了您的好友请求";
                        str_toast = from + "同意了您的好友请求";
                    } else if (presence.getType().equals(Presence.Type.unsubscribe)) {//拒绝订阅
                        str = from + "拒绝了您的好友请求";
                        str_toast = from + "拒绝了您的好友请求";
                        myApp.myFriendDB.delete(user_id, from);
                        myApp.SetListFromFriendDB(user_id);
                        updateFriend();
                    } else if (presence.getType().equals(Presence.Type.unsubscribed)) {//取消订阅
                        str = from + "将你从好友中删除";
                        str_toast = from + "将你从好友中删除";
                        myApp.myFriendDB.delete(user_id, from);
                        myApp.SetListFromFriendDB(user_id);
                        updateFriend();
                    } else if (presence.getType().equals(Presence.Type.unavailable)) {//离线
                        str = from + "下线了";
                    } else if (presence.getType().equals(Presence.Type.available)) {//上线
                        str = from + "上线了";
                    }
                    if(str.length()>0){
                        myApp.Print(str);
                    }
                    if(str_toast.length()>0){
                        myApp.MakeToast(str_toast);
                    }
                }
            }
        };
        //添加监听
        myApp.getmConnection().addAsyncStanzaListener(packetListener, filter);
    }

    //去聊天的界面
    public void GotoChatActivity(String friend_id){
        Bundle bundle = new Bundle();
        bundle.putString("owner_id", user_id);
        bundle.putString("friend_id", friend_id);
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    //退出登录时，删掉用户的数据
    public void Exit(){
        myApp.myMessageDB.clear();
        myApp.myFriendDB.clear();
        myApp.myChatDB.clear();
        myApp.myNewFriendDB.clear();
        myApp.myFriendList.clear();
        myApp.myMessageList.clear();
        editor.clear();
        editor.commit();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        //跳到MainActivity时清除掉LoginActivity
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    //更新MessageFragment界面的数据列表
    public void updateMessage(){
        EventMessage eventMessage = new EventMessage();
        eventMessage.setWhich(0);
        eventMessage.setMyMessageList(myApp.myMessageList);
        EventBus.getDefault().post(eventMessage);
    }

    //更新FriendFragment的数据列表
    public void updateFriend(){
        EventMessage eventMessage = new EventMessage();
        eventMessage.setWhich(10);
        eventMessage.setMyFriendList(myApp.myFriendList);
        EventBus.getDefault().post(eventMessage);
    }
    //更新自己的头像显示
    private void updateMineFragment(){
        EventMessage eventMessage = new EventMessage();
        eventMessage.setWhich(3);
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

    //更新Chat界面的内容
    public void updateChat(MyChat myChat){
        EventMessage eventMessage = new EventMessage();
        eventMessage.setWhich(2);
        eventMessage.setMyChat(myChat);
        EventBus.getDefault().post(eventMessage);
    }

    @Override
    protected void onDestroy() {
        myApp.disConnect();
        if(EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }

    public void GotoInfoActivity(){
        Intent intent = new Intent(MainActivity.this, InfoActivity.class);
        //跳到MainActivity时清除掉InfoActivity
        Bundle bundle = new Bundle();
        bundle.putString("user_id", user_id);
        bundle.putString("user_pw", user_pw);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void GotoAddFriendActivity(){
        Intent intent = new Intent(MainActivity.this, AddFriendActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("user_id", user_id);;
        bundle.putString("user_pw", user_pw);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void GotoNewFriendActivity(){
        Intent intent = new Intent(MainActivity.this, NewFriendActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("user_id", user_id);;
        bundle.putString("user_pw", user_pw);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void GotoNewFunctionActivity(){
        Intent intent = new Intent(MainActivity.this, NewFunctionActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("user_id", user_id);
        bundle.putString("user_pw", user_pw);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
