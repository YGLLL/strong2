package com.ygl.strong.utils.videocache.strong;

import android.content.ContentValues;
import android.text.TextUtils;
import com.ygl.strong.db.bean.VideoDetail;
import com.ygl.strong.utils.LogUtil;
import org.litepal.LitePal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
/**
 * 批量获取播放链接
 */
public class PreloadUrlsTask implements Runnable {

    public List<VideoDetail> mNextList;

    public Function1<List<VideoDetail>, Unit> mNext;

    /**
     * 储存原始链接
     */
    public final static HashMap<String,String> RAW_URLS = new HashMap<>();

    @Override
    public void run() {
        List<VideoDetail> successList = new ArrayList<>();
        for (VideoDetail videoDetail : mNextList){
            String bvid = videoDetail.getBvid();
            String cid = videoDetail.getCid();
//            String playUrl = "";
//            try {
//                playUrl = BilibiliVideoFetcher.getBilibiliVideoPlaybackLink(bvid, cid);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
            String playUrl = BilibiliPlayUrlFetcher.fetchPlayUrl(bvid,cid);
            if (!TextUtils.isEmpty(playUrl)) {
                // 播放 playUrl
                LogUtil.e("PreloadUrlsTask","获得播放链接："+playUrl);
                RAW_URLS.put(bvid,playUrl);
                // 成功获取链接，重置失败计数
                if (videoDetail.getVideoPlayUrlFailCount() != 0) {
                    ContentValues values = new ContentValues();
                    values.put("videoPlayUrlFailCount", 0);
                    LitePal.update(VideoDetail.class, values, videoDetail.getId());
                }
                successList.add(videoDetail);
            } else {
                // 失败处理：失败计数+1 并更新数据库
                LogUtil.e("PreloadUrlsTask","获取播放链接失败: bvid=" + bvid);
                int newFailCount = videoDetail.getVideoPlayUrlFailCount() + 1;
                ContentValues values = new ContentValues();
                values.put("videoPlayUrlFailCount", newFailCount);
                LitePal.update(VideoDetail.class, values, videoDetail.getId());
            }
        }
        mNext.invoke(successList);
    }
}
