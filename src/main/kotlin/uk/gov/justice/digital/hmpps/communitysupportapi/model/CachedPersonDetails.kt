package uk.gov.justice.digital.hmpps.communitysupportapi.model

import java.time.Instant
import java.util.UUID

data class CachedPersonDetails(
  val id: UUID,
  val firstName: String,
  val lastName: String,
  val identifier: String,
  val identifierType: String,
  val createdAt: Instant,
  val mostRecentSentenceDate: Instant,
)
