package com.ygl.strong.db

import android.content.ContentValues
import com.ygl.strong.db.bean.VideoDetail
import com.ygl.strong.utils.Constant
import org.litepal.LitePal


object DB {
    fun isNewVideo(videoDetail:VideoDetail): Boolean {
        val list = LitePal
            .select("id")
            .where("aid = ? and bvid = ? and cid = ?",videoDetail.aid,videoDetail.bvid,videoDetail.cid)
            .find(VideoDetail::class.java)
        return list.size == 0
    }

    /**
     * 读取未观看视频，排除已加载的视频（bvid + cid 为唯一标识）。
     * 不使用 OFFSET，由调用方通过增大 excludeVideos 来模拟分页。
     */
    fun readUnWatchVideo(pageSize: Int, excludeVideos: List<VideoDetail> = emptyList()): List<VideoDetail> {
        if (excludeVideos.isEmpty()) {
            return LitePal
                .where("watchDate = ?", "0")
                .order("id")
                .limit(pageSize)
                .find(VideoDetail::class.java)
        }

        val excludeKeys = excludeVideos.map { "${it.bvid}|${it.cid}" }
        val placeholders = excludeKeys.joinToString(", ") { "?" }

        val sql = """
            SELECT * FROM VideoDetail
            WHERE watchDate = '0'
            AND (bvid || '|' || cid) NOT IN ($placeholders)
            ORDER BY id
            LIMIT $pageSize
        """.trimIndent()

        val cursor = LitePal.findBySQL(sql, *excludeKeys.toTypedArray())
        val result = mutableListOf<VideoDetail>()
        cursor.use { c ->
            while (c.moveToNext()) {
                result.add(VideoDetail.fromCursor(c))
            }
        }
        return result
    }

    fun recordVideoPlayInfo(id: Long, date: Long, happyScore: Int) {
        val values = ContentValues()
        values.put("watchDate",date.toString())
        values.put("happyScore",happyScore.toString())
        LitePal.update(VideoDetail::class.java, values, id)
        if (happyScore == -1){
            userFeelChange()
        }
    }

    fun userFeelChange(){

    }
}
