package com.ygl.strong.ui.main

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.ygl.strong.R
import com.ygl.strong.base.BaseActivity
import com.ygl.strong.db.DB
import com.ygl.strong.db.bean.VideoDetail
import com.ygl.strong.ui.search.SearchActivity
import com.ygl.strong.utils.Constant
import com.ygl.strong.utils.LogUtil
import com.ygl.strong.utils.Utils
import com.ygl.strong.utils.videocache.strong.PreloadManager
import com.ygl.strong.utils.videocache.strong.PreloadUrlsTask
import com.ygl.strong.utils.videocache.strong.ProxyVideoCacheManager
import com.ygl.strong.widget.VerticalViewPager
import xyz.doikki.videoplayer.player.VideoView

class MainActivity : BaseActivity() {

    var mViewPager: VerticalViewPager? = null
    var mVideoView: VideoView? = null
    var mTiktok2Adapter: Tiktok2Adapter? = null
    val mVideoList: MutableList<VideoDetail> = mutableListOf()
    var mController: TikTokController? = null
    var mCurPos:Int = 0
    private var mPreloadManager: PreloadManager? = null
    private var mTvReplyCount:TextView? = null

    private var mDBpage = 0//读取数据库的页数
    private val READ_VIDEO_SIZE = 10//读取数据库的大小

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setStatusBarTransparent()
        setControlBarTransparent()

        startInit()
    }

    private fun startInit() {
        //初始化三大核心部件
        initViewPager()
        initVideoView()
        mPreloadManager = PreloadManager.getInstance(this)

        //设置ui
        val vSearch = findViewById<View>(R.id.v_search)
        val ivReply = findViewById<View>(R.id.iv_reply)
        mTvReplyCount = findViewById(R.id.tv_reply_count)
        vSearch.setOnClickListener {
            startActivity(Intent(this,SearchActivity::class.java))
        }
        ivReply.setOnClickListener {
            if (mLoading?.isShowing != true){
                getCurReply()
            }
        }

        //开始加载数据
        loadVideos(true)
    }

    private fun getCurReply() {
        val videoDetail: VideoDetail = mVideoList[mCurPos]
        val replyFragment = ReplyFragment(videoDetail.aid)
        replyFragment.show(supportFragmentManager,replyFragment.tag)
    }

    /**
     * 从数据库读取视频数据并获取播放链接
     * 获取完播放链接后增页并开始缓存
     */
    private fun loadVideos(firstLoad:Boolean = false) {
        if (firstLoad)showLoading(false,getString(R.string.loading_network_data))

        //从数据库读取数据
        mDBpage++
        //排除已经加载到pageView的视频（bvid+cid为唯一标识）
        val nextList = DB.readUnWatchVideo(mDBpage, READ_VIDEO_SIZE, mVideoList)
        //如果下一页不满了，则从网络加载数据
        val nextNextList = DB.readUnWatchVideo(mDBpage+1, READ_VIDEO_SIZE, mVideoList)
        if (nextNextList.size<READ_VIDEO_SIZE){
            LogUtil.e("MainA","加载网络数据")
            Utils.loadVideoDataByNetwork { msg->
                if (!TextUtils.isEmpty(msg)){
                    showToast(msg)
                }
            }
        }

        if (nextList.isEmpty())return
        //获取播放链接
        mPreloadManager?.preloadUrls(nextList){
            runOnUiThread {
                if (firstLoad){
                    //如果是第一次加载数据，则自动播放
                    mPreloadManager?.setVideoPreloadedCallback{ bvid ->
                        if (bvid == nextList[0].bvid){
                            runOnUiThread {
                                dismissLoading()
                                mViewPager?.currentItem = 0//移动到第0页
                                mViewPager?.post { startPlay(0) }
                                mPreloadManager?.setVideoPreloadedCallback(null)
                            }
                        }
                    }
                }

                mVideoList.addAll(nextList)
                mTiktok2Adapter?.notifyDataSetChanged()//执行完这行后，会在mTiktok2Adapter内部触发预加载
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mVideoView?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mVideoView?.release()
        mPreloadManager?.removeAllPreloadTask()
        //清除缓存，实际使用可以不需要清除，这里为了方便测试
        ProxyVideoCacheManager.clearAllCache(this)
    }

    private fun initViewPager() {
        mViewPager = findViewById(R.id.vvp)
        mViewPager?.setOffscreenPageLimit(10) //预加载10，缓存15
        mTiktok2Adapter = Tiktok2Adapter(mVideoList)
        mViewPager?.setAdapter(mTiktok2Adapter)
        mViewPager?.setOverScrollMode(View.OVER_SCROLL_NEVER) //禁止过度滑动
        mViewPager?.setOnPageChangeListener(object : SimpleOnPageChangeListener() {
            private var mCurItem = 0

            /**
             * VerticalViewPager是否反向滑动
             */
            private var mIsReverseScroll = false
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                if (position == mCurItem) {
                    return
                }
                mIsReverseScroll = position < mCurItem//用户滑动到下一个，或者上一个视频时，更新这个方向布尔值，true是反向滑动，false是正常向上滑动
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == mCurPos) return  //如果滑动后，没有进入下一个视频，那么就不重新播放当前视频了
                recordVideoPlayInfo(mCurPos)
                startPlay(position)
                //当看到倒数第三页时，添加下一波数据
                if (position == (mVideoList.size-3)){
                    loadVideos()
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (state == VerticalViewPager.SCROLL_STATE_DRAGGING) { //拖动
                    mCurItem = mViewPager?.getCurrentItem()?:0 //拖动时，把当前viewpage的序号赋值给mCurItem
                }
                if (state == VerticalViewPager.SCROLL_STATE_IDLE) { //闲置
                    mPreloadManager?.resumePreload(mCurPos, mIsReverseScroll) //预加载
                } else {
                    mPreloadManager?.pausePreload(mCurPos, mIsReverseScroll) //停止预加载
                }
            }
        })
    }

    private fun initVideoView() {
        mVideoView = VideoView(this) //不是从xml拿的View
        mVideoView?.setLooping(true) //循环播放
        //以下只能二选一，看你的需求
        mVideoView?.setRenderViewFactory(TikTokRenderViewFactory.create())
//        mVideoView.setScreenScaleType(VideoView.SCREEN_SCALE_CENTER_CROP);

        mController = TikTokController(this)
        mVideoView?.setVideoController(mController)
    }

    private fun startPlay(position: Int) {
        val count = mViewPager?.childCount?:0
        for (i in 0 until count) {
            val itemView = mViewPager?.getChildAt(i)
            val viewHolder = itemView?.tag as Tiktok2Adapter.ViewHolder
            if (viewHolder.mPosition == position) {
                mVideoView?.release()
                Utils.removeViewFormParent(mVideoView)

                val videoDetail: VideoDetail = mVideoList[position]
                mTvReplyCount?.text = videoDetail.reply

                val rawUrl = PreloadUrlsTask.RAW_URLS[videoDetail.bvid]
                val cacheUrl = mPreloadManager?.getPlayUrl(rawUrl)
                LogUtil.e("MainA","rawUrl:${rawUrl}")
                LogUtil.e("MainA","cacheUrl:${cacheUrl}")
                if (rawUrl == cacheUrl){
                    //没有缓存，使用原链接播放
                    showToast(getString(R.string.loading_at_full_capacity))
                    val headers: HashMap<String, String> = HashMap()
                    headers["Host"] = Utils.playUrl2Host(rawUrl)
                    headers["Referer"] = Constant.PLAY_REFERER
                    headers["User-Agent"] = Constant.PLAY_USER_AGENT
                    mVideoView?.setUrl(rawUrl,headers)
                }else{
                    //有缓存，使用缓存播放
                    mVideoView?.setUrl(cacheUrl)
                }

                //请点进去看isDissociate的解释
                mController?.addControlComponent(viewHolder.mTikTokView, true)
                viewHolder.mPlayerContainer.addView(mVideoView, 0)
                mVideoView?.start()
                mCurPos = position
                break
            }
        }
    }

    private fun recordVideoPlayInfo(position: Int){
        val date = System.currentTimeMillis()
        val curPosition = mVideoView?.currentPosition?:0
        val duration = mVideoView?.duration?:0
        var ratio = 0f
        if (duration != 0L){
            ratio = curPosition.toFloat()/duration
        }
        val happyScore = if (ratio<0.5){
            -1
        }else{
            0
        }
        val curVideo = mVideoList[position]
        DB.recordVideoPlayInfo(curVideo.id,date,happyScore)
    }


}