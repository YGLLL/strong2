package com.ygl.strong.utils.videocache.strong;
import okhttp3.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ygl.strong.utils.Constant;
import com.ygl.strong.utils.LogUtil;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BilibiliPlayUrlFetcher {

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();
    private static final Gson gson = new Gson();

    // 缓存 WBI mix key（有效期约几小时，可加过期逻辑）
    private static String cachedMixKey = null;
    private static long mixKeyExpiry = 0;

    /**
     * 获取视频播放地址（主入口）
     */
    public static String fetchPlayUrl(String bvid, String cid) {
        try {
            String mixKey = getWbiMixKey();
            if (mixKey == null) {
                LogUtil.e("PreloadUrlsTask", "Failed to get WBI mix key");
                return "";
            }

            // 构造参数（必须按字典序排序）
            Map<String, String> params = new TreeMap<>();
            params.put("bvid", bvid);
            params.put("cid", cid);
            params.put("qn", "64");// 清晰度 64=720P
            params.put("otype", "json");
            params.put("fnver", "0");
            params.put("fnval", "1");//1,MP4;;16,DASH (H.264),音视频分离,返回 dash.video[] + dash.audio[];;64,DASH (H.265),同上，HEVC编码;;4048,DASH,全格式,自动返回所有可用编码（AVC/HEVC/AV1）
            params.put("fourk", "1");// 允许4K

            long wts = System.currentTimeMillis() / 1000;
            params.put("wts", String.valueOf(wts));

            // 生成 w_rid
            String queryWithoutWrid = buildQueryString(params);
            String toSign = removeInvalidChars(queryWithoutWrid) + mixKey;
            String wRid = md5(toSign);

            // 拼接最终 URL
            String url = "https://api.bilibili.com/x/player/wbi/playurl?" + queryWithoutWrid + "&w_rid=" + wRid;

            LogUtil.e("PreloadUrlsTask", "PlayUrl Request URL: " + url);

            Request request = new Request.Builder()
                    .url(url)
                    .header("Referer", Constant.PLAY_REFERER)
                    .header("User-Agent", Constant.PLAY_USER_AGENT)
                    .header("Cookie", Constant.INSTANCE.getTEST_COOKIE())
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    LogUtil.e("PreloadUrlsTask", "Response code: " + response.code());
                    return "";
                }

                String responseData = response.body().string();
                LogUtil.e("PreloadUrlsTask", "PlayUrl Response: " + responseData);

                JsonObject root = new JsonParser().parse(responseData).getAsJsonObject();
                int code = root.get("code").getAsInt();

                if (code == 0) {
                    JsonObject data = root.getAsJsonObject("data");
                    if (data.has("durl")) {
                        JsonObject durl = data.getAsJsonArray("durl").get(0).getAsJsonObject();
                        return durl.get("url").getAsString();
                    } else {
                        LogUtil.e("PreloadUrlsTask", "No 'durl' in response");
                        return "";
                    }
                } else {
                    String message = root.has("message") ? root.get("message").getAsString() : "Unknown error";
                    LogUtil.e("PreloadUrlsTask", "API Error: " + code + " - " + message);
                    return "";
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取 WBI mix key (img_key + sub_key)
     */
    private static synchronized String getWbiMixKey() throws IOException {
        long now = System.currentTimeMillis();
        if (cachedMixKey != null && now < mixKeyExpiry) {
            return cachedMixKey;
        }

        Request request = new Request.Builder()
                .url("https://api.bilibili.com/x/web-interface/nav")
                .header("Referer", Constant.PLAY_REFERER)
                .header("User-Agent", Constant.PLAY_USER_AGENT)
                .header("Cookie", Constant.INSTANCE.getTEST_COOKIE())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) return null;

            String body = response.body().string();
            JsonObject root = new JsonParser().parse(body).getAsJsonObject();
            if (root.get("code").getAsInt() != 0) return null;

            JsonObject data = root.getAsJsonObject("data");
            if (!data.has("wbi_img")) return null;

            JsonObject wbiImg = data.getAsJsonObject("wbi_img");
            String imgKey = extractKeyFromUrl(wbiImg.get("img_url").getAsString());
            String subKey = extractKeyFromUrl(wbiImg.get("sub_url").getAsString());

            cachedMixKey = imgKey + subKey;
            mixKeyExpiry = now + 3 * 60 * 60 * 1000; // 缓存3小时
            return cachedMixKey;
        }
    }

    /**
     * 从 URL 中提取 key（去掉路径和后缀）
     */
    private static String extractKeyFromUrl(String url) {
        // https://i0.hdslb.com/bfs/wbi/7cd0e9d8a3f8e9c7b6a5d4c3b2a1f0e9d8a7c6b5.jpg
        int lastSlash = url.lastIndexOf('/');
        int dotIndex = url.indexOf('.', lastSlash);
        if (lastSlash == -1 || dotIndex == -1) return "";
        return url.substring(lastSlash + 1, dotIndex);
    }

    /**
     * 构建查询字符串 a=1&b=2...
     */
    private static String buildQueryString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) sb.append('&');
            sb.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return sb.toString();
    }

    /**
     * 移除非法字符（B站要求）
     */
    private static String removeInvalidChars(String input) {
        // 替换 !"'()* 为空（B站前端逻辑）
        return input.replace("!", "")
                .replace("\"", "")
                .replace("'", "")
                .replace("(", "")
                .replace(")", "")
                .replace("*", "");
    }

    /**
     * MD5 工具方法
     */
    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}