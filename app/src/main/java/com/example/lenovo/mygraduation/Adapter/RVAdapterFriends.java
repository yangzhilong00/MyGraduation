package com.example.lenovo.mygraduation.Adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovo.mygraduation.Bean.MyFriend;
import com.example.lenovo.mygraduation.Bean.MyMessage;
import com.example.lenovo.mygraduation.MainActivity;
import com.example.lenovo.mygraduation.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by lenovo on 2019/2/23.
 */

public class RVAdapterFriends extends RecyclerView.Adapter<RVAdapterFriends.ViewHolder> {

    private List<MyFriend> myFriendsList;
    private MainActivity context;

    static class ViewHolder extends RecyclerView.ViewHolder {

        View itemview_fiend;
        ImageView imageview_image;
        TextView textview_name;

        public ViewHolder(View view) {
            super(view);
            itemview_fiend = view;
            imageview_image = (ImageView)view.findViewById(R.id.friends_image);
            textview_name = (TextView)view.findViewById(R.id.friends_name);
        }
    }

    public RVAdapterFriends(MainActivity context, List<MyFriend> myFriendsList) {
        this.context = context;
        this.myFriendsList = myFriendsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.itemview_fiend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                MyFriend temp_myfriend = myFriendsList.get(position);
//                Toast.makeText(v.getContext(), "you clicked view " + temp_myfriend.friend_id, Toast.LENGTH_SHORT).show();
                context.GotoChatActivity(temp_myfriend.friend_id);
            }
        });
        holder.itemview_fiend.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int position = holder.getAdapterPosition();
                        MyFriend temp_myfriend = myFriendsList.get(position);
                        context.removeFriend(temp_myfriend.friend_id);
                        return true;
                    }
                }
        );
        return holder;
    }

    //这个方法主要用于适配渲染数据到View中
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MyFriend temp_myfriend = myFriendsList.get(position);
        Bitmap bitmap = context.getImage(temp_myfriend.friend_id);
        if(bitmap != null){
            BitmapDrawable bd = new BitmapDrawable(bitmap);
            Drawable d = (Drawable) bd;
            holder.imageview_image.setBackground(d);
        }
        holder.textview_name.setText(temp_myfriend.friend_id);
    }

    //BaseAdapter的getCount方法了，即总共有多少个条目
    @Override
    public int getItemCount() {
        return myFriendsList.size();
    }

    public void setNewData(List<MyFriend> myFriendsList){
        this.myFriendsList = myFriendsList;
    }

}