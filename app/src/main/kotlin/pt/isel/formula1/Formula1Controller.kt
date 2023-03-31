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
    fun searchDriver(
        @ArRoute teamName: String,
        @ArQuery driver: String?,
        @ArQuery active: Boolean?,
    ): Optional<List<Driver>> {
        return repo[teamName]
            ?.let {
                if(active != null && driver != null){
                    Optional.of(it.filter { dr -> dr.name.contains(driver) && (dr.active == active) })
                     }
                else if(active!=null){
                    Optional.of(it.filter { it.active==active })
                }
                else if (driver!=null) {
                    Optional.of(it.filter { it.name==driver })
                } else {
                    Optional.of(it)
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
    fun removeDriver(
        @ArRoute teamName: String,
        @ArRoute driverId: Int
    ): Optional<Driver> {
        val stds = repo[teamName] ?: return Optional.empty()
        val s = stds.firstOrNull { it.driverId == driverId } ?: return Optional.empty()
        repo[teamName] = stds.filter { it.driverId != driverId }
        return Optional.of(s)
    }
}
//SInais sonoros responder C