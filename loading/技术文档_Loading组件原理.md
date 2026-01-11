# Loading组件技术文档 - 实现原理与内存泄漏防护

## 📋 目录
1. [架构设计](#架构设计)
2. [核心实现原理](#核心实现原理)
3. [内存泄漏防护机制](#内存泄漏防护机制)
4. [生命周期监控](#生命周期监控)
5. [尺寸自适应算法](#尺寸自适应算法)
6. [代码示例分析](#代码示例分析)

---

## 架构设计

### 三层架构

```
┌─────────────────────────────────────┐
│         Loading (工具类)             │
│  提供简单的静态方法接口              │
│  show() / hide() / release()        │
└──────────────┬──────────────────────┘
               │
               ↓
┌─────────────────────────────────────┐
│      LoadingManager (管理器)         │
│  管理显示/隐藏逻辑                    │
│  生命周期监听                         │
│  资源管理                             │
└──────────────┬──────────────────────┘
               │
               ↓
┌─────────────────────────────────────┐
│      LoadingView (自定义View)        │
│  实际的UI界面                        │
│  尺寸计算                            │
│  动画显示                            │
└─────────────────────────────────────┘
```

### 类职责划分

| 类名 | 职责 | 关键方法 |
|------|------|----------|
| **Loading** | 提供静态方法接口，管理LoadingManager实例 | `show()`, `hide()`, `release()` |
| **LoadingManager** | 管理loading的显示/隐藏，监听生命周期 | `show()`, `hide()`, `registerLifecycleListener()` |
| **LoadingView** | 自定义View，负责UI渲染和尺寸计算 | `calculateAndSetSizes()`, `forceRecalculateSize()` |

---

## 核心实现原理

### 1. Manager复用机制

**问题：** 为什么需要复用Manager？

**原因：** 如果每次`show()`都创建新的Manager，会导致：
- 旧的loading view可能还在显示
- 新的Manager无法控制旧的view
- 造成资源浪费

**解决方案：** 使用WeakHashMap存储target和Manager的映射关系

```java
private static WeakHashMap<Object, LoadingManager> managerMap = new WeakHashMap<>();

private static LoadingManager getOrCreateManager(Object target) {
    // 先检查是否已存在
    LoadingManager manager = managerMap.get(target);
    if (manager != null && isManagerValid(manager, target)) {
        return manager;  // 复用
    }
    
    // 创建新的
    manager = LoadingManager.with(target);
    managerMap.put(target, manager);
    return manager;
}
```

**优势：**
- ✅ 同一个target总是使用同一个Manager
- ✅ 自动管理Manager的生命周期（WeakHashMap）
- ✅ 避免重复创建

### 2. 父容器获取逻辑

**关键代码：**
```java
private ViewGroup getParentView(Object target) {
    if (target instanceof Activity) {
        // 获取Activity的content view
        ViewGroup contentView = decorView.findViewById(android.R.id.content);
        return contentView;
    } else if (target instanceof Fragment) {
        // Fragment的view本身或父容器
        View view = fragment.getView();
        if (view instanceof ViewGroup) {
            return (ViewGroup) view;
        }
        return (ViewGroup) view.getParent();
    } else if (target instanceof View) {
        // 如果target是ViewGroup，直接使用
        if (view instanceof ViewGroup) {
            return (ViewGroup) view;
        }
        // 否则使用父容器
        return (ViewGroup) view.getParent();
    }
    return null;
}
```

**设计要点：**
- Activity：使用content view（排除ActionBar等）
- Fragment：优先使用Fragment的view本身
- ViewGroup：直接使用target本身
- View：使用target的父容器

### 3. 尺寸计算时机

**问题：** 如何确保每次显示时尺寸都正确？

**解决方案：** 多重保障机制

```java
// 1. onSizeChanged - 尺寸变化时
@Override
protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    if (w > 0 && h > 0) {
        calculateAndSetSizes(w, h);
    }
}

// 2. onAttachedToWindow - 添加到窗口时
@Override
protected void onAttachedToWindow() {
    post(() -> {
        if (getWidth() > 0 && getHeight() > 0) {
            calculateAndSetSizes(getWidth(), getHeight());
        }
    });
}

// 3. show()后强制计算
loadingView.post(() -> {
    loadingView.forceRecalculateSize();
});
```

**为什么需要三次？**
- `onSizeChanged`：可能在某些情况下不触发
- `onAttachedToWindow`：确保添加到窗口后计算
- `forceRecalculateSize`：强制重新计算，确保一致性

---

## 内存泄漏防护机制

### 1. 弱引用（WeakReference）

**原理：**
```java
private WeakReference<Object> targetRef = new WeakReference<>(target);
```

**作用：**
- 弱引用不会阻止对象被GC回收
- 当target被回收时，`targetRef.get()`返回null
- 避免持有强引用导致内存泄漏

**对比：**
```java
// ❌ 强引用 - 可能导致内存泄漏
private Object target;  // 强引用，阻止GC

// ✅ 弱引用 - 安全
private WeakReference<Object> targetRef;  // 弱引用，不阻止GC
```

### 2. 生命周期监听

#### Activity生命周期监听

**方式1：LifecycleOwner（推荐）**
```java
if (target instanceof LifecycleOwner) {
    LifecycleOwner lifecycleOwner = (LifecycleOwner) target;
    lifecycleOwner.getLifecycle().addObserver(lifecycleObserver);
}
```

**方式2：ActivityLifecycleCallbacks（兼容）**
```java
if (target instanceof Activity) {
    Application application = activity.getApplication();
    application.registerActivityLifecycleCallbacks(callbacks);
}
```

#### Fragment生命周期监听

```java
if (fragment instanceof LifecycleOwner) {
    ((LifecycleOwner) fragment).getLifecycle().addObserver(lifecycleObserver);
}
```

#### 自动隐藏逻辑

```java
lifecycleObserver = (source, event) -> {
    if (event == Lifecycle.Event.ON_PAUSE || 
        event == Lifecycle.Event.ON_STOP || 
        event == Lifecycle.Event.ON_DESTROY) {
        hide();  // 自动隐藏
    }
};
```

**触发时机：**
- `ON_PAUSE`：Activity进入后台
- `ON_STOP`：Activity完全不可见
- `ON_DESTROY`：Activity被销毁

### 3. 资源清理流程

```java
public void release() {
    hide();                           // 1. 隐藏loading
    unregisterLifecycleListener();    // 2. 取消监听
    isLifecycleRegistered = false;    // 3. 重置标志
    targetRef.clear();                // 4. 清空弱引用
    loadingView = null;               // 5. 清空view引用
    lifecycleObserver = null;         // 6. 清空观察者
    activityLifecycleCallbacks = null; // 7. 清空回调
}
```

**清理顺序很重要：**
1. 先隐藏（移除view）
2. 再取消监听（避免回调触发）
3. 最后清空引用

---

## 生命周期监控

### 日志系统

**日志分类：**
- `[创建]` - 对象创建
- `[显示]` - 显示过程
- `[隐藏]` - 隐藏过程
- `[释放]` - 资源释放
- `[生命周期]` - 生命周期事件
- `[尺寸]` - 尺寸计算
- `[窗口]` - 窗口相关
- `[管理]` - Manager管理

### 监控示例

**正常流程日志：**
```
[创建] LoadingManager - Target类型: Activity, Target: MainActivity
[显示] show() - 开始显示Loading, 文字: 加载中...
[管理] getOrCreateManager() - 创建新的Manager: MainActivity
[显示] show() - 创建新的LoadingView
[窗口] onAttachedToWindow() - LoadingView已添加到窗口
[尺寸] calculateAndSetSizes() - 容器尺寸: 100x100
[生命周期] registerLifecycleListener() - 注册LifecycleOwner监听
[隐藏] hide() - LoadingView已从父容器移除
[生命周期] unregisterLifecycleListener() - 取消注册LifecycleOwner监听
[释放] release() - LoadingManager资源已释放
```

**异常情况日志：**
```
[显示] show() - Target已被回收，无法显示
[隐藏] hide() - LoadingView不存在或未添加到父容器
[生命周期] unregisterLifecycleListener() - Target已被回收
```

### 如何查看日志

1. 打开Android Studio的Logcat
2. 过滤标签：`LoadingManager`、`LoadingView`、`Loading`
3. 查看完整的生命周期流程

---

## 尺寸自适应算法

### 计算公式

```java
// 1. 容器尺寸 = 父容器宽度 / 3
int containerSize = parentWidth / 3;
containerSize = Math.max(containerSize, 80);  // 最小80px

// 2. ProgressBar尺寸 = 容器尺寸 / 2
int progressBarSize = containerSize / 2;
progressBarSize = Math.max(progressBarSize, 24);  // 最小24px

// 3. Padding = 容器尺寸 / 8
int padding = containerSize / 8;
padding = Math.max(padding, 16);  // 最小16px
```

### 尺寸计算示例

| 父容器宽度 | 容器尺寸 | ProgressBar尺寸 | Padding |
|-----------|---------|----------------|---------|
| 300px     | 100px   | 50px          | 16px    |
| 600px     | 200px   | 100px         | 25px    |
| 900px     | 300px   | 150px         | 37px    |
| 240px     | 80px    | 40px          | 16px    |

### 为什么这样设计？

1. **容器宽度 = 父容器 / 3**
   - 不会太大，占用太多空间
   - 不会太小，用户看不清
   - 保持合适的比例

2. **ProgressBar = 容器 / 2**
   - 在容器中有足够的留白
   - 视觉上更舒适

3. **最小尺寸限制**
   - 确保在小屏幕上也能正常显示
   - 避免尺寸过小导致显示异常

---

## 代码示例分析

### 示例1：Activity中显示Loading

```java
// 用户调用
Loading.show(this, "加载中...");

// 内部流程
1. Loading.show(activity, text)
   → getOrCreateManager(activity)
   → LoadingManager.with(activity)  // 创建Manager
   → manager.show(text)

2. LoadingManager.show(text)
   → getParentView(activity)  // 获取content view
   → new LoadingView(context)  // 创建View
   → parentView.addView(loadingView)  // 添加到父容器
   → registerLifecycleListener(activity)  // 注册监听

3. LoadingView
   → onAttachedToWindow()  // 添加到窗口
   → calculateAndSetSizes()  // 计算尺寸
   → 显示loading界面
```

### 示例2：自动隐藏机制

```java
// 用户按Home键
Activity.onPause()
   ↓
Lifecycle.Event.ON_PAUSE
   ↓
lifecycleObserver.onStateChanged()
   ↓
LoadingManager.hide()
   ↓
parentView.removeView(loadingView)
   ↓
✅ Loading自动隐藏
```

### 示例3：内存泄漏防护

```java
// 场景：Activity被销毁
Activity.onDestroy()
   ↓
Lifecycle.Event.ON_DESTROY
   ↓
LoadingManager.hide()  // 隐藏loading
   ↓
LoadingManager.unregisterLifecycleListener()  // 取消监听
   ↓
targetRef.clear()  // 清空弱引用
   ↓
loadingView = null  // 清空view引用
   ↓
✅ 没有内存泄漏
```

---

## 总结

### 核心设计原则

1. **简单易用**：提供静态方法，一行代码显示/隐藏
2. **自动管理**：自动监听生命周期，自动清理资源
3. **内存安全**：使用弱引用，避免内存泄漏
4. **尺寸自适应**：根据容器大小自动调整
5. **详细日志**：完整的生命周期追踪

### 关键实现点

1. ✅ WeakHashMap管理Manager实例
2. ✅ WeakReference持有target引用
3. ✅ LifecycleObserver监听生命周期
4. ✅ 多重保障的尺寸计算机制
5. ✅ 完善的资源清理流程

### 最佳实践

1. ✅ 让Loading自动管理，不要手动持有Manager引用
2. ✅ 在Activity/Fragment销毁时调用`release()`（可选，会自动清理）
3. ✅ 使用日志监控生命周期
4. ✅ 不要重复创建Manager（使用Loading工具类）

---

**通过这个文档，你应该能够完全理解Loading组件的实现原理和内存泄漏防护机制。如有疑问，可以查看源代码或日志。** 🎓

