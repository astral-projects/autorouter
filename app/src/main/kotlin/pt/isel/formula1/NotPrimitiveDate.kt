package pt.isel.formula1

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

class NotPrimitiveDate(
    @JsonProperty("raceId") val date: LocalDate
)