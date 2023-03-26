package pt.isel.autorouter

import java.util.stream.Stream

fun Stream<ArHttpRoute>.jsonServer() = JsonServer(this)

fun Any.autorouterReflect(): Stream<ArHttpRoute> = AutoRouterReflect.autorouterReflect(this)

fun Any.autorouterDynamic(): Stream<ArHttpRoute> = AutoRouterDynamic.autorouterDynamic(this)