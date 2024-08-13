package com.ygl.strong.http

import com.ygl.strong.http.base.Http

object Api {
    val BILIBILI = Http.http.createMainApi(BiliBiliInterface::class.java)
    val API_BILIBILI = Http.http.createApiApi(ApiBiliBiliInterface::class.java)
}