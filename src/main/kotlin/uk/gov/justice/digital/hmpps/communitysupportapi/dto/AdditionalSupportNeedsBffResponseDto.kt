package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.PersonAdditionalSupportNeeds

data class Selection(
  val selected: Boolean,
  val value: String? = null,
)

data class RefereeName(
  val firstName: String,
  val middleName: String? = null,
  val lastName: String,
)

data class AdditionalSupportNeedsBffResponseDto(
  val refereeName: RefereeName,
  val physicalHealth: Selection? = null,
  val mentalEmotionalHealth: Selection? = null,
  val neurodiversity: Selection? = null,
  val locationTravel: Selection? = null,
  val caringResponsibilities: Selection? = null,
  val employmentResponsibilities: Selection? = null,
  val diversity: Selection? = null,
  val anythingElse: Selection? = null,
  val needsAdditionalSupport: Boolean = false,
) {
  companion object {
    fun from(person: Person, personAdditionalSupportNeeds: PersonAdditionalSupportNeeds): AdditionalSupportNeedsBffResponseDto = AdditionalSupportNeedsBffResponseDto(
      refereeName = RefereeName(firstName = person.firstName, lastName = person.lastName),
      physicalHealth = personAdditionalSupportNeeds.physicalHealthDetails?.let { Selection(true, it) }
        ?: Selection(false),
      mentalEmotionalHealth = personAdditionalSupportNeeds.neurodiversityDetails?.let { Selection(true, it) }
        ?: Selection(false),
      neurodiversity = personAdditionalSupportNeeds.neurodiversityDetails?.let { Selection(true, it) }
        ?: Selection(false),
      locationTravel = personAdditionalSupportNeeds.locationTravelDetails?.let { Selection(true, it) }
        ?: Selection(false),
      caringResponsibilities = personAdditionalSupportNeeds.caringResponsibilitiesDetails?.let { Selection(true, it) }
        ?: Selection(false),
      employmentResponsibilities = personAdditionalSupportNeeds.employmentResponsibilitiesDetails?.let { Selection(true, it) }
        ?: Selection(false),
      diversity = personAdditionalSupportNeeds.diversityDetails?.let { Selection(true, it) }
        ?: Selection(false),
      anythingElse = personAdditionalSupportNeeds.anythingElseDetails?.let { Selection(true, it) }
        ?: Selection(false),
      needsAdditionalSupport = !personAdditionalSupportNeeds.noAdditionalSupportNeeded,
    )
  }
}
