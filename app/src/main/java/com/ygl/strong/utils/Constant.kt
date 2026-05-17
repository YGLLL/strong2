package com.ygl.strong.utils

import com.ygl.strong.BuildConfig

object Constant {
    const val PLAY_REFERER = "https://www.bilibili.com"
    const val PLAY_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.104 Safari/537.36"

    /**
     * B站 Cookie，从 BuildConfig 读取（编译时由 constants.properties 注入）
     * constants.properties 不在 git 中，避免泄露
     */
    val TEST_COOKIE: String
        get() = BuildConfig.BILIBILI_COOKIE
}