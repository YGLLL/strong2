# STRONG

**Android 短视频播放器，基于 Bilibili 逆向 API 获取视频数据，使用 dkplayer + Compose + MVVM 构建。**

项目采用预加载 + 网络代理缓存技术，保证视频间切换的流畅性。通过 VerticalViewPager + PreloadManager 实现滑窗预加载策略，在用户体验和内存占用之间取得平衡。

UI 层全部采用 **Jetpack Compose** 声明式 UI，结合 **MVVM + Kotlin 协程** 管理异步数据流。

## 界面展示

<img src="screenshot/1.jpg" width="300"/>
<img src="screenshot/2.jpg" width="300"/>

## 项目特点

- **仿抖音滑动播放** — 基于 VerticalViewPager 实现无限上下滑动，配合滑动窗口预加载机制，切换视频丝滑流畅
- **网络代理缓存** — 采用预缓存 + 播放缓存分离策略，避免重复下载，降低播放延迟
- **Bilibili 真实数据** — 通过逆向 B 站 HTTP API 获取视频列表、播放链接等真实数据
- **Room 本地持久化** — 视频列表数据先入库再展示，减少网络请求依赖
- **Compose 声明式 UI** — 全部界面使用 Jetpack Compose 构建，状态驱动渲染

## 技术栈

| 组件 | 用途 |
|---|----|
| dkplayer | 视频播放器（底层 ExoPlayer） |
| PreloadManager | 预加载策略 |
| 网络代理缓存 | 视频文件磁盘缓存（AndroidProxyCache） |
| ExoPlayer | 核心播放引擎 |
| **Jetpack Compose** | **声明式 UI** |
| **MVVM + ViewModel** | **UI 状态管理** |
| **Kotlin Coroutines / Flow** | **异步数据流** |
| Retrofit2 | Bilibili HTTP API 接口请求 |
| **Room** | **视频列表本地持久化（替代 LitePal）** |
| Glide | 封面图、头像等图片加载 |
| Hawk | 键值对数据存储 |

## 项目结构

```
app/src/main/java/com/ygl/strong/
├── app/            — Application，全局初始化
├── base/           — Activity 基类
├── db/             — Room 数据库（Entity / DAO / Database）
├── http/           — Bilibili API 接口（Retrofit + OkHttp）
├── ui/
│   ├── launch/     — 启动页（Compose + LauncherViewModel）
│   ├── main/       — 主列表播放页（Compose + MainViewModel），评论区面板（ReplyPanel + ReplyViewModel）
│   └── search/     — 搜索页
├── utils/          — 工具类
├── utils/videocache/         — 网络代理缓存源码
├── utils/videocache/strong   — 自定义预缓存策略
└── widget/         — VerticalViewPager 等自定义组件
```

## 数据流

```
打开 APP → LauncherActivity（Compose）
  → LauncherViewModel 获取视频列表并写入 Room
  → 跳转 MainActivity（Compose）
  → MainViewModel 从 Room 读取视频列表
  → AndroidView 嵌入 VerticalViewPager
  → PreloadManager 预加载 → 首次缓存就绪后播放
  → 滑动切换 → 新视频进入预加载列表 → 旧视频释放
```

## 架构

```
UI 层 (Compose @Composable)
    │  collectAsStateWithLifecycle()
    ▼
ViewModel 层 (MainViewModel / LauncherViewModel / ReplyViewModel)
    │  viewModelScope.launch { ... }
    ▼
数据层 (Room DAO / Retrofit API)
```

- UI 通过 `collectAsStateWithLifecycle` 订阅 ViewModel 中的 `StateFlow`
- ViewModel 直接调用 Room DAO 和 Retrofit
- 网络请求、DB 读写均在协程中执行
- 视频播放器核心（VerticalViewPager / TikTokController）保留原生 View 实现，通过 `AndroidView` Compose interop 嵌入

## 运行

1. 将 `constants.properties.example` 复制为 `constants.properties`
2. 填入需要的值，具体见 `constants.properties.example`
3. 使用 Android Studio 打开项目根目录，Sync Gradle 后运行

## 使用的开源库

- [dkplayer](https://github.com/Doikki/DKVideoPlayer) — 视频播放器框架
- [retrofit2](https://github.com/square/retrofit) — HTTP 请求
- [Room](https://developer.android.com/training/data-storage/room) — 数据库操作
- [Glide](https://github.com/bumptech/glide) — 图片加载
- [Hawk](https://github.com/orhanobut/hawk) — 键值对数据存储
- [Jetpack Compose](https://developer.android.com/jetpack/compose) — 声明式 UI
