package com.ygl.strong.ui.launch

import android.content.Intent
import android.os.Bundle
import com.ygl.strong.R
import com.ygl.strong.base.BaseActivity
import com.ygl.strong.db.DB
import com.ygl.strong.db.bean.VideoDetail
import com.ygl.strong.http.Api
import com.ygl.strong.http.dto.DynamicRecommendDto
import com.ygl.strong.http.dto.RecommendDto
import com.ygl.strong.ui.main.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by ygl-gpd
 * Created date:2023/4/18 23:45
 **/
class LauncherActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        setStatusBarTransparent()
        setControlBarTransparent()
        loadData(){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun loadData(next:()->Unit) {
        Api.BILIBILI.dynamicRecommended().enqueue(object : Callback<DynamicRecommendDto> {
            override fun onResponse(call: Call<DynamicRecommendDto>, response: Response<DynamicRecommendDto>) {
                val body = response.body()

                body?.data?.archives?.forEach { bean->
                    val videoDetail = VideoDetail()
                    videoDetail.aid = bean.aid
                    videoDetail.bvid = bean.bvid
                    videoDetail.cid = bean.cid
                    videoDetail.title = bean.title
                    videoDetail.reply = bean.stat?.reply?:""
                    videoDetail.tname = bean.tname

                    if (DB.isNewVideo(videoDetail)){
                        videoDetail.save()
                    }
                }
                next.invoke()
            }

            override fun onFailure(call: Call<DynamicRecommendDto>, t: Throwable) {
                showToast("load data Failure")
            }

        })
    }
}