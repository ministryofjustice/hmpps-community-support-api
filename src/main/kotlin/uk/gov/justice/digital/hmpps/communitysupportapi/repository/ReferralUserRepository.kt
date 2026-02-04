package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import java.util.UUID

interface ReferralUserRepository : JpaRepository<ReferralUser, UUID> {
  fun findByHmppsAuthId(hmppsAuthId: String): ReferralUser?
}
