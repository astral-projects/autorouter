package pt.isel

import org.junit.jupiter.api.Test
import pt.isel.autorouter.instance.buildStudentInstanceWithClass
import pt.isel.autorouter.instance.buildStudentInstance
import pt.isel.autorouter.instance.buildAnyInstanceWithClass
import kotlin.test.assertEquals

class BuildInstanceWithClass {
    private val nr = 9876
    private val name = "Ole Super"
    private val group = 7
    private val semester = 5
    private val bodyArgs = mapOf(
        "nr" to nr.toString(),
        "name" to name,
        "group" to group.toString(),
        "semester" to semester.toString()
    )
    private val bodyArgsJava = mapOf(
        "arg0" to nr.toString(),
        "arg1" to name,
        "arg2" to group.toString(),
        "arg3" to semester.toString()
    )

    @Test
    fun `build a Student instance with a builder made directly from the Student class`() {
        val expected = Student(nr, name, group, semester)
        val actual: Student = buildStudentInstance().createInstance(bodyArgs)
        assertEquals(expected, actual)
    }

    @Test
    fun `build a Student instance with a builder which does not know about Student class`() {
        val expected = Student(nr, name, group, semester)
        val actual: Any = buildStudentInstanceWithClass().createInstance(Student::class.java, bodyArgs)
        assertEquals(expected, actual as Student)
    }

    @Test
    fun `build Student instance with a generic builder`() {
        val expected = Student(nr, name, group, semester)
        val actual: Any = buildAnyInstanceWithClass().createInstance(Student::class.java, bodyArgsJava)
        assertEquals(expected, actual as Student)
    }
}