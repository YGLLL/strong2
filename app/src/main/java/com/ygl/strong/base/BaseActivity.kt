package com.ygl.strong.base

import android.R
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.ygl.strong.widget.LoadingDialog
import java.lang.reflect.Method

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
     * 把状态栏设成透明
     * 适配 API 35：Android 15 强制 edge-to-edge，通过代码 opt-out
     */
    protected open fun setStatusBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Android 15 (API 35) 强制 edge-to-edge，通过反射调用 Window.setDecorFitsSystemWindows(true) opt-out
            if (Build.VERSION.SDK_INT >= 35) {
                try {
                    val setDecorFitsSystemWindows: Method = window.javaClass.getMethod(
                        "setDecorFitsSystemWindows", Boolean::class.javaPrimitiveType
                    )
                    setDecorFitsSystemWindows.invoke(window, true)
                } catch (e: Exception) {
                    showToast("opt-out edge-to-edge error")
                }
            }

            val decorView = window.decorView
            decorView.setOnApplyWindowInsetsListener { v: View, insets: WindowInsets? ->
                val defaultInsets = v.onApplyWindowInsets(insets)
                defaultInsets.replaceSystemWindowInsets(
                    defaultInsets.systemWindowInsetLeft,
                    0,
                    defaultInsets.systemWindowInsetRight,
                    defaultInsets.systemWindowInsetBottom
                )
            }
            ViewCompat.requestApplyInsets(decorView)
            window.statusBarColor = ContextCompat.getColor(this, R.color.transparent)
        }
    }

    //设置沉浸式虚拟键，在MIUI系统中，虚拟键背景透明。原生系统中，虚拟键背景半透明。
    protected open fun setControlBarTransparent(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
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