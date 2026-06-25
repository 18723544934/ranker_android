# PerfTop - Android 性能排行榜 App

一个面向数码爱好者的 Android 性能排行榜应用，提供 CPU/GPU 性能数据对比、天梯图可视化等功能。

## 项目结构

```
D:\performance_ranker_android\
├── app/                                    # Android 客户端
│   ├── src/main/
│   │   ├── java/com/perftop/android/
│   │   │   ├── MainActivity.kt
│   │   │   ├── PerfTopApplication.kt
│   │   │   ├── core/              # 核心组件
│   │   │   │   ├── theme/       # Material 3 主题
│   │   │   │   └── navigation/    # 导航配置
│   │   │   ├── domain/model/       # 领域模型
│   │   │   ├── data/               # 数据层
│   │   │   │   ├── local/      # Room 数据库
│   │   │   │   ├── remote/     # Retrofit API
│   │   │   │   └── repository/  # Repository
│   │   │   └── presentation/      # 展示层
│   │   │       ├── ranking/     # 排行榜
│   │   │       ├── detail/      # 详情页
│   │   │       ├── compare/     # 对比功能
│   │   │       ├── ladder/      # 天梯图
│   │   │       ├── favorites/    # 收藏
│   │   │       └── settings/    # 设置
│   │   └── res/                    # 资源文件
│   └── build.gradle.kts
└── backend/                              # 后端服务
    ├── api/                            # FastAPI 应用
    ├── database/                        # 数据库模型
    ├── spiders/                         # Scrapy 爬虫
    ├── tasks/                           # Celery 定时任务
    └── docker-compose.yml
```

## 技术栈

### Android 客户端
- **语言**: Kotlin 1.9+
- **UI 框架**: Jetpack Compose (Material 3)
- **架构**: MVVM + Repository + Navigation Compose
- **依赖注入**: Hilt
- **持久化**: Room + DataStore
- **网络**: Retrofit2 + OkHttp3 + Kotlinx Serialization
- **图表**: Vico
- **图片加载**: Coil
- **后台任务**: WorkManager

### 后端服务
- **API 框架**: FastAPI
- **数据库**: PostgreSQL + Redis
- **爬虫**: Scrapy + Playwright
- **任务队列**: Celery + Redis
- **部署**: Docker + Docker Compose

## 快速开始

### 前置要求

#### Android 客户端
- Android Studio Hedgehog | 2023.1.1 或更高版本
- JDK 17
- Android SDK 34
- Gradle 8.2

#### 后端服务
- Python 3.11+
- Docker 和 Docker Compose
- PostgreSQL 15
- Redis 7

### 启动后端服务

```bash
cd backend

# 复制环境变量
cp .env.example .env

# 使用 Docker Compose 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

后端服务将在以下端口启动：
- API: http://localhost:8000
- API 文档: http://localhost:8000/docs
- Flower (Celery 监控): http://localhost:5555

### 启动 Android 客户端

1. 在 Android Studio 中打开项目
2. 等待 Gradle 同步完成
3. 连接 Android 设备或启动模拟器
4. 点击运行按钮

### 运行数据爬虫

```bash
cd backend/spiders

# 运行 Geekbench 爬虫
scrapy crawl geekbench

# 运行 PassMark 爬虫
scrapy crawl passmark

# 查看爬虫输出
ls output/
```

## 功能特性

### 已实现功能
- ✅ 性能排行榜（PC CPU/GPU，移动 CPU/GPU）
- ✅ 硬件详情页（规格、跑分、雷达图）
- ✅ 硬件对比功能（2-5 款对比）
- ✅ 性能天梯图（横向柱状图，支持缩放）
- ✅ 收藏功能（分组管理）
- ✅ 筛选功能（品牌、架构、核心数、年份）
- ✅ Material 3 主题（深色/浅色）
- ✅ 离线支持（Room 数据库缓存）
- ✅ 后台数据同步（WorkManager）

### 数据源
- Geekbench Browser
- PassMark CPU/GPU
- 3DMark
- 安兔兔（合规获取）

## API 端点

### 硬件列表
```
GET /v1/hardwares?category=pc_cpu&sort_by=overall_score&order=desc&page=1&per_page=20
```

### 硬件详情
```
GET /v1/hardwares/{id}
```

### 硬件对比
```
GET /v1/hardwares/compare?ids=1,2,3
```

### 搜索
```
GET /v1/hardwares/search?q=snapdragon&category=mobile_cpu
```

### 筛选选项
```
GET /v1/meta/filters?category=pc_gpu
```

## 开发指南

### 添加新功能
1. 在 `domain/model/` 中定义数据模型
2. 在 `data/local/` 中添加 Room 实体和 DAO
3. 在 `data/remote/` 中添加 API 接口
4. 在 `data/repository/` 中实现 Repository
5. 在 `presentation/` 中创建 UI 和 ViewModel
6. 更新导航配置

### 运行测试
```bash
# Android 单元测试
./gradlew test

# Android UI 测试
./gradlew connectedAndroidTest

# 后端测试
cd backend
pytest
```

## 部署

### Android App
```bash
# 生成签名 APK
./gradlew assembleRelease

# 生成 Android App Bundle
./gradlew bundleRelease
```

### 后端 API
```bash
cd backend

# 构建 Docker 镜像
docker-compose build

# 推送到容器仓库
docker-compose push
```

## 数据来源声明
本应用使用的性能数据来源于以下公开网站：
- [Geekbench Browser](https://browser.geekbench.com/)
- [PassMark](https://www.cpubenchmark.net/)
- [3DMark](https://www.3dmark.com/)

数据抓取遵守各网站的 robots.txt 规则，仅供学习和个人使用。商业使用需获得官方授权。

## 许可证
本项目仅供学习和参考使用。

## 贡献
欢迎提交 Issue 和 Pull Request！

## 联系方式
- 项目地址: https://github.com/yourusername/perftop-android
- 问题反馈: https://github.com/yourusername/perftop-android/issues
