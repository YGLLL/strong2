package com.ygl.strong.http

import com.ygl.strong.http.base.Http

object Api {
    val BILIBILI = Http.http.createApi(BiliBiliInterface::class.java)
}