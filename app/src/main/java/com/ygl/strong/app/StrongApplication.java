package com.ygl.strong.app;


import android.app.Application;
import android.content.Context;
import com.ygl.strong.http.base.Http;
import org.litepal.LitePal;

public class StrongApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Http.initHttp();
        LitePal.initialize(this);
    }
}
