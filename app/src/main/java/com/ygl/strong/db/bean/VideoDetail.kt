package com.ygl.strong.db.bean

import org.litepal.crud.LitePalSupport

class VideoDetail : LitePalSupport() {
    var id:Long = 0
    var aid:String = ""
    var bvid:String = ""
    var cid:String = ""
    var title:String = ""
    var happyScore:Int = 0
    var watchDate:Long = 0//0表示没有看过此视频
    var reply:String = ""
    var tname:String = ""
    var videos:Int = 0
}