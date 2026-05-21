package com.ygl.strong.app;


import android.app.Application;
import com.orhanobut.hawk.Hawk;
import com.ygl.strong.http.base.Http;
import org.litepal.LitePal;

public class StrongApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Http.initHttp();//网络请求模块初始化
        LitePal.initialize(this);//数据库初始化
        Hawk.init(this).build();//键值对储存初始化
    }
}
