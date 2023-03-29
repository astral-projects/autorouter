package pt.isel

import org.junit.jupiter.api.Test
import pt.isel.autorouter.ArVerb
import pt.isel.autorouter.autorouterReflect
import pt.isel.formula1.Driver
import pt.isel.formula1.Formula1Controller
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AutoRouterTestForFormula1 {
    private fun getTeamSize(controller: Formula1Controller, team: String)
        = controller.repo[team]?.size
    @Test
    fun `add a driver to a team`() {
        val controller = Formula1Controller()
        val routes = controller.autorouterReflect().toList()
        val driverId = "3"
        val team = "RedBull"
        val initialTeamSize = getTeamSize(controller, team)
        requireNotNull(initialTeamSize)
        val route = routes.first { it.path == "/team/{teamName}/drivers/{driverId}" && it.method == ArVerb.PUT }
        // Get team initial (drivers) size
        val res = route.handler.handle(
            /* routeArgs = */ mapOf(
                "teamName" to team,
                "driverId" to driverId
            ),
            /* queryArgs = */ emptyMap(),
            /* bodyArgs = */ mapOf(
                "driverId" to driverId,
                "name" to "driverTest",
                "carPrice" to "20.000",
                "active" to "true",
                "numberOfTrophies" to "null",
            ),
        )
        assertEquals(
            Driver(2023, "driverName", 250.0, true, null),
            res.get() as Driver,
        )
        assertEquals(initialTeamSize + 1, getTeamSize(controller, team))
    }

    @Test
    fun `try to add a driver to a team but body values are not in expected order`() {
        val controller = Formula1Controller()
        val routes = controller.autorouterReflect().toList()
        val driverId = "3"
        val team = "RedBull"
        val initialTeamSize = getTeamSize(controller, team)
        requireNotNull(initialTeamSize)
        val route = routes.first { it.path == "/team/{teamName}/drivers/{driverId}" && it.method == ArVerb.PUT }
        // Get team initial (drivers) size
        val res = route.handler.handle(
            /* routeArgs = */ mapOf(
                "teamName" to team,
                "driverId" to driverId
            ),
            /* queryArgs = */ emptyMap(),
            /* bodyArgs = */ mapOf(
                "name" to "driverTest",
                "driverId" to driverId,
                "active" to "true",
                "carPrice" to "20.000",
                "numberOfTrophies" to "null",
            ),
        )
        assertFailsWith<IllegalArgumentException> {
            res.get() as Driver
        }
    }

    @Test
    fun `try to add a driver to a team but request body values do not correspond to expected method parameter names`() {
        val controller = Formula1Controller()
        val routes = controller.autorouterReflect().toList()
        val driverId = "3"
        val team = "RedBull"
        val initialTeamSize = getTeamSize(controller, team)
        requireNotNull(initialTeamSize)
        val route = routes.first { it.path == "/team/{teamName}/drivers/{driverId}" && it.method == ArVerb.PUT }
        // Get team initial (drivers) size
        val res = route.handler.handle(
            /* routeArgs = */ mapOf(
                "teamName" to team,
                "driverId" to driverId
            ),
            /* queryArgs = */ emptyMap(),
            /* bodyArgs = */ mapOf(
                "name" to "driverTest",
                "driverId" to driverId,
                "active" to "true",
                "carPrice" to "20.000",
                "numberOfTrophies" to "null",
            ),
        )
        assertFailsWith<IllegalArgumentException> {
            res.get() as Driver
        }
    }

    @Test
    fun `try to add a driver but one of the body values is not a primitive type`() {
        TODO()
    }

    @Test
    fun `try to add a driver but send values that could not be converted to primitive types`() {
        TODO()
    }
}