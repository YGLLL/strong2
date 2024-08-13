package com.ygl.strong.utils

import android.view.View
import android.widget.FrameLayout
import com.ygl.strong.http.dto.RecommendDto

object Utils {
    /**
     * 将View从父控件中移除
     */
    fun removeViewFormParent(v: View?) {
        if (v == null) return
        val parent = v.parent
        if (parent is FrameLayout) {
            parent.removeView(v)
        }
    }

    fun playUrl2Host(url: String?): String {
        var host = ""
        url?.let {
//            LogUtil.e("my url:",it)
            val s = it.indexOf("//")
            var e = it.indexOf(".cn")
            var tailLength = 3
            if (e == -1){
                e = it.indexOf(".com")
                tailLength = 4
            }
            if (s>0 && e>0 && s<e){
                host = it.substring(s+2,e+tailLength)
            }
        }
//        LogUtil.e("my host:",host)
        return host
    }
}