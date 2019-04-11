package com.example.lenovo.mygraduation.Adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovo.mygraduation.Bean.MyMessage;
import com.example.lenovo.mygraduation.MainActivity;
import com.example.lenovo.mygraduation.R;

import java.util.List;

/**
 * Created by lenovo on 2019/2/23.
 */

public class RVAdapterMessage extends RecyclerView.Adapter<RVAdapterMessage.ViewHolder> {

    private List<MyMessage> myMessageList;
    private MainActivity context;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View itemview_message;
        ImageView message_image;
        TextView message_id;
        TextView message_content;
        TextView message_date;

        public ViewHolder(View view) {
            super(view);
            itemview_message = view;
            message_image = view.findViewById(R.id.message_image);
            message_id = view.findViewById(R.id.message_id);
            message_content = view.findViewById(R.id.message_content);
            message_date = view.findViewById(R.id.message_date);
        }
    }

    public RVAdapterMessage(MainActivity context, List<MyMessage> myMessageList) {
        this.context = context;
        this.myMessageList = myMessageList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.itemview_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                MyMessage temp_mymessage = myMessageList.get(position);
//                Toast.makeText(v.getContext(), "you clicked view " + temp_mymessage.friend_id, Toast.LENGTH_SHORT).show();
                context.GotoChatActivity(temp_mymessage.friend_id);
            }
        });
        holder.itemview_message.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int position = holder.getAdapterPosition();
                        MyMessage temp_mymessage = myMessageList.get(position);
                        Toast.makeText(v.getContext(), "you clicked view " + temp_mymessage.friend_id, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
        );
        return holder;
    }

    //这个方法主要用于适配渲染数据到View中
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MyMessage tempmessage = myMessageList.get(position);
        Bitmap bitmap = context.getImage(tempmessage.friend_id);
        if(bitmap != null){
            BitmapDrawable bd = new BitmapDrawable(bitmap);
            Drawable d = (Drawable) bd;
            holder.message_image.setBackground(d);
        }
        holder.message_id.setText(tempmessage.friend_id);
        holder.message_content.setText(tempmessage.message_content);
        holder.message_date.setText(tempmessage.message_date.substring(11,16));
    }

    //BaseAdapter的getCount方法了，即总共有多少个条目
    @Override
    public int getItemCount() {
        return myMessageList.size();
    }

    public void setNewData(List<MyMessage> myMessageList){
        this.myMessageList = myMessageList;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}