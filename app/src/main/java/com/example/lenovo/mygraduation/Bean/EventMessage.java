package com.example.lenovo.mygraduation.Bean;

import org.jivesoftware.smack.chat2.Chat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 2019/3/11.
 */

public class EventMessage {
    private int which;
    //0代表传入到MessageFragment
    //1x代表传入到FriendFragment 10:更新friend列表 11:设置new标志 12:将new标志取消
    //2代表传入到ChatActivity
    //3代表传入到MineFragment
    private List<MyMessage> myMessageList;
    private List<MyFriend> myFriendList;
    private MyChat myChat;
    public EventMessage(){
        myMessageList = new ArrayList<MyMessage>();
        myFriendList = new ArrayList<MyFriend>();
    }

    public void setWhich(int i){
        which = i;
    }
    public void setMyMessageList(List<MyMessage> myMessageList){
        this.myMessageList = myMessageList;
    }
    public void setMyFriendList(List<MyFriend> myFriendList){
        this.myFriendList = myFriendList;
    }

    public void setMyChat(MyChat myChat){
        this.myChat = myChat;
    }

    public int getWhich(){
        return which;
    }

    public List<MyMessage> getMyMessageList(){
        return myMessageList;
    }

    public List<MyFriend> getMyFriendList(){
        return myFriendList;
    }

    public MyChat getMyChat(){
        return myChat;
    }
}
