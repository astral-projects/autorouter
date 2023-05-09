package pt.isel.`complex-controller`

import com.fasterxml.jackson.annotation.JsonProperty

data class Matriculation (
    @JsonProperty("plate") val plate: String,
    @JsonProperty("date") val date: Date,
    @JsonProperty("country") val country: String
)
