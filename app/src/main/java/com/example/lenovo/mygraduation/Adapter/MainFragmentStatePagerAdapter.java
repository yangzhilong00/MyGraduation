package com.example.lenovo.mygraduation.Adapter;

/**
 * Created by lenovo on 2019/2/25.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.example.lenovo.mygraduation.MainActivity;

import java.util.List;

/**
 * 定义自己的ViewPager适配器。
 * 也可以使用FragmentPagerAdapter。关于这两者之间的区别，可以自己去搜一下。
 */
public class MainFragmentStatePagerAdapter extends FragmentStatePagerAdapter
{
    private MainActivity context;
    private List<Fragment> fragmentlist;
    public MainFragmentStatePagerAdapter(MainActivity context2, FragmentManager fm, List<Fragment> fragmentlist2)
    {
        super(fm);
        context = context2;
        fragmentlist = fragmentlist2;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentlist.get(position);
    }

    @Override
    public int getCount() {
        return fragmentlist.size();
    }

    /**
     * 每次更新完成ViewPager的内容后，调用该接口，此处复写主要是为了让导航按钮上层的覆盖层能够动态的移动
     */
    @Override
    public void finishUpdate(ViewGroup container)
    {
        super.finishUpdate(container);//这句话要放在最前面，否则会报错
        //获取当前的视图是位于ViewGroup的第几个位置，用来更新对应的覆盖层所在的位置
        context.ChangeTab();
    }
}