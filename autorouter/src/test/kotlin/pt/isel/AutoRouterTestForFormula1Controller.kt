package pt.isel

import org.junit.jupiter.api.Test
import pt.isel.autorouter.ArHttpRoute
import pt.isel.autorouter.ArVerb
import pt.isel.autorouter.autorouterReflect
import pt.isel.classroom.Student
import pt.isel.formula1.Driver
import pt.isel.formula1.Formula1Controller
import kotlin.test.assertEquals

class AutoRouterTestForFormula1Controller {
    //Perguntar se é possivel por reflexao ver se um tipo e nullable
    //Para evitar martelada

    // Get the drivers
    // do tests with nullable params
    // properties in any Ar.
    // test other types of java not just the primitives, class with no primitive types
    //private data class Date(val x: Int)
    // private data class Track(val id: Int, val creationDate: Date)
    // give the class without constructor
    // give the class with empty constructor
    // do the same for other routes

    @Test
    fun remove_driver_by_driverId_via_reflection() {
        val controller = Formula1Controller()
        remove_driver_by_driverId(
            controller,
            controller.autorouterReflect().toList(),
        )
    }

    private fun remove_driver_by_driverId( controller: Formula1Controller, routes: List<ArHttpRoute>) {
        val r = routes.first { it.path == "/teams/{teamName}/drivers/{driverId}" && it.method == ArVerb.DELETE }
        val nrDrivers = controller.repo["Mercedes"]?.size ?: 0
        val res = r.handler.handle(
            mapOf("teamName" to "Mercedes", "driverId" to "5"),
            emptyMap(),
            emptyMap(),
        )
        assertEquals(
            Driver(5, "Valtteri Bottas", 902345.88, true, 3),
            res.get() as Driver
        )
        assertEquals(nrDrivers - 1, controller.repo["Mercedes"]?.size)
    }


}
