package com.ygl.strong.ui.main

import android.text.TextUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ygl.strong.db.DB
import com.ygl.strong.db.bean.VideoDetail
import com.ygl.strong.utils.LogUtil
import com.ygl.strong.utils.Utils
import com.ygl.strong.utils.videocache.strong.PreloadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainViewModel : ViewModel() {

    companion object {
        private const val READ_VIDEO_SIZE = 10
    }

    /** 唯一真实列表，Tiktok2Adapter 直接持有此引用 */
    val backingList = mutableListOf<VideoDetail>()
    private val _videoList = MutableStateFlow<List<VideoDetail>>(emptyList())
    val videoList: StateFlow<List<VideoDetail>> = _videoList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var isLoadVideosPlayUrl = false

    private val _events = MutableSharedFlow<MainEvent>(extraBufferCapacity = 5)
    val events: SharedFlow<MainEvent> = _events.asSharedFlow()

    /** 外部注入，Activity 初始化后设置 */
    var preloadManager: PreloadManager? = null

    /**
     * 加载下一页视频。
     * @param firstLoad 是否首次加载（显示 LoadingDialog，ready 后自动播放第一个）
     */
    fun loadVideos(firstLoad: Boolean = false) {
        if (isLoadVideosPlayUrl) return
        isLoadVideosPlayUrl = true

        viewModelScope.launch {
            if (firstLoad) _isLoading.value = true

            // 1. DB 读取本地未看视频
            val nextList = DB.readUnWatchVideo(READ_VIDEO_SIZE, backingList)
            val combinedExclude = mutableListOf<VideoDetail>().apply {
                addAll(backingList)
                addAll(nextList)
            }
            val nextNextList = DB.readUnWatchVideo(READ_VIDEO_SIZE, combinedExclude)

            // 2. 不足则网络补充
            if (nextNextList.size < READ_VIDEO_SIZE) {
                LogUtil.e("MainVM", "加载网络数据")
                val msg = loadNetworkDataSuspend()
                if (!TextUtils.isEmpty(msg)) {
                    _events.emit(MainEvent.ShowToast(msg))
                }
            }

            if (nextList.isEmpty()) {
                isLoadVideosPlayUrl = false
                _isLoading.value = false
                return@launch
            }

            // 3. 预加载播放链接（IO 线程）
            val preloadMgr = preloadManager
            if (preloadMgr == null) {
                isLoadVideosPlayUrl = false
                _isLoading.value = false
                return@launch
            }
            val successList = withContext(Dispatchers.IO) {
                preloadUrlsSuspend(preloadMgr, nextList)
            }

            // 4. 更新列表（回到 Main）
            backingList.addAll(successList)
            _videoList.value = backingList.toList()
            _events.tryEmit(MainEvent.DataSetChanged)
            isLoadVideosPlayUrl = false
            _isLoading.value = false

            if (firstLoad) {
                _events.emit(MainEvent.FirstDataReady)
            }
        }
    }

    private suspend fun loadNetworkDataSuspend(): String = suspendCoroutine { cont ->
        Utils.loadVideoDataByNetwork { msg -> cont.resume(msg) }
    }

    private suspend fun preloadUrlsSuspend(
        preloadMgr: PreloadManager,
        nextList: List<VideoDetail>
    ): List<VideoDetail> = suspendCoroutine { cont ->
        preloadMgr.preloadUrls(nextList) { successList -> cont.resume(successList) }
    }
}

/** 一次性事件 */
sealed interface MainEvent {
    data class ShowToast(val message: String) : MainEvent
    data object FirstDataReady : MainEvent
    data object DataSetChanged : MainEvent
}
