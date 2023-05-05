package pt.isel.controllerTest

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

class VehicleType (
    @JsonProperty("type") val type: String,
    @JsonProperty("brand") val brand: String,
    @JsonProperty("matriculation") val matriculation: Matriculation,
    velocity: Double
) {
    private val MINIMUM_VELOCITY = 120.0
    init {
        require(velocity > MINIMUM_VELOCITY)
        // type must be "Car" or "Motorcycle" with all in lowercase
        require(type.matches(Regex("car|motorcycle")))
    }
}
