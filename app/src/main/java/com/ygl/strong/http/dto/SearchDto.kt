package com.ygl.strong.http.dto

class SearchDto {
    var code:String = ""
    var message:String = ""
    var ttl:String = ""
    var data:SearchData? = null
    class SearchData{
        var result:List<SearchDataResult>? = null
    }
    class SearchDataResult{
        var result_type:String = ""
        var data:List<SearchDataResultData>? = null
    }
    class SearchDataResultData{
        var aid:String = ""
        var bvid:String = ""
        var title:String = ""
        var pic:String = ""
    }
}