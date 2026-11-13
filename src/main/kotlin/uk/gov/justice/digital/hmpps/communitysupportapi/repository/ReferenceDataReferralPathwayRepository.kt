package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataReferralPathway
import java.util.UUID

interface ReferenceDataReferralPathwayRepository : JpaRepository<ReferenceDataReferralPathway, UUID>
