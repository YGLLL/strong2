package com.ygl.strong.db

import android.content.ContentValues
import com.ygl.strong.db.bean.VideoDetail
import com.ygl.strong.utils.Constant
import org.litepal.LitePal


object DB {
    fun isNewVideo(videoDetail:VideoDetail): Boolean {
        val list = LitePal
            .select("id")//选择要返回的列，这样写也许会提高性能？
            .where("aid = ? and bvid = ? and cid = ?",videoDetail.aid,videoDetail.bvid,videoDetail.cid)
            .find(VideoDetail::class.java)
        return list.size == 0
    }

    /**
     * page 从1开始
     */
    fun readVideo(page:Int, pageSize:Int) : List<VideoDetail> {
        return LitePal
            .order("id desc")
            .limit(pageSize)
            .offset((page - 1) * pageSize)
            .find(VideoDetail::class.java)
    }

    fun userFeelChange(){

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
}