package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralNote
import java.util.UUID

data class SubmitCaseNoteRequest(
  @field:NotNull(message = "The note title must not be null")
  val title: String,

  @field:NotNull(message = "The note details must not be null")
  val details: String,

  val shouldNotify: Boolean = false,

  val pathways: List<UUID> = emptyList(),
)

fun SubmitCaseNoteRequest.toReferralNote(
  referralId: UUID,
  createdBy: String,
  createdAt: java.time.OffsetDateTime,
): ReferralNote = ReferralNote(
  id = UUID.randomUUID(),
  referralId = referralId,
  createdAt = createdAt,
  createdBy = createdBy,
  title = title,
  details = details,
  shouldNotify = shouldNotify,
)
