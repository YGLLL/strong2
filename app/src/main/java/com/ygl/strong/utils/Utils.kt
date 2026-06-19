package com.ygl.strong.utils

import android.view.View
import android.widget.FrameLayout
import com.ygl.strong.db.DB
import com.ygl.strong.db.bean.VideoDetail
import com.ygl.strong.http.Api
import com.ygl.strong.http.dto.DynamicRecommendDto
import com.ygl.strong.http.dto.RecommendDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    fun loadVideoDataByNetwork(next:(msg:String)->Unit) {
        Api.BILIBILI.dynamicRecommended().enqueue(object : Callback<DynamicRecommendDto> {
            override fun onResponse(call: Call<DynamicRecommendDto>, response: Response<DynamicRecommendDto>) {
                val body = response.body()

                body?.data?.archives?.forEach { bean->
                    val videoDetail = VideoDetail.fromDynamicRecommendDtoVideoDetail(bean)

                    if (DB.isNewVideo(videoDetail) && !isSlowVideo(videoDetail)){
                        DB.insert(videoDetail)
                    }
                }
                next.invoke("")
            }

            override fun onFailure(call: Call<DynamicRecommendDto>, t: Throwable) {
                next.invoke("load data Failure")
            }
        })
    }

    fun isSlowVideo(videoDetail: VideoDetail): Boolean {
//        return (videoDetail.title.indexOf("《")!=-1 && videoDetail.title.indexOf("》")!=-1) || (videoDetail.videos>1)
        return (videoDetail.duration > 20*60) || (videoDetail.videos>1)
    }
}