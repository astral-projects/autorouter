package pt.isel

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pt.isel.autorouter.ArHttpRoute
import pt.isel.autorouter.autorouterReflect
import pt.isel.controllerTest.*
import kotlin.test.assertFailsWith

class TestComplex {

    private lateinit var controller: ControllerTest

    @BeforeEach
    fun setup() {
        controller = ControllerTest()
    }

    /*
    * Test that verifies the following conditions:
    * when trying to send a non-primitive type constructor parameter, declaration order should not matter
    * try to add a driver to a team but request body values do not correspond to expected method parameter names
    * try to add a driver but one of the body values is not a primitive type */
    @Test
    fun getMyComplexClasss() {
        addRoad(controller.autorouterReflect().toList())
    }

    private fun addRoad(routes: List<ArHttpRoute>) {
        val vechile = VehicleType("car", "Audi", Matriculation("AA-00-00", Date("12-05-2003"), "Portugal"), 130.0)
        val res = routes.first { it.path == "/road/{road}/loc/{location}" }.handler.handle(
            mapOf("roadName" to "A22", "location" to "Lisbon"),
            mapOf("velocity" to "120.00"),
            mapOf(
                "type" to "${vechile.type}",
                "brand" to "${vechile.brand}",
                "matriculation" to "${vechile.matriculation}",
                "plate" to "${vechile.matriculation.plate}",
                "date" to "${vechile.matriculation.date}",
                "myDate" to "${vechile.matriculation.date.myDate}",
                "country" to "${vechile.matriculation.country}",
                "velocity" to "${vechile.velocity}"
            )
        )
        assertEquals(
            Road("A22", "Lisbon", vechile),
            res.get() as Road
        )
    }

    @Test
    fun testTofail2() {
        `try to add but two consturcotr parameter with the same name `(controller.autorouterReflect().toList())
    }

    private fun `try to add but two consturcotr parameter with the same name `(routes: List<ArHttpRoute>) {
        val vechile = VehicleType("car", "Audi", Matriculation("AA-00-00", Date("12-05-2003"), "Portugal"), 130.0)
        assertFailsWith<RuntimeException> {
            val res = routes.first { it.path == "/road/{road}/loc/{location}" }.handler.handle(
                mapOf("roadName" to "A22", "location" to "Lisbon"),
                mapOf("velocity" to "120.00"),
                mapOf(
                    "type" to "${vechile.type}",
                    "brand" to "${vechile.brand}",
                    "matriculation" to "${vechile.matriculation}",
                    "plate" to "${vechile.matriculation.plate}",
                    "date" to "${vechile.matriculation.date}",
                    "date" to "${vechile.matriculation.date.myDate}",//Parameters with the same name should
                    "country" to "${vechile.matriculation.country}",
                    "velocity" to "${vechile.velocity}"
                )
            )
        }
    }


    @Test
    fun testTofail() {
        `try to add but send values with incorrect name parameter`(controller.autorouterReflect().toList())
    }

    private fun `try to add but send values with incorrect name parameter`(routes: List<ArHttpRoute>) {
        val vechile = VehicleType("car", "Audi", Matriculation("AA-00-00", Date("12-05-2003"), "Portugal"), 130.0)
        assertFailsWith<RuntimeException> {
            val res = routes.first { it.path == "/road/{road}/loc/{location}" }.handler.handle(
                mapOf("roadName" to "A22", "location" to "Lisbon"),
                mapOf("velocity" to "120.00"),
                mapOf(
                    "type" to "${vechile.type}",
                    "brand" to "${vechile.brand}",
                    "matriculation123" to "${vechile.matriculation}", //This one is incorrect
                    "plate" to "${vechile.matriculation.plate}",
                    "date" to "${vechile.matriculation.date}",
                    "myDate" to "${vechile.matriculation.date.myDate}",
                    "country" to "${vechile.matriculation.country}",
                    "velocity" to "${vechile.velocity}"
                )
            )
        }
    }

    @Test
    fun `Should throw exception when a method parameter does not have an @ArType annotation`() {
        val routes = controller.autorouterReflect().toList()
        val handler = routes.first { it.path == "/road/{road}" }.handler
        assertFailsWith<RuntimeException> {
            handler.handle(
                mapOf("roadName" to "A22", "location" to "Lisbon"),
                emptyMap(),
                emptyMap()
            )
        }
    }
}