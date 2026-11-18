package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralNoteOrNonContactSessionRow
import uk.gov.justice.digital.hmpps.communitysupportapi.util.SHORT_DATE_TIME_FORMAT
import java.util.UUID

private const val DETAILS_MAX_WORDS = 40

data class ReferralNoteOrNonContactSessionBffDto(
  val id: UUID,
  val createdAt: String,
  val createdBy: String,
  val title: String,
  val details: String,
  val detailsTruncated: Boolean,
) {
  companion object {
    fun from(row: ReferralNoteOrNonContactSessionRow): ReferralNoteOrNonContactSessionBffDto {
      val truncatedDetails = row.details.truncateDetails()
      return ReferralNoteOrNonContactSessionBffDto(
        id = row.id,
        createdAt = row.createdAt.format(SHORT_DATE_TIME_FORMAT),
        createdBy = row.createdBy,
        title = row.title,
        details = truncatedDetails.text,
        detailsTruncated = truncatedDetails.truncated,
      )
    }
  }
}

private data class TruncatedDetails(val text: String, val truncated: Boolean)

private fun String.truncateDetails(): TruncatedDetails {
  val words = trim().split(Regex("\\s+"))
  return if (words.size <= DETAILS_MAX_WORDS) {
    TruncatedDetails(this, truncated = false)
  } else {
    TruncatedDetails(words.take(DETAILS_MAX_WORDS).joinToString(" "), truncated = true)
  }
}
