package com.ygl.strong.utils.videocache.headers;

import android.text.TextUtils;

import com.ygl.strong.utils.Constant;
import com.ygl.strong.utils.Utils;
import com.ygl.strong.utils.videocache.headers.HeaderInjector;

import java.util.HashMap;
import java.util.Map;

/**
 * Empty {@link com.ygl.strong.utils.videocache.headers.HeaderInjector} implementation.
 *
 * @author Lucas Nelaupe (https://github.com/lucas34).
 */
public class EmptyHeadersInjector implements HeaderInjector {

    @Override
    public Map<String, String> addHeaders(String url) {
        HashMap headers =new HashMap<String, String>();
        headers.put("Host", Utils.INSTANCE.playUrl2Host(url));
        headers.put("Referer", Constant.INSTANCE.PLAY_REFERER);
        headers.put("User-Agent",Constant.INSTANCE.PLAY_USER_AGENT);
        return headers;
    }
}
