package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime
import java.util.UUID

data class SubmitNonContactSessionRequest(
  @field:NotNull(message = "The session title must not be null")
  val title: String,

  @field:NotNull(message = "The session details must not be null")
  val details: String,

  @field:NotNull(message = "The session start date must not be null")
  val startDate: OffsetDateTime,

  @field:NotNull(message = "The session hours duration must not be null")
  val durationHours: Int,

  @field:NotNull(message = "The session minute duration must not be null")
  val durationMinutes: Int,

  @field:NotEmpty(message = "The session must have at least one pathway")
  val pathways: List<UUID>,
)
