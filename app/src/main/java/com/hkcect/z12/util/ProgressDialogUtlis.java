package com.hkcect.z12.util;

import android.app.ProgressDialog;
import android.content.Context;

import com.hkcect.z12.R;

/**
 * Created by Administrator on 2017/8/16.
 */

public class ProgressDialogUtlis {

    private static ProgressDialog progressDialog;

    public static void showProgressDialog(Context context){
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(context.getString(R.string.dialog_title));
        progressDialog.setMessage(context.getString(R.string.dialog_wait_message));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }
    public static void dismissProgressDialog(){
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }

}
