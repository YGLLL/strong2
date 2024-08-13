package com.ygl.strong.utils.videocache.strong;

import com.google.gson.Gson;
import com.ygl.strong.db.bean.VideoDetail;
import com.ygl.strong.http.dto.PlayUrlDto;
import com.ygl.strong.utils.LogUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PreloadUrlsTask implements Runnable {

    public List<VideoDetail> mNextList;

    public Function0<Unit> mNext;

    /**
     * 储存原始链接
     */
    public final static HashMap<String,String> RAW_URLS = new HashMap<>();

    @Override
    public void run() {
        for (VideoDetail videoDetail : mNextList){
            String bvid = videoDetail.getBvid();
            String cid = videoDetail.getCid();
            String url = "https://api.bilibili.com/x/player/playurl?qn=64&otype=json&bvid="+bvid+"&cid="+cid+"&type=";
            LogUtil.e("Http","PreloadUrlsTask url:"+url);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                // 获取响应码
                int responseCode = response.code();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // 获取响应数据
                    String responseData = response.body().string();
                    LogUtil.e("Http","PreloadUrlsTask:"+responseData);
                    // 处理响应数据
                    Gson g = new Gson();
                    PlayUrlDto dto = g.fromJson(responseData,PlayUrlDto.class);
                    if (dto.getCode().equals("0")){
                        RAW_URLS.put(bvid,dto.getData().getDurl().get(0).getUrl());
                    }else {
                        RAW_URLS.put(bvid,"");
                    }
                } else {
                    // 处理错误情况
                    RAW_URLS.put(bvid,"");
                }
                // 关闭响应体
                response.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        mNext.invoke();
    }
}
