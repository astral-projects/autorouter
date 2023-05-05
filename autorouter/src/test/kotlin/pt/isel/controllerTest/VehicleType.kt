package pt.isel.controllerTest

import java.util.*

class VehicleType (type: String, brand: String, matriculation: Matriculation, velocity: Double) {
    private val MINIMUM_VELOCITY = 120.0
    init {
        require(velocity > MINIMUM_VELOCITY)
        // type must be "Car" or "Motorcycle" with all in lowercase
        require(type.matches(Regex("car|motorcycle")))
    }
}
