package pt.isel.formula1

import com.fasterxml.jackson.annotation.JsonProperty

data class RaceTrack(
    @JsonProperty("raceId") val raceId: Int,
    @JsonProperty("circuit") val circuit: String,
    @JsonProperty("location") val location: String
)