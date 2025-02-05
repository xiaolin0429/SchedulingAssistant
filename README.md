# 排班助手 (Scheduling Assistant)

## 项目简介
排班助手是一款专为需要倒班工作的人员设计的Android应用。它提供了直观的排班管理、闹钟提醒和数据统计功能，帮助用户更好地管理自己的工作时间。

## 核心功能
- 班次管理：创建、编辑和删除班次
- 闹钟提醒：为不同班次设置闹钟
- 数据统计：查看排班统计和分析
- 个性化设置：自定义班次颜色和提醒方式

## 技术架构

### 目录结构
```
app/src/main/
├── java/com/schedule/assistant/
│   ├── data/
│   │   ├── dao/          # 数据访问对象
│   │   ├── entity/       # 数据实体类
│   │   ├── repository/   # 数据仓库
│   │   └── converter/    # 类型转换器
│   ├── ui/
│   │   ├── adapter/      # 列表适配器
│   │   ├── alarm/        # 闹钟管理
│   │   ├── calendar/     # 日历组件
│   │   ├── dialog/       # 对话框
│   │   ├── home/         # 主页
│   │   ├── profile/      # 个人中心
│   │   ├── shift/        # 班次管理
│   │   └── stats/        # 统计分析
│   ├── viewmodel/        # 视图模型
│   └── util/             # 工具类
├── res/
│   ├── layout/          # 布局文件
│   ├── drawable/        # 图形资源
│   ├── values/          # 资源值
│   └── navigation/      # 导航图
└── AndroidManifest.xml
```

### 系统架构图
```
┌──────────────────────────────── Application Layer ────────────────────────────────┐
│                                                                                   │
│  ┌────────────────────────────────── UI Layer ──────────────────────────────────┐ │
│  │                                                                              │ │
│  │  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    │ │
│  │  │HomeFragment │    │ShiftFragment│    │AlarmFragment│    │StatsFragment│    │ │
│  │  │  Adapter    │    │  Adapter    │    │  Adapter    │    │  Adapter    │    │ │
│  │  │  Binding    │    │  Binding    │    │  Binding    │    │  Binding    │    │ │
│  │  └──────┬──────┘    └──────┬──────┘    └──────┬──────┘    └──────┬──────┘    │ │
│  │         │                  │                   │                   │         │ │
│  └─────────┼──────────────────┼───────────────────┼───────────────────┼─────────┘ │
│            │                  │                   │                   │           │
│  ┌─────────┼──────────────────┼───────────────────┼───────────────────┼─────────┐ │
│  │         │           ViewModel Layer            │                   │         │ │
│  │         ▼                  ▼                   ▼                   ▼         │ │
│  │  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    │ │
│  │  │   HomeVM    │    │   ShiftVM   │    │   AlarmVM   │    │   StatsVM   │    │ │
│  │  │  LiveData   │    │  LiveData   │    │  LiveData   │    │  LiveData   │    │ │
│  │  │  Coroutine  │    │  Coroutine  │    │  Coroutine  │    │  Coroutine  │    │ │
│  │  └──────┬──────┘    └──────┬──────┘    └──────┬──────┘    └──────┬──────┘    │ │
│  │         │                  │                   │                   │         │ │
│  └─────────┼──────────────────┼───────────────────┼───────────────────┼─────────┘ │
│            │                  │                   │                   │           │
│  ┌─────────┼──────────────────┼───────────────────┼───────────────────┼─────────┐ │
│  │         │           Repository Layer           │                   │         │ │
│  │         ▼                  ▼                   ▼                   ▼         │ │
│  │  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    │ │
│  │  │ ShiftRepo   │    │  AlarmRepo  │    │  StatsRepo  │    │  UserRepo   │    │ │
│  │  │   Cache     │    │   Cache     │    │   Cache     │    │   Cache     │    │ │
│  │  │   Sync      │    │   Sync      │    │   Sync      │    │   Sync      │    │ │
│  │  └──────┬──────┘    └──────┬──────┘    └──────┬──────┘    └──────┬──────┘    │ │
│  │         │                  │                   │                   │         │ │
│  └─────────┼──────────────────┼───────────────────┼───────────────────┼─────────┘ │
│            │                  │                   │                   │           │
│  ┌─────────┼──────────────────┼───────────────────┼───────────────────┼─────────┐ │
│  │         │           Data Source Layer          │                   │         │ │
│  │         ▼                  ▼                   ▼                   ▼         │ │
│  │  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    │ │
│  │  │   Room DB   │    │   Shared    │    │    File     │    │   Remote    │    │ │
│  │  │   Entity    │    │   Prefs     │    │   System    │    │    API      │    │ │
│  │  │    DAO      │    │  Settings   │    │  Storage    │    │  Service    │    │ │
│  │  └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘    │ │
│  │                                                                              │ │
│  └──────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                   │
└───────────────────────────────────────────────────────────────────────────────────┘
```

### 数据流向图
```
数据流向：
┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│     UI       │ -> │  ViewModel   │ -> │  Repository  │ -> │ Data Source  │
│    Layer     │    │    Layer     │    │    Layer     │    │    Layer     │
└──────────────┘    └──────────────┘    └──────────────┘    └──────────────┘
       ▲                   │                   │                   │
       │                   │                   │                   │
       └───────────────────┴───────────────────┴───────────────────┘
                LiveData观察            异步数据流

数据处理流程：
1. UI Layer (表现层)
   ├─ 用户操作触发事件
   ├─ 调用ViewModel方法
   └─ 观察LiveData变化

2. ViewModel Layer (视图模型层)
   ├─ 处理UI逻辑和状态管理
   ├─ 调用Repository方法
   └─ 通过LiveData更新UI

3. Repository Layer (数据仓库层)
   ├─ 实现数据缓存策略
   ├─ 协调多数据源访问
   └─ 处理数据同步逻辑

4. Data Source Layer (数据源层)
   ├─ Room数据库操作
   ├─ SharedPreferences配置
   ├─ 文件系统访问
   └─ 远程API调用(预留)
```

### 架构说明
1. **UI Layer（表现层）**
   - Fragment：页面展示和用户交互
   - Adapter：列表数据适配
   - ViewBinding：视图绑定
   - 采用单Activity多Fragment结构

2. **ViewModel Layer（视图模型层）**
   - 管理UI状态和数据
   - 处理业务逻辑
   - 使用LiveData实现数据观察
   - 保证配置变更时的数据存活

3. **Repository Layer（数据仓库层）**
   - 统一的数据访问接口
   - 数据来源的协调和调度
   - 实现数据缓存策略
   - 处理数据转换和业务规则

4. **Data Source Layer（数据源层）**
   - Room：本地数据库存储
   - SharedPreferences：配置信息
   - FileSystem：文件存储
   - RemoteAPI：网络数据（预留）

### 关键特性
- 单向数据流
- 关注点分离
- 依赖注入
- 响应式编程
- 可测试性设计

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

### 技术实现细节

#### 1. 数据流转与状态管理
```
数据流转示意：
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   View      │     │  ViewModel  │     │ Repository  │
│  (UI状态)    │ ←──  │  (数据转换) │ ←── │ (数据获取)   │
└─────────────┘     └─────────────┘     └─────────────┘
      │                    ↑                   ↑
      │                    │                   │
      └────────────────────┘                   │
         用户操作触发        数据库/网络/文件操作   │
                           ↑                   │
                           └───────────────────┘
```

#### 2. 关键组件实现
- **数据绑定机制**
  ```java
  // ViewModel：管理UI状态和数据
  public class HomeViewModel extends AndroidViewModel {
      private final MutableLiveData<List<ShiftTypeEntity>> shiftTypes = new MutableLiveData<>();
      private final ShiftTypeRepository shiftTypeRepository;

      public HomeViewModel(Application application) {
          super(application);
          shiftTypeRepository = new ShiftTypeRepository(application);
      }

      public LiveData<List<ShiftTypeEntity>> getShiftTypes() {
          return shiftTypeRepository.getAllShiftTypes();
      }
  }

  // Fragment：观察数据变化并更新UI
  public class HomeFragment extends Fragment {
      private HomeViewModel viewModel;
      private FragmentHomeBinding binding;

      @Override
      public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
          super.onViewCreated(view, savedInstanceState);
          viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

          // 观察数据变化
          viewModel.getShiftTypes().observe(getViewLifecycleOwner(), shiftTypes -> {
              if (shiftTypes != null) {
                  updateShiftTypeDisplay(shiftTypes);
              }
          });
      }

      // UI更新方法
      private void updateShiftTypeDisplay(List<ShiftTypeEntity> shiftTypes) {
          // 更新UI显示
          binding.recyclerView.setAdapter(new ShiftTypeAdapter(shiftTypes));
      }
  }
  ```

- **异步操作处理**
  ```java
  // Repository中的协程使用
  public class ShiftRepository {
      public LiveData<List<Shift>> getShifts() {
          return shiftDao.getAllShifts();  // Room自动在后台线程执行
      }
      
      public void insertShift(Shift shift) {
          executor.execute(() -> shiftDao.insert(shift));
      }
  }
  ```

#### 3. 模块间通信
```
┌─────────────┐   Event Bus   ┌─────────────┐
│  Fragment A │ ────────────> │  Fragment B │
└─────────────┘               └─────────────┘
      ↑                            ↑
      │                            │
      └────────────────────────────┘
           共享ViewModel通信

通信方式：
1. LiveData观察
2. SharedViewModel共享
3. Navigation组件传参
4. EventBus消息
```

#### 4. 数据缓存策略
```
┌─────────────┐
│  内存缓存     │ ← 首选
└─────────────┘
       ↓
┌─────────────┐
│  本地数据库   │ ← 持久化
└─────────────┘
       ↓
┌─────────────┐
│  网络请求     │ ← 备选
└─────────────┘

缓存策略：
1. 优先使用内存缓存
2. 内存缓存失效时访问数据库
3. 必要时请求网络更新
```

#### 5. 错误处理机制
```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   异常发生   │ ──>  │  错误转换    │ ──> │  UI展示     │
└─────────────┘     └─────────────┘     └─────────────┘
                          ↓
                    ┌─────────────┐
                    │  日志记录    │
                    └─────────────┘

错误处理流程：
1. 异常捕获与转换
2. 错误信息本地化
3. 用户友好提示
4. 日志记录追踪
```

#### 6. 性能优化策略
```
性能优化要点：
┌────────────────────┐
│     UI渲染优化      │
├────────────────────┤
│ - ViewHolder复用   ｜
│ - 布局层级优化       │
│ - 按需加载          │
└────────────────────┘

┌────────────────────┐
│     数据加载优化     │
├────────────────────┤
│ - 分页加载          │
│ - 预加载机制         │
│ - 缓存策略          │
└────────────────────┘

┌────────────────────┐
│     内存优化        │
├────────────────────┤
│ - 内存泄漏监控       │
│ - 大对象延迟加载     │
│ - 及时释放资源       │
└────────────────────┘
```

### ✅ 已完成功能
- ✅ 项目基础架构搭建
  - MVVM架构实现
  - Navigation导航框架集成
  - ViewBinding实现
  - 依赖注入框架集成
  - 数据库迁移到版本5
  - 异常处理机制完善

- ✅ 数据层实现
  - Room数据库配置
  - 实体类定义（ShiftType, ShiftTemplate, Alarm等）
  - DAO接口实现（ShiftTypeDao, ShiftTemplateDao, AlarmDao）
  - Repository模式封装
  - 数据转换器实现
  - 缓存策略优化

- ✅ UI层实现
  - Fragment基类封装
  - 自定义View组件（CustomRecyclerView等）
  - Material Design 3规范应用
  - 响应式布局适配
  - ViewBinding集成
  - 列表适配器优化

- ✅ 班次管理模块
  - 班次类型CRUD操作
  - 班次时间更新功能
  - 班次详情查看
  - 班次模板功能
  - 数据库存储和查询优化
  - 列表项交互优化
  - 自定义颜色支持
  - 数据验证机制

- ✅ 个人信息模块
  - 用户信息管理（头像、昵称）
  - 应用设置（主题切换、语言设置、通知设置）
  - 数据管理（备份恢复、缓存清理）
  - 帮助与反馈（FAQ、反馈、日志导出）
  - 关于应用（版本信息、开源许可、开发者信息）
  - 多语言支持

### 🚧 开发中功能
- 🔄 闹钟提醒模块 (进度：80%)
  - ✅ 基础闹钟功能（创建、管理、编辑、删除）
  - ✅ 系统闹钟集成（权限管理、系统调度）
  - ✅ 异常处理机制
  - ✅ 数据管理和存储
  - ✅ 用户界面基础实现
  - ✅ 性能优化和兼容性适配
  - 🔄 重复提醒设置
  - 🔄 铃声选择功能
  - ⏳ 贪睡功能
  - ⏳ 智能防打扰
  - ⏳ 渐进式音量增加
  - ⏳ 与排班功能深度集成

- 🔄 统计模块 (进度：70%)
  - ✅ 基础统计（按月份统计班次数量、工作时长统计）
  - ✅ 数据可视化（班次分布饼图、工作时长柱状图）
  - ✅ 时间筛选（月份选择、自定义日期范围、快速选择）
  - ✅ 性能优化和用户体验优化
  - ✅ 自定义统计维度
  - ✅ 多维度数据分析
  - ⏳ 班次详细列表
  - ⏳ 数据导出功能
  - ⏳ 工作强度分析
  - ⏳ 班次规律分析
  - ⏳ 预测分析支持
  - ⏳ PDF报告生成

### 📋 待开发功能

#### ⏰ Phase 4: 闹钟管理功能扩展
- 🔄 [TASK-4.3] 闹钟高级功能
  - 🔄 自定义铃声
  - 🔄 振动模式配置
  - 🔄 贪睡功能
  - ⏳ 智能唤醒
  - ⏳ 节假日适配
  - ⏳ 班次智能关联

#### 📊 Phase 5: 统计功能扩展
- 🔄 [TASK-5.3] 高级统计功能
  - ⏳ 班次详细列表
  - ⏳ 数据导出功能
  - ⏳ 工作强度分析
  - ⏳ 班次规律分析
  - ⏳ 预测分析支持
  - ⏳ PDF报告生成

#### ⚙️ Phase 6: 设置中心
- ⏳ [TASK-6.1] 设置界面扩展
  - 自定义主题
  - 数据同步设置
  - 安全设置
  - 性能优化选项
  - 开发者选项

#### 🤖 Phase 7: 自动排班
- ⏳ [TASK-7.1] 自动排班功能
  - 自定义排班规则
    - 按天重复（每天、隔天等）
    - 按周重复（每周一/二/三等）
    - 按月重复（每月指定日期）
  - 规则优先级设置
  - 规则生效时间范围
  - 规则例外日期设置
  - 规则冲突检测
  - 批量应用规则
  - 规则模板管理
  - 一键清除规则

#### 🔧 Phase 8: 性能优化与测试
- ⏳ [TASK-8.1] 性能优化
  - 启动速度优化
  - 内存使用优化
  - 电池使用优化
  - 存储空间优化
  - 网络请求优化

- ⏳ [TASK-8.2] 测试完善
  - 单元测试覆盖
  - UI自动化测试
  - 性能测试
  - 兼容性测试
  - 安全性测试

#### 📋 Phase 9: 发布与运维
- ⏳ [TASK-9.1] 应用发布
  - 版本检查更新
  - 增量更新支持
  - 应用市场发布
  - 渠道包构建
  - 发布流程优化

- ⏳ [TASK-9.2] 运维支持
  - 错误日志收集
  - 用户反馈系统
  - 使用统计分析
  - 远程配置更新
  - 在线支持服务

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
git clone https://github.com/xiaolin0429/SchedulingAssistant.git
```

2. 使用Android Studio打开项目

3. 同步Gradle依赖
```bash
./gradlew build
```

## 开发规范
1. 代码规范
   - 遵循Java代码规范
   - 使用统一的代码格式化工具
   - 保持代码简洁清晰
   - 遵守最小修改原则，不允许随意修改现有代码
   - 需要修改现有功能时，优先使用组合/聚合/依赖注入等设计模式
   - 保留原有方法/类/接口的功能，确保向后兼容
   - 按照设计模式思想，遵守六大原则：开闭原则、里氏代换原则、依赖倒转原则、接口隔离原则、迪米特法则、合成复用原则
2. 资源文件修改规范
   - 修改资源文件前必须全局检查引用关系
   - 确保修改不影响其他模块的正常调用
   - 对于共用资源，需要评估修改影响范围
   - 建议新增资源而不是修改现有资源
   - 必要的修改需要在代码审查时重点关注
   - 修改资源文件时，需要检查是否存在类似可重复资源，如果存在，复用现有资源
   - 注意en和zh双语言的资源文件版本，确保两者一致，默认strings.xml文件为英文
3. 命名规范
   - 类名：大驼峰命名法
   - 变量名：小驼峰命名法
   - 常量：全大写下划线分隔
4. 注释规范
   - 类和方法必须添加文档注释
   - 关键代码添加必要的注释
   - 保持注释的及时更新
5. 版本控制
   - 使用Git进行版本控制
   - 遵循分支管理规范
   - 提交信息要清晰明确

### 作者
- 开发团队：[xiaolin0429]
- 联系邮箱：[yulin0429@foxmail.com]

### 许可证
本项目采用 MIT 许可证 
