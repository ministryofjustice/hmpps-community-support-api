package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CommunityServiceProvider
import java.util.*

interface CommunityServiceProviderRepository : JpaRepository<CommunityServiceProvider, UUID>
