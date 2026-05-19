package com.ygl.strong.base

import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.ygl.strong.widget.LoadingDialog

open class BaseActivity : AppCompatActivity() {
    protected var mLoading: LoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mLoading = LoadingDialog(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mLoading = null
    }

    /**
     * 沉浸式 UI：状态栏和导航栏透明，内容延伸到系统栏下方
     * 使用 AndroidX Activity 1.8.0+ 的 enableEdgeToEdge()，适配 API 15~35
     */
    protected open fun setTransparentSystemUI() {
        enableEdgeToEdge()
        // MIUI/HyperOS 默认会给导航栏/手势条白色背景
        // 强制设为深色，适配视频播放器深色底部
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        }
    }

    protected open fun showToast(str:String){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show()
    }

    protected fun showLoading() {
        runOnUiThread { showLoading(true) }
    }
    protected fun showLoading(cancelable:Boolean) {
        if (mLoading != null && mLoading?.isShowing != true) {
            runOnUiThread { mLoading?.show(cancelable) }
        }
    }

    protected fun showLoading(cancelable:Boolean,text:String) {
        if (mLoading != null && mLoading?.isShowing != true) {
            runOnUiThread { mLoading?.show(cancelable,text) }
        }
    }

    protected fun dismissLoading() {
        if (mLoading != null && mLoading?.isShowing != false) {
            runOnUiThread { mLoading?.dismiss() }
        }
    }
}