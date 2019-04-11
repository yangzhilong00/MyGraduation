package com.example.lenovo.mygraduation.MyFragment;

import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.lenovo.mygraduation.Adapter.RVAdapterFriends;
import com.example.lenovo.mygraduation.Bean.EventMessage;
import com.example.lenovo.mygraduation.Bean.MyFriend;
import com.example.lenovo.mygraduation.Bean.DataSerializable;
import com.example.lenovo.mygraduation.MainActivity;
import com.example.lenovo.mygraduation.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FriendsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendsFragment extends Fragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private RecyclerView recyclerview_friends;
    private RelativeLayout button_newfriend;
    private RelativeLayout button_groupchat;
    private ImageView iv_new;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RVAdapterFriends adapter;
    private List<MyFriend> myFriendsList;

    private OnFragmentInteractionListener mListener;

    public FriendsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendsFragment newInstance(String param1, String param2) {
        FriendsFragment fragment = new FriendsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        //注册EventBus
        EventBus.getDefault().register(this);

//        DataSerializable dataSerializable = (DataSerializable)getArguments().getSerializable("data_to_friendfragment");
//        myFriendsList = dataSerializable.getListFriend();
        myFriendsList = new ArrayList<MyFriend>();
        recyclerview_friends = (RecyclerView)view.findViewById(R.id.recycleview_friends);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        /*// 设置布局管理器
        //垂直分布和横向分布  LinearLayoutManager.HORIZONTAL  VERTICAL
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);*/
        recyclerview_friends.setLayoutManager(layoutManager);
        adapter = new RVAdapterFriends((MainActivity)getActivity(),myFriendsList);
        recyclerview_friends.setAdapter(adapter);

        iv_new = (ImageView)view.findViewById(R.id.iv_new);
        button_newfriend = (RelativeLayout)view.findViewById(R.id.button_newfriend);
        button_groupchat = (RelativeLayout)view.findViewById(R.id.button_groupchat);
        button_newfriend.setOnClickListener(this);
        button_groupchat.setOnClickListener(this);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.button_newfriend){
            ((MainActivity)getActivity()).GotoNewFriendActivity();
        }
        if(id == R.id.button_groupchat){

        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(EventMessage eventMessage) {
        int which = eventMessage.getWhich();
        if(which == 10){ //更新friend列表
            myFriendsList = eventMessage.getMyFriendList();
            adapter.setNewData(myFriendsList);
            adapter.notifyDataSetChanged();
        }
        if(which == 11){ //设置更新标志为可见
            iv_new.setVisibility(View.VISIBLE);
        }
        if(which == 12){ //设置更新标志为隐藏
            iv_new.setVisibility(View.GONE);
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
