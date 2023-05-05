package pt.isel

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pt.isel.autorouter.ArHttpRoute
import pt.isel.autorouter.autorouterReflect
import pt.isel.formula1.Driver
import pt.isel.formula1.Formula1Controller
import java.security.SecureRandom
import kotlin.test.assertContentEquals

class TestComplex {
    /*
    private fun getTeamSize(controller: Formula1Controller, team: String) = controller.repo[team]?.size ?: 0
    private val randomNumber: Int
        get() = SecureRandom().nextInt(Int.MAX_VALUE)
    private lateinit var controller:

    @BeforeEach
    fun setup() {
        controller = ControllerTest()
    }


    @Test
    fun getMyComplexClasss(){
        getComplex(controller.autorouterReflect().toList())
    }

    private fun getComplex(routes: List<ArHttpRoute>) {
        val res = routes.first { it.path == "" }.handler.handle(
            mapOf("teamName" to "Ferrari"),
            emptyMap(),
            emptyMap()
        )
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
        assertFailsWith<RuntimeException> {
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
    fun `check if an exception in thrown if a method parameter is not annotated with @Ar type annotation `() {
        val routes = controller.autorouterReflect().toList()
        val route = routes.first { it.path == "/teams/{teamName}/drivers/{driverId}/annot"
                && it.method == ArVerb.DELETE }
        val team = "Mercedes"
        val nrDrivers = getTeamSize(controller, team)
        assertFailsWith<RuntimeException> {
            val res = route.handler.handle(
                mapOf("teamName" to team, "driverId" to "5"),
                emptyMap(),
                emptyMap(),
            )
        }
    }
*/
}