package com.ygl.strong.utils.videocache.strong;

import com.google.gson.Gson;
import com.ygl.strong.db.bean.VideoDetail;
import com.ygl.strong.http.dto.PlayUrlDto;
import com.ygl.strong.utils.Constant;
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
/**
 * 批量获取播放链接
 */
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
            String playUrl = BilibiliPlayUrlFetcher.fetchPlayUrl(bvid, cid);
            if (!playUrl.isEmpty()) {
                // 播放 playUrl
                LogUtil.e("PreloadUrlsTask","获得播放链接："+playUrl);
                RAW_URLS.put(bvid,playUrl);
            } else {
                // 失败处理
                LogUtil.e("PreloadUrlsTask","获得播放链接失败");
                RAW_URLS.put(bvid,"");
            }
        }
        mNext.invoke();
    }
}
