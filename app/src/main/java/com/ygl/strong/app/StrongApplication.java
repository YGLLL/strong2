package com.ygl.strong.app;


import android.app.Application;
import android.content.Context;
import com.ygl.strong.http.base.Http;
import org.litepal.LitePal;

public class StrongApplication extends Application {

    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        Http.initHttp();
        LitePal.initialize(this);
    }
}
