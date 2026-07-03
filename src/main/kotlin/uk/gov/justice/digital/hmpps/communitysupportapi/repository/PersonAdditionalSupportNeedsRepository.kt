package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.PersonAdditionalSupportNeeds
import java.util.UUID

@Repository
interface PersonAdditionalSupportNeedsRepository : JpaRepository<PersonAdditionalSupportNeeds, UUID> {

  fun findByReferralId(referralId: UUID): PersonAdditionalSupportNeeds?

  fun findByPersonId(personId: UUID): List<PersonAdditionalSupportNeeds>

  fun existsByReferralId(referralId: UUID): Boolean
}
