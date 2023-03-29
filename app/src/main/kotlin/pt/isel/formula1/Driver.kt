package pt.isel.formula1

import com.fasterxml.jackson.annotation.JsonProperty

data class Driver(
    @JsonProperty("driverId") val driverId: Int,
    @JsonProperty("Name") val name: String,
    @JsonProperty("CarPrice") val carPrice: Double,
    @JsonProperty("Active") val active: Boolean,
    @JsonProperty("numberOfTrophies") val nrTrophies: Int?,
)
