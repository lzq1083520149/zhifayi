package com.hkcect.z12.example;


import java.io.File;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.hkcect.z12.R;
import com.hkcect.z12.album.GestureListener;
import com.hkcect.z12.album.ImageDownloaderTask;
import com.ntk.util.Util;


	/*
	图片显示页面
	只有一个ImagView
	 */
public class GalleryActivity extends Activity {
	
    private final static String TAG = "GalleryActivity";

    String imagePath;
    String name;
    int position;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        
        Bundle bundle = this.getIntent().getExtras();
        name = bundle.getString("name");
        imagePath = bundle.getString("url");
        position = bundle.getInt("position");       
        
        final ImageView imageView = (ImageView) findViewById(R.id.imageView1);
        
		
		if(Util.isContainExactWord(imagePath, "http")) {
			File f = new File(Util.local_thumbnail_path + "/" + name);
			if (f.exists() == false) {
				new ImageDownloaderTask(imageView).execute(imagePath, name);
			} else {
				Bitmap myBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
				imageView.setImageBitmap(myBitmap);
			}
		} else {			
			final File folder = new File(Util.local_photo_path);
			final String[] list = folder.list();
			
			Bitmap myBitmap = BitmapFactory.decodeFile(imagePath);
			imageView.setImageBitmap(myBitmap);			
			imageView.setLongClickable(true);  
			imageView.setOnTouchListener(new GestureListener(this) {
		        @Override
		        public boolean left() {  	        	
		        	if(position < list.length-1) {	        	
		        		position = position + 1;
		        		Bitmap myBitmap = BitmapFactory.decodeFile(folder.toString() + "/" + list[position]);
		        		imageView.setImageBitmap(myBitmap);
		        	} else {
		        		Toast.makeText(GalleryActivity.this, "最后一张!!", Toast.LENGTH_SHORT).show();
		        	}
		            return super.left();  
		        }
		        @Override  
		        public boolean right() {  
		        	if(position > 0) {
		        		position = position - 1;
		        		Bitmap myBitmap = BitmapFactory.decodeFile(folder.toString() + "/" + list[position]);
		        		imageView.setImageBitmap(myBitmap);
		        	} else {
		        		Toast.makeText(GalleryActivity.this, "第一张!!", Toast.LENGTH_SHORT).show();
		        	}
		            return super.right();  
		        }  
			});
		}  
    } 
}


