package com.ygl.strong.http

import com.ygl.strong.http.dto.RecommendDto
import retrofit2.Call
import retrofit2.http.GET

interface BiliBiliInterface {
    @GET("index/ding.json")
    fun getRecommend(): Call<RecommendDto>
}