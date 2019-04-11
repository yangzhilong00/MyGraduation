package com.example.lenovo.mygraduation.MyFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lenovo.mygraduation.Adapter.RVAdapterMessage;
import com.example.lenovo.mygraduation.Bean.EventMessage;
import com.example.lenovo.mygraduation.Bean.MyMessage;
import com.example.lenovo.mygraduation.Bean.DataSerializable;
import com.example.lenovo.mygraduation.MainActivity;
import com.example.lenovo.mygraduation.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;


public class MessageFragment extends Fragment {
    private RecyclerView recyclerview_message;
    List<MyMessage> myMessageList;
    RVAdapterMessage adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        //注册EventBus
        EventBus.getDefault().register(this);

        recyclerview_message = (RecyclerView)view.findViewById(R.id.recycleview_message);
//        DataSerializable dataSerializable = (DataSerializable)getArguments().getSerializable("data_to_messagefragment");
//        myMessageList = dataSerializable.getListMessage();
        myMessageList = new ArrayList<MyMessage>();

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        /*// 设置布局管理器
        //垂直分布和横向分布  LinearLayoutManager.HORIZONTAL  VERTICAL
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);*/
        recyclerview_message.setLayoutManager(layoutManager);


        adapter = new RVAdapterMessage((MainActivity)getActivity(), myMessageList);
        recyclerview_message.setAdapter(adapter);
        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(EventMessage eventMessage) {
        int which = eventMessage.getWhich();
        if(which == 0){
            myMessageList = eventMessage.getMyMessageList();
            adapter.setNewData(myMessageList);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        if(EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroyView();
    }
}
