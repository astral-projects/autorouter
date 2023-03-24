package pt.isel

import com.fasterxml.jackson.annotation.JsonProperty

data class Driver(
    @JsonProperty("driverId") val driverId: Int,
    @JsonProperty("Name") val name: String,
    @JsonProperty("carNumber") val carNumber: String,
    @JsonProperty("teamNumber") val teamNr: Int,
)
