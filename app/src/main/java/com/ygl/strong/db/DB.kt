package com.ygl.strong.db

import android.content.Context
import androidx.sqlite.db.SimpleSQLiteQuery
import com.ygl.strong.db.bean.VideoDetail
import kotlin.jvm.JvmStatic

/**
 * 数据库操作封装。
 * 初始化时需调用 [DB.init]，在 Application.onCreate() 中执行。
 */
object DB {

    private var dao: VideoDetailDao? = null

    @JvmStatic
    fun init(context: Context) {
        dao = AppDatabase.getInstance(context).videoDetailDao()
    }

    private fun ensureDao(): VideoDetailDao {
        return dao ?: throw IllegalStateException("DB not initialized — call DB.init(context) in Application.onCreate()")
    }

    fun isNewVideo(videoDetail: VideoDetail): Boolean {
        return ensureDao().countByKeys(videoDetail.aid, videoDetail.bvid, videoDetail.cid) == 0
    }

    /**
     * 读取未观看视频，排除已加载的视频（bvid + cid 为唯一标识）。
     * 不使用 OFFSET，由调用方通过增大 excludeVideos 来模拟分页。
     */
    fun readUnWatchVideo(pageSize: Int, excludeVideos: List<VideoDetail> = emptyList()): List<VideoDetail> {
        if (excludeVideos.isEmpty()) {
            return ensureDao().readUnWatched(pageSize)
        }
        // 有排除列表时，在 SQL 层过滤 (bvid||'|'||cid) NOT IN (...)
        val excludeKeys = excludeVideos.map { "${it.bvid}|${it.cid}" }
        val placeholders = excludeKeys.joinToString(", ") { "?" }
        val sql = """
            SELECT * FROM VideoDetail
            WHERE watchDate = 0
            AND videoPlayUrlFailCount < 4
            AND (bvid || '|' || cid) NOT IN ($placeholders)
            ORDER BY id
            LIMIT $pageSize
        """.trimIndent()
        return ensureDao().readUnWatchedExcluding(
            SimpleSQLiteQuery(sql, excludeKeys.toTypedArray())
        )
    }

    fun recordVideoPlayInfo(id: Long, date: Long, happyScore: Int) {
        ensureDao().recordPlayInfo(id, date, happyScore)
        if (happyScore == -1) {
            userFeelChange()
        }
    }

    @JvmStatic
    fun updateFailCount(id: Long, count: Int) {
        ensureDao().updateFailCount(id, count)
    }

    @JvmStatic
    fun insert(video: VideoDetail) {
        ensureDao().insert(video)
    }

    /** 占位回调，暂空 */
    fun userFeelChange() {
    }
}
