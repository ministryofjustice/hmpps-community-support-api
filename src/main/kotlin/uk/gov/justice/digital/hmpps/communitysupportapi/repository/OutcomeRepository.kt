package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataOutcome
import java.util.UUID

interface OutcomeRepository : JpaRepository<ReferenceDataOutcome, UUID> {
  fun findAllByNeedIdOrderByOrderNumberAsc(needId: UUID): List<ReferenceDataOutcome>
}
