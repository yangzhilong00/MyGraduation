package com.example.lenovo.mygraduation.Bean;

import com.example.lenovo.mygraduation.Bean.MyFriend;
import com.example.lenovo.mygraduation.Bean.MyMessage;

import java.io.Serializable;
import java.util.List;

/**
 * Created by lenovo on 2019/3/6.
 */

public class DataSerializable implements Serializable {
    private List<MyFriend> myFriendList;
    private List<MyMessage> myMessageList;

    public void setListFriend(List<MyFriend> myFriendList){
        this.myFriendList = myFriendList;
    }
    public List<MyFriend> getListFriend(){
        return myFriendList;
    }
    public void setListMessage(List<MyMessage> myMessageList){
        this.myMessageList = myMessageList;
    }
    public List<MyMessage> getListMessage(){
        return myMessageList;
    }
}
