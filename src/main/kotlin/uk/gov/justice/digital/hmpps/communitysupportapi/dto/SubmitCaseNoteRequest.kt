package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import jakarta.validation.constraints.NotNull
import java.util.UUID

data class SubmitCaseNoteRequest(
  @field:NotNull(message = "The note title must not be null")
  val title: String,

  @field:NotNull(message = "The note details must not be null")
  val details: String,

  val shouldNotify: Boolean = false,

  val pathways: List<UUID> = emptyList(),
)
