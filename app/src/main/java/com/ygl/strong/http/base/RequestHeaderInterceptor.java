package com.ygl.strong.http.base;

import android.text.TextUtils;

import com.ygl.strong.utils.LogUtil;

import java.io.IOException;
import java.nio.charset.Charset;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

public class RequestHeaderInterceptor implements Interceptor {

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        RequestBody requestBody = request.body();
        Request.Builder builder = request.newBuilder();


        if (!TextUtils.isEmpty(Http.sessionId)) {
            builder.addHeader("sessionId", Http.sessionId);
//            builder.addHeader("token", Http.sessionId);
        }
//        if (!CheckUtil.isNull(Http.user_la)) {
//            builder.addHeader("la", Http.user_la);
//        }

        //封装一个公共参数，la，语言1中文2英文
//        if (requestBody instanceof FormBody) {
//            FormBody formBody = (FormBody) requestBody;
//            FormBody.Builder bodyBuilder = new FormBody.Builder();
//            //把原来的参数添加到新的构造器，（因为没找到直接添加，所以就new新的）
//            for (int i = 0; i < formBody.size(); i++) {
//                bodyBuilder.addEncoded(formBody.encodedName(i), formBody.encodedValue(i));
//            }
//            if (!CheckUtil.isNull(Http.user_la)) {
//                bodyBuilder.addEncoded("la", Http.user_la);
//            }
//            if (!CheckUtil.isNull(Http.sessionId)) {
//                bodyBuilder.addEncoded("sessionId", Http.sessionId);
//
//            }
//
//            formBody = bodyBuilder.build();
//
//            request = request.newBuilder().post(formBody).build();
//
//        }

        LogUtil.e("Http request sessionId",Http.sessionId);
        //打印请求信息
        String requestMessage = request.method() + ' ' + request.url();
        if (requestBody != null) {
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            requestMessage += "?" + buffer.readString(Charset.forName("UTF-8"));
        }
        LogUtil.e("Http",requestMessage);
        return chain.proceed(builder.build());
    }
}