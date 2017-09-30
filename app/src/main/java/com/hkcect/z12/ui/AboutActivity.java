package com.hkcect.z12.ui;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.hkcect.z12.R;

public class AboutActivity extends BaseActivity {

    private TextView tv_version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.about);
        setSupportActionBar(toolbar);

        tv_version = (TextView) findViewById(R.id.tv_version);
        tv_version.setText(getVersion());

    }

    /**
     * 获取版本
     * @return 版本(版本号)
     */
    public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            return info.versionName + "(" + info.versionCode + ")";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}
