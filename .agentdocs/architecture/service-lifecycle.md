# 服务生命周期与保活 (Service Lifecycle)

为了保证网速指示器在后台持续运行并实时更新，App 必须维护一个稳定的前台服务 (Foreground Service)。

## 1. Service 配置

- **类名**: `NetworkMonitorService`
- **类型**: `Foreground Service`
- **foregroundServiceType**: `dataSync` (Android 14+ 强制要求指定类型并声明权限)。

```xml

<service android:name=".service.NetworkMonitorService" android:foregroundServiceType="dataSync"
    android:exported="false" />
```

## 2. 启动与保活

### 2.1 启动流程

1. **用户开启**: 用户在主界面 Toggle "Enable Monitor"。
2. **Context.startForegroundService()**: 启动服务。
3. **startForeground()**: 服务 onCreate/onStartCommand 中必须在 5 秒内调用 `startForeground`
   ，绑定一个持续显示的 Notification，否则会被系统杀掉并抛出 ANR。

### 2.2 周期性任务 (Ticker)

- 使用 Kotlin Coroutines `flow` 或 `Handler` 实现 1000ms 的周期性任务。
- **任务内容**:
    - 获取当前网速 (Repository.getSpeed)。
    - 生成 Notification Bitmap。
    - 更新 NotificationManager。
    - 发送 EventBus/StateFlow 消息通知 UI 层 (悬浮窗/主页)。

## 3. Android 14 (API 34) 适配

Android 14 对前台服务有严格限制：

- **权限声明**: 必须在 Manifest 中声明 `android.permission.FOREGROUND_SERVICE` 和
  `android.permission.FOREGROUND_SERVICE_DATA_SYNC`。
- **运行时机**: 仅当 App 处于前台（Visible）时才能调用 `startForegroundService`。若 App 在后台尝试启动服务，会抛出
  `ForegroundServiceStartNotAllowedException`。
    - **处理策略**: 确保服务的启动操作仅由用户在 UI 界面手动触发，或者在 BootReceiver (开机自启)
      中依循系统允许的豁免规则进行。

## 4. 资源释放

- 当用户手动关闭功能，或点击通知栏 "Exit" 按钮时，调用 `stopForeground(STOP_FOREGROUND_REMOVE)` 并
  `stopSelf()`，彻底释放资源，停止计费/耗电。
