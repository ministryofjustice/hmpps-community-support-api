package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataNeed
import java.util.UUID

interface NeedRepository : JpaRepository<ReferenceDataNeed, UUID> {
  fun findAllByIdInOrderByOrderNumberAsc(needIds: Collection<UUID>): List<ReferenceDataNeed>
  fun findAllByOrderByOrderNumberAsc(): List<ReferenceDataNeed>
}
