package pt.isel.controllerTest

class Matriculation (plate: String, date: Date, country: String) {
    init {
        // plate need to be in the follow format: XX-00-00
        require(plate.matches(Regex("[A-Z]{2}-[0-9]{2}-[0-9]{2}")))
    }
}
