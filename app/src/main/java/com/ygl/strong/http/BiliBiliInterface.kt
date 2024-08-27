package com.ygl.strong.http

import com.ygl.strong.http.dto.DynamicRecommendDto
import com.ygl.strong.http.dto.PagelistDto
import com.ygl.strong.http.dto.PlayUrlDto
import com.ygl.strong.http.dto.ReplyDto
import com.ygl.strong.http.dto.SearchDto
import com.ygl.strong.utils.Constant
import retrofit2.Call
import retrofit2.http.*

interface BiliBiliInterface {
    @GET("x/player/playurl")
    fun getPlayUrl(@Query("cid") cid:String,
                   @Query("bvid") bvid:String,
                   @Query("qn") qn:String = "64",
                   @Query("type") type:String = "",
                   @Query("otype") otype:String = "json"): Call<PlayUrlDto>

    @GET("x/v2/reply")
    fun getReplys(@Header("Cookie") cookie:String = Constant.TEST_COOKIE,
                  @Query("jsonp") jsonp:String = "jsonp",
                  @Query("pn") pn:String,
                  @Query("ps") ps:String = "20",
                  @Query("type") type:String = "1",
                  @Query("oid") oid:String,
                  @Query("sort") sort:String = "2"): Call<ReplyDto>

    @GET("x/web-interface/search/all/v2")
    fun search(
        @Header("User-Agent") agent:String = Constant.PLAY_USER_AGENT,
        @Header("Cookie") cookie:String = Constant.TEST_COOKIE,
        @Query("keyword") keyword:String,
        @Query("page") page:String = "1",
        @Query("pagesize") pagesize:String = "20"): Call<SearchDto>

    @GET("x/player/pagelist")
    fun pagelist(@Query("bvid") bvid:String,
                 @Query("jsonp") jsonp:String = "jsonp"): Call<PagelistDto>

    @GET("x/web-interface/dynamic/region")
    fun dynamicRecommended(@Query("ps") ps:String = "100",
                           @Query("rid") rid:String = "1"): Call<DynamicRecommendDto>
}