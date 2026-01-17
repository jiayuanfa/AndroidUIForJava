# Android Studio 注释模板配置指南

## 🎯 方法概览

1. **内置模板**：Android Studio 自带的 File Header 和 Live Template（免费，无需安装）
2. **第三方插件**：功能更强大的插件（需要安装）

---

## 🔌 方法零：第三方插件（推荐）

### 插件1：JavaDoc（最常用）

**功能特点：**
- ✅ 快速生成符合 Javadoc 标准的注释
- ✅ 自动提取方法参数、返回值等信息
- ✅ 支持自定义模板
- ✅ 快捷键快速生成

**安装步骤：**
1. `File` → `Settings` → `Plugins`
2. 搜索 `JavaDoc` 或 `JavaDoc Generator`
3. 点击 `Install` 安装
4. 重启 Android Studio

**使用方法：**
- 将光标放在类或方法上
- 按快捷键 `Alt + Insert`（Windows/Linux）或 `Cmd + N`（Mac）
- 选择 `JavaDoc` 或 `Generate JavaDoc`
- 或者使用快捷键 `Shift + Ctrl + G`（部分版本）

**推荐插件：**
- `JavaDoc` by JetBrains（官方推荐）
- `JavaDoc Generator` by Sergey Timoshin
- `JavaDoc Helper` by Sergey Timoshin

### 插件2：Code Comment

**功能特点：**
- ✅ 支持多种注释风格
- ✅ 自定义作者、日期格式
- ✅ 支持文件头和方法注释

**安装步骤：**
1. `File` → `Settings` → `Plugins`
2. 搜索 `Code Comment`
3. 点击 `Install` 安装

### 插件3：Javadoc Helper

**功能特点：**
- ✅ 智能生成 Javadoc 注释
- ✅ 自动识别参数和返回值
- ✅ 支持自定义模板

**安装步骤：**
1. `File` → `Settings` → `Plugins`
2. 搜索 `Javadoc Helper`
3. 点击 `Install` 安装

### 插件4：Kotlin Comment（Kotlin 项目）

**功能特点：**
- ✅ 专门为 Kotlin 设计
- ✅ 支持 KDoc 格式
- ✅ 快速生成注释

**安装步骤：**
1. `File` → `Settings` → `Plugins`
2. 搜索 `Kotlin Comment` 或 `KDoc`
3. 点击 `Install` 安装

---

## 📝 方法一：文件头注释模板（File Header）

### 配置步骤

1. **打开设置**
   - Windows/Linux: `File` → `Settings`
   - Mac: `Android Studio` → `Preferences`

2. **导航到模板设置**
   - `Editor` → `File and Code Templates`
   - 选择 `Includes` 标签页
   - 选择 `File Header`

3. **配置模板内容**

```java
/**
 * ${NAME}
 * 
 * @author ${USER}
 * @date ${DATE} ${TIME}
 * @description TODO
 */
```

### 可用变量

- `${NAME}` - 文件名（不含扩展名）
- `${USER}` - 当前用户名
- `${DATE}` - 当前日期（格式：yyyy/MM/dd）
- `${TIME}` - 当前时间（格式：HH:mm）
- `${YEAR}` - 当前年份
- `${MONTH}` - 当前月份
- `${DAY}` - 当前日期
- `${HOUR}` - 当前小时
- `${MINUTE}` - 当前分钟

### 使用效果

创建新文件时，会自动在文件顶部添加注释：

```java
/**
 * MainActivity
 * 
 * @author Administrator
 * @date 2026/01/13 23:30
 * @description TODO
 */
package com.example.androiduidemo;

public class MainActivity extends AppCompatActivity {
    // ...
}
```

---

## 📝 方法二：Live Template（代码模板）

### 配置步骤

1. **打开设置**
   - `File` → `Settings` → `Editor` → `Live Templates`

2. **创建模板组**
   - 点击 `+` 号
   - 选择 `Template Group`
   - 输入组名，如：`MyTemplates`

3. **创建注释模板**
   - 在组中点击 `+` 号
   - 选择 `Live Template`
   - 配置如下：

#### 模板1：类注释

**Abbreviation（缩写）**: `classcomment`

**Description（描述）**: 添加类注释

**Template text（模板内容）**:
```java
/**
 * $CLASS_NAME$
 * 
 * @author $USER$
 * @date $DATE$ $TIME$
 * @description $DESCRIPTION$
 */
```

**⚠️ 重要：如何启用 "Edit variables" 按钮**

1. **先输入模板内容**：在 `Template text` 中输入包含 `$VARIABLE$` 的模板
2. **检查变量格式**：确保变量使用 `$变量名$` 格式（注意：是 `$变量名$`，不是 `${变量名}`）
3. **点击 "Edit variables"**：输入模板后，按钮应该会变为可点击状态

**如果按钮仍然是灰色，请检查：**
- ✅ 模板中是否包含 `$变量名$` 格式的变量
- ✅ 变量名是否正确（区分大小写）
- ✅ 是否在 `Template text` 区域输入了内容

**Applicable contexts（适用上下文）**:
- ✅ Java: declaration
- ✅ Kotlin: declaration

**Variables（变量设置）** - 点击 "Edit variables" 后配置：
- `CLASS_NAME` → Expression: `className()` (内置函数)
- `USER` → Expression: `user()` (内置函数)
- `DATE` → Expression: `date()` (内置函数)
- `TIME` → Expression: `time()` (内置函数)
- `DESCRIPTION` → Expression: 留空（手动输入）

#### 模板2：方法注释

**Abbreviation（缩写）**: `methodcomment`

**Description（描述）**: 添加方法注释

**Template text（模板内容）**:
```java
/**
 * $METHOD_NAME$
 * 
 * @author $USER$
 * @date $DATE$ $TIME$
 * @param $PARAM$ $PARAM_DESC$
 * @return $RETURN_DESC$
 */
```

**Applicable contexts（适用上下文）**:
- ✅ Java: declaration
- ✅ Kotlin: declaration

**Variables（变量设置）**:
- `METHOD_NAME` → `methodName()` (内置函数)
- `USER` → `user()` (内置函数)
- `DATE` → `date()` (内置函数)
- `TIME` → `time()` (内置函数)
- `PARAM` → 留空，手动输入
- `PARAM_DESC` → 留空，手动输入
- `RETURN_DESC` → 留空，手动输入

#### 模板3：简单注释

**Abbreviation（缩写）**: `comment`

**Description（描述）**: 快速添加注释

**Template text（模板内容）**:
```java
/**
 * $DESCRIPTION$
 * 
 * @author $USER$
 * @date $DATE$ $TIME$
 */
```

**Applicable contexts（适用上下文）**:
- ✅ Java: everywhere
- ✅ Kotlin: everywhere

**Variables（变量设置）**:
- `DESCRIPTION` → 留空，手动输入
- `USER` → `user()` (内置函数)
- `DATE` → `date()` (内置函数)
- `TIME` → `time()` (内置函数)

### 使用方法

1. 在代码中输入缩写（如 `classcomment`）
2. 按 `Tab` 键或 `Enter` 键
3. 模板会自动展开
4. 填写需要手动输入的部分

---

## 📝 方法三：自定义文件模板

### 配置步骤

1. **打开设置**
   - `File` → `Settings` → `Editor` → `File and Code Templates`
   - 选择 `Files` 标签页

2. **创建Java类模板**

点击 `+` 创建新模板，或编辑现有的 `Java Class`：

```java
#parse("File Header.java")
package ${PACKAGE_NAME};

/**
 * ${NAME}
 * 
 * @author ${USER}
 * @date ${DATE} ${TIME}
 * @description TODO
 */
public class ${NAME} {
    #if (${VISIBILITY} == "public")
    public ${NAME}() {
    }
    #end
}
```

### 使用效果

创建新Java类时，会自动生成：

```java
package com.example.androiduidemo;

/**
 * MyClass
 * 
 * @author Administrator
 * @date 2026/01/13 23:30
 * @description TODO
 */
public class MyClass {
    public MyClass() {
    }
}
```

---

## 🎨 推荐的注释模板样式

### 样式1：简洁版

```java
/**
 * ${NAME}
 * 
 * @author ${USER}
 * @date ${DATE} ${TIME}
 */
```

### 样式2：详细版

```java
/**
 * ${NAME}
 * 
 * <p>功能描述：TODO</p>
 * 
 * @author ${USER}
 * @date ${DATE} ${TIME}
 * @version 1.0
 */
```

### 样式3：带版权信息

```java
/**
 * ${NAME}
 * 
 * @author ${USER}
 * @date ${DATE} ${TIME}
 * @description TODO
 * 
 * Copyright (c) ${YEAR} Your Company. All rights reserved.
 */
```

### 样式4：方法注释模板

```java
/**
 * ${METHOD_NAME}
 * 
 * <p>功能描述：${DESCRIPTION}</p>
 * 
 * @param ${PARAM_NAME} ${PARAM_DESC}
 * @return ${RETURN_DESC}
 * @throws ${EXCEPTION_TYPE} ${EXCEPTION_DESC}
 * @author ${USER}
 * @date ${DATE} ${TIME}
 */
```

---

## 🔧 高级配置

### 自定义日期格式

在 `File Header` 模板中，可以使用以下方式自定义日期格式：

```java
/**
 * ${NAME}
 * 
 * @author ${USER}
 * @date ${YEAR}-${MONTH}-${DAY} ${HOUR}:${MINUTE}
 */
```

### 使用Groovy脚本

在Live Template中，可以使用Groovy脚本：

```java
/**
 * ${NAME}
 * 
 * @author ${groovyScript("System.getProperty('user.name')", "")}
 * @date ${DATE} ${TIME}
 */
```

### 配置快捷键

1. `File` → `Settings` → `Keymap`
2. 搜索 `Live Template`
3. 为常用的模板配置快捷键

---

## 📋 完整配置示例

### File Header 模板（推荐）

```java
/**
 * ${NAME}
 * 
 * <p>功能描述：TODO</p>
 * 
 * @author ${USER}
 * @date ${DATE} ${TIME}
 * @version 1.0
 */
```

### Live Template - 类注释

**缩写**: `classdoc`

**模板**:
```java
/**
 * $CLASS_NAME$
 * 
 * <p>功能描述：$DESCRIPTION$</p>
 * 
 * @author $USER$
 * @date $DATE$ $TIME$
 * @version 1.0
 */
```

### Live Template - 方法注释

**缩写**: `methoddoc`

**模板**:
```java
/**
 * $METHOD_NAME$
 * 
 * <p>功能描述：$DESCRIPTION$</p>
 * 
 * @param $PARAM$ $PARAM_DESC$
 * @return $RETURN_DESC$
 * @author $USER$
 * @date $DATE$ $TIME$
 */
```

---

## ✅ 验证配置

1. 创建新文件，检查是否自动添加文件头注释
2. 输入模板缩写（如 `classdoc`），按 `Tab` 键，检查是否展开
3. 检查变量是否正确替换（用户名、日期、时间等）

---

## ⚠️ 常见问题解决

### 问题1：Edit variables 按钮是灰色的

**原因：**
- 模板中没有定义变量（没有使用 `$变量名$` 格式）
- 变量格式错误

**解决方法：**

1. **确保模板中有变量**
   ```java
   // ✅ 正确：使用 $变量名$ 格式
   /**
    * $CLASS_NAME$
    * @author $USER$
    */
   
   // ❌ 错误：没有变量或格式错误
   /**
    * 固定文本
    * @author Administrator
    */
   ```

2. **检查变量格式**
   - ✅ 正确：`$VARIABLE$`（美元符号包围）
   - ❌ 错误：`${VARIABLE}`（大括号格式，Live Template不支持）
   - ❌ 错误：`VARIABLE`（没有美元符号）

3. **操作步骤**
   - 先在 `Template text` 中输入包含变量的模板
   - 输入完成后，`Edit variables` 按钮应该变为可点击
   - 如果还是灰色，检查变量格式是否正确

### 问题2：变量没有自动替换

**解决方法：**
- 在 `Edit variables` 对话框中，为每个变量设置 `Expression`
- 使用内置函数：`className()`, `user()`, `date()`, `time()` 等
- 如果变量需要手动输入，Expression 留空即可

### 问题3：模板不生效

**解决方法：**
- 检查 `Applicable contexts` 是否选择了正确的上下文
- 确保在正确的位置使用模板（如类声明处使用类注释模板）
- 检查缩写是否正确输入

### 问题4：快捷键冲突

**解决方法：**
- `File` → `Settings` → `Keymap`
- 搜索 `Live Template`
- 修改快捷键避免冲突

## 💡 提示

1. **File Header** 适用于所有新创建的文件
2. **Live Template** 适用于在代码中快速插入注释
3. 可以根据项目需求自定义模板内容
4. 建议团队统一使用相同的注释模板
5. **变量格式很重要**：必须使用 `$变量名$` 格式才能启用变量编辑功能

---

## 🔗 相关资源

- [Android Studio 官方文档 - File Templates](https://www.jetbrains.com/help/idea/using-file-and-code-templates.html)
- [Android Studio 官方文档 - Live Templates](https://www.jetbrains.com/help/idea/using-live-templates.html)
- [JetBrains Plugin Repository](https://plugins.jetbrains.com/)

## 📦 插件安装地址

### 在 Android Studio 中安装

1. `File` → `Settings` → `Plugins`
2. 点击 `Marketplace` 标签页
3. 搜索插件名称
4. 点击 `Install` 安装

### 推荐的插件列表

| 插件名称 | 功能 | 评分 | 下载量 |
|---------|------|------|--------|
| **JavaDoc** | 快速生成 Javadoc 注释 | ⭐⭐⭐⭐⭐ | 高 |
| **JavaDoc Generator** | 增强的 Javadoc 生成器 | ⭐⭐⭐⭐ | 中 |
| **Code Comment** | 多种注释风格支持 | ⭐⭐⭐⭐ | 中 |
| **Javadoc Helper** | 智能注释助手 | ⭐⭐⭐⭐ | 中 |
| **Kotlin Comment** | Kotlin 注释生成 | ⭐⭐⭐⭐ | 中 |

### 插件配置示例（JavaDoc 插件）

安装后，可以在插件设置中配置默认模板：

1. `File` → `Settings` → `Tools` → `JavaDoc`（或对应插件设置）
2. 配置模板变量：
   - `@author` - 自动使用系统用户名
   - `@date` - 自动使用当前日期
   - `@version` - 可自定义默认值

### 插件 vs 内置模板对比

| 特性 | 第三方插件 | 内置模板 |
|------|-----------|---------|
| **安装** | 需要安装 | 无需安装 |
| **功能** | 更强大，智能识别 | 基础功能 |
| **自定义** | 支持高级配置 | 基础配置 |
| **性能** | 可能略慢 | 快速 |
| **推荐度** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |

**建议：**
- 如果只需要基础功能，使用内置模板即可
- 如果需要更强大的功能（如自动识别参数），推荐使用插件

