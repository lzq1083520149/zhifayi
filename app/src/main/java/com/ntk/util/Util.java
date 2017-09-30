package com.ntk.util;



import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Environment;

import com.ntk.nvtkit.ae;
import com.ntk.nvtkit.b;

public class Util extends ae {

	public static String device_ip = "192.168.1.254";
	public static String movie_url = "rtsp://192.168.1.254/xxx.mov";
	//public static String movie_url = "udp://@:8888";
	public static String photo_url = "http://192.168.1.254:8192";
	
	
	public static String root_path = Environment.getExternalStorageDirectory().toString();
	public static String local_thumbnail_path = root_path + "/ZD/THUMBNAIL";
	public static String local_photo_path = root_path + "/ZD/PHOTO";
	public static String local_movie_path = root_path + "/ZD/MOVIE";
	public static String log_path = root_path + "/ZD/LOG";
	
    public static final int BLOCKING_LEVEL_NONE = 0;  
    public static final int BLOCKING_LEVEL_LOW = 1;
    public static final int BLOCKING_LEVEL_MID = 2;
    public static final int BLOCKING_LEVEL_HIGH = 3;
    
    public static final int ASPECTRARIO_BESTFIT = 0;  
    public static final int ASPECTRARIO_FULLSCREEN = 1;  
	
	public static String getDeciceIP() {
		return device_ip;
	}
	
	public static void setDeciceIP(String ip) {
		device_ip = ip;
	}
	
	public static boolean checkLocalFolder(){
		
		File nvt_dir = new File(root_path + "/ZD");
		if (!nvt_dir.exists())
			nvt_dir.mkdir();
		
		File tn_dir = new File(root_path + "/ZD/THUMBNAIL");
		if (!tn_dir.exists())
			tn_dir.mkdir();
		
		File ph_dir = new File(root_path + "/ZD/PHOTO");
		if (!ph_dir.exists())
			ph_dir.mkdir();
		
		File mv_dir = new File(root_path + "/ZD/MOVIE");
		if (!mv_dir.exists())
			mv_dir.mkdir();
		
		File log_dir = new File(root_path + "/ZD/LOG");
		if (!log_dir.exists())
			log_dir.mkdir();
		
		return (tn_dir.exists() && ph_dir.exists() && mv_dir.exists() && log_dir.exists()) ; 
	}
	
    public static boolean isContainExactWord(String fullString, String partWord){
        String pattern = partWord;
        Pattern p=Pattern.compile(pattern);
        Matcher m=p.matcher(fullString);
        return m.find();
    }

    public static String setMovie_time(String h,String m,String s){
		String var3 = a("http://" + Util.getDeciceIP() + "/?custom=1&cmd=3006&str=" + h + ":" + m + ":" + s);
        if(var3 == null) {
			return null;
		} else {
			ParseResult var4 = c(var3);
			return var4 == null?null:(!var4.getStatus().equals("0")?null:"succeed");
		}

	}

}