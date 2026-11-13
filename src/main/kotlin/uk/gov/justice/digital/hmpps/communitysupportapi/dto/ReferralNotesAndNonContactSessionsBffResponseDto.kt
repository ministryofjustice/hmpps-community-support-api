package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralNonContactSession
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralNote
import uk.gov.justice.digital.hmpps.communitysupportapi.util.SHORT_DATE_FORMAT
import uk.gov.justice.digital.hmpps.communitysupportapi.util.SHORT_DATE_TIME_FORMAT
import java.util.UUID

private const val DESCRIPTION_MAX_LENGTH = 250

data class ReferralNotesAndNonContactSessionsBffResponseDto(
  val notesAndNonContactSessions: List<ReferralNoteOrNonContactSessionBffDto>,
)

data class ReferralNoteOrNonContactSessionBffDto(
  val id: UUID,
  val createdAt: String,
  val title: String,
  val descriptionRichText: String,
) {
  companion object {
    fun fromNote(note: ReferralNote): ReferralNoteOrNonContactSessionBffDto = ReferralNoteOrNonContactSessionBffDto(
      id = note.id,
      createdAt = note.createdAt.format(SHORT_DATE_TIME_FORMAT),
      title = note.title,
      descriptionRichText = note.details.truncate(),
    )

    fun fromNonContactSession(session: ReferralNonContactSession): ReferralNoteOrNonContactSessionBffDto {
      val activityDate = session.startDateTime.format(SHORT_DATE_FORMAT)
      return ReferralNoteOrNonContactSessionBffDto(
        id = session.id,
        createdAt = session.createdAt.format(SHORT_DATE_TIME_FORMAT),
        title = session.title,
        descriptionRichText = "Date of activity: $activityDate\n${session.details.truncate()}",
      )
    }
  }
}

private fun String.truncate(): String = if (length <= DESCRIPTION_MAX_LENGTH) this else take(DESCRIPTION_MAX_LENGTH).trimEnd() + "..."
