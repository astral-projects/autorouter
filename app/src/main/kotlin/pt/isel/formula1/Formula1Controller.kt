package pt.isel.formula1

import pt.isel.autorouter.ArVerb
import pt.isel.autorouter.annotations.ArBody
import pt.isel.autorouter.annotations.ArQuery
import pt.isel.autorouter.annotations.ArRoute
import pt.isel.autorouter.annotations.AutoRouter
import java.time.LocalDate
import java.util.*

object Formula1Controller {
    val repo = mutableMapOf(
        "RedBull" to listOf(
            Driver(1, "Max Verstappen", 340000.50, true, 1),
            Driver(2, "Pierre Gasly", 540000.25, false, null),
            Driver(3, "Alexander Albon", 280000.89, true, 4),
        ),
        "Mercedes" to listOf(
            Driver(4, "Lewis Hamilton", 6700000.78, false, null),
            Driver(5, "Valtteri Bottas", 902345.88, true, 3),
            Driver(6, "George Russell", 140789.89, false, null),
        ),
        "Ferrari" to listOf(
            Driver(7, "Charles Leclerc", 654792.45, true, 6),
            Driver(8, "Sebastian Vettel", 789675.45, false, null),
            Driver(9, "Carlos Sainz", 126789.12, true, 2),
        ),
    )

    /**
     * Example:
     *   http://localhost:4000/teams/Ferrari?driver=Max&active=true
     */
    @Synchronized
    @AutoRouter("/teams/{teamName}")
    fun searchDrivers(
        @ArRoute teamName: String,
        @ArQuery driver: String?,
        @ArQuery active: Boolean?,
    ): Optional<List<Driver>> {
        return repo[teamName]
            ?.let {
                if (active != false && driver != null) {
                    println("heye1")
                    // retrieves a driver that is active
                    Optional.of(it.filter { dr -> dr.name.contains(driver) && dr.active == active })
                } else if (driver != null) {
                    println("heye2")
                    // retrieves a single driver
                    Optional.of(it.filter { dr -> dr.name == driver })
                } else if (active != false) {
                    println("heye3").also { println(driver).also { println(active) } }
                    // retrieves all active drivers for this team
                    Optional.of(it.filter { dr -> dr.active == active })
                } else {
                    println("heye4")
                    // retrieves the full team
                    Optional.of(it)
                }
            } ?: Optional.empty()
    }

    /**
     * Example:
     *   curl --header "Content-Type: application/json" \
     *     --request PUT \
     *     --data '{"driverId": "7", "name":"Carlos Sainz","carPrice":"20.01", "active":"true", "nrTrophies":"3"}' \
     *     http://localhost:4000/teams/Ferrari/drivers/7
     */
    @Synchronized
    @AutoRouter("/teams/{teamName}/drivers/{driverId}", method = ArVerb.PUT)
    fun addDriver(
        @ArRoute teamName: String,
        @ArRoute driverId: Int,
        @ArBody dr: Driver,
    ): Optional<Driver> {
        if (driverId != dr.driverId) return Optional.empty()
        val drivers = repo[teamName] ?: emptyList()
        repo[teamName] = drivers.filter { it.driverId != driverId } + dr
        return Optional.of(dr)
    }

    @Synchronized
    @AutoRouter("/teams/{teamName}/drivers/{driverId}", method = ArVerb.PUT)
    fun addDriverJoinDate(
        @ArRoute teamName: String,
        @ArRoute driverId: Int,
        @ArQuery joinDate: NotPrimitiveDate,
    ): Optional<Driver> {
        // Empty endpoint for testing purposes
        return Optional.empty()
    }

    @Synchronized
    @AutoRouter("/teams/{teamName}/drivers/{driverId}/races", method = ArVerb.PUT)
    fun addDriverAndRace(
        @ArRoute teamName: String,
        @ArRoute driverId: Int,
        @ArBody dr: Driver,
        @ArBody raceTrack: RaceTrack
    ): Optional<List<Any>> {
        return Optional.of(listOf(dr, raceTrack))
    }

    /**
     * Example:
     *   curl --request DELETE http://localhost:4000/teams/Mercedes/drivers/2
     */
    @Synchronized
    @AutoRouter("/teams/{teamName}/drivers/{driverId}", method = ArVerb.DELETE)
    fun removeDriver(
        @ArRoute teamName: String,
        @ArRoute driverId: Int
    ): Optional<Driver> {
        val drivers = repo[teamName] ?: return Optional.empty()
        val s = drivers.firstOrNull { it.driverId == driverId } ?: return Optional.empty()
        repo[teamName] = drivers.filter { it.driverId != driverId }
        return Optional.of(s)
    }
}
