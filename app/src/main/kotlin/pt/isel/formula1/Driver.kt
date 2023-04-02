package pt.isel.formula1

import com.fasterxml.jackson.annotation.JsonProperty

data class Driver(
    @JsonProperty("driverId") val driverId: Int,
    @JsonProperty("name") val name: String,
    @JsonProperty("carPrice") val carPrice: Double,
    @JsonProperty("active") val active: Boolean,
    @JsonProperty("nTrophies") val nrTrophies: Int?,
)
