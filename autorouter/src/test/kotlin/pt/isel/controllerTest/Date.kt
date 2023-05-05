package pt.isel.controllerTest

class Date (date: String) {
    init {
        // date need to be in the follow format: DD-MM-YYYY
        require(date.matches(Regex("[0-9]{2}-[0-9]{2}-[0-9]{4}")))
    }
}
