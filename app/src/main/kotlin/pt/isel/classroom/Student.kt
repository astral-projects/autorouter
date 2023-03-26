package pt.isel.classroom

import com.fasterxml.jackson.annotation.JsonProperty

data class Student constructor(
    @JsonProperty("nr") val nr: Int,
    @JsonProperty("name") val name: String,
    @JsonProperty("group") val group: Int,
    @JsonProperty("semester") val semester: Int)