package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralNonContactSession
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralNote
import uk.gov.justice.digital.hmpps.communitysupportapi.util.SHORT_DATE_TIME_FORMAT
import java.util.UUID

private const val DETAILS_MAX_LENGTH = 250

data class ReferralNotesAndNonContactSessionsBffResponseDto(
  val notesAndNonContactSessions: List<ReferralNoteOrNonContactSessionBffDto>,
)

data class ReferralNoteOrNonContactSessionBffDto(
  val id: UUID,
  val createdAt: String,
  val createdBy: String,
  val title: String,
  val details: String,
  val detailsTruncated: Boolean,
) {
  companion object {
    fun fromNote(note: ReferralNote): ReferralNoteOrNonContactSessionBffDto {
      val truncatedDetails = note.details.truncateDetails()
      return ReferralNoteOrNonContactSessionBffDto(
        id = note.id,
        createdAt = note.createdAt.format(SHORT_DATE_TIME_FORMAT),
        createdBy = note.createdBy,
        title = note.title,
        details = truncatedDetails.text,
        detailsTruncated = truncatedDetails.truncated,
      )
    }

    fun fromNonContactSession(session: ReferralNonContactSession): ReferralNoteOrNonContactSessionBffDto {
      val truncatedDetails = session.details.truncateDetails()
      return ReferralNoteOrNonContactSessionBffDto(
        id = session.id,
        createdAt = session.createdAt.format(SHORT_DATE_TIME_FORMAT),
        createdBy = session.createdBy,
        title = session.title,
        details = truncatedDetails.text,
        detailsTruncated = truncatedDetails.truncated,
      )
    }
  }
}

private data class TruncatedDetails(val text: String, val truncated: Boolean)

private fun String.truncateDetails(): TruncatedDetails = if (length <= DETAILS_MAX_LENGTH) {
  TruncatedDetails(this, truncated = false)
} else {
  TruncatedDetails(take(DETAILS_MAX_LENGTH).trimEnd() + "...", truncated = true)
}
