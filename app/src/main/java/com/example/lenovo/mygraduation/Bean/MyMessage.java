package com.example.lenovo.mygraduation.Bean;

import java.sql.Time;
import java.util.Date;

/**
 * Created by lenovo on 2019/2/23.
 */

public class MyMessage {
    public String owner_id;
    public String friend_id;
    public String message_content;
    public String message_date;
    public int message_type;

    public MyMessage(String owner_id, String friend_id, int message_type, String message_content, String message_date){
        this.owner_id = owner_id;
        this.friend_id = friend_id;
        this.message_content = message_content;
        this.message_date = message_date;
        this.message_type = message_type;
    }
}
