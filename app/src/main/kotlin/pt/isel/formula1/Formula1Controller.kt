package pt.isel.formula1

import pt.isel.autorouter.ArVerb
import pt.isel.autorouter.annotations.ArBody
import pt.isel.autorouter.annotations.ArQuery
import pt.isel.autorouter.annotations.ArRoute
import pt.isel.autorouter.annotations.AutoRouter
import java.util.*

class Formula1Controller {
    val repo = mutableMapOf(
        "RedBull" to listOf(
            Driver(1, "Max Verstappen", "47RB", 1),
            Driver(2, "Pierre Gasly", "12RB", 2),
            Driver(3, "Alexander Albon", "24RB", 3),
        ),
        "Mercedes" to listOf(
            Driver(4, "Lewis Hamilton", "35M", 1),
            Driver(5, "Valtteri Bottas", "31M", 2),
            Driver(6, "George Russell", "22M", 3),
        ),
        "Ferrari" to listOf(
            Driver(7, "Charles Leclerc", "15F", 1),
            Driver(8, "Sebastian Vettel", "55F", 2),
            Driver(9, "Carlos Sainz", "67F", 3),
        ),
    )

    /**
     * Example:
     *   http://localhost:4000/teams/Ferrari?driver=Max
     */
    @Synchronized
    @AutoRouter("/teams/{teamName}")
    fun search(
        @ArRoute teamName: String,
        @ArQuery driver: String?
    ): Optional<List<Driver>> {
        return repo[teamName]
            ?.let {
                if (driver == null) {
                    Optional.of(it)
                } else {
                    Optional.of(it.filter { dr -> dr.name.lowercase().contains(driver.lowercase()) })
                }
            }
            ?: Optional.empty()
    }

    /**
     * Example:
     *   curl --header "Content-Type: application/json" \
     *     --request PUT \
     *     --data '{"driverId": "7", "name":"Carlos Sainz","carNumber":"58F", "teamNumber":"4"}' \
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
        val stds = repo[teamName] ?: emptyList()
        repo[teamName] = stds.filter { it.driverId != driverId } + dr
        return Optional.of(dr)
    }

    /**
     * Example:
     *   curl --request DELETE http://localhost:4000/teams/Mercedes/drivers/2
     */
    @Synchronized
    @AutoRouter("/teams/{teamName}/drivers/{driverId}", method = ArVerb.DELETE)
    fun removeDriver(@ArRoute teamName: String, @ArRoute driverId: Int): Optional<Driver> {
        val stds = repo[teamName] ?: return Optional.empty()
        val s = stds.firstOrNull { it.driverId == driverId } ?: return Optional.empty()
        repo[teamName] = stds.filter { it.driverId != driverId }
        return Optional.of(s)
    }
}
