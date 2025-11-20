package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import java.util.UUID

interface ReferralRepository : JpaRepository<Referral, UUID>
