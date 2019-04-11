package com.example.lenovo.mygraduation.Bean;

/**
 * Created by lenovo on 2019/3/10.
 */

public class MyChat {
    public int chat_id;
    public String sender_id;
    public String receiver_id;
    public String chat_content;
    public int chat_type;
    public String chat_date;
    public int chat_state;

    public MyChat(int chat_id, String sender_id, String receiver_id, String chat_content, int chat_type, String chat_date, int chat_state){
        this.chat_id = chat_id;
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
        this.chat_content = chat_content;
        this.chat_type = chat_type;
        this.chat_date = chat_date;
        this.chat_state = chat_state;
    }
}
