package vip.mystery0.pixelpulse.data.source

data class NetSpeedData(
    val downloadSpeed: Long = 0L,
    val uploadSpeed: Long = 0L
) {
    val totalSpeed: Long get() = downloadSpeed + uploadSpeed
}

data class NetworkTrafficData(
    val rxBytes: Long,
    val txBytes: Long,
)

interface ISpeedDataSource {
    suspend fun getTrafficData(): NetworkTrafficData?
}
