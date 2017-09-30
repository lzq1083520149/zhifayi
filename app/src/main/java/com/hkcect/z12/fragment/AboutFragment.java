package com.hkcect.z12.fragment;


import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.hkcect.z12.R;
import com.hkcect.z12.ui.AboutActivity;
import com.hkcect.z12.ui.SpaceManagementActivity;
import com.hkcect.z12.utils.StringUtils;


public class AboutFragment extends Fragment implements View.OnClickListener {


    private static AboutFragment inStance = null;

    public AboutFragment() {
    }

    public synchronized static AboutFragment newInstance() {
        if (inStance == null) {
            inStance = new AboutFragment();
        }
        return inStance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_about, container, false);
        view.findViewById(R.id.tv_space).setOnClickListener(this);
        view.findViewById(R.id.tv_about).setOnClickListener(this);
        //view.findViewById(R.id.tv_language).setOnClickListener(this);
//        CheckBox cb_night_mode = (CheckBox) view.findViewById(R.id.cb_night_mode);
//        cb_night_mode.setChecked(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(StringUtils.night_mode,false));
//        cb_night_mode.setOnCheckedChangeListener(this);
        return view ;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_space://存储管理
                startActivity(new Intent(getContext(), SpaceManagementActivity.class));
                break;
            case R.id.tv_about://关于页面
                startActivity(new Intent(getContext(), AboutActivity.class));
                break;
//            case R.id.tv_language://语言切换
//                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                builder.setView(R.layout.dialog_language_select);
//                break;
        }
    }

//    @Override
//    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
//        if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
//                == Configuration.UI_MODE_NIGHT_YES) {
//            sp.edit().putBoolean(StringUtils.night_mode, false).apply();
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//        } else {
//            sp.edit().putBoolean(StringUtils.night_mode, true).apply();
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//        }
//        getActivity().recreate();
//    }
}
