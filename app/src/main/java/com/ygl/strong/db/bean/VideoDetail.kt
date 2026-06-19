package com.ygl.strong.db.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ygl.strong.http.dto.DynamicRecommendDto

@Entity(tableName = "VideoDetail")
class VideoDetail {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    var aid: String = ""

    var bvid: String = ""

    var cid: String = ""

    var title: String = ""

    @ColumnInfo(name = "happyScore")
    var happyScore: Int = 0

    @ColumnInfo(name = "watchDate")
    var watchDate: Long = 0 // 0 表示没有看过此视频

    var reply: String = ""

    var tname: String = ""

    var videos: Int = 0

    @ColumnInfo(name = "first_frame")
    var first_frame: String = ""

    @ColumnInfo(name = "short_link_v2")
    var short_link_v2: String = ""

    var duration: Int = 0

    @ColumnInfo(name = "videoPlayUrlFailCount")
    var videoPlayUrlFailCount: Int = 0

    companion object {
        fun fromDynamicRecommendDtoVideoDetail(bean: DynamicRecommendDto.VideoDetail): VideoDetail {
            val videoDetail = VideoDetail()
            videoDetail.aid = bean.aid
            videoDetail.bvid = bean.bvid
            videoDetail.cid = bean.cid
            videoDetail.title = bean.title
            videoDetail.happyScore = 0
            videoDetail.watchDate = 0
            videoDetail.reply = bean.stat?.reply ?: ""
            videoDetail.tname = bean.tname
            videoDetail.videos = bean.videos
            videoDetail.first_frame = bean.first_frame
            videoDetail.short_link_v2 = bean.short_link_v2
            videoDetail.duration = bean.duration
            videoDetail.videoPlayUrlFailCount = 0
            return videoDetail
        }
    }
}
