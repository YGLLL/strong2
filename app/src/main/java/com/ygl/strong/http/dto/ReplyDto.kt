package com.ygl.strong.http.dto

class ReplyDto {
    var code:String = ""
    var message:String = ""
    var ttl:String = ""
    var data:DataClass? = null

    class DataClass{
        var replies:List<RepliesClass>? = null
    }
    class RepliesClass{
        var member:MemberClass? = null
        var content:ContentClass? = null
        var reply_control:ReplyControlClass? = null
    }
    class MemberClass{
        var uname:String = ""
        var avatar:String = ""
    }
    class ContentClass{
        var message:String = ""
    }
    class ReplyControlClass{
        var max_line:String = ""
        var time_desc:String = ""
    }
}