package com.ygl.strong.db.bean

import android.database.Cursor
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
    var first_frame:String = ""
    var short_link_v2:String = ""
    var duration:Int = 0

    companion object {
        /**
         * 从 findBySQL 返回的 Cursor 解析一行到 VideoDetail。
         * 新增字段时请同步此方法。
         */
        fun fromCursor(c: Cursor): VideoDetail {
            val video = VideoDetail()
            video.id = c.getLong(c.getColumnIndexOrThrow("id"))
            video.aid = c.getString(c.getColumnIndexOrThrow("aid")) ?: ""
            video.bvid = c.getString(c.getColumnIndexOrThrow("bvid")) ?: ""
            video.cid = c.getString(c.getColumnIndexOrThrow("cid")) ?: ""
            video.title = c.getString(c.getColumnIndexOrThrow("title")) ?: ""
            video.happyScore = c.getInt(c.getColumnIndexOrThrow("happyScore"))
            video.watchDate = c.getLong(c.getColumnIndexOrThrow("watchDate"))
            video.reply = c.getString(c.getColumnIndexOrThrow("reply")) ?: ""
            video.tname = c.getString(c.getColumnIndexOrThrow("tname")) ?: ""
            video.videos = c.getInt(c.getColumnIndexOrThrow("videos"))
            video.first_frame = c.getString(c.getColumnIndexOrThrow("first_frame")) ?: ""
            video.short_link_v2 = c.getString(c.getColumnIndexOrThrow("short_link_v2")) ?: ""
            video.duration = c.getInt(c.getColumnIndexOrThrow("duration"))
            return video
        }
    }
}