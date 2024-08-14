package com.ygl.strong.http.base;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Http {
    public static Http http;
    public static void initHttp() {
        http = new Http();
    }

    public final static long DEFAULT_TIMEOUT = 40L;
    public static String sessionId;
    public static String baseUrl = "https://api.bilibili.com/";

    private Retrofit mRetrofit;

    public Http() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new RequestLogInterceptor())
                .addInterceptor(new RequestHeaderInterceptor())
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        mRetrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public <T> T createApi(Class<T> tClass) {
        return mRetrofit.create(tClass);
    }

}
