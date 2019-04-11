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

import com.example.lenovo.mygraduation.Bean.MyUser;
import com.example.lenovo.mygraduation.NewFriendActivity;
import com.example.lenovo.mygraduation.R;

import java.util.List;

/**
 * Created by lenovo on 2019/3/21.
 */

public class RVAdapterNewFriend extends RecyclerView.Adapter<RVAdapterNewFriend.ViewHolder>  {
    private List<String> newFriendList;
    //这样就可以调用Activity中的函数
    private NewFriendActivity context;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View itemview_newfriend;
        ImageView iv_userhead;
        TextView tv_userid;
        Button button_agree;
        Button button_refuse;


        public ViewHolder(View view) {
            super(view);
            itemview_newfriend = view;
            iv_userhead = (ImageView)view.findViewById(R.id.iv_userhead);
            tv_userid = (TextView) view.findViewById(R.id.tv_userid);
            button_agree = (Button) view.findViewById(R.id.button_agree);
            button_refuse = (Button)view.findViewById(R.id.button_refuse);
        }
    }

    public RVAdapterNewFriend(NewFriendActivity context, List<String> newFriendList) {
        this.context = context;
        this.newFriendList = newFriendList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.newfriend_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.button_agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                context.agreeFriend(position);
            }
        });
        holder.button_refuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                context.refuseFriend(position);
            }
        });
        return holder;
    }

    //这个方法主要用于适配渲染数据到View中
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String tempuser = newFriendList.get(position);
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
        return newFriendList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void setNewData(List<String> newFriendList){
        this.newFriendList = newFriendList;
    }

}
