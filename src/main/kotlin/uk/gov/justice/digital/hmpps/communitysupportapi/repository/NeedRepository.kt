package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Need
import java.util.UUID

interface NeedRepository : JpaRepository<Need, UUID> {
  fun findAllByOrderByOrderNumberAsc(): List<Need>
}
