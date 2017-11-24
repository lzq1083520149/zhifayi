package com.hkcect.z12.util;

import android.os.Parcel;
import android.os.Parcelable;



public class FileDownload implements Parcelable {
    private String Url;
    private String Name;
    private int selectPosstion;

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getSelectPosstion() {
        return selectPosstion;
    }

    public void setSelectPosstion(int selectPosstion) {
        this.selectPosstion = selectPosstion;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Url);
        dest.writeString(Name);
        dest.writeInt(selectPosstion);
    }


    // 1.必须实现Parcelable.Creator接口,否则在获取Person数据的时候，会报错，如下：
    // android.os.BadParcelableException:
    // Parcelable protocol requires a Parcelable.Creator object called  CREATOR on class com.um.demo.Person
    // 2.这个接口实现了从Percel容器读取Person数据，并返回Person对象给逻辑层使用
    // 3.实现Parcelable.Creator接口对象名必须为CREATOR，不如同样会报错上面所提到的错；
    // 4.在读取Parcel容器里的数据事，必须按成员变量声明的顺序读取数据，不然会出现获取数据出错
    // 5.反序列化对象
    public static final Creator CREATOR = new Creator(){

        @Override
        public FileDownload createFromParcel(Parcel source) {
            // TODO Auto-generated method stub
            // 必须按成员变量声明的顺序读取数据，不然会出现获取数据出错
            FileDownload p = new FileDownload();
            p.setUrl(source.readString());
            p.setName(source.readString());
            p.setSelectPosstion(source.readInt());
            return p;
        }

        @Override
        public FileDownload[] newArray(int size) {
            return new FileDownload[size];
        }
    };
}
