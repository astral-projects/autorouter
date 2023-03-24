package pt.isel

import pt.isel.autorouter.autorouterReflect
import pt.isel.autorouter.jsonServer

fun main() {
    Formula1().autorouterReflect().jsonServer().start(4000)
}
