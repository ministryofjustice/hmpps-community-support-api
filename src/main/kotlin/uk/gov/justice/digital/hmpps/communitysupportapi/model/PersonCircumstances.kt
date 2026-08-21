package uk.gov.justice.digital.hmpps.communitysupportapi.model

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.DisabilitiesDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.HomeOfficeInterestDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.PersonalCircumstanceDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.PersonalDetailsAndCircumstancesDto
import java.time.LocalDateTime

data class PersonalCircumstance(
  val type: String? = null,
  val description: String? = null,
  val subType: String? = null,
  val subDescription: String? = null,
  val updatedAt: LocalDateTime? = null,
) {
  companion object {
    fun from(circumstance: PersonalCircumstanceDto): PersonalCircumstance = PersonalCircumstance(
      type = circumstance.type?.code,
      description = circumstance.type?.description,
      subType = circumstance.subtype?.code,
      subDescription = circumstance.subtype?.description,
      updatedAt = circumstance.updatedAt,
    )
  }
}

data class Disability(
  val type: String? = null,
  val description: String? = null,
  val updatedAt: LocalDateTime? = null,
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
    fun from(circumstances: PersonalDetailsAndCircumstancesDto, homeOfficeInterest: HomeOfficeInterestDto) = PersonDetailsAndCircumstances(
      preferredLanguage = circumstances.preferredLanguage?.description,
      personalCircumstances = circumstances.personalCircumstances.map { circumstance -> PersonalCircumstance.from(circumstance) },
      disabilities = circumstances.disabilities.map { disability -> Disability.from(disability) },
      offenderPersonalityDisorder = circumstances.offenderPersonalityDisorder?.status?.description,
      ofHomeOfficeInterest = homeOfficeInterest.exists,
      homeOfficeInterestNotes = homeOfficeInterest.notes,
    )
  }
}
