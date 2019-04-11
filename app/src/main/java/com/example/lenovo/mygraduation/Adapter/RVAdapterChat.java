package com.example.lenovo.mygraduation.Adapter;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.lenovo.mygraduation.Bean.MyChat;
import com.example.lenovo.mygraduation.ChatActivity;
import com.example.lenovo.mygraduation.R;

import java.util.List;

/**
 * Created by lenovo on 2019/3/13.
 */

public class RVAdapterChat extends RecyclerView.Adapter<RVAdapterChat.ViewHolder> {

    private List<MyChat> myChatList;
    private ChatActivity context;
    String user_id;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View itemview_chat;
        RelativeLayout view_user;
        RelativeLayout view_friend;
        ImageView user_image;
        ImageView friend_image;
        TextView user_content;
        TextView friend_content;

        public ViewHolder(View view) {
            super(view);
            itemview_chat = view;
            view_user = view.findViewById(R.id.view_user);
            view_friend = view.findViewById(R.id.view_friend);
            user_image = view.findViewById(R.id.user_image);
            user_content = view.findViewById(R.id.user_content);
            friend_image = view.findViewById(R.id.friend_image);
            friend_content = view.findViewById(R.id.friend_content);
        }
    }

    public RVAdapterChat(ChatActivity context, List<MyChat> myChatList, String user_id) {
        this.context = context;
        this.myChatList = myChatList;
        this.user_id = user_id;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.itemview_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                MyChat temp_mychat = myChatList.get(position);

            }
        });
        holder.itemview_chat.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int position = holder.getAdapterPosition();
                        MyChat temp_mychat = myChatList.get(position);
                        return true;
                    }
                }
        );
        return holder;
    }

    //这个方法主要用于适配渲染数据到View中
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MyChat tempchat = myChatList.get(position);
        //如果自己是发送者
        if(tempchat.sender_id.equals(user_id)){
            Bitmap bitmap = context.getImage(tempchat.sender_id);
            if(bitmap!=null){
                holder.user_image.setImageBitmap(bitmap);
            }
            holder.user_content.setText(tempchat.chat_content);
        }
        else{//如果自己是发送者
            holder.view_user.setVisibility(View.GONE);
            holder.view_friend.setVisibility(View.VISIBLE);
            Bitmap bitmap = context.getImage(tempchat.sender_id);
            if(bitmap!=null){
                holder.friend_image.setImageBitmap(bitmap);
            }
            holder.friend_content.setText(tempchat.chat_content);
        }
    }

    //BaseAdapter的getCount方法了，即总共有多少个条目
    @Override
    public int getItemCount() {
        return myChatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

}