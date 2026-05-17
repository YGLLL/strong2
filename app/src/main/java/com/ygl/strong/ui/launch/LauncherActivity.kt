package com.ygl.strong.ui.launch

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.TextView
import com.ygl.strong.BuildConfig
import com.ygl.strong.R
import com.ygl.strong.base.BaseActivity
import com.ygl.strong.db.DB
import com.ygl.strong.db.bean.VideoDetail
import com.ygl.strong.http.Api
import com.ygl.strong.http.dto.DynamicRecommendDto
import com.ygl.strong.http.dto.RecommendDto
import com.ygl.strong.ui.main.MainActivity
import com.ygl.strong.utils.Utils
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
        findViewById<TextView>(R.id.tv_build_number).text = BuildConfig.BUILD_NUMBER
        Utils.loadVideoDataByNetwork{msg->
            if (TextUtils.isEmpty(msg)){
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }else{
                showToast(msg)
            }
        }
    }
}