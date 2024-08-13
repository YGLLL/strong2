package com.ygl.strong.utils.videocache.strong;

import android.content.Context;
import android.text.TextUtils;

import com.ygl.strong.db.bean.VideoDetail;
import com.ygl.strong.utils.videocache.HttpProxyCacheServer;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import xyz.doikki.videoplayer.util.L;

/**
 * 抖音预加载工具，使用AndroidVideoCache实现
 */
public class PreloadManager {

    private static PreloadManager sPreloadManager;

    /**
     * 单线程池，按照添加顺序依次执行{@link PreloadTask}
     */
    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    /**
     * 保存正在预加载的{@link PreloadTask}
     */
    private final LinkedHashMap<String, PreloadTask> mPreloadTasks = new LinkedHashMap<>();

    /**
     * 标识是否需要预加载
     */
    private boolean mIsStartPreload = true;

    private final HttpProxyCacheServer mHttpProxyCacheServer;

    /**
     * 预加载的大小，每个视频预加载1M，这个参数可根据实际情况调整
     */
    public static final int PRELOAD_LENGTH = 1024 * 1024;

    private OnVideoPreloaded onVideoPreloaded;

    private PreloadManager(Context context) {
        mHttpProxyCacheServer = ProxyVideoCacheManager.getProxy(context);
    }

    public static PreloadManager getInstance(Context context) {
        if (sPreloadManager == null) {
            synchronized (PreloadManager.class) {
                if (sPreloadManager == null) {
                    sPreloadManager = new PreloadManager(context.getApplicationContext());
                }
            }
        }
        return sPreloadManager;
    }

    /**
     * 开始预加载
     */
    public void addPreloadTask(String rawUrl, int position) {
        if (isPreloaded(rawUrl)) return;
        PreloadTask task = new PreloadTask();
        task.mRawUrl = rawUrl;
        task.mPosition = position;
        task.mCacheServer = mHttpProxyCacheServer;
        task.mPreloadManager = sPreloadManager;
        L.i("addPreloadTask: " + position);
        mPreloadTasks.put(rawUrl, task);

        if (mIsStartPreload) {
            //开始预加载
            task.executeOn(mExecutorService);
        }
    }

    /**
     * 判断该播放地址是否已经预加载
     */
    private boolean isPreloaded(String rawUrl) {
        //先判断是否有缓存文件，如果已经存在缓存文件，并且其大小大于1KB，则表示已经预加载完成了
        File cacheFile = mHttpProxyCacheServer.getCacheFile(rawUrl);
        if (cacheFile.exists()) {
            if (cacheFile.length() >= 1024) {
                return true;
            } else {
                //这种情况一般是缓存出错，把缓存删掉，重新缓存
                cacheFile.delete();
                return false;
            }
        }
        //再判断是否有临时缓存文件，如果已经存在临时缓存文件，并且临时缓存文件超过了预加载大小，则表示已经预加载完成了
        File tempCacheFile = mHttpProxyCacheServer.getTempCacheFile(rawUrl);
        if (tempCacheFile.exists()) {
            return tempCacheFile.length() >= PRELOAD_LENGTH;
        }

        return false;
    }

    /**
     * 暂停预加载
     * 在ViewPage发生滑动（不是闲置）的时候
     * 根据是否反向滑动取消在position之下或之上的PreloadTask
     *
     * @param position 当前滑到的位置
     * @param isReverseScroll 列表是否反向滑动
     */
    public void pausePreload(int position, boolean isReverseScroll) {
        L.d("pausePreload：" + position + " isReverseScroll: " + isReverseScroll);
        mIsStartPreload = false;
        for (Map.Entry<String, PreloadTask> next : mPreloadTasks.entrySet()) {
            PreloadTask task = next.getValue();
            if (isReverseScroll) {//如果是向下（反向）滑动
                if (task.mPosition >= position) {
                    task.cancel();//凡是position之后的视频缓存任务，全部取消
                }
            } else {
                if (task.mPosition <= position) {
                    task.cancel();//凡是position之前的视频缓存任务，全部取消
                }
            }
        }
    }

    /**
     * 恢复预加载
     * 在ViewPage未发生滑动（闲置）的时候
     * 根据是否反向滑动开始在position之下或之上的PreloadTask
     *
     * @param position        当前滑到的位置
     * @param isReverseScroll 列表是否反向滑动
     */
    public void resumePreload(int position, boolean isReverseScroll) {
        L.d("resumePreload：" + position + " isReverseScroll: " + isReverseScroll);
        mIsStartPreload = true;
        for (Map.Entry<String, PreloadTask> next : mPreloadTasks.entrySet()) {
            PreloadTask task = next.getValue();
            if (isReverseScroll) {//如果是向下（反向）滑动
                if (task.mPosition < position) {//如果这个任务是position之前的任务
                    if (!isPreloaded(task.mRawUrl)) {//如果没有预加载完毕
                        task.executeOn(mExecutorService);//将mPosition这个预加载任务添加到队列
                    }
                }
            } else {//如果是向上（正向）滑动
                if (task.mPosition > position) {//如果这个任务是position之后的任务
                    if (!isPreloaded(task.mRawUrl)) {//如果没有预加载完毕
                        task.executeOn(mExecutorService);//将mPosition这个预加载任务添加到队列
                    }
                }
            }
        }
    }

    /**
     * 通过原始地址取消预加载
     */
    public void removePreloadTask(String rawUrl) {
        PreloadTask task = mPreloadTasks.get(rawUrl);
        if (task != null) {
            task.cancel();
            mPreloadTasks.remove(rawUrl);
        }
    }

    /**
     * 取消所有的预加载
     */
    public void removeAllPreloadTask() {
        Iterator<Map.Entry<String, PreloadTask>> iterator = mPreloadTasks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, PreloadTask> next = iterator.next();
            PreloadTask task = next.getValue();
            task.cancel();
            iterator.remove();
        }
    }

    /**
     * 获取播放地址
     */
    public String getPlayUrl(String rawUrl) {
        PreloadTask task = mPreloadTasks.get(rawUrl);
        if (task != null) {
            task.cancel();
        }
        if (isPreloaded(rawUrl)) {
            return mHttpProxyCacheServer.getProxyUrl(rawUrl);
        } else {
            return rawUrl;
        }
    }

    public void preloadUrls(List<VideoDetail> nextList, @NotNull Function0<Unit> next) {
        PreloadUrlsTask task = new PreloadUrlsTask();
        task.mNextList = nextList;
        task.mNext = next;
        new Thread(task).start();
    }

    public void oneVideoPreloaded(String url) {
        if (onVideoPreloaded!=null){
            String bvid = "";
            for (Map.Entry<String, String> next : PreloadUrlsTask.RAW_URLS.entrySet()) {
                if (Objects.equals(next.getValue(), url)) {
                    bvid = next.getKey();
                }
            }
            onVideoPreloaded.onVideoPreloaded(bvid);
        }
    }

    public void setVideoPreloadedCallback(OnVideoPreloaded callback) {
        onVideoPreloaded = callback;
    }
}
