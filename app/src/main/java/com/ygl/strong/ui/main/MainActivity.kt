package com.ygl.strong.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.ygl.strong.R
import com.ygl.strong.base.BaseActivity
import com.ygl.strong.db.DB
import com.ygl.strong.db.bean.VideoDetail
import com.ygl.strong.http.Api
import com.ygl.strong.http.dto.ReplyDto
import com.ygl.strong.ui.search.SearchActivity
import com.ygl.strong.utils.Constant
import com.ygl.strong.utils.LogUtil
import com.ygl.strong.utils.Utils
import com.ygl.strong.utils.videocache.strong.PreloadManager
import com.ygl.strong.utils.videocache.strong.PreloadUrlsTask
import com.ygl.strong.utils.videocache.strong.ProxyVideoCacheManager
import com.ygl.strong.widget.VerticalViewPager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        } else {
            startInit()
        }
    }

    private fun startInit() {
        initViewPager()
        initVideoView()
        mPreloadManager = PreloadManager.getInstance(this)

        val vSearch = findViewById<View>(R.id.v_search)
        val flReply = findViewById<View>(R.id.fl_reply)
        mTvReplyCount = findViewById(R.id.tv_reply_count)
        vSearch.setOnClickListener {
            startActivity(Intent(this,SearchActivity::class.java))
        }
        flReply.setOnClickListener {
            if (mLoading?.isShowing != true){
                getCurReply()
            }
        }

        loadVideos(true)
    }

    private fun getCurReply() {
        val videoDetail: VideoDetail = mVideoList[mCurPos]
        val replyFragment = ReplyFragment(videoDetail.aid)
        replyFragment.show(supportFragmentManager,replyFragment.tag)
    }

    private fun loadVideos(firstLoad:Boolean = false) {
        if (firstLoad)showLoading()
        mDBpage++
        val nextList = DB.readVideo(mDBpage,READ_VIDEO_SIZE)
        if (nextList.isEmpty())return
        mPreloadManager?.preloadUrls(nextList){
            runOnUiThread {
                if (firstLoad){
                    mPreloadManager?.setVideoPreloadedCallback{ bvid ->
                        if (bvid == nextList[0].bvid){
                            runOnUiThread {
                                dismissLoading()
                                mViewPager?.currentItem = 0
                                mViewPager?.post { startPlay(0) }
                                mPreloadManager?.setVideoPreloadedCallback(null)
                            }
                        }
                    }
                }
                mVideoList.addAll(nextList)
                mTiktok2Adapter?.notifyDataSetChanged()
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
        mViewPager?.setOffscreenPageLimit(4) //预加载4，缓存9
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
                    val headers: HashMap<String, String> = HashMap()
                    headers.put("Host",Utils.playUrl2Host(rawUrl))
                    headers.put("Referer",Constant.PLAY_REFERER)
                    headers.put("User-Agent",Constant.PLAY_USER_AGENT)
                    mVideoView?.setUrl(rawUrl,headers)
                }else{
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startInit()
            } else {
                showToast("没有足够的权限")
            }
        }
    }
}