### 🎉🎉🎉 The First Release 🎉🎉🎉

* **Core: Precise Network Monitoring (核心：精准网速监控)**
    * Implemented `TrafficStats` + `ConnectivityManager` based monitoring logic to filter out
      virtual interfaces (like `tun0` from VPNs).
    * 实现了基于 `TrafficStats` 和 `ConnectivityManager` 的监控逻辑，智能过滤 VPN (`tun0`)
      等虚拟接口流量，解决网速翻倍显示的问题。

* **UI: Material Design 3 (界面：Material Design 3)**
    * Full Jetpack Compose implementation with Material You (Dynamic Color) support.
    * 全面采用 Jetpack Compose 实现，并支持 Material You (动态取色) 主题。

* **Feature: Notification Monitor (功能：通知栏显示)**
    * Real-time network speed indicator in the notification bar / status bar.
    * 支持在通知栏/状态栏实时显示当前网速。

* **Feature: Floating Window (功能：悬浮窗)**
    * Overlay window with drag-and-drop support and position locking.
    * 支持桌面悬浮窗显示，可自由拖拽并支持位置锁定。

* **Feature: Toolbox (功能：工具箱)**
    * Integrated Cloudflare Speed Test using Chrome Custom Tabs.
    * 集成 Chrome Custom Tabs，内置 Cloudflare 快捷测速功能。
