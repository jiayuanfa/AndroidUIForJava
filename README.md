# Android 自定义UI组件库

一个功能丰富、易于使用的Android自定义UI组件库，使用Java开发，支持在Activity、Fragment和自定义View中使用。

## 📦 组件列表

本项目包含以下自定义UI组件：

| 组件 | 功能描述 | 文档 |
|------|---------|------|
| **CustomDialog** | 自定义弹窗组件，支持标题、内容、按钮自定义 | - |
| **CustomToast** | 自定义Toast组件，支持成功、错误、警告、信息四种类型 | - |
| **CustomDatePicker** | 自定义日期选择器 | - |
| **CustomSearchBox** | 自定义搜索框组件 | - |
| **RefreshRecyclerView** | 下拉刷新和上拉加载更多的RecyclerView | [技术文档](refreshrecyclerview/技术文档_刷新组件原理.md) |
| **Loading** | Loading加载组件，支持Activity/Fragment/View，自动管理内存 | [使用文档](loading/README.md) \| [技术文档](loading/技术文档_Loading组件原理.md) |
| **ProgressBar** | 进度条组件，支持线性和环形两种模式，带动画效果 | [使用文档](progressbar/README.md) \| [技术文档](progressbar/技术文档_ProgressBar组件原理.md) |
| **LazyLoadView** | ViewStub懒加载组件，演示布局懒加载最佳实践 | [使用文档](lazyloadview/README.md) \| [技术文档](lazyloadview/技术文档_ViewStub懒加载最佳实践.md) |

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone <项目地址>
cd AndroidUIForJava
```

### 2. 打开项目

使用Android Studio打开项目，等待Gradle同步完成。

### 3. 运行示例

直接运行`app`模块，在主页面可以体验所有组件的功能。

## 📖 组件使用指南

### Loading组件

**功能：** 在任意Activity、Fragment或View中显示loading效果

**特点：**
- ✅ 一句话显示/隐藏
- ✅ 自动适应大小
- ✅ 自动管理内存，防止泄漏
- ✅ 页面不可见时自动隐藏

**使用示例：**
```java
// 在Activity中
Loading.show(this, "加载中...");
Loading.hide();

// 在Fragment中
Loading.show(this);
Loading.hide();

// 在自定义View中
Loading.show(myView);
Loading.hide();
```

**详细文档：** [Loading组件文档](loading/README.md)

### ProgressBar组件

**功能：** 显示任务进度的进度条组件

**特点：**
- ✅ 支持线性和环形两种模式
- ✅ 进度值范围：0-1（0表示0%，1表示100%）
- ✅ 支持平滑动画效果
- ✅ 支持从XML设置初始进度
- ✅ 默认进度：25%

**使用示例：**
```xml
<!-- XML中使用 -->
<com.example.progressbar.CustomProgressBar
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:progress="0.5"
    app:progressMode="linear" />
```

```java
// 代码中使用
CustomProgressBar progressBar = findViewById(R.id.progress_bar);
progressBar.setProgress(0.75f);  // 设置到75%，带动画
progressBar.setProgressMode(CustomProgressBar.MODE_CIRCULAR);  // 切换到环形
```

**详细文档：** [ProgressBar组件文档](progressbar/README.md)

### LazyLoadView组件

**功能：** 使用ViewStub实现布局懒加载，提升应用性能

**特点：**
- ✅ 使用ViewStub延迟加载布局，减少初始内存占用
- ✅ 支持代码动态创建布局并懒加载
- ✅ 自动防止重复加载，确保ViewStub只加载一次
- ✅ 提供完整的生命周期日志

**使用示例：**
```java
LazyLoadCardView cardView = findViewById(R.id.card_view);
cardView.setTitle("我的卡片");

// 懒加载内容区域（只有在需要时才加载）
cardView.loadContent();

// 懒加载操作区域（可选）
cardView.loadActions();

// 代码动态加载布局
cardView.loadContentFromCode(R.layout.my_custom_layout);
```

**详细文档：** [LazyLoadView组件文档](lazyloadview/README.md)

### CustomDialog组件

**功能：** 自定义弹窗组件

**使用示例：**
```java
new CustomDialog(this)
    .setTitle("提示")
    .setMessage("这是一个自定义弹窗")
    .setPositiveButton("确定", (dialog, which) -> {
        // 确定按钮点击事件
    })
    .setNegativeButton("取消", (dialog, which) -> {
        // 取消按钮点击事件
    })
    .show();
```

### CustomToast组件

**功能：** 自定义Toast，支持多种类型

**使用示例：**
```java
// 成功提示
CustomToast.showSuccess(this, "操作成功！");

// 错误提示
CustomToast.showError(this, "操作失败！");

// 信息提示
CustomToast.showInfo(this, "这是一条信息");

// 警告提示
CustomToast.showWarning(this, "请注意！");
```

### CustomDatePicker组件

**功能：** 自定义日期选择器

**使用示例：**
```java
new CustomDatePicker(this)
    .setOnDateSelectedListener((year, month, dayOfMonth) -> {
        String date = String.format("%d年%d月%d日", year, month, dayOfMonth);
        // 处理选择的日期
    })
    .show();
```

### CustomSearchBox组件

**功能：** 自定义搜索框

**使用示例：**
```xml
<com.example.searchbox.CustomSearchBox
    android:id="@+id/search_box"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

```java
CustomSearchBox searchBox = findViewById(R.id.search_box);
searchBox.setOnSearchListener(keyword -> {
    // 处理搜索
});
```

### RefreshRecyclerView组件

**功能：** 支持下拉刷新和上拉加载更多的RecyclerView

**详细文档：** [刷新组件文档](refreshrecyclerview/技术文档_刷新组件原理.md)

## 🏗️ 项目结构

```
AndroidUIForJava/
├── app/                    # 示例应用
│   ├── src/main/java/     # 测试Activity和Fragment
│   └── src/main/res/      # 测试页面布局
├── customdialog/          # 自定义弹窗组件
├── customtoast/           # 自定义Toast组件
├── datepicker/            # 日期选择器组件
├── searchbox/             # 搜索框组件
├── refreshrecyclerview/   # 刷新RecyclerView组件
├── loading/               # Loading组件
│   ├── README.md          # 使用文档
│   └── 技术文档_Loading组件原理.md
└── progressbar/           # 进度条组件
    ├── README.md          # 使用文档
    └── 技术文档_ProgressBar组件原理.md
```

## 🔧 技术栈

- **语言：** Java
- **最低SDK：** 24 (Android 7.0)
- **目标SDK：** 36
- **构建工具：** Gradle
- **依赖管理：** Gradle Version Catalog

## 📝 依赖说明

所有组件都是独立的模块，可以根据需要选择性引入：

```gradle
dependencies {
    // 按需引入组件
    implementation project(':loading')
    implementation project(':progressbar')
    implementation project(':customdialog')
    // ... 其他组件
}
```

## 🎯 设计原则

1. **简单易用**：提供简洁的API，一行代码即可使用
2. **功能完整**：每个组件都经过精心设计，功能完善
3. **内存安全**：自动管理资源，防止内存泄漏
4. **文档完善**：提供详细的使用文档和技术文档
5. **易于扩展**：代码结构清晰，易于二次开发

## 📚 文档导航

### 组件文档

- [Loading组件 - 使用文档](loading/README.md)
- [Loading组件 - 技术文档](loading/技术文档_Loading组件原理.md)
- [ProgressBar组件 - 使用文档](progressbar/README.md)
- [ProgressBar组件 - 技术文档](progressbar/技术文档_ProgressBar组件原理.md)
- [RefreshRecyclerView组件 - 技术文档](refreshrecyclerview/技术文档_刷新组件原理.md)

### 快速查找

- **想快速上手？** → 查看各组件的README.md
- **想深入了解原理？** → 查看各组件的技术文档
- **想看代码示例？** → 运行app模块，查看测试页面

## 🧪 测试

项目包含完整的测试页面，运行app模块后可以：

1. 在主页面体验所有组件
2. 进入各个组件的专门测试页面
3. 查看不同场景下的使用效果

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📄 许可证

本项目采用MIT许可证。

---

**希望这个组件库能帮助你快速开发Android应用！** 🎉
