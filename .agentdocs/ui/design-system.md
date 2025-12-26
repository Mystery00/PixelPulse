# 设计系统 (Design System)

Pixel Pulse 严格遵循 Modern Android Development (MAD) 指南，全面采用 Jetpack Compose 构建 UI。

## 1. 主题与配色 (Theming)

### 1.1 Material Design String

- **风格**: **Material 3 + Material You + Material Express**。
- **动态取色 (Dynamic Color)**:
    - 必须启用 `DynamicColors.applyToActivitiesIfAvailable(this)`。
    - UI 颜色直接映射系统壁纸色调，确保与原生系统（Settings, Quick Settings）视觉一致。
- **主要控件**: 使用 M3 标准组件 (`Scaffold`, `TopAppBar`, `Switch`, `Card`, `NavigationBar` 等)。

## 2. 通知栏动态图标 (Notification Icon)

### 实现方案

1. **默认状态**: 首次启动默认**关闭**，需用户手动开启。
2. **创建 Bitmap**:
    - **单行模式**: 若显示模式为 `BOTH` (同时显示)，**将上下行网速相加**，仅绘制**一行**
      合并后的网速文本 (e.g., "5.2 M/s")。
    - **单向模式**: 仅绘制上行或下行流量。
3. **Canvas 绘制**: 使用 `Canvas` 和 `Paint` 将文字绘制在 Bitmap 中央。
    - **字体大小**: 需根据系统状态栏高度动态适配，或提供用户手动调节选项。
    - **颜色**:
        - 默认：白色/灰色（适配深色/浅色状态栏）。
        - 进阶：检测系统 Dark Mode 状态自动反色。
3. **IconCompat**: 将 Bitmap 转换为 `Icon` 对象。
4. **NotificationBuilder**: 设置 `.setSmallIcon(Icon)` (注意：部分 OEM ROM 可能不支持 setSmallIcon 传
   Bitmap，仅支持 ResID，但在 Pixel 类原生上通常可行，或者使用 `.setLargeIcon` 配合隐藏的小图标)。
    - *Pixel 特例*: 原生 Android 通常只显示小图标蒙版 (Alpha Mask)。若要显示具体数字，可能需要利用
      `setSmallIcon(IconCompat.createWithBitmap(bitmap))`，这在不同 Android 版本上表现不一，需重点测试。

## 3. 悬浮窗 (Floating Window)

### 3.1 窗口类型

- 使用 `TYPE_APPLICATION_OVERLAY`。
- 必须先申请 `SYSTEM_ALERT_WINDOW` 权限。

### 3.2 Compose in WindowManager

- 使用 `ComposeView` 作为 WindowManager 的 View Root。
  -设置 `LifecycleOwner` 和 `SavedStateRegistryOwner` 以确保 Compose 生命周期正常。

```kotlin
val composeView = ComposeView(context).apply {
    setContent {
        PixelPulseTheme {
            OverlayContent(...)
        }
    }
}
windowManager.addView(composeView, params)
```

### 3.3 交互

- **触摸穿透**: 默认情况下悬浮窗应捕获 Touch 事件以支持拖拽。
- **位置记忆**: 每次拖拽结束 (Drag End)，记录当前 (x, y) 坐标到 DataStore，下次启动时恢复。
