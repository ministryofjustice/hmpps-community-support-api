package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.PersonAdditionalSupportNeeds

data class Selection(
  val selected: Boolean,
  val value: String? = null,
) {
  companion object {
    fun fromString(value: String?): Selection = if (value == null) {
      Selection(false)
    } else {
      Selection(true, value)
    }
  }
}

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
      physicalHealth = Selection.fromString(personAdditionalSupportNeeds.physicalHealthDetails),
      mentalEmotionalHealth = Selection.fromString(personAdditionalSupportNeeds.mentalEmotionalHealthDetails),
      neurodiversity = Selection.fromString(personAdditionalSupportNeeds.neurodiversityDetails),
      locationTravel = Selection.fromString(personAdditionalSupportNeeds.locationTravelDetails),
      caringResponsibilities = Selection.fromString(personAdditionalSupportNeeds.caringResponsibilitiesDetails),
      employmentResponsibilities = Selection.fromString(personAdditionalSupportNeeds.employmentResponsibilitiesDetails),
      diversity = Selection.fromString(personAdditionalSupportNeeds.diversityDetails),
      anythingElse = Selection.fromString(personAdditionalSupportNeeds.anythingElseDetails),
      needsAdditionalSupport = !personAdditionalSupportNeeds.noAdditionalSupportNeeded,
    )
  }
}
