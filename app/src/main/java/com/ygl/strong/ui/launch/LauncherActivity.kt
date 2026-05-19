package com.ygl.strong.ui.launch

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.TextView
import com.ygl.strong.R
import com.ygl.strong.base.BaseActivity
import com.ygl.strong.ui.main.MainActivity
import com.ygl.strong.utils.Constant
import com.ygl.strong.utils.Utils

class LauncherActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTransparentSystemUI()
        setContentView(R.layout.activity_launcher)
        findViewById<TextView>(R.id.tv_build_number).apply {
            if (Constant.IS_DEBUG) {
                text = Constant.BUILD_NUMBER
                visibility = android.view.View.VISIBLE
            } else {
                visibility = android.view.View.GONE
            }
        }
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