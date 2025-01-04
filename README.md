# 排班助手 (Scheduling Assistant)

## 项目简介
排班助手是一款专为需要倒班工作的人员设计的Android应用。它提供了直观的排班管理、闹钟提醒和数据统计功能，帮助用户更好地管理自己的工作时间。

## 核心功能
- 班次管理：创建、编辑和删除班次
- 闹钟提醒：为不同班次设置闹钟
- 数据统计：查看排班统计和分析
- 个性化设置：自定义班次颜色和提醒方式

## 技术架构

### 基础框架
- 开发语言：Java 17
- 最低SDK：Android 7.0 (API 24)
- 目标SDK：Android 14 (API 34)
- 构建工具：Gradle 8.2.0

### 架构模式
- MVVM (Model-View-ViewModel)架构
- Repository模式数据管理
- 单Activity多Fragment结构
- LiveData响应式编程

### 数据层
- SQLite数据库
  - Room持久化库
  - DAO数据访问模式
  - 数据迁移策略
- SharedPreferences配置存储
- 文件存储系统

### UI组件
- AndroidX组件库
- Material Design 3
- RecyclerView + ListAdapter
- ViewBinding视图绑定
- CalendarView日历控件
- ConstraintLayout布局
- Navigation导航组件

### 功能组件
- WorkManager任务调度
- AlarmManager闹钟服务
- NotificationManager通知管理
- MPAndroidChart图表展示
- FileProvider文件共享

### 工具支持
- Kotlin协程（Java互操作）
- Lifecycle组件
- ViewPager2页面切换
- SwipeRefreshLayout下拉刷新
- BottomSheetDialog底部弹窗

### 测试框架
- JUnit单元测试
- Espresso UI测试
- Mockito模拟测试
- AndroidX Test测试支持库

## 开发进度

#### 已完成功能
- TASK-1.1 项目初始化与基础配置 (100%)
- TASK-1.2 数据库设计与实现 (100%)
- TASK-2.1 底部导航栏布局实现 (100%)
- TASK-2.2 导航栏模块基础功能 (100%)

#### 开发中功能
- 🔄 日历标记显示 (预计完成时间: 2025-01-10)
- 🔄 班次统计功能 (预计完成时间: 2025-01-15)
- 🔄 闹钟提醒功能 (预计完成时间: 2025-01-20)

#### 待开发功能
- TASK-4.2 班次操作功能
- TASK-4.3 班次排序功能
- TASK-5.1 闹钟管理布局
- TASK-5.2 设置闹钟功能
- TASK-5.3 编辑与删除闹钟
- TASK-6.1 统计界面布局
- TASK-6.2 日期范围选择与图表
- TASK-6.3 排班详情显示
- TASK-7.1 设置界面布局
- TASK-7.2 通知设置功能
- TASK-7.3 日志提取功能
- TASK-8.1 性能优化
- TASK-8.2 代码重构与清理
- TASK-8.3 单元测试与UI测试
- TASK-9.1 数据加密实现
- TASK-9.2 通信安全保障
- TASK-10.1 应用打包与发布
- TASK-10.2 用户反馈与改进

## 项目结构
```
app/
├── build.gradle                  # 应用级构建配置
├── proguard-rules.pro           # 混淆规则配置
└── src/
    ├── main/                    # 主要源代码
    │   ├── java/
    │   │   └── com/schedule/assistant/
    │   │       ├── data/           # 数据层
    │   │       │   ├── dao/        # 数据访问对象
    │   │       │   ├── entity/     # 数据实体类
    │   │       │   ├── repository/ # 数据仓库
    │   │       │   └── database/   # 数据库配置
    │   │       ├── ui/             # 界面层
    │   │       │   ├── activity/   # Activity组件
    │   │       │   ├── fragment/   # Fragment组件
    │   │       │   ├── adapter/    # RecyclerView适配器
    │   │       │   ├── dialog/     # 对话框组件
    │   │       │   └── viewmodel/  # ViewModel组件
    │   │       ├── service/        # 服务组件
    │   │       │   ├── alarm/      # 闹钟服务
    │   │       │   └── notification/# 通知服务
    │   │       └── util/           # 工具类
    │   │           ├── extension/  # 扩展函数
    │   │           └── helper/     # 辅助工具
    │   └── res/                    # 资源文件
    │       ├── layout/            # 布局文件
    │       ├── drawable/          # 图形资源
    │       ├── values/            # 常量资源
    │       ├── navigation/        # 导航图
    │       ├── menu/             # 菜单资源
    │       └── xml/              # XML配置
    ├── androidTest/               # UI测试
    │   └── java/
    │       └── com/schedule/assistant/
    │           ├── ui/           # UI测试用例
    │           └── util/         # 测试工具
    └── test/                     # 单元测试
        └── java/
            └── com/schedule/assistant/
                ├── data/         # 数据层测试
                ├── viewmodel/    # ViewModel测试
                └── util/         # 工具类测试
```

## 开发进度

### ✅ 已完成功能
- ✅ 项目基础架构搭建
- ✅ 数据库设计与实现
- ✅ 主页面布局实现
- ✅ 基础班次管理功能
- ✅ 日历标记显示
- ✅ 班次统计功能
- ✅ 主页排班信息展示

### 🚧 开发中功能
- 🔄 闹钟提醒功能 (预计完成时间: 2025-01-20)
- 🔄 班次管理布局与RecyclerView (预计完成时间: 2025-01-25)

#### 📱 Phase 2: 主页功能：日历与排班显示
- ✅ [TASK-2.1] 主页布局
  - 顶部标题与统计区域
  - 日历视图区域
  - 底部排班信息与操作按钮
- ✅ [TASK-2.2] 排班数据展示
  - 日历标记功能
  - 班次统计显示
  - 当日排班信息
- ✅ [TASK-2.3] 日期与排班切换
  - 日期选择交互
  - 排班信息更新
  - 视图状态管理

### 📋 待开发功能清单

#### 📦 Phase 1: 项目初始化与基础搭建
- ✅ [TASK-1.1] 项目初始化
  - Android项目创建
  - Gradle配置
  - 依赖库集成
- ✅ [TASK-1.2] 数据库设计与实现
  - SQLite表设计
  - Room实体类创建
  - DAO接口实现

#### 📱 Phase 3: 班次管理功能 (预计完成: 2025-01-25)
- 🔄 [TASK-3.1] 班次管理布局与RecyclerView
- ⏳ [TASK-3.2] 班次操作功能
- ⏳ [TASK-3.3] 班次排序功能

#### ⏰ Phase 4: 闹钟管理功能 (预计完成: 2025-02-05)
- ⏳ [TASK-4.1] 闹钟管理布局
- ⏳ [TASK-4.2] 设置闹钟功能
- ⏳ [TASK-4.3] 编辑与删除闹钟

#### 📊 Phase 5: 排班统计 (预计完成: 2025-02-15)
- ⏳ [TASK-5.1] 统计界面布局
- ⏳ [TASK-5.2] 日期范围选择与图表更新
- ⏳ [TASK-5.3] 排班详情显示

#### ⚙️ Phase 6: 我的设置 (预计完成: 2025-02-25)
- ⏳ [TASK-6.1] 设置界面布局
- ⏳ [TASK-6.2] 通知设置功能
- ⏳ [TASK-6.3] 日志提取功能

#### 🔧 Phase 7: 性能优化与测试 (预计完成: 2025-03-05)
- ⏳ [TASK-7.1] 性能优化
  - RecyclerView优化
  - 数据库查询优化
  - UI渲染优化
- ⏳ [TASK-7.2] 代码重构与清理
- ⏳ [TASK-7.3] 单元测试与UI测试

#### 🔒 Phase 8: 安全与数据保护 (预计完成: 2025-03-15)
- ⏳ [TASK-8.1] 数据加密
- ⏳ [TASK-8.2] 通信安全

#### 🚀 Phase 9: 发布与部署 (预计完成: 2025-03-25)
- ⏳ [TASK-9.1] 应用打包与发布
- ⏳ [TASK-9.2] 用户反馈与改进

### 任务状态说明
- ✅ 已完成
- 🔄 开发中
- ⏳ 待开发
- ❌ 已取消

## 快速开始

### 环境要求
- Android Studio Hedgehog | 2023.1.1 或更高版本
- JDK 17 或更高版本
- Android SDK Platform 34
- Android Build Tools v34.0.0

### 安装步骤
1. 克隆项目到本地
```bash
git clone https://github.com/yourusername/SchedulingAssistant.git
```

2. 使用Android Studio打开项目

3. 同步Gradle依赖
```bash
./gradlew build
```

4. 运行项目
- 选择目标设备（真机或模拟器）
- 点击运行按钮或使用快捷键`Shift + F10`

## 使用指南
1. 首次启动
   - 授予必要权限
   - 查看功能引导
   
2. 基本操作
   - 点击日期查看/编辑班次
   - 左右滑动切换月份
   - 使用底部导航切换功能
   
3. 高级功能
   - 长按日期进行快速编辑
   - 使用筛选器查找特定班次
   - 导出统计数据

## 开发规范
1. 代码规范
   - 遵循Java代码规范
   - 使用统一的代码格式化工具
   - 保持代码简洁清晰

2. 命名规范
   - 类名：大驼峰命名法
   - 变量名：小驼峰命名法
   - 常量：全大写下划线分隔

3. 注释规范
   - 类和方法必须添加文档注释
   - 关键代码添加必要的注释
   - 保持注释的及时更新

4. 版本控制
   - 使用Git进行版本控制
   - 遵循分支管理规范
   - 提交信息要清晰明确

### 最近更新
1. 完成项目初始化和基础框架搭建
2. 实现底部导航栏和基本页面切换
3. 开始主页日历功能开发
4. 进行班次管理模块的开发

### 下一步计划
1. 完成主页日历的排班展示功能
2. 实现班次的添加、编辑和删除功能
3. 开发闹钟管理模块
4. 进行数据统计功能的开发

### 贡献指南
1. Fork 项目仓库
2. 创建功能分支
3. 提交代码更改
4. 发起合并请求

### 版本历史
- v0.1.0 (2025-01-04)
  - 项目初始化
  - 基础框架搭建
  - 底部导航实现

### 作者
- 开发团队：[团队名称]
- 联系邮箱：[邮箱地址]

### 许可证
本项目采用 MIT 许可证 