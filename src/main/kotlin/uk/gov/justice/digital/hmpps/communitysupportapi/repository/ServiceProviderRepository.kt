package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ServiceProvider
import java.util.UUID

interface ServiceProviderRepository : JpaRepository<ServiceProvider, UUID> {
  fun findAllByAuthGroupIdIn(authGroupIds: List<String>): List<ServiceProvider>
}
