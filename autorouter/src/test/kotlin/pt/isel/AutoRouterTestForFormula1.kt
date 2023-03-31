package pt.isel

import org.junit.jupiter.api.Test
import pt.isel.autorouter.ArVerb
import pt.isel.autorouter.autorouterReflect
import pt.isel.classroom.Student
import pt.isel.formula1.Driver
import pt.isel.formula1.Formula1Controller
import kotlin.streams.toList
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AutoRouterTestForFormula1 {
    @Test
    fun getDriversFromFerrari(){
        val controller=Formula1Controller()
        val routes = controller.autorouterReflect().toList()
        val res =  routes.first { it.path == "/teams/{teamName}"}.handler.handle(
            mapOf("teamName" to "Ferrari"),
            emptyMap(),
            emptyMap())
        assertContentEquals(
            listOf(
                Driver(7, "Charles Leclerc", 654792.45, true, 6),
                Driver(8, "Sebastian Vettel", 789675.45, false, null),
                Driver(9, "Carlos Sainz", 126789.12, true, 2),
            ),
            res.get() as List<Driver>
        )
    }
    @Test
    fun getDriverVersttappen(){
        val controller=Formula1Controller()
        val routes = controller.autorouterReflect().toList()
        val res =  routes.first { it.path == "/teams/{teamName}"}.handler.handle(
            mapOf("teamName" to "RedBull"),
            mapOf("driver" to "Max Verstappen"),
            mapOf("active" to "true"))
        val driver = (res.get() as List<Driver>).first()
        assertEquals(Driver(1, "Max Verstappen", 340000.50, true, 1), driver)
        assertEquals("Max Verstappen",driver.name)
        assertEquals(true, driver.active)
        assertEquals(1,driver.nrTrophies)
    }
    @Test
    fun getAtiveDrivers(){
        val controller=Formula1Controller()
        val routes = controller.autorouterReflect().toList()
        val res =  routes.first { it.path == "/teams/{teamName}"}.handler.handle(
            mapOf("teamName" to "Ferrari"),
            mapOf("active" to "true"),
            emptyMap())
        val driver = (res.get() as List<Driver>)
        assertContentEquals(listOf(
            Driver(7, "Charles Leclerc", 654792.45, true, 6),
            Driver(9, "Carlos Sainz", 126789.12, true, 2))
            ,driver)
        assertEquals(2,driver.size)
        assertEquals(
            Driver(9, "Carlos Sainz", 126789.12, true, 2)
            ,driver.find { it.driverId==9 })
        assertEquals(
            Driver(7, "Charles Leclerc", 654792.45, true, 6)
            ,driver.find { it.driverId==7 })
    }


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