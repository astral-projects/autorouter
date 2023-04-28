package pt.isel

import org.junit.jupiter.api.BeforeEach
import pt.isel.autorouter.ArHttpRoute
import pt.isel.autorouter.ArVerb
import pt.isel.autorouter.autorouterDynamic
import pt.isel.autorouter.autorouterReflect
import pt.isel.formula1.Driver
import pt.isel.formula1.Formula1Controller
import pt.isel.formula1.RaceTrack
import java.security.SecureRandom
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class AutoRouterTestForFormula1 {
    private fun getTeamSize(controller: Formula1Controller, team: String) = controller.repo[team]?.size ?: 0
    private val randomNumber: Int
        get() = SecureRandom().nextInt(Int.MAX_VALUE)
    private lateinit var controller: Formula1Controller

    @BeforeEach
    fun setup() {
        controller = Formula1Controller()
    }

    @Test
    fun `get all drivers from ferrari with reflect`() {
        getAllDriversFromFerrari(controller.autorouterReflect().toList())
    }

    @Test
    fun `get all drivers from ferrari with dynamic`() {
        getAllDriversFromFerrari(controller.autorouterDynamic().toList())
    }

    private fun getAllDriversFromFerrari(routes: List<ArHttpRoute>) {
        val res = routes.first { it.path == "/teams/{teamName}" }.handler.handle(
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
    fun `get driver named Versttappen with reflect`() {
        getDriverNamedVersstappen(controller.autorouterReflect().toList())
    }

    @Test
    fun `get driver named Versttappen with dynamic`() {
        getDriverNamedVersstappen(controller.autorouterDynamic().toList())
    }


    private fun getDriverNamedVersstappen(routes: List<ArHttpRoute>) {
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
    fun `get active drivers of the Ferrari team with reflect`(){
        getActiveDriversFerrariTeam(controller.autorouterReflect().toList())
    }

    @Test
    fun `get active drivers of the Ferrari team with dynamic`(){
        getActiveDriversFerrariTeam(controller.autorouterDynamic().toList())
    }

    private fun getActiveDriversFerrariTeam(routes:List<ArHttpRoute>) {
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
    fun `remove driver with dynamic`() {
        removeADriverFromTheMercedesTeam(
            controller.autorouterDynamic().toList()
        )
    }

    @Test
    fun `remove driver with reflect`() {
        removeADriverFromTheMercedesTeam(
            controller.autorouterReflect().toList()
        )
    }

    private fun removeADriverFromTheMercedesTeam(routes: List<ArHttpRoute>) {
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
    fun `add a driver by id to the RedBull team with reflect`() {
        addADriverByItToTheRedBullTeam(
            controller.autorouterReflect().toList()
        )
    }

    @Test
    fun `add a driver by id to the RedBull team with dynamic`() {
        addADriverByItToTheRedBullTeam(
            controller.autorouterDynamic().toList()
        )
    }

    private fun addADriverByItToTheRedBullTeam(routes: List<ArHttpRoute>) {
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
    fun `try to add a driver but send values that can be grouped to more than one type with reflect`() {
        addADriverByIdWithMoreThanOneComplexType(
            controller.autorouterReflect().toList()
        )
    }

    @Test
    fun `try to add a driver but send values that can be grouped to more than one type with dynamic`() {
        addADriverByIdWithMoreThanOneComplexType(
            controller.autorouterDynamic().toList()
        )
    }

    private fun addADriverByIdWithMoreThanOneComplexType(routes: List<ArHttpRoute>) {
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

}