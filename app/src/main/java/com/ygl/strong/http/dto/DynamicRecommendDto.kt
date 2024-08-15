package com.ygl.strong.http.dto

class DynamicRecommendDto {
    var code:String = ""
    var pages:String = ""
    var results:String = ""
    var data:DataClass? = null

    class DataClass{
        var archives:List<VideoDetail> = arrayListOf()
    }

    class VideoDetail{
        var aid:String = ""
        var tname:String = ""
        var title:String = ""
        var cid:String = ""
        var bvid:String = ""
        var stat: StatClass? = null
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