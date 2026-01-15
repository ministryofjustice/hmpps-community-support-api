package uk.gov.justice.digital.hmpps.communitysupportapi.model

data class PersonAggregate(
  val person: Person,
  val additionalDetails: PersonAdditionalDetails?,
)
