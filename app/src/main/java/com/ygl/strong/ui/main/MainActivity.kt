package com.ygl.strong.ui.main

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.viewpager.widget.ViewPager
import com.ygl.strong.R
import com.ygl.strong.base.BaseActivity
import com.ygl.strong.db.DB
import com.ygl.strong.db.bean.VideoDetail
import com.ygl.strong.ui.main.MainViewModel
import com.ygl.strong.utils.Constant
import com.ygl.strong.utils.LogUtil
import com.ygl.strong.utils.Utils
import com.ygl.strong.utils.videocache.strong.PreloadManager
import com.ygl.strong.utils.videocache.strong.PreloadUrlsTask
import com.ygl.strong.utils.videocache.strong.ProxyVideoCacheManager
import com.ygl.strong.widget.VerticalViewPager
import xyz.doikki.videoplayer.player.VideoView

class MainActivity : BaseActivity() {

    private val viewModel: MainViewModel by viewModels()

    var mViewPager: VerticalViewPager? = null
    var mVideoView: VideoView? = null
    var mTiktok2Adapter: Tiktok2Adapter? = null
    var mController: TikTokController? = null
    var mCurPos: Int = 0
    private var mPreloadManager: PreloadManager? = null

    /** Compose 状态：回复数文本 */
    private var mReplyCount by mutableStateOf("")

    /** Compose 状态：ViewPager 是否已就绪 */
    private var mViewPagerReady by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTransparentSystemUI()

        // 注入 PreloadManager 到 ViewModel
        mPreloadManager = PreloadManager.getInstance(this)
        viewModel.preloadManager = mPreloadManager

        setContent { MainContent() }
    }

    // ─────────────────────── Compose UI ───────────────────────

    @Composable
    private fun MainContent() {
        // 观察视频列表
        val videoList by viewModel.videoList.collectAsStateWithLifecycle()

        // 列表变化 → 刷新适配器
        LaunchedEffect(videoList) {
            mTiktok2Adapter?.notifyDataSetChanged()
        }

        // 观察一次性事件（toast、首次就绪）
        LaunchedEffect(Unit) {
            viewModel.events.collect { event ->
                when (event) {
                    is MainEvent.ShowToast -> showToast(event.message)
                    is MainEvent.FirstDataReady -> handleFirstDataReady()
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // 底层：VerticalViewPager
            AndroidView(
                factory = { ctx ->
                    VerticalViewPager(ctx).also { vvp ->
                        onViewPagerCreated(vvp)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // 顶层：回复按钮
            ReplyOverlay(
                replyCount = mReplyCount,
                onReplyClick = {
                    if (mLoading?.isShowing != true) {
                        getCurReply()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 5.dp, bottom = 20.dp)
            )
        }
    }

    @Composable
    private fun ReplyOverlay(
        replyCount: String,
        onReplyClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
        ) {
            Icon(
                painter = painterResource(id = R.mipmap.comment),
                contentDescription = "回复",
                tint = Color.White,
                modifier = Modifier
                    .size(50.dp)
                    .clickable { onReplyClick() }
            )
            Text(
                text = replyCount,
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.height(200.dp))
        }
    }

    // ─────────────────────── 初始化 ───────────────────────

    private fun onViewPagerCreated(vvp: VerticalViewPager) {
        mViewPager = vvp
        mViewPager?.id = View.generateViewId()
        mViewPager?.setOffscreenPageLimit(10)

        // 适配器持有 ViewModel.backingList 同一引用
        mTiktok2Adapter = Tiktok2Adapter(viewModel.backingList)
        mViewPager?.setAdapter(mTiktok2Adapter)
        mViewPager?.setOverScrollMode(View.OVER_SCROLL_NEVER)
        mViewPager?.setOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            private var mCurItem = 0
            private var mIsReverseScroll = false

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                if (position == mCurItem) return
                mIsReverseScroll = position < mCurItem
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == mCurPos) return
                recordVideoPlayInfo(mCurPos)
                startPlay(position)

                val listSize = viewModel.videoList.value.size
                if ((listSize - (position + 1)) < 4) {
                    LogUtil.e("MainA",
                        "开始添加下一波数据, 当前总页数：$listSize, 当前播放页码：${position + 1}, 视频标题：${viewModel.videoList.value[position].title}")
                    viewModel.loadVideos()
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (state == VerticalViewPager.SCROLL_STATE_DRAGGING) {
                    mCurItem = mViewPager?.currentItem ?: 0
                }
                if (state == VerticalViewPager.SCROLL_STATE_IDLE) {
                    mPreloadManager?.resumePreload(mCurPos, mIsReverseScroll)
                } else {
                    mPreloadManager?.pausePreload(mCurPos, mIsReverseScroll)
                }
            }
        })

        // 视频播放核心
        initVideoView()

        mViewPagerReady = true

        // 开始加载数据
        viewModel.loadVideos(true)
    }

    /**
     * 首次数据就绪：设置预加载完成回调，自动播放第一个视频。
     */
    private fun handleFirstDataReady() {
        mPreloadManager?.setVideoPreloadedCallback { _, pos ->
            if (pos == 0) {
                runOnUiThread {
                    dismissLoading()
                    mViewPager?.currentItem = 0
                    mViewPager?.post { startPlay(0) }
                    mPreloadManager?.setVideoPreloadedCallback(null)
                }
            }
        }
    }

    private fun initVideoView() {
        mVideoView = VideoView(this)
        mVideoView?.setLooping(true)
        mVideoView?.setRenderViewFactory(TikTokRenderViewFactory.create())

        mController = TikTokController(this)
        mVideoView?.setVideoController(mController)
    }

    // ─────────────────────── 播放 ───────────────────────

    override fun onPause() {
        super.onPause()
        mVideoView?.pause()
    }

    private fun startPlay(position: Int) {
        val count = mViewPager?.childCount ?: 0
        val videoList = viewModel.videoList.value
        for (i in 0 until count) {
            val itemView = mViewPager?.getChildAt(i)
            val viewHolder = itemView?.tag as Tiktok2Adapter.ViewHolder
            if (viewHolder.mPosition == position) {
                mVideoView?.release()
                Utils.removeViewFormParent(mVideoView)

                val videoDetail: VideoDetail = videoList[position]
                mReplyCount = videoDetail.reply // 触发 Compose 重组

                val rawUrl = PreloadUrlsTask.RAW_URLS[videoDetail.bvid]
                val cacheUrl = mPreloadManager?.getPlayUrl(rawUrl)
                val cacheLogMessage: String
                if (rawUrl == cacheUrl) {
                    showToast(getString(R.string.loading_at_full_capacity))
                    val headers = HashMap<String, String>().apply {
                        put("Host", Utils.playUrl2Host(rawUrl))
                        put("Referer", Constant.PLAY_REFERER)
                        put("User-Agent", Constant.PLAY_USER_AGENT)
                    }
                    mVideoView?.setUrl(rawUrl, headers)
                    cacheLogMessage = "无缓存xxx"
                } else {
                    mVideoView?.setUrl(cacheUrl)
                    cacheLogMessage = "有缓存"
                }

                mController?.addControlComponent(viewHolder.mTikTokView, true)
                viewHolder.mPlayerContainer.addView(mVideoView, 0)
                mVideoView?.start()
                mCurPos = position
                LogUtil.e("MainA",
                    "当前总页数：${videoList.size}, 当前播放页码：${position + 1}, $cacheLogMessage, 视频标题：${videoDetail.title}")
                break
            }
        }
    }

    private fun getCurReply() {
        val videoDetail = viewModel.videoList.value[mCurPos]
        val replyFragment = ReplyFragment(videoDetail.aid)
        replyFragment.show(supportFragmentManager, replyFragment.tag)
    }

    private fun recordVideoPlayInfo(position: Int) {
        val date = System.currentTimeMillis()
        val curPosition = mVideoView?.currentPosition ?: 0
        val duration = mVideoView?.duration ?: 0
        val ratio = if (duration != 0L) curPosition.toFloat() / duration else 0f
        val happyScore = if (ratio < 0.5) -1 else 0
        DB.recordVideoPlayInfo(viewModel.videoList.value[position].id, date, happyScore)
    }

    // ─────────────────────── 生命周期 ───────────────────────

    override fun onDestroy() {
        super.onDestroy()
        releaseResources()
    }

    private var mBackPressedTime = 0L
    private var mIsExiting = false

    override fun onBackPressed() {
        val now = System.currentTimeMillis()
        if (now - mBackPressedTime > 2000) {
            mBackPressedTime = now
            showToast(getString(R.string.press_again_to_exit, getString(R.string.app_name)))
        } else {
            releaseResources()
            super.onBackPressed()
        }
    }

    private fun releaseResources() {
        if (mIsExiting) return
        mIsExiting = true
        mVideoView?.release()
        mPreloadManager?.removeAllPreloadTask()
        ProxyVideoCacheManager.clearAllCache(this)
    }
}
