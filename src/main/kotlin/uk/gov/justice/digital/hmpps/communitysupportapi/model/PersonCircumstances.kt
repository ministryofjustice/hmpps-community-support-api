package uk.gov.justice.digital.hmpps.communitysupportapi.model

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.DisabilitiesDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.HomeOfficeInterestDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.PersonalCircumstanceDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.PersonalDetailsAndCircumstancesDto
import java.time.OffsetDateTime

data class PersonalCircumstance(
  val type: String? = null,
  val description: String? = null,
  val subType: String? = null,
  val subDescription: String? = null,
  val updatedAt: OffsetDateTime? = null,
) {
  companion object {
    fun from(circumstance: PersonalCircumstanceDto): PersonalCircumstance = PersonalCircumstance(
      type = circumstance.type?.code,
      description = circumstance.type?.description,
      subType = circumstance.subType?.code,
      subDescription = circumstance.subType?.description,
      updatedAt = circumstance.updatedAt,
    )
  }
}

data class Disability(
  val type: String? = null,
  val description: String? = null,
  val updatedAt: OffsetDateTime? = null,
) {
  companion object {
    fun from(disability: DisabilitiesDto): Disability = Disability(
      type = disability.type?.code,
      description = disability.type?.description,
      updatedAt = disability.updatedAt,
    )
  }
}

data class PersonDetailsAndCircumstances(
  val preferredLanguage: String? = null,
  val personalCircumstances: List<PersonalCircumstance> = emptyList(),
  val disabilities: List<Disability> = emptyList(),
  val offenderPersonalityDisorder: String? = null,
  val ofHomeOfficeInterest: Boolean? = null,
  val homeOfficeInterestNotes: String? = null,
) {
  companion object {
    fun from(personDetailsAndCircumstances: PersonalDetailsAndCircumstancesDto, homeOfficeInterest: HomeOfficeInterestDto) = PersonDetailsAndCircumstances(
      preferredLanguage = personDetailsAndCircumstances.preferredLanguage?.description,
      personalCircumstances = personDetailsAndCircumstances.personalCircumstances.map { circumstance -> PersonalCircumstance.from(circumstance) },
      disabilities = personDetailsAndCircumstances.disabilities.map { disability -> Disability.from(disability) },
      offenderPersonalityDisorder = personDetailsAndCircumstances.offenderPersonalityDisorder?.status?.description,
      ofHomeOfficeInterest = homeOfficeInterest.exists,
      homeOfficeInterestNotes = homeOfficeInterest.notes,
    )
  }
}
