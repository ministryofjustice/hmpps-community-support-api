package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralNote
import java.util.UUID

interface ReferralNotesRepository : JpaRepository<ReferralNote, UUID> {
  fun findAllByReferralIdAndDeletedAtIsNullOrderByCreatedAtDesc(referralID: UUID): List<ReferralNote>
}
