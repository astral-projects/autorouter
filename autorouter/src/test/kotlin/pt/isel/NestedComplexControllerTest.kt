package pt.isel

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pt.isel.autorouter.ArHttpRoute
import pt.isel.autorouter.autorouterDynamic
import pt.isel.autorouter.autorouterReflect
import pt.isel.`complex-controller`.*
import kotlin.test.assertFailsWith

class NestedComplexControllerTest {

    private lateinit var controller: NestedComplexController

    @BeforeEach
    fun setup() {
        controller = NestedComplexController()
    }

    @Test
    fun `A complex type that has other complex types is send in the same map via reflect`() {
        testNestedComplexTypes(controller.autorouterReflect().toList())
    }

    @Test
    fun `A complex type that has other complex types is send in the same map via dynamic`() {
        testNestedComplexTypes(controller.autorouterDynamic().toList())
    }

    private fun testNestedComplexTypes(routes: List<ArHttpRoute>) {
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
    fun `try to add a road but send two constructor parameters with the same name`() {
        val routes = controller.autorouterReflect().toList()
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
                    "date" to "${vechile.matriculation.date.myDate}", // Parameters with the same name should throw error
                    "country" to "${vechile.matriculation.country}",
                    "velocity" to "${vechile.velocity}"
                )
            )
        }
    }

    @Test
    fun `try to add but send values with incorrect name parameter`() {
        val routes = controller.autorouterReflect().toList()
        val vechile = VehicleType("car", "Audi", Matriculation("AA-00-00", Date("12-05-2003"), "Portugal"), 130.0)
        assertFailsWith<RuntimeException> {
            val res = routes.first { it.path == "/road/{road}/loc/{location}" }.handler.handle(
                mapOf("roadName" to "A22", "location" to "Lisbon"),
                mapOf("velocity" to "120.00"),
                mapOf(
                    "type" to "${vechile.type}",
                    "brand" to "${vechile.brand}",
                    "matriculation " to "${vechile.matriculation}",
                    "1231312314141511515" to "${vechile.matriculation.plate}", // This one is incorrect
                    "date" to "${vechile.matriculation.date}",
                    "myDate" to "${vechile.matriculation.date.myDate}",
                    "country" to "${vechile.matriculation.country}",
                    "velocity" to "${vechile.velocity}"
                )
            )
        }
    }

}