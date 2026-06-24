# Android 性能排行榜 App 开发文档

---

## 1. 项目概述

### 1.1 产品名称

**PerfTop**（安卓版）

### 1.2 产品定位

面向数码爱好者、装机用户与移动设备发烧友的 Android 应用。提供电脑与移动端 CPU/GPU 的性能排行榜、多维度筛选对比、天梯图可视化，帮助用户快速评估硬件性能并辅助购买决策。

### 1.3 目标平台

- Android 8.0 (API 26) 及以上

- 适配手机、平板及折叠屏设备（支持横竖屏、分屏、平行视界）

### 1.4 核心差异化

- 业界首创桌面级 x86/x64 与移动 ARM 芯片同台对比

- 综合评分算法聚合 Geekbench、3DMark、PassMark、安兔兔等主流基准测试

- 可交互的性能天梯图（横向柱状图），支持缩放与点击

- 离线优先数据策略与后台静默更新

---

## 2. 功能需求

### 2.1 用户故事（User Stories）

| 编号    | 用户故事                                       |
| ----- | ------------------------------------------ |
| US-01 | 按类别（电脑CPU、电脑GPU、手机CPU、手机GPU）查看排行榜，默认综合性能降序 |
| US-02 | 可按单核、多核、游戏、能效等不同维度排序                       |
| US-03 | 通过关键词搜索型号，快速定位排名                           |
| US-04 | 点击任一型号查看详细规格、各项跑分、发布日期、功耗等信息               |
| US-05 | 选择两款或以上硬件，在统一视图中并排对比参数与跑分                  |
| US-06 | 以可交互天梯图（横向条形图）查看所有型号的相对性能                  |
| US-07 | 数据自动后台更新，可手动刷新；离线时正常查看缓存数据                 |
| US-08 | 收藏感兴趣的型号，支持分组管理                            |
| US-09 | 使用筛选器限定架构、品牌、核心数、年份等范围                     |
| US-10 | 查看各型号的“性价比”指数（性能/参考价格比）                    |

### 2.2 功能模块划分

1. **排行首页**
   
   - 4 个硬件类别 Tab：PC CPU / PC GPU / Mobile CPU / Mobile GPU
   
   - 排序维度切换（综合、单核、多核、游戏、能效等）
   
   - 列表卡片：排名、型号、品牌、核心参数、综合评分条形图
   
   - 下拉刷新、上拉加载更多（分页）

2. **搜索与筛选**
   
   - 全局搜索栏，支持模糊匹配型号名与内部代号
   
   - 筛选面板：品牌、架构、核心数范围、频率范围、发布年份、TDP 范围
   
   - 筛选后动态重排

3. **详情页**
   
   - 型号全称、产品图、架构、制程、核心/线程数、频率、缓存、TDP
   
   - 多源跑分卡片（Geekbench 5/6、Cinebench、3DMark、安兔兔等）
   
   - 性能雷达图（单核、多核、GPU、内存、AI）
   
   - 历史排名趋势（折线图）
   
   - 收藏/取消收藏
   
   - “开始对比”按钮

4. **对比功能**
   
   - 型号选择器：从收藏、最近浏览或搜索中选入（2~5 款）
   
   - 并排规格表格，优胜项高亮
   
   - 性能对比柱状图/雷达图
   
   - 一键生成对比结果图片并分享

5. **天梯图视图**
   
   - 横向柱状图展示所选类别全部型号，长度对应综合评分
   
   - 按品牌着色，支持双指缩放与滚动
   
   - 点击任意条目跳转详情
   
   - 实时响应筛选器

6. **收藏与历史**
   
   - “我的收藏”页面，可创建分组
   
   - “最近浏览”自动记录（本地）

7. **设置**
   
   - 数据更新策略：仅在 Wi-Fi 下自动更新 / 手动
   
   - 基准测试权重自定义（调整综合评分算法）
   
   - 缓存管理：清除离线数据
   
   - 显示主题：浅色/深色（跟随系统或手动）
   
   - 关于页：数据来源声明、版本号、反馈入口

---

## 3. 非功能需求

| 类别    | 要求                                        |
| ----- | ----------------------------------------- |
| 性能    | 列表滑动 60fps；详情页加载 ≤ 1s；对比图表渲染 ≤ 0.5s       |
| 离线支持  | 核心排行数据缓存至本地 Room 数据库，无网络时全功能可用            |
| 数据准确性 | 多源加权算法保证综合评分客观，原始分数可查                     |
| 安全性   | 全链路 HTTPS；不收集任何个人身份信息                     |
| 可维护性  | 客户端采用 MVVM + Repository 模式；后端爬虫与 API 分离   |
| 兼容性   | 最低 API 26，通过 Jetpack Compose 及资源限定符适配各类屏幕 |
| 国际化   | 初版仅简体中文，架构预留多语言支持                         |
| 功耗与流量 | 后台更新使用 WorkManager，仅满足条件时执行；更新时优先增量同步     |

---

## 4. 技术栈与工具

### 4.1 客户端（Android）

- **语言**：Kotlin 1.9+

- **UI 框架**：Jetpack Compose (Material 3)

- **架构**：MVVM + Repository + Navigation Compose

- **响应式**：Kotlin Flow、StateFlow

- **依赖注入**：Hilt (Dagger)

- **持久化**：Room (SQLite) + DataStore (偏好设置)

- **网络**：Retrofit2 + OkHttp3 + Kotlinx Serialization

- **图表**：Vico (Compose 原生图表) 或 MPAndroidChart (经 AndroidView 封装)

- **图片加载**：Coil (Compose 整合)

- **后台任务**：WorkManager (定期数据同步)

- **构建系统**：Gradle (Kotlin DSL)

- **测试**：JUnit5、MockK、Turbine、Compose UI Test、Espresso

- **代码规范**：Ktlint / Detekt

### 4.2 后端服务（复用 iOS 版设计）

- **爬虫**：Python (Scrapy) + Celery 定时任务

- **API 层**：FastAPI / Node.js (Express)

- **数据库**：PostgreSQL + Redis (缓存热点排行)

- **文件存储**：AWS S3 / 阿里云 OSS (硬件图片)

- **部署**：Docker + Kubernetes (或 ECS)

### 4.3 数据源

- Geekbench Browser

- PassMark CPU/GPU

- 3DMark 官方排行榜

- 安兔兔公开分数（需合规获取）

- 官方规格（Intel ARK、AMD 官网等）

> 法律提示：抓取须遵守目标站点 robots.txt 与使用条款，优先调用官方公开 API；商业使用建议取得授权。

---

## 5. UI / UX 设计

### 5.1 设计原则

- 遵循 Material 3 设计规范，支持 Dynamic Color（Android 12+）

- 信息层次清晰：排名数字 → 型号名 → 关键分数 → 条形图

- 主色调：科技蓝 + 深色曲面背景

- 手势：长按列表项弹出上下文菜单（收藏/对比），双指缩放天梯图

- 支持大屏布局：列表与详情并排（平板/折叠屏展开态）

### 5.2 导航结构

text

底部导航栏：
├─ 排行 (首页)
│   └─ 顶部硬件类别 Tab Row (PC CPU / PC GPU / Mobile CPU / Mobile GPU)
│       └─ 排序按钮 (DropdownMenu)
│       └─ 筛选器 (Modal Bottom Sheet)
│       └─ LazyColumn 列表 → 详情页
├─ 天梯图
│   └─ 类别切换 + 筛选 → 横向条形图
├─ 对比
│   └─ 型号选择器 → 对比结果页
├─ 收藏
│   └─ 分组收藏列表 → 详情
└─ 设置

### 5.3 关键页面 UI 描述

**排行榜列表项 (Compose)**

- `Row`：排名 `Text`（带升降三角图标）、`Column`（型号、品牌+核心参数）、综合分数 + `LinearProgressIndicator` 样式彩色条

**详情页**

- 可滚动 `Column`：顶部图片（Coil 加载），型号名与收藏 `IconButton`

- 水平 `LazyRow` 显示各跑分来源卡片

- 雷达图（Canvas 自定义绘制）

- 规格表格（`LazyVerticalGrid` 或自定义 Composable）

- 底部悬浮 “开始对比” `ExtendedFloatingActionButton`

**对比页面**

- 顶部：已选型号 `LazyRow` 胶囊标签，可移除

- 规格对比 `Table`（通过 `LazyColumn` + 等宽 `Row` 实现），差异项高亮

- 分组柱状图（Vico `ColumnChart`）

- 分享 `FloatingActionButton`

**天梯图**

- 横向 `LazyColumn`，每项为一个宽度按比例拉伸的 `Box`，显示型号与分数

- `transformable` 手势修饰符实现缩放

---

## 6. 数据模型设计

### 6.1 Kotlin 数据类（网络层 & Room）

kotlin

@Serializable
@Entity(tableName = "hardwares")
data class Hardware(
    @PrimaryKey val id: Int,
    val name: String,
    val brand: String,
    val category: Category, // enum: PC_CPU, PC_GPU, MOBILE_CPU, MOBILE_GPU
    val architecture: String,
    val launchDate: String?, // ISO 8601
    @Embedded val specs: Specs?,
    @ColumnInfo(name = "overall_score") val overallScore: Double,
    @Embedded val price: PriceInfo?
)
@Serializable
data class Specs(
    val cores: Int?,
    val threads: Int?,
    val baseClockGHz: Double?,
    val boostClockGHz: Double?,
    val tdpWatts: Int?,
    val lithography: String?,
    val vramGB: Int?,
    val memoryType: String?,
    val bandwidthGBs: Double?,
    val cache: String?
)
@Serializable
@Entity(tableName = "benchmarks")
data class Benchmark(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val hardwareId: Int,
    val source: String,
    val metric: String,
    val score: Double,
    val unit: String
)
@Serializable
data class PriceInfo(
    val currency: String,
    val amount: Double,
    val source: String,
    val updated: String?
)
@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hardwareId: Int,
    val groupName: String = "默认"
)
@Entity(tableName = "history")
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hardwareId: Int,
    val visitedAt: Long = System.currentTimeMillis()
)

### 6.2 Room 数据库关系

- `Hardware` 与 `Benchmark` 通过 `hardwareId` 关联，DAO 中通过 `@Transaction` 查询合并列表。

- 收藏与历史分别独立操作。

### 6.3 后端数据库表（PostgreSQL）

保持与 iOS 版相同设计，略。

---

## 7. 网络层与 API 设计

### 7.1 基础 URL

`https://api.perftop.example.com/v1`

### 7.2 主要端点

| 方法  | 路径                                                   | 描述              |
| --- | ---------------------------------------------------- | --------------- |
| GET | `/hardwares`                                         | 排行列表，支持分页、排序、筛选 |
| GET | `/hardwares/{id}`                                    | 详情（含跑分）         |
| GET | `/hardwares/compare?ids=1,2,3`                       | 对比数据            |
| GET | `/hardwares/search?q=snapdragon&category=mobile_cpu` | 搜索              |
| GET | `/meta/filters?category=pc_gpu`                      | 动态筛选选项          |
| GET | `/hardwares/export/all`                              | 全量数据包（用于首次缓存）   |

### 7.3 Retrofit 接口定义示例

kotlin

interface PerfTopApi {
    @GET("hardwares")
    suspend fun getHardwares(
        @Query("category") category: String,
        @Query("sort_by") sortBy: String = "overall_score",
        @Query("order") order: String = "desc",
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("brand") brand: String? = null,
        @Query("core_min") coreMin: Int? = null
    ): ApiResponse<List<Hardware>>
    @GET("hardwares/{id}")
    suspend fun getHardwareDetail(@Path("id") id: Int): ApiResponse<HardwareWithBenchmarks>
}

### 7.4 客户端网络策略

- 使用 `OkHttp` 拦截器添加缓存头，支持 `ETag` 增量更新。

- `Repository` 层遵循“网络优先 + 本地回退”模式：先返回 Room 数据，同时请求网络更新。

- 全量数据包首次下载后解压并插入 Room，后续只拉取差量。

---

## 8. 核心功能实现方案

### 8.1 排行榜与筛选

- `HomeViewModel` 持有 `StateFlow<List<HardwareItem>>`，通过 `category` 和 `filter` 变化触发 API 请求。

- `LazyColumn` 展示，下拉刷新使用 `pullRefresh` (material3)。

- 筛选面板用 `ModalBottomSheet`，由 `FilterViewModel` 管理。

- 分页通过 `PagingData` + `RemoteMediator` 实现：先查 Room，再请求网络，自动加载更多。

### 8.2 详情与雷达图

- `DetailViewModel` 根据 `hardwareId` 加载详情，优先 Room。

- 雷达图用 Canvas 绘制多边形，维度包括综合、单核、多核、GPU、AI 等。

- 分数卡片 `LazyRow`，支持横向滚动。

- 趋势图（历史排名）用 `LineChart` (Vico) 展示。

### 8.3 对比功能

- `CompareViewModel` 维护已选型号 ID 列表（最多 5 个），通过 API `/compare` 获取对比数据。

- 表格：自定义 Composable，遍历规格列表，每列宽度均分，数值比较后对最高值 `Surface` 设为绿色背景，最低值红色背景。

- 柱状图：Vico `ColumnChart`，x 轴为型号，y 轴为各项跑分，支持分组柱状。

- 分享：将对比表与图表组合成 `Bitmap`，通过 `ShareCompat` 分享。

### 8.4 天梯图

- 使用 `LazyColumn` 渲染所有条目，每个条目为一个 `Box`，宽度根据 `overallScore` 占最高分比例动态计算。

- 通过 `Modifier.pointerInput` 捕获缩放手势，调整比例尺；结合 `graphicsLayer` 进行变换。

- 为确保流畅，仅绘制可见区域，内部缓存评分最大最小值。

### 8.5 搜索

- 搜索栏使用 `SearchBar` (Material3)，输入防抖 300ms。

- 先查本地 Room 名称字段的模糊匹配（`LIKE '%query%'`），同时请求网络接口，合并结果去重。

- 历史搜索记录存储于 DataStore。

### 8.6 收藏与历史

- Room DAO 提供增删查操作，收藏支持按 `groupName` 分组。

- 历史记录插入/更新，上限 100 条，超出则按 `visitedAt` 删除最早记录。

### 8.7 权重自定义

- 设置页面可调整各基准来源的权重滑块（0~100%）。

- 综合评分在客户端重新计算：归一化所有跑分，然后加权求和。

- 权重存储于 DataStore，变化后 `HomeViewModel` 对本地数据重排序，无需网络请求。

### 8.8 后台数据更新

- 使用 `WorkManager` 的 `PeriodicWorkRequest`，约束条件：网络为 Wi-Fi 且电量充足。

- Worker 内调用 API，比对 `Last-Modified`，下载增量数据并更新 Room。

- 更新完成后发送 `Notification`（可选，静默更新更友好），通知栏显示“数据已更新”。

---

## 9. 数据更新与缓存策略

- **首次启动**：若 Room 为空，则显示加载动画，下载全量 gzip JSON 数据包，解析后批量插入 Room。

- **日常更新**：WorkManager 每 6~12 小时执行一次增量同步，或应用回到前台时触发一次性检查。

- **离线降级**：所有 Repository 方法先尝试返回本地数据，若本地无数据则抛出错误并由 UI 展示“无网络”状态。

- **缓存清理**：用户可手动清除 Room 中的硬件数据（保留收藏与历史），或通过设置清除所有本地数据。

---

## 10. 测试策略

### 10.1 单元测试

- ViewModel、Repository 逻辑测试，使用 MockK 模拟依赖。

- 权重计算、排序、筛选算法测试。

- Room DAO 测试（使用 in-memory 数据库）。

### 10.2 集成测试

- Retrofit 接口模拟 (MockWebServer)，验证数据解析与错误映射。

- Room 数据库迁移测试。

### 10.3 UI 测试

- 使用 Compose Testing API 验证关键界面：排行榜项正确显示、点击跳转详情。

- 对比页面数据高亮逻辑测试。

- Espresso 用于端到端流程：启动 → 切换分类 → 搜索 → 详情 → 对比 → 分享。

### 10.4 数据准确性审计

- 人工抽样核对原始数据源，确保分数误差 < 1%。

- 综合评分算法文档化，设置页提供查看权重公式。

---

## 11. 开发计划与里程碑

### 阶段一：MVP（5 周）

- 搭建后端爬虫与 API，完成 PC CPU 数据。

- Android 项目基础架构：Hilt + Room + Retrofit + Compose 导航。

- 实现排行首页（仅 PC CPU）、详情页、搜索。

- 本地缓存与离线查看。

- CI/CD (GitHub Actions) 打包测试 APK。

### 阶段二：核心完善（5 周）

- 加入 PC GPU、手机 CPU/GPU 数据与分类切换。

- 对比功能（2~3 型号）。

- 天梯图视图（初版）。

- 收藏与历史记录模块。

- 平板/折叠屏布局优化。

### 阶段三：体验优化（4 周）

- 完整筛选面板（动态选项）。

- 自定义权重计算。

- 图表美化、动画过渡。

- 多语言框架准备。

- 分享图片生成。

### 阶段四：测试与发布（3 周）

- 全面功能测试、性能分析（Profiler）。

- 内部封闭测试 (Google Play Internal Testing) 或 Firebase App Distribution。

- 合规检查：隐私政策、数据来源声明、无障碍。

- 提交 Google Play 审核并上线。

---

## 12. 发布与维护计划

- **Google Play 上架**：
  
  - 打包为 Android App Bundle (AAB)。
  
  - 填写数据安全声明（无需账号，不收集个人信息）。
  
  - 准备截图、特性图、简短/完整说明。
  
  - 设置目标受众年龄与内容分级。

- **持续维护**：
  
  - 后端每日更新数据，客户端后台定期同步。
  
  - 关注新硬件发布，及时扩充数据库。
  
  - 用户反馈通过应用内入口（邮件或 GitHub Issues）收集。
  
  - 每两周迭代修复缺陷，每月功能更新。

---

## 13. 附录

### 13.1 主要依赖库 (build.gradle.kts)

kotlin

// Compose BOM
implementation(platform("androidx.compose:compose-bom:2024.02.00"))
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.ui:ui-tooling-preview")
// Navigation
implementation("androidx.navigation:navigation-compose:2.7.7")
// Lifecycle + ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
// Hilt
implementation("com.google.dagger:hilt-android:2.50")
kapt("com.google.dagger:hilt-compiler:2.50")
implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
// Room
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")
// Retrofit + Kotlinx Serialization
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
// Coil
implementation("io.coil-kt:coil-compose:2.5.0")
// Vico (Charts)
implementation("com.patrykandpatrick.vico:compose:1.13.1")
// WorkManager
implementation("androidx.work:work-runtime-ktx:2.9.0")
implementation("androidx.hilt:hilt-work:1.1.0")
// Testing
testImplementation("junit:junit:4.13.2")
testImplementation("io.mockk:mockk:1.13.9")
testImplementation("app.cash.turbine:turbine:0.12.3")
androidTestImplementation("androidx.compose.ui:ui-test-junit4")

### 13.2 数据源合规检查表

- 严格控制爬虫请求间隔，避免造成负担。

- 应用内显著位置标注数据来源（例如“数据来自 Geekbench Browser”）。

- 若无官方 API，仅抓取公开榜单页面，并在关于页明确声明。

- 若未来商业化，需取得数据授权或购买许可。

### 13.3 参考应用

- 安兔兔评测 (Antutu)

- Geekbench

- “手机性能排行” App

- CPU-Monkey 网站

---

**文档版本**：2.0 (Android)  
**作者**：产品与开发团队  
**日期**：2026-06-24
