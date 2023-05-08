package pt.isel.controllerTest

import pt.isel.autorouter.ArVerb.*
import pt.isel.autorouter.annotations.ArBody
import pt.isel.autorouter.annotations.ArQuery
import pt.isel.autorouter.annotations.ArRoute
import pt.isel.autorouter.annotations.AutoRouter
import java.util.*

/**
 * This controller was created to test situations that could not be tested in the
 * other controllers, since they were using both reflection and dynamic implmentations.
 */
class ControllerTest {
    val repo = mutableMapOf(
        "car" to listOf(
            Road("A22", "Lisbon", VehicleType("car","Audi",Matriculation("AA-00-00", Date("12-05-2003"), "Portugal"), 130.0)),
            Road("A33", "Porto", VehicleType("car", "BMW", Matriculation("AA-12-00", Date("12-09-2020"), "Portugal"), 200.6)),
            Road("A20", "Faro", VehicleType("car", "Mercedes", Matriculation("AA-00-12", Date("12-05-2023"), "Berlim"), 150.2)),
            Road("A12", "Lisbon", VehicleType("car", "Renault", Matriculation("BB-12-BL", Date("25-09-2013"), "Alemanha"), 149.6)),
        ),
        "motorcycle" to listOf(
            Road("A23", "Lisbon", VehicleType("motorcycle","Yamaha", Matriculation("AZ-00-BA", Date("12-05-2003"), "Portugal"), 130.0)),
            Road("A22", "Porto", VehicleType("motorcycle","Honda", Matriculation("AZ-12-BA", Date("12-09-2020"), "Escócia"), 200.6)),
            Road("A20", "Faro", VehicleType("motorcycle", "Suzuki", Matriculation("AZ-MK-09", Date("12-05-2023"), "Portugal"), 300.9)),
            Road("A19", "Lisbon", VehicleType("motorcycle", "Kawasaki", Matriculation("89-UA-14", Date("25-09-2013"), "Portugal"), 149.8)),
        )
    )

    /**
     * Example:
     *   http://localhost:4000/road/A22?velocity=130.0
     */
    @Synchronized
    @AutoRouter("/road/{road}/loc/{location}", method = PUT)
    fun addRoad(
        @ArRoute roadName: String,
        @ArRoute location: String,
        @ArQuery velocity: Double?,
        @ArBody vehicle: VehicleType,
        @ArBody matriculation: Matriculation,
        @ArBody date: Date,
    ): Optional<Road> {
        return Optional.of(Road(roadName, location, vehicle))
    }

    @Synchronized
    @AutoRouter("/road/{road}", method = DELETE )
    fun methodWithoutArAnnotation(
        @ArRoute roadName: String,
        location: String
    ): Optional<Road> {
        return Optional.empty()
    }

}