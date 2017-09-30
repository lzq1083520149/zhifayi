package com.hkcect.z12.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.SharedPreferencesCompat;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/17.
 */

public class SharedPreferencesHelper {
    private static SharedPreferences sharedPreferences;
    /**
     * 保存在手机里面的名字
     */
    public static final String FILE_NAME = "shared_data";
    private static SharedPreferences.Editor editor;

    public SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**
     * 保存数据的方法，拿到数据保存数据的基本类型，然后根据类型调用不同的保存方法
     *
     * @param key
     * @param object
     */
    public static void put(String key, Object object) {

        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * 获取保存数据的方法，我们根据默认值的到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param key           键的值
     * @param defaultObject 默认值
     * @return
     */

    public static Object get(String key, Object defaultObject) {
            if (defaultObject instanceof String) {
                return sharedPreferences.getString(key, (String) defaultObject);
            } else if (defaultObject instanceof Integer) {
                return sharedPreferences.getInt(key, (Integer) defaultObject);
            } else if (defaultObject instanceof Boolean) {
                return sharedPreferences.getBoolean(key, (Boolean) defaultObject);
            } else if (defaultObject instanceof Float) {
                return sharedPreferences.getFloat(key, (Float) defaultObject);
            } else if (defaultObject instanceof Long) {
                return sharedPreferences.getLong(key, (Long) defaultObject);
            } else {
                return sharedPreferences.getString(key, null);
            }
    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param key
     */
    public static void remove(String key) {
        editor.remove(key);
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * 清除所有的数据
     */
    public static void clear() {
        editor.clear();
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * 查询某个key是否存在
     *
     * @param key
     * @return
     */
    public static boolean contains(String key) {
        return sharedPreferences.contains(key);
    }

    /**
     * 返回所有的键值对
     *
     * @return
     */
    public static Map<String, ?> getAll() {
        return sharedPreferences.getAll();
    }
    private static class SharedPreferencesCompat{

        /*对SharedPreference的使用做了建议的封装，对外公布出put，get，remove，clear等等方法；
        注意一点，里面所有的commit操作使用了SharedPreferencesCompat.apply进行了替代，
        目的是尽可能的使用apply代替commit，首先说下为什么，因为commit方法是同步的，
        并且我们很多时候commit操作都是UI线程中，毕竟是IO操作，尽可能异步；所以我们使用
   apply进行替代，apply异步的进行写入；但是apply相当于commit来说是new API呢，
       为了更好的兼容，我们做了适配；SharedPreferencesCompat也可以给大家创建兼容类提供一定的参考*/

        private static final Method sApplyMethod = findApplyMethod();
        private static final String TAG = "SharedPreferencesHelper";

        /**
         * 反射查找apply的方法
         * @return
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        private static Method findApplyMethod(){
            try
            {
                Class clz = SharedPreferences.Editor.class;
                return clz.getMethod("apply");
            } catch (NoSuchMethodException e) {

                Log.e(TAG,"ConfigManager."+e.getMessage());
            }

            return null;
        }

        /**
         * 如果找到则使用apply执行，否则使用commit
         * @param editor
         */
        public static void apply(SharedPreferences.Editor editor)
        {
            try
            {
                if (sApplyMethod != null)
                {
                    sApplyMethod.invoke(editor);
                    return;
                }
            } catch (IllegalArgumentException e)
            {
                Log.e(TAG,"apply."+e.getMessage());
            } catch (IllegalAccessException e)
            {
                Log.e(TAG,"apply."+e.getMessage());
            } catch (InvocationTargetException e)
            {
                Log.e(TAG,"apply."+e.getMessage());
            }
            editor.commit();
        }
    }
}