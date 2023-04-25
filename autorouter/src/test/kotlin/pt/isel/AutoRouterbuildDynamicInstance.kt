package pt.isel

import org.junit.jupiter.api.Test
import pt.isel.classroom.Student
import pt.isel.autorouter.buildStudentInstance


class AutoRouterbuildDynamicInstance {
    private val nr = 37123
    private val name = "John Doe"
    private val group = 13
    private val semester = 2

    @Test
    fun `build a Student instance`() {
        val expected = Student(nr, name, group, semester)
        val actual = buildStudentInstance().createInstance(mapOf(
            "nr" to nr.toString(),
            "name" to name,
            "group" to group.toString(),
            "semester" to semester.toString()
        ))
        println("expecteed = $expected")
        println("actual = $actual")
    }
}