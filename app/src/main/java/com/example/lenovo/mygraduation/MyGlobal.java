package com.example.lenovo.mygraduation;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.lenovo.mygraduation.Bean.MyChat;
import com.example.lenovo.mygraduation.Bean.MyFriend;
import com.example.lenovo.mygraduation.Bean.MyMessage;
import com.example.lenovo.mygraduation.MyDataBase.MyChatDB;
import com.example.lenovo.mygraduation.MyDataBase.MyFriendDB;
import com.example.lenovo.mygraduation.MyDataBase.MyMessageDB;
import com.example.lenovo.mygraduation.MyDataBase.MyNewFriendDB;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.android.AndroidSmackInitializer;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntries;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
//import org.jxmpp.jid.EntityBareJid;
//import org.jxmpp.jid.impl.JidCreate;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.MembershipKey;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by lenovo on 2019/2/3.
 */

public class MyGlobal extends Application {
    //服务器信息
    final String XMPP_DOMAIN = "win10.yangzhilong.cn";
    final String XMPP_HOST = "192.168.199.228";
    final int XMPP_PORT = 5222;

    //远程数据库信息
    static private String connectString = "jdbc:mysql://192.168.199.228:3306/openfire"
            + "?autoReconnect=true&useUnicode=true&useSSL=false"
            + "&characterEncoding=utf-8&serverTimezone=UTC";
    static private String  sql_user="root";
    static private String  sql_pwd="123456";

    //数据列表
    public List<MyFriend> myFriendList;
    public List<MyMessage> myMessageList;

    //本地数据库
    public MyFriendDB myFriendDB;
    public MyMessageDB myMessageDB;
    public MyChatDB myChatDB;
    public MyNewFriendDB myNewFriendDB;

    private ChatManager chatManager;

    private final int TOAST = -1;
    //连接对象
    private AbstractXMPPConnection mConnection;

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("MyGrobal:onCreate");

        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("数据库类导入成功");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        AndroidSmackInitializer androidSmackInitializer = new AndroidSmackInitializer();
        androidSmackInitializer.initialize();

        myFriendList = new ArrayList<MyFriend>();
        myMessageList = new ArrayList<MyMessage>();
        myFriendDB = new MyFriendDB(this);
        myMessageDB = new MyMessageDB(this);
        myChatDB = new MyChatDB(this);
        myNewFriendDB = new MyNewFriendDB(this);
    }

    public void SetListFromFriendDB(String user_id) {
        myFriendList = myFriendDB.getAllFriend(user_id);
    }

    public void SetListFromMessageDB(String user_id) {
        myMessageList = myMessageDB.getAllMessage(user_id);
    }

    public void MakeToast(String str) {
        android.os.Message msg = new android.os.Message();
        msg.what = TOAST;
        Bundle bundle = new Bundle();
        bundle.putString("message", str);
        msg.setData(bundle);
        myhandler.sendMessage(msg);
    }

    public Handler myhandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {      //判断标志位
                case TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString("message"), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void Print(String s) {
        System.out.println(s);
    }

    //添加好友
    public boolean addFriend(String owner_id, String friend_id){
        Roster roster = Roster.getInstanceFor(mConnection);
        try {
            //发送好友请求
            roster.createEntry(friend_id + "@" + XMPP_DOMAIN, friend_id, new String[]{"Friends"});
            System.out.println("成功发送好友请求");
            return true;

        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            MakeToast("无法连接服务器");
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        return false;
    }

    //同意好友请求
    public boolean agreeNewFriend(String owner_id, String friend_id){
        Presence pres = new Presence(Presence.Type.subscribed);//拒绝unsubscribed
        pres.setTo(friend_id+"@"+XMPP_DOMAIN);
        try {
            mConnection.sendStanza(pres); //同意请求
            if(!addFriend(owner_id, friend_id)){ //发送好友申请
                return false;
            }
            return true;
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        return false;
    }

    //拒绝好友请求
    public boolean refuseNewFriend(String friend_id){
        Presence pres = new Presence(Presence.Type.unsubscribed);//拒绝unsubscribed
        pres.setTo(friend_id+"@"+XMPP_DOMAIN);
        try {
            mConnection.sendStanza(pres);
            return true;
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean connect() {
        //配置一个TCP连接
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setServiceName(XMPP_DOMAIN)//设置服务器名称，可以到openfire服务器管理后台查看
                .setHost(XMPP_HOST)//设置主机
                .setPort(5222)//设置端口
                .setConnectTimeout(20000)//设置连接超时时间
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)//设置是否启用安全连接
                .build();
        XMPPTCPConnection connection = new XMPPTCPConnection(config);//根据配置生成一个连接
        this.mConnection = connection;
        mConnection.addConnectionListener(new XMPPConnectionListener());
        Print("开始连接");
        try {
            connection.connect();//连接到服务器
            chatManager = ChatManager.getInstanceFor(mConnection);
            this.mConnection = connection;
            Print("连接成功");
            return true;
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            e.printStackTrace();
        }
        return false;
    }

    public class XMPPConnectionListener implements ConnectionListener {

        @Override
        public void connected(XMPPConnection connection) {
            //已连接上服务器
        }

        @Override
        public void authenticated(XMPPConnection connection, boolean resumed) {
            //已认证
        }

        @Override
        public void connectionClosed() {
            //连接已关闭
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            //关闭连接发生错误
        }

        @Override
        public void reconnectionSuccessful() {
            //重连成功
        }

        @Override
        public void reconnectingIn(int seconds) {
            //重连中
        }

        @Override
        public void reconnectionFailed(Exception e) {
            //重连失败
        }
    }

    public void disConnect() {
        if (mConnection != null && mConnection.isConnected()) mConnection.disconnect();
    }

    //是否已连接
    public boolean ifConnect() {
        if (mConnection != null && mConnection.isConnected()) return true;
        else return false;
    }

    //登录
    public boolean login(String userName, String passWord) {
        Print("正在登录...");
        try {
            if (!mConnection.isAuthenticated()) { // 判断是否登录
                mConnection.login(userName, passWord);
                Print("登录成功！");
                return true;
            }
            Print("已被登录，登录失败...");
            return false;
        } catch (XMPPException | SmackException | IOException e) {
            e.printStackTrace();
            Print("登录出错...");
            return false;
        }
    }

    public AbstractXMPPConnection getmConnection() {
        return mConnection;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public String getXMPP_DOMAIN() {
        return XMPP_DOMAIN;
    }

    public String getXMPP_HOST() {
        return XMPP_HOST;
    }

    public String getCurrentDate(String pattern) {

        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String timestamp = formatter.format(curDate);

        return timestamp;
    }

    public String get_from_message(Message message) {
        String temp = message.getFrom();
        String result = "";
        for (int i = 0; i < temp.length(); i++) {
            if (temp.charAt(i) == '@') {
                result = temp.substring(0, i);
                break;
            }
        }
        return result;
    }

    public String get_to_message(Message message) {
        String temp = message.getTo();
        String result = "";
        for (int i = 0; i < temp.length(); i++) {
            if (temp.charAt(i) == '@') {
                result = temp.substring(0, i);
                break;
            }
        }
        return result;
    }

    public void getUserHead(String user_id) throws XMPPException.XMPPErrorException{
        System.out.println("获取用户头像信息: " + user_id);
        VCard vcard = new VCard();
        try {
            vcard.load(mConnection, user_id + "@" + XMPP_DOMAIN);
            if (vcard.getAvatar() != null) {
                byte[] b = vcard.getAvatar();
                //取出图形为Bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
                SaveImage(bitmap, user_id);
            }
        } catch (SmackException.NoResponseException e1) {
            e1.printStackTrace();
        }catch (SmackException.NotConnectedException e1) {
            e1.printStackTrace();
        }
    }

    //向服务器上传头像并保存
    public void saveUserHead(String user_id, Bitmap bitmap) {
        if(bitmap == null) return ;
        System.out.println("保存用户头像信息: " + user_id);
        VCard vcard = new VCard();
        try {
            vcard.load(mConnection, user_id + "@" + XMPP_DOMAIN);
            //bitmap转为byte数组
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] b = baos.toByteArray();
            vcard.setAvatar(b);
            vcard.save(mConnection);
            SaveImage(bitmap, user_id);
            System.out.println("保存用户头像成功");
//            MakeToast("头像保存成功");
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
            MakeToast("头像保存失败");
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
            MakeToast("头像保存失败");
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            MakeToast("头像保存失败");
        }
    }

    //将bitmap文件保存到本地
    public void SaveImage(Bitmap image, String user_id) {
        //照片通常存在DCIM文件夹中
        String sdCardDir = getApplicationContext().getCacheDir() + "/DCIM/";
//        String sdCardDir = Environment.getExternalStorageDirectory()+"/DCIM/";
        //为APP创建一个文件夹来存储图片
        File appDir = new File(sdCardDir, "MyGraduation");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        //图片名称
        String fileName = user_id + ".jpg";
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

    public List<String> searchUsers(String userName) throws XMPPException.XMPPErrorException {
        List<String> result = new ArrayList<String>();
        System.out.println("查询开始..............." + mConnection.getHost() + mConnection.getServiceName());
        UserSearchManager usm = new UserSearchManager(mConnection);
        try {
            Form searchForm = usm.getSearchForm("search."+XMPP_DOMAIN);
            Form answerForm = searchForm.createAnswerForm();
            answerForm.setAnswer("Username", true);
            answerForm.setAnswer("search", userName);
            ReportedData data = usm.getSearchResults(answerForm, "search."+XMPP_DOMAIN);
            List<ReportedData.Row> it = data.getRows();
            for(ReportedData.Row row:it){
                String temp_userid = row.getValues("Username").get(0);
                result.add(temp_userid);
//                System.out.println(row.getValues("Username"));
//                System.out.println(row.getValues("Username").get(0));
                getUserHead(temp_userid);
            }
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        return result;
    }

    //远程数据库操作
    public void mysql_insert_myfriend(String user_id,String friend_id){
        new Thread() {
            public void run() {
                try {
                    Connection con = DriverManager.getConnection(connectString, sql_user, sql_pwd);
                    Statement stmt = con.createStatement();
                    String QueryString = "SELECT * FROM myfriend WHERE owner_id='" + user_id + "' AND friend_id='" + friend_id + "'";
                    ResultSet rs = stmt.executeQuery(QueryString);
                    if(!rs.next()){
                        rs.close();
                        String InsertString = "INSERT INTO myfriend(owner_id, friend_id) VALUES('"+user_id+"','"+friend_id+"')";
                        int i = stmt.executeUpdate(InsertString);
                        System.out.println("远程数据库修改成功");
                    }
                    stmt.close();
                    con.close();
                } catch (SQLException e) { //连接失败
                    e.printStackTrace();
                }
            }
        }.start();
    }
    public void mysql_delete_myfriend(String user_id,String friend_id){
        new Thread() {
            public void run() {
                try {
                    Connection con = DriverManager.getConnection(connectString, sql_user, sql_pwd);
                    Statement stmt = con.createStatement();
                    String DeleteString = "DELETE FROM myfriend WHERE myuser='"+user_id+"' AND myfriend='"+friend_id+"'";
                    int i = stmt.executeUpdate(DeleteString);
                    stmt.close();
                    con.close();
                    System.out.println("远程数据库修改成功");
                } catch (SQLException e) { //连接失败
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
