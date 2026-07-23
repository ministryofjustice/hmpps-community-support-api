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
    fun default(): Selection = Selection(false)
  }
}

data class RefereeName(
  val firstName: String,
  val middleName: String? = null,
  val lastName: String,
)

data class AdditionalSupportNeedsBffResponseDto(
  val refereeName: RefereeName,
  val physicalHealth: Selection,
  val mentalEmotionalHealth: Selection,
  val neurodiversity: Selection,
  val locationTravel: Selection,
  val caringResponsibilities: Selection,
  val employmentResponsibilities: Selection,
  val diversity: Selection,
  val anythingElse: Selection,
  val needsAdditionalSupport: Boolean = false,
) {
  companion object {
    fun fromNeeds(person: Person, personAdditionalSupportNeeds: PersonAdditionalSupportNeeds): AdditionalSupportNeedsBffResponseDto = AdditionalSupportNeedsBffResponseDto(
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
    fun fromPerson(person: Person): AdditionalSupportNeedsBffResponseDto = AdditionalSupportNeedsBffResponseDto(
      refereeName = RefereeName(firstName = person.firstName, lastName = person.lastName),
      physicalHealth = Selection.default(),
      mentalEmotionalHealth = Selection.default(),
      neurodiversity = Selection.default(),
      locationTravel = Selection.default(),
      caringResponsibilities = Selection.default(),
      employmentResponsibilities = Selection.default(),
      diversity = Selection.default(),
      anythingElse = Selection.default(),
      needsAdditionalSupport = false,
    )
  }
}

data class NeedsInterpreterBffResponseDto(
  val refereeName: RefereeName,
  val language: Selection? = null,
) {
  companion object {
    fun from(person: Person, personAdditionalSupportNeeds: PersonAdditionalSupportNeeds): NeedsInterpreterBffResponseDto = NeedsInterpreterBffResponseDto(
      refereeName = RefereeName(firstName = person.firstName, lastName = person.lastName),
      language = Selection.fromString(personAdditionalSupportNeeds.interpreterLanguage),
    )
  }
}
