# 核心功能需求 (Product Requirements)

| 项目       | 内容                                                     |
|----------|--------------------------------------------------------|
| **产品名称** | Pixel Pulse                                            |
| **版本**   | v0.1.0                                                 |
| **定位**   | 专为 Pixel/原生 Android 设计的精准网速指示器（支持 Shizuku 修正 VPN 流量统计） |

## 1. 项目背景与痛点

在开启 VPN（如 V2Ray, Clash, WireGuard 等）的环境下，Android 传统的网速显示 App (基于 `TrafficStats`/
`NetworkStatsManager`) 往往会将物理接口（`wlan0`/`rmnet`）与虚拟接口（`tun0`
）的流量叠加计算。这导致通知栏显示的网速通常是实际速度的 **2 倍**，产生误导。

**Pixel Pulse** 旨在通过**混合数据源策略**解决此痛点，在 Shizuku 授权下直接读取 Kernel
数据剔除虚拟接口流量，还原真实网速。

## 2. 核心特性 (Features)

### 2.1 精准流量统计 (Shizuku Enhanced)

* **默认模式**: 使用 Android 原生 `NetworkStatsManager`。
* **Shizuku 模式**:
    * 通过 **Shizuku Binder** 机制连接系统服务 (`INetworkManagementService` 或其它内部服务)
      获取底层网络数据 (不使用 `newProcess`/`cat` 命令)。
    * **接口黑名单**: 完全依赖用户配置（**无默认忽略接口**）。
    * **计算公式**: `TrueSpeed = Sum(All_Interfaces) - Sum(User_Blacklisted_Interfaces)`。

### 2.2 原生体验 (Native Experience)

* **Design**: Material 3 + Material You + **Material Express** 风格。
* **Android 14/15 适配**: 针对最新的 Android API 进行优化。
* **无广告/轻量级**: 专注于核心功能，极低功耗。

### 2.3 多样的显示方式

* **首次启动**: 默认**不开启**任何显示，由用户自行选择通知栏或悬浮窗。
* **通知栏动态图标**: 实时绘制 Bitmap 更新通知栏图标。
* **桌面悬浮窗**: 支持独立开关与拖拽。
* **同时显示模式 (Both)**:
    * 显示逻辑: `Total Speed = Upload + Download`。
    * UI 表现: 仅展示一行合并后的总网速文本。

### 2.4 实用工具箱

* **Cloudflare 测速**: 集成 Chrome Custom Tabs (CCT) 快速访问 `speed.cloudflare.com`。

## 3. 技术规格 (Technical Requirements)

* **Min SDK**: 29 (Android 10)
* **Target SDK**: 35 (Android 15)
* **架构**: MVVM + Clean Architecture (Simplified)
* **语言**: Kotlin
* **UI**: Jetpack Compose
* **DI**: Koin
* **IPC**: Rikka Shizuku API

## 4. 功能需求详情

| ID      | 模块       | 功能点         | 描述                                                       | 优先级 |
|:--------|:---------|:------------|:---------------------------------------------------------|:----|
| **F01** | **核心服务** | 前台服务保活      | 启动 `dataSync` 类型的 Foreground Service，需处理 Android 14+ 适配。 | P0  |
| **F02** | **数据源**  | 标准数据源       | 调用 `NetworkStatsManager` 获取流量数据。                         | P0  |
| **F03** | **数据源**  | Shizuku 数据源 | 检测授权，Shell 读取 `/proc/net/dev`，解析并过滤数据。                   | P0  |
| **F04** | **UI**   | 仪表盘首页       | 显示当前网速、运行模式 (Standard/Shizuku)、授权状态。                     | P0  |
| **F05** | **配置**   | 接口过滤        | 提供 UI 配置忽略的接口列表 (如 `tun0`, `ppp0`)，支持快捷添加。               | P1  |
| **F06** | **UI**   | 通知栏更新       | 每秒绘制 Bitmap 并更新 Notification。                            | P0  |
| **F07** | **UI**   | 悬浮窗         | 实现 Compose 悬浮窗，处理 Touch 事件与 WindowManager 交互。            | P1  |
| **F08** | **工具**   | 网络测速        | CCT 呼起 Cloudflare Speed Test。                            | P2  |

## 5. 数据存储 (DataStore)

| Key                   | 类型      | 默认值          | 说明                |
|:----------------------|:--------|:-------------|:------------------|
| `enable_shizuku`      | Boolean | `false`      | 主动开启 Shizuku 模式开关 |
| `ignored_interfaces`  | Set     | `[]` (Empty) | 黑名单接口集合           |
| `display_mode`        | Enum    | `BOTH`       | 显示模式 (上/下/合并总速)   |
| `enable_notification` | Boolean | `false`      | 通知栏开关 (默认关闭)      |
| `enable_overlay`      | Boolean | `false`      | 悬浮窗开关             |

## 6. 非功能性需求 (NFR)

* **功耗控制**: 屏幕关闭时应停止或降低刷新频率（待定，需权衡后台保活需求）。
* **异常恢复**: Shizuku 服务死亡 (`BinderDead`) 时，需无缝降级至标准模式并提示用户。
* **隐私安全**: 仅在本地处理流量计数，绝不上传任何网络流量数据。
