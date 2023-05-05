package pt.isel.controllerTest

import com.fasterxml.jackson.annotation.JsonProperty

class Matriculation (
    @JsonProperty("plate") val plate: String,
    @JsonProperty("date") val date: Date,
    @JsonProperty("country") val country: String
) {
    init {
        // plate need to be in the follow format: XX-00-00
        require(plate.matches(Regex("[A-Z]{2}-[0-9]{2}-[0-9]{2}")))
    }
}
