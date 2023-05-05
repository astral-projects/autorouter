package pt.isel.controllerTest

import com.fasterxml.jackson.annotation.JsonProperty

class Matriculation (
    @JsonProperty("plate") val plate: String,
    @JsonProperty("date") val date: Date,
    @JsonProperty("country") val country: String
)
