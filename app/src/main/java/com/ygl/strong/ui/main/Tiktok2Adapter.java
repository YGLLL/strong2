package com.ygl.strong.ui.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ygl.strong.R;
import com.ygl.strong.db.bean.VideoDetail;
import com.ygl.strong.utils.videocache.strong.PreloadManager;
import com.ygl.strong.utils.videocache.strong.PreloadUrlsTask;
import com.ygl.strong.widget.TikTokView;

import java.util.ArrayList;
import java.util.List;

public class Tiktok2Adapter extends PagerAdapter {

    /**
     * View缓存池，从ViewPager中移除的item将会存到这里面，用来复用
     */
    private List<View> mViewPool = new ArrayList<>();

    /**
     * 数据源
     */
    private List<VideoDetail> mVideoBeans;

    public Tiktok2Adapter(List<VideoDetail> videoBeans) {
        this.mVideoBeans = videoBeans;
    }

    @Override
    public int getCount() {
        return mVideoBeans == null ? 0 : mVideoBeans.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Context context = container.getContext();
        View view = null;
        if (mViewPool.size() > 0) {//取第一个进行复用
            view = mViewPool.get(0);
            mViewPool.remove(0);
        }

        ViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_tik_tok, container, false);
            viewHolder = new ViewHolder(view);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        VideoDetail item = mVideoBeans.get(position);
        //当创建页面时则开始预加载
        PreloadManager.getInstance(context).addPreloadTask(PreloadUrlsTask.RAW_URLS.get(item.getBvid()), position);
        Glide.with(context)
                .asBitmap()
                .load(item.getFirst_frame())
                .into(new CustomViewTarget<ImageView, Bitmap>(viewHolder.mThumb) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                        if (resource.getHeight() > resource.getWidth()) {
                            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        } else {
                            view.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        }
                        view.setImageBitmap(resource);
                    }

                    @Override
                    public void onResourceCleared(Drawable placeholder) {
                        view.setImageDrawable(placeholder);
                    }

                    @Override
                    public void onLoadFailed(Drawable errorDrawable) {
                        view.setImageDrawable(errorDrawable);
                    }
                });
        viewHolder.mTitle.setText(item.getTitle());
        viewHolder.mTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(item.getShort_link_v2())); // 替换为你要打开的网址
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                }
                return true;
            }
        });
        viewHolder.mPosition = position;
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        View itemView = (View) object;
        container.removeView(itemView);
        VideoDetail item = mVideoBeans.get(position);
        //取消预加载
        PreloadManager.getInstance(container.getContext()).removePreloadTask(PreloadUrlsTask.RAW_URLS.get(item.getBvid()));
        //保存起来用来复用
        mViewPool.add(itemView);
    }

    /**
     * 借鉴ListView item复用方法
     */
    public static class ViewHolder {

        public int mPosition;
        public TextView mTitle;//标题
        public ImageView mThumb;//封面图
        public TikTokView mTikTokView;
        public FrameLayout mPlayerContainer;

        ViewHolder(View itemView) {
            mTikTokView = itemView.findViewById(R.id.tiktok_View);
            mTitle = mTikTokView.findViewById(R.id.tv_title);
            mThumb = mTikTokView.findViewById(R.id.iv_thumb);
            mPlayerContainer = itemView.findViewById(R.id.container);
            itemView.setTag(this);
        }
    }
}
