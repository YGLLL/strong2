package com.ygl.strong.ui.launch

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.TextView
import com.ygl.strong.BuildConfig
import com.ygl.strong.R
import com.ygl.strong.base.BaseActivity
import com.ygl.strong.ui.main.MainActivity
import com.ygl.strong.utils.Utils

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