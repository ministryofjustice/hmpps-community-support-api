package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import java.util.UUID

data class ReferenceDataReferralPathwayDto(val id: UUID, val title: String)
data class ReferenceDataReferralPathwaysDto(val pathways: List<ReferenceDataReferralPathwayDto>)
