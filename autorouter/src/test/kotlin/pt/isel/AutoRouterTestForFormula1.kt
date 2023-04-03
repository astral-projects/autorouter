package pt.isel

import org.junit.jupiter.api.Test
import pt.isel.autorouter.ArVerb
import pt.isel.autorouter.autorouterReflect
import pt.isel.autorouter.exceptions.ArTypeAnnotationNotFoundException
import pt.isel.formula1.Driver
import pt.isel.formula1.Formula1Controller
import pt.isel.formula1.NotPrimitiveDate
import pt.isel.formula1.RaceTrack
import java.security.SecureRandom
import java.time.LocalDate
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AutoRouterTestForFormula1 {
    private fun getTeamSize(controller: Formula1Controller, team: String) = controller.repo[team]?.size ?: 0
    private val randomNumber: Int
        get() = SecureRandom().nextInt(Int.MAX_VALUE)
    private val controller = Formula1Controller

    @Test
    fun `get all drivers from the Ferrari team`() {
        val routes = controller.autorouterReflect().toList()
        val res = routes.first { it.path == "/teams/{teamName}" }.handler.handle(
            mapOf("teamName" to "Ferrari"),
            emptyMap(),
            emptyMap()
        )
        println(res)
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
    fun `get driver named Versttappen`() {
        val routes = controller.autorouterReflect().toList()
        val res = routes.first { it.path == "/teams/{teamName}" }.handler.handle(
            mapOf("teamName" to "RedBull"),
            mapOf("driver" to "Max Verstappen", "active" to "true"),
            emptyMap()
        )
        val driver = (res.get() as List<Driver>).first()
        assertEquals(Driver(1, "Max Verstappen", 340000.50, true, 1), driver)
        assertEquals("Max Verstappen", driver.name)
        assertEquals(true, driver.active)
        assertEquals(1, driver.nrTrophies)
    }

    @Test
    fun `get active drivers of the Ferrari team`() {
        val routes = controller.autorouterReflect().toList()
        val res = routes.first { it.path == "/teams/{teamName}" }.handler.handle(
            mapOf("teamName" to "Ferrari"),
            mapOf("active" to "true"),
            emptyMap()
        )
        val drivers = (res.get() as List<Driver>)
        assertEquals(2, drivers.size)
        assertContentEquals(
            listOf(
                Driver(7, "Charles Leclerc", 654792.45, true, 6),
                Driver(9, "Carlos Sainz", 126789.12, true, 2)
            ), drivers
        )
    }

    @Test
    fun `remove a Mercedes driver by id`() {
        val routes = controller.autorouterReflect().toList()
        val route = routes.first { it.path == "/teams/{teamName}/drivers/{driverId}" && it.method == ArVerb.DELETE }
        val team = "Mercedes"
        val nrDrivers = getTeamSize(controller, team)
        val res = route.handler.handle(
            mapOf("teamName" to team, "driverId" to "5"),
            emptyMap(),
            emptyMap(),
        )
        assertEquals(
            Driver(5, "Valtteri Bottas", 902345.88, true, 3),
            res.get() as Driver
        )
        assertEquals(nrDrivers - 1, controller.repo["Mercedes"]?.size)
    }

    @Test
    fun `add a driver by id to the RedBull team`() {
        val routes = controller.autorouterReflect().toList()
        val driverId = randomNumber
        val team = "RedBull"
        val initialTeamSize = getTeamSize(controller, team)
        val route = routes.first {
            it.path == "/teams/{teamName}/drivers/{driverId}" && it.method == ArVerb.PUT
        }
        // Create driver
        val newDriver = Driver(driverId, "driverName", 250.0, true, 3)
        val res = route.handler.handle(
            mapOf("teamName" to team, "driverId" to "${newDriver.driverId}"),
            emptyMap(),
            mapOf(
                "driverId" to "${newDriver.driverId}",
                "name" to newDriver.name,
                "carPrice" to "${newDriver.carPrice}",
                "active" to "${newDriver.active}",
                "nrTrophies" to "${newDriver.nrTrophies}"
            )
        )
        assertEquals(
            newDriver,
            res.get() as Driver,
        )
        assertEquals(initialTeamSize + 1, getTeamSize(controller, team))
    }

    @Test
    fun `when trying to send a non primitive type constructor parameter, declaration order should not matter`() {
        val routes = controller.autorouterReflect().toList()
        val driverId = randomNumber
        val team = "RedBull"
        val initialTeamSize = getTeamSize(controller, team)
        val route = routes.first {
            it.path == "/teams/{teamName}/drivers/{driverId}" && it.method == ArVerb.PUT
        }
        // Create driver
        val newDriver = Driver(driverId, "driverName", 250.0, true, 3)
        val res = route.handler.handle(
            mapOf("teamName" to team, "driverId" to "${newDriver.driverId}"),
            emptyMap(),
            mapOf(
                "active" to "${newDriver.active}",
                "nrTrophies" to "${newDriver.nrTrophies}",
                "name" to newDriver.name,
                "driverId" to "${newDriver.driverId}",
                "carPrice" to "${newDriver.carPrice}",
            )
        )
        assertEquals(
            newDriver,
            res.get() as Driver,
        )
        assertEquals(initialTeamSize + 1, getTeamSize(controller, team))
    }

    @Test
    fun `try to add a driver to a team but request body values do not correspond to expected method parameter names`() {
        val routes = controller.autorouterReflect().toList()
        val driverId = randomNumber
        val team = "RedBull"
        val route = routes.first {
            it.path == "/teams/{teamName}/drivers/{driverId}" && it.method == ArVerb.PUT
        }
        // Create driver
        val newDriver = Driver(driverId, "driverName", 250.0, true, 3)
        assertFailsWith<RuntimeException> {
            route.handler.handle(
                mapOf("teamName" to team, "driverId" to "${newDriver.driverId}"),
                emptyMap(),
                mapOf(
                    "active" to "${newDriver.active}",
                    "nrTrophies" to "${newDriver.nrTrophies}",
                    // should be "name", since the value is not found is parsed as null thus failing in object creation
                    "name?" to newDriver.name,
                    "driverId" to "${newDriver.driverId}",
                    "carPrice" to "${newDriver.carPrice}",
                )
            )
        }
    }

    @Test
    fun `try to add a driver but one of the body values is not a primitive type`() {
        val routes = controller.autorouterReflect().toList()
        val team = "RedBull"
        val route = routes.first {
            it.path == "/teams/{teamName}/drivers/{driverId}/date" && it.method == ArVerb.PUT
        }
        assertFailsWith<NumberFormatException> {
            route.handler.handle(
                mapOf("teamName" to team, "driverId" to randomNumber.toString()),
                mapOf(
                    // Add a parameter which is not of type primitive and holds at least one parameter
                    // which is also not of type primitive
                    "joinDate" to NotPrimitiveDate(LocalDate.now()).toString(),
                ),
                emptyMap()
            )
        }
    }

    @Test
    fun `try to add a driver but send values that could not be converted to primitive types`() {
        val routes = controller.autorouterReflect().toList()
        val driverId = randomNumber
        val team = "RedBull"
        val route = routes.first {
            it.path == "/teams/{teamName}/drivers/{driverId}" && it.method == ArVerb.PUT
        }
        // Create driver
        val newDriver = Driver(driverId, "driverName", 250.0, true, 3)
        assertFailsWith<Exception> {
            route.handler.handle(
                mapOf("teamName" to team, "driverId" to "${newDriver.driverId}"),
                emptyMap(),
                mapOf(
                    "driverId" to "${newDriver.driverId}",
                    "name" to newDriver.name,
                    "carPrice" to "------", // Expecting a double
                    "active" to "${newDriver.active}",
                    "nrTrophies" to "${newDriver.nrTrophies}"
                )
            )
        }
    }

    @Test
    fun `try to add a driver but send values that can be grouped to more than one type`() {
        val routes = controller.autorouterReflect().toList()
        val driverId = randomNumber
        val team = "RedBull"
        val route = routes.first {
            it.path == "/teams/{teamName}/drivers/{driverId}/races" && it.method == ArVerb.PUT
        }
        // Create driver
        val newRace = RaceTrack(randomNumber, "road circuit", "Casablanca")
        val newDriver = Driver(driverId, "driverName", 250.0, true, 3)
        val res = route.handler.handle(
            mapOf("teamName" to team, "driverId" to "${newDriver.driverId}"),
            emptyMap(),
            mapOf(
                "active" to "${newDriver.active}",
                "nrTrophies" to "${newDriver.nrTrophies}",
                "location" to newRace.location,
                "name" to newDriver.name,
                "driverId" to "${newDriver.driverId}",
                "raceId" to "${newRace.raceId}",
                "carPrice" to "${newDriver.carPrice}",
                "circuit" to newRace.circuit,
            )
        )
        assertEquals(
            listOf(newDriver, newRace),
            res.get()
        )
    }

    @Test
    fun `check if an exception in thrown if a method parameter is not annotated with @Ar type annotation `() {
        val routes = controller.autorouterReflect().toList()
        val route = routes.first { it.path == "/teams/{teamName}/drivers/{driverId}/annot"
                && it.method == ArVerb.DELETE }
        val team = "Mercedes"
        val nrDrivers = getTeamSize(controller, team)
        assertFailsWith<ArTypeAnnotationNotFoundException> {
            val res = route.handler.handle(
                mapOf("teamName" to team, "driverId" to "5"),
                emptyMap(),
                emptyMap(),
            )
        }
    }
}