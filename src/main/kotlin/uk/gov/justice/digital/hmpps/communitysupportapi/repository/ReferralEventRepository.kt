package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEvent
import java.util.*

interface ReferralEventRepository : JpaRepository<ReferralEvent, UUID>
