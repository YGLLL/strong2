package com.ygl.strong.http.dto

class PlayUrlDto {
    var code:String = ""
    var message:String = ""
    var ttl:String = ""
    var data:DataBean? = null

    class DataBean{
        var durl:List<UrlBean>? = null
    }

    class UrlBean{
        var url:String = ""
    }
}