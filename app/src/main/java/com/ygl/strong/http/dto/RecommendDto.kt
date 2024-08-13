package com.ygl.strong.http.dto

import com.google.gson.annotations.SerializedName

class RecommendDto {
    var douga:RecommendClass? = null
    var teleplay:RecommendClass? = null
    var kichiku:RecommendClass? = null
    var dance:RecommendClass? = null
    var bangumi:RecommendClass? = null
    var fashion:RecommendClass? = null
    var life:RecommendClass? = null
    var guochuang:RecommendClass? = null
    @SerializedName("")
    var empty:RecommendClass? = null
    var movie:RecommendClass? = null
    var music:RecommendClass? = null
    var technology:RecommendClass? = null
    var game:RecommendClass? = null
    var ent:RecommendClass? = null

    var code:String = ""
    var pages:String = ""
    var results:String = ""

    class RecommendClass{
        @SerializedName("0")
        var a0:RecommendClassDetail? = null
        @SerializedName("1")
        var a1:RecommendClassDetail? = null
        @SerializedName("2")
        var a2:RecommendClassDetail? = null
        @SerializedName("3")
        var a3:RecommendClassDetail? = null
        @SerializedName("4")
        var a4:RecommendClassDetail? = null
        @SerializedName("5")
        var a5:RecommendClassDetail? = null
        @SerializedName("6")
        var a6:RecommendClassDetail? = null
        @SerializedName("7")
        var a7:RecommendClassDetail? = null
        @SerializedName("8")
        var a8:RecommendClassDetail? = null
        @SerializedName("9")
        var a9:RecommendClassDetail? = null
    }

    class RecommendClassDetail{
        var aid:String = ""
        var title:String = ""
        var cid:String = ""
        var bvid:String = ""
        var classes:String = ""
        var stat:StatClass? = null
    }
    class StatClass{
        var view:String = ""
        var danmaku:String = ""
        var reply:String = ""
        var favorite:String = ""
        var coin:String = ""
        var share:String = ""
        var like:String = ""
        var dislike:String = ""
    }
}