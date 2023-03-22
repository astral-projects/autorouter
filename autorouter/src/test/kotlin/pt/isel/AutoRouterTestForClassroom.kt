/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package pt.isel

import pt.isel.autorouter.ArHttpRoute
import pt.isel.autorouter.autorouterDynamic
import pt.isel.autorouter.autorouterReflect
import kotlin.test.Test
import kotlin.test.assertContentEquals

class AutoRouterTestForClassroom {

    @Test fun get_students_via_reflection() {
        get_students(
            ClassroomController().autorouterReflect().toList(),
        )
    }

    @Test fun get_students_via_dynamic() {
        get_students(
            ClassroomController().autorouterDynamic().toList(),
        )
    }

    private fun get_students(routes: List<ArHttpRoute>) {
        val r = routes.first { it.path == "/classroom/{classroom}" }
        val res = r.handler.handle(
            mapOf("classroom" to "i42d"),
            emptyMap(),
            emptyMap(),
        )
        assertContentEquals(
            listOf(
                Student(9876, "Ole Super", 7, 5),
                Student(4536, "Isel Maior", 7, 5),
                Student(5689, "Ever Sad", 7, 3),
            ),
            res.get() as List<Student>,
        )
    }

    @Test fun get_students_with_name_containing_word_via_reflection() {
        get_students_with_name_containing_word(
            ClassroomController().autorouterReflect().toList(),
        )
    }

    @Test fun get_students_with_name_containing_word_via_dynamic() {
        get_students_with_name_containing_word(
            ClassroomController().autorouterDynamic().toList(),
        )
    }

    private fun get_students_with_name_containing_word(routes: List<ArHttpRoute>) {
        val r = routes.first { it.path == "/classroom/{classroom}" }
        val res = r.handler.handle(
            mapOf("classroom" to "i42d"),
            mapOf("student" to "maior"),
            emptyMap(),
        )
        assertContentEquals(
            listOf(Student(4536, "Isel Maior", 7, 5)),
            res.get() as List<Student>,
        )
    }
}
