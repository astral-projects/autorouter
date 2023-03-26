package pt.isel

import pt.isel.autorouter.autorouterReflect
import pt.isel.autorouter.jsonServer
import pt.isel.classroom.ClassroomController

fun main() {
    ClassroomController().autorouterReflect().jsonServer().start(4000)
}
