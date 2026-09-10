package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Outcome
import java.util.UUID

interface OutcomeRepository : JpaRepository<Outcome, UUID> {
  fun findAllByNeedIdOrderByOrderNumberAsc(needId: UUID): List<Outcome>
}
