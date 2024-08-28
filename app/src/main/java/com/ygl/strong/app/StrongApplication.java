package com.ygl.strong.app;


import android.app.Application;
import com.orhanobut.hawk.Hawk;
import com.ygl.strong.http.base.Http;
import org.litepal.LitePal;

public class StrongApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Http.initHttp();
        LitePal.initialize(this);
        Hawk.init(this).build();
    }
}
