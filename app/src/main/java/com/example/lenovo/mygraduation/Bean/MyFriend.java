package com.example.lenovo.mygraduation.Bean;

/**
 * Created by lenovo on 2019/3/3.
 */

public class MyFriend {
    public String owner_id;
    public String friend_id;

    public MyFriend(){
        owner_id = "";
        friend_id = "";
    }

    public MyFriend(String owner_id, String friend_id){
        this.owner_id = owner_id;
        this.friend_id = friend_id;
    }
}
