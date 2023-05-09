package pt.isel.`complex-controller`

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class VehicleType (
    @JsonProperty("type") val type: String,
    @JsonProperty("brand") val brand: String,
    @JsonProperty("matriculation") val matriculation: Matriculation,
    @JsonProperty("velocity") val velocity: Double
)
