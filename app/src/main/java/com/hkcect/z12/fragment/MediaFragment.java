package com.hkcect.z12.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hkcect.z12.R;
import com.hkcect.z12.adapter.MyViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;



public class MediaFragment extends Fragment {

    public MediaFragment() {
    }

    public ViewPager viewPagerMedia;

    // ViewPager的数据适配器
    private MyViewPagerAdapter mAdapter;
    // TabLayout中的tab标题
    private String[] mTitles;

    // 填充到ViewPager中的Fragment
    private List<Fragment> mFragments;

    private TabLayout mTabLayout;

    private static MediaFragment inStance = null;

    public static synchronized MediaFragment newInstance() {
        if (inStance == null){
            inStance = new MediaFragment();
        }
        return inStance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inStance = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media, container, false);
        viewPagerMedia = (ViewPager) view.findViewById(R.id.viewpager_meida);
        mTabLayout = (TabLayout) view.findViewById(R.id.tab_media);
        initData();

        return view;
    }

    private void initData() {
        //初始化填充到ViewPager中的Fragment集合
        mFragments = new ArrayList<>();
        mTitles = getResources().getStringArray(R.array.tab_media);

        Bundle mBundle = new Bundle();
        mBundle.putInt("column-count", 2);
        //添加图片页面
        PhotoFragment mFragment = PhotoFragment.newInstance(3);
        mFragment.setArguments(mBundle);
        mFragments.add(0, mFragment);

        //添加视频页面
        Bundle b = new Bundle();
        b.putInt("column-count",1);
        VideoFragment videoFragment = VideoFragment.newInstance(3);
        videoFragment.setArguments(b);
        mFragments.add(1, videoFragment);

        mAdapter = new MyViewPagerAdapter(getActivity().getSupportFragmentManager(), mTitles, mFragments);
        viewPagerMedia.setAdapter(mAdapter);
        // 设置ViewPager最大缓存的页面个数
        viewPagerMedia.setOffscreenPageLimit(2);
        // 将TabLayout和ViewPager进行关联，让两者联动起来
        mTabLayout.setupWithViewPager(viewPagerMedia);
        // 设置Tablayout的Tab显示ViewPager的适配器中的getPageTitle函数获取到的标题
        mTabLayout.setTabsFromPagerAdapter(mAdapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
