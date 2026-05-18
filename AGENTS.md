# strong2 - AI工程上下文

## 🧭 项目本质
strong2 是一个 Android 短视频播放器应用，基于 Bilibili 逆向 API 获取视频数据，并使用 dkplayer 进行播放。

核心目标不是“功能完整”，而是：
- 播放稳定性优先
- API兼容性优先
- 架构简洁可维护

---

## 项目文件结构
### 项目在系统中的路径：
/mnt/c/back/apj/strong2
### 主要代码文件夹介绍(位于项目路径/app/src/main/java/com/ygl/strong)：
- app:存放Application
- base:存放常用组件父类
- db:存放操作数据库的代码
- http:定义了API接口（来自逆向哔哩哔哩）
- ui:存放UI代码
- utils:存放工具类
- utils/videocache:存放视频缓存工具,采用Android官方缓存技术
- utils/videocache/strong:存放针对strong项目的自定义缓存相关代码
- widget:存放自定义组件

---

## 🧱 系统架构（关键认知）

### 核心库：
- dkplayer：安卓视频播放器，封装MediaPlayer、ExoPlayer、IjkPlayer。模仿抖音并实现预加载，列表播放，悬浮播放，广告播放，弹幕，视频水印，视频滤镜(github链接:https://github.com/Doikki/DKVideoPlayer)
- videocache：Android官方缓存技术，源码直接放在路径utils/videocache

### 数据流：
打开APP → LauncherActivity → 获取视频列表并存入数据库 → MainActivity → 从数据库读取视频列表 → 根据视频列表获取播放链接 → com.ygl.strong.widget.VerticalViewPager → Tiktok2Adapter.instantiateItem → PreloadManager开始预加载 → PreloadManager.setVideoPreloadedCallback → 开始播放

### 播放器结构：
MainActivity → com.ygl.strong.widget.VerticalViewPager → Tiktok2Adapter → TikTokView(继承自xyz.doikki.videoplayer.controller.IControlComponent) → xyz.doikki.videoplayer.player.VideoView → VideoView.setVideoController(mController) → mController?.addControlComponent(viewHolder.mTikTokView, true)将TikTokView绑定到VideoView

### 预加载结构：
MainActivity → com.ygl.strong.widget.VerticalViewPager → Tiktok2Adapter.instantiateItem数个视频进入预加载列表 → PreloadManager.setVideoPreloadedCallback第一个视频预加载完成开始播放 → 滑动切换视频 → Tiktok2Adapter.instantiateItem新的视频进入预加载列表

---

## ⚠️ 当前已知核心问题（最高优先级）
暂时没有问题

---

## 🧠 AI工作原则（非常重要）

当处理 strong2 时：

### 1. 默认原则
- 所有问题都属于 strong2 项目上下文
- 不要假设是通用Android问题

### 2. 修改原则
- 优先最小修改
- 避免重构架构
- 不引入新复杂层（除非必要）

### 3. 调试原则
- 先分析数据流
- 再分析播放器状态
- 再给修改方案

### 4. 禁止行为
- ❌ 不要推荐过度架构（MVVM过度拆分等）
- ❌ 不要无根据重写播放器
- ❌ 不要脱离 ExoPlayer 原生逻辑

---

## 📊 优先级

1. API稳定性
2. UI滑动流畅性
3. 架构优化（最低优先级）

---

## 🎯 AI默认行为

- 默认你正在调试 strong2
- 默认使用 Java/kotlin + ExoPlayer 语境