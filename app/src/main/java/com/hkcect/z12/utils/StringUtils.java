package com.hkcect.z12.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static final String night_mode = "night_mode";//夜间模式
    //public static final String devices_name = "NVT_CARD";//设备wifi开头名
    public static final String devices_name = "SportsDV";//设备wifi开头名
   // public static final String devices_name = "portsDV";//设备wifi开头名
    //public static final String devices_name = "CarDV";//设备wifi开头名
    public static final String setting = "setting";//
    public static final String space = "space";//


    public static String root_path = Environment.getExternalStorageDirectory().toString();

    public static String local_media_path = root_path + "/Z12/Media/";
    public static String local_media_down_path = "Z12/Media";


    /*
    格式化时间为yyyy-MM-dd HH:mm:ss
     */
    public static String fromatDate(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(time);
    }


    public  static String fromatDate2(long time){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(time);
    }


    /*
    是否为密码
    字母与数字的组合，长度在8到20
     */
    public static boolean isPWD(String str) {
        String regEx = "^[a-zA-Z]\\w{7,19}$";
        Pattern pat = Pattern.compile(regEx);
        Matcher mat = pat.matcher(str);
        return mat.find();
    }

    /**
     * 密码长度8-20位（包含8,20）的数字或字母以及 "_" "-"
     * @param pwd
     * @return
     */
    public static boolean isPassword(String pwd){
        String regEx = "^[a-z0-9_-]{8,20}$";
        Pattern pat = Pattern.compile(regEx);
        Matcher mat = pat.matcher(pwd);
        return mat.find();
    }
    /*
    是否为数字
     */
    public static boolean isNumber(String str) {
        String regEx = "[0-9]*";
        Pattern pat = Pattern.compile(regEx);
        Matcher mat = pat.matcher(str);
        return mat.find();
    }

    /*
    StringUtils.root_path路径所在磁盘是否有多余空间
     */
    public static boolean hasSpace(Context context){
        Long free = 0L;
        File file  = new File(StringUtils.root_path);
        if (file.exists()){
            free  = file.getFreeSpace();
        }

        SharedPreferences sp = context.getSharedPreferences(StringUtils.setting,Context.MODE_PRIVATE);
        int space = sp.getInt(StringUtils.space,300);

        return free/1024/1024 > space;
    }


    /*
    字节长度转化为（*.**）的GB或MB、KB格式
     */
    public static String byte2FitMemorySize(long byteNum) {
        if (byteNum < 0) {
            return "shouldn't be less than zero!";
        } else if (byteNum < 1024) {
            return String.format("%.2fB", (double) byteNum + 0.0005);
        } else if (byteNum < 1048576) {
            return String.format("%.2fKB", (double) byteNum / 1024 + 0.0005);
        } else if (byteNum < 1073741824) {
            return String.format("%.2fMB", (double) byteNum / 1048576 + 0.0005);
        } else {
            return String.format("%.2fGB", (double) byteNum / 1073741824 + 0.0005);
        }
    }



}
