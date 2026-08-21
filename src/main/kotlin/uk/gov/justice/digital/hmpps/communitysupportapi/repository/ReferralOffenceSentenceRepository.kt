package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralOffenceSentence
import java.util.UUID

@Repository
interface ReferralOffenceSentenceRepository : JpaRepository<ReferralOffenceSentence, UUID> {
  fun findByReferralId(referralId: UUID): ReferralOffenceSentence?
}
