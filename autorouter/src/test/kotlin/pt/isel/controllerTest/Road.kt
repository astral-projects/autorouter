package pt.isel.controllerTest

import com.fasterxml.jackson.annotation.JsonProperty

class Road(
    @JsonProperty("name") val name: String,
    @JsonProperty("location") val location: String,
    @JsonProperty("car") val car: VehicleType)
