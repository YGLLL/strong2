package com.ygl.strong.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.ygl.strong.db.bean.VideoDetail

@Dao
interface VideoDetailDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(video: VideoDetail)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(videos: List<VideoDetail>)

    @Query("SELECT COUNT(*) FROM VideoDetail WHERE aid = :aid AND bvid = :bvid AND cid = :cid")
    fun countByKeys(aid: String, bvid: String, cid: String): Int

    @Query("SELECT * FROM VideoDetail WHERE watchDate = 0 AND videoPlayUrlFailCount < 4 ORDER BY id")
    fun readAllUnWatched(): List<VideoDetail>

    @Query("SELECT * FROM VideoDetail WHERE watchDate = 0 AND videoPlayUrlFailCount < 4 ORDER BY id LIMIT :limit")
    fun readUnWatched(limit: Int): List<VideoDetail>

    @Query("UPDATE VideoDetail SET watchDate = :date, happyScore = :score WHERE id = :id")
    fun recordPlayInfo(id: Long, date: Long, score: Int)

    @Query("UPDATE VideoDetail SET videoPlayUrlFailCount = :count WHERE id = :id")
    fun updateFailCount(id: Long, count: Int)

    /**
     * 排除列表的原生查询：在 SQL 层做 (bvid||'|'||cid) NOT IN (...) + LIMIT
     */
    @RawQuery(observedEntities = [VideoDetail::class])
    fun readUnWatchedExcluding(query: SupportSQLiteQuery): List<VideoDetail>
}
