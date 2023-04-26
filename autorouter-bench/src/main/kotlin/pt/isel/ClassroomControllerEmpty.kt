package pt.isel

import pt.isel.autorouter.ArVerb
import pt.isel.autorouter.annotations.ArBody
import pt.isel.autorouter.annotations.ArQuery
import pt.isel.autorouter.annotations.ArRoute
import pt.isel.autorouter.annotations.AutoRouter
import pt.isel.autorouter.Student
import java.util.*

class ClassroomControllerEmpty {

    /**
     * Immutable. No addition and changes allowed.
     */
    val repo = mapOf(
        "i41d" to listOf(
            Student(7236, "Jonas Mancas Lubri", 56, 4),
        ),
        "i42d" to listOf(
            Student(9876, "Ole Super", 7, 5),
        )
    )
    @Synchronized
    @AutoRouter("/classroom/{classroom}")
    fun search(@ArRoute classroom: String, @ArQuery student: String?): Optional<List<Student>> {
        return repo[classroom]
            ?.let {
                if(student == null) Optional.of(it)
                else Optional.of(it.filter { st -> st.name.lowercase().contains(student.lowercase()) })
            }
            ?: Optional.empty()
    }

    /**
     * Example:
     *   curl --header "Content-Type: application/json" \
     *     --request PUT \
     *     --data '{"nr": "7777", "name":"Ze Gato","group":"11", "semester":"3"}' \
     *     http://localhost:4000/classroom/i42d/students/7777
     */
    @Synchronized
    @AutoRouter("/classroom/{classroom}/students/{nr}", method = ArVerb.PUT)
    fun addStudent(
        @ArRoute classroom: String,
        @ArRoute nr: Int,
        @ArBody s: Student
    ): Optional<Student> {
        return Optional.of(s)
    }
    /**
     * Example:
     *   curl --request DELETE http://localhost:4000/classroom/i42d/students/4536
     */
    @Synchronized
    @AutoRouter("/classroom/{classroom}/students/{nr}", method = ArVerb.DELETE)
    fun removeStudent(@ArRoute classroom: String, @ArRoute nr: Int) : Optional<Student> {
        val stds = repo[classroom] ?: return Optional.empty()
        val s = stds.firstOrNull { it.nr == nr } ?: return Optional.empty()
        return Optional.of(s)
    }
}