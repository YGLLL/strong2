package com.ygl.strong.http.dto

class PagelistDto {
    var code:String = ""
    var message:String = ""
    var ttl:String = ""
    var data:List<DataBean>? = null

    class DataBean{
        var cid:String = ""
    }
}