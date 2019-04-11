package com.example.lenovo.mygraduation.Adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovo.mygraduation.AddFriendActivity;
import com.example.lenovo.mygraduation.R;

import java.util.List;

/**
 * Created by lenovo on 2019/3/21.
 */

public class RVAdapterAddFriend extends RecyclerView.Adapter<RVAdapterAddFriend.ViewHolder> {
    private List<String> addFriendList;
    private AddFriendActivity context;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View itemview_addfriend;
        ImageView iv_userhead;
        TextView tv_userid;
        Button button_add;
        public ViewHolder(View view) {
            super(view);
            itemview_addfriend = view;
            iv_userhead = view.findViewById(R.id.iv_userhead);
            tv_userid = view.findViewById(R.id.tv_userid);
            button_add = view.findViewById(R.id.button_add);
        }
    }

    public RVAdapterAddFriend(AddFriendActivity context, List<String> addFriendList) {
        this.context = context;
        this.addFriendList = addFriendList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.addfriend_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                String user_id = addFriendList.get(position);
                context.addFriend(user_id);
            }
        });
        holder.iv_userhead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                String user_id = addFriendList.get(position);
                Toast.makeText(v.getContext(), "you clicked view " + user_id, Toast.LENGTH_SHORT).show();
            }
        });
        return holder;
    }

    //这个方法主要用于适配渲染数据到View中
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String tempuser = addFriendList.get(position);
        holder.tv_userid.setText(tempuser);
        Bitmap bitmap = context.getImage(tempuser);
        if(bitmap != null){
            BitmapDrawable bd = new BitmapDrawable(bitmap);
            Drawable d = (Drawable) bd;
            holder.iv_userhead.setBackground(d);
        }
    }

    //BaseAdapter的getCount方法了，即总共有多少个条目
    @Override
    public int getItemCount() {
        return addFriendList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void setNewData(List<String> addFriendList){
        this.addFriendList = addFriendList;
    }
}
