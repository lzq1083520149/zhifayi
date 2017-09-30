package com.hkcect.z12.utils;

import android.util.Log;
 
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
 
/**
* Created by TrillGates on 2017/2/28.
*/
public class InterfaceProxy implements InvocationHandler {
 
    private static final String TAG = "InterfaceProxy";
 
    /**
     * 这里的话直接去获取对象,把这个接口的字节码对象数组扔进来就可以了
     */
    public static Object getInstance(Class<?>[] interfaces) {
        return Proxy.newProxyInstance(InterfaceProxy.class.getClassLoader(), interfaces, new InterfaceProxy());
    }
     
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
 
        Log.d(TAG, "回调了... " + method.getName());
 
        //判断是什么方法被调用了嘛
        String methodName = method.getName();
 
        if (methodName.equals("onSuccess")) {
 
            //这里的话那么就是成功咯!!!
            Log.d(TAG, "删除wifi配置成功了...");
 
        } else if (methodName.equals("onFailure")) {
            Log.d(TAG, "删除wifi配置失败...");
 
            //找出失败的理由来呀!
            int arg = (int) args[0];
            Log.d(TAG, "失败的原因是..." + arg);
        }
        return null;
    }


}