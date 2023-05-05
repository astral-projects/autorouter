package pt.isel.controllerTest

class ControllerTest {
    val repo = mutableMapOf(
        "car" to listOf(
            Road("A22", "Lisbon", VehicleType("car","Audi",Matriculation("AA-00-00", Date("12-05-2003"), "Portugal"), 130.0)),
            Road("A22", "Porto", VehicleType("car", "BMW", Matriculation("AA-12-00", Date("12-09-2020"), "Portugal"), 200.6)),
            Road("A22", "Faro", VehicleType("car", "Mercedes", Matriculation("AA-00-12", Date("12-05-2023"), "Berlim"), 150.2)),
            Road("A22", "Lisbon", VehicleType("car", "Renault", Matriculation("BB-12-BL", Date("25-09-2013"), "Alemanha"), 149.6)),
        ),
        "motorcycle" to listOf(
            Road("A22", "Lisbon", VehicleType("motorcycle","Yamaha", Matriculation("AZ-00-BA", Date("12-05-2003"), "Portugal"), 130.0)),
            Road("A22", "Porto", VehicleType("motorcycle","Honda", Matriculation("AZ-12-BA", Date("12-09-2020"), "Escócia"), 200.6)),
            Road("A22", "Faro", VehicleType("motorcycle", "Suzuki", Matriculation("AZ-MK-09", Date("12-05-2023"), "Portugal"), 300.9)),
            Road("A22", "Lisbon", VehicleType("motorcycle", "Kawasaki", Matriculation("89-UA-14", Date("25-09-2013"), "Portugal"), 149.8)),
        )
    )
}
