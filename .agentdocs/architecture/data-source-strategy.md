# 混合数据源策略 (Hybrid Data Source Strategy)

Pixel Pulse 采用“混合数据源”架构，旨在解决 Android 流量统计在 VPN 环境下的双倍显示问题。

## 1. 架构概览

系统内部抽象了 `ISpeedDataSource` 接口，根据当前运行环境（是否授权 Shizuku）动态提供不同的实现。

```kotlin
interface ISpeedDataSource {
    fun getNetSpeed(): NetSpeedData
}
```

## 2. 标准模式 (Standard Mode)

- **适用场景**:
    - 默认状态。
    - 用户未安装 Shizuku。
    - Shizuku 服务异常断开。
- **实现原理**: 此模式直接调用 Android 官方 API `NetworkStatsManager`。
- **数据获取**:
    - 查询 `NetworkCapabilities.TRANSPORT_WIFI` (Wi-Fi 接口流量)。
    - 查询 `NetworkCapabilities.TRANSPORT_CELLULAR` (蜂窝网络流量)。
    - 将两者累加。
- **优缺点**:
    - ✅ 兼容性极佳，无需特殊权限。
    - ❌ 无法区分物理网卡与 VPN 虚拟网卡，导致开启 VPN 时显示的网速通常为真实值的 2 倍。

## 3. Shizuku 增强模式 (Shizuku Mode)

- **适用场景**:
    - 用户已授权 Shizuku。
    - 用户在设置中开启 "Enable Shizuku Mode"。
- **实现原理**: 利用 Shizuku 提供的 **Binder** 机制连接到系统服务 (如 `INetworkManagementService`)
  获取底层网络数据。**注意：不使用 `newProcess` 创建新进程，也不解析文本文件。**
- **数据处理流程**:

    1. **连接服务**: 通过 Shizuku 获取系统服务的 IBinder 接口。
    2. **获取数据**: 调用相关服务方法直接获取网络接口统计信息。
    3. **应用黑名单 (Blacklist Logic)**:
        - 读取用户配置的 `ignored_interfaces` 列表（**默认列表为空**，完全由用户定义）。
        - 遍历接口数据，若接口名在黑名单中，则剔除。
    4. **计算总和**: 将剩余接口的数据累加。

## 4. 模式切换与降级

- **自动切换**: App 启动时检测 Shizuku 权限，若有则自动激活增强模式（需用户设置允许）。
- **异常降级**: 监听 Shizuku `BinderDeadListener`。一旦检测到 Shizuku 服务端死亡，立即切换回标准模式，并记录日志或提示用户。
