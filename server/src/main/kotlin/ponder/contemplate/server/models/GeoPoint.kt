package ponder.contemplate.server.models

import kotlinx.serialization.Serializable

@Serializable
data class GeoPoint(
    val latitude: Double,
    val longitude: Double
) {
    fun toList(): List<Double> = listOf(latitude, longitude)
}