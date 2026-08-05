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

data class AdditionalSupportNeedsBffResponseDto(
  val refereeName: RefereeNameDto,
  val physicalHealth: Selection,
  val mentalEmotionalHealth: Selection,
  val neurodiversity: Selection,
  val locationTravel: Selection,
  val caringResponsibilities: Selection,
  val employmentResponsibilities: Selection,
  val diversity: Selection,
  val anythingElse: Selection,
  val needsAdditionalSupport: Boolean? = null,
) {
  companion object {
    fun fromNeeds(person: Person, personAdditionalSupportNeeds: PersonAdditionalSupportNeeds): AdditionalSupportNeedsBffResponseDto = AdditionalSupportNeedsBffResponseDto(
      refereeName = RefereeNameDto(firstName = person.firstName, lastName = person.lastName),
      physicalHealth = Selection.fromString(personAdditionalSupportNeeds.physicalHealthDetails),
      mentalEmotionalHealth = Selection.fromString(personAdditionalSupportNeeds.mentalEmotionalHealthDetails),
      neurodiversity = Selection.fromString(personAdditionalSupportNeeds.neurodiversityDetails),
      locationTravel = Selection.fromString(personAdditionalSupportNeeds.locationTravelDetails),
      caringResponsibilities = Selection.fromString(personAdditionalSupportNeeds.caringResponsibilitiesDetails),
      employmentResponsibilities = Selection.fromString(personAdditionalSupportNeeds.employmentResponsibilitiesDetails),
      diversity = Selection.fromString(personAdditionalSupportNeeds.diversityDetails),
      anythingElse = Selection.fromString(personAdditionalSupportNeeds.anythingElseDetails),
      needsAdditionalSupport = personAdditionalSupportNeeds.additionalSupportNeeded,
    )
    fun fromPerson(person: Person): AdditionalSupportNeedsBffResponseDto = AdditionalSupportNeedsBffResponseDto(
      refereeName = RefereeNameDto(firstName = person.firstName, lastName = person.lastName),
      physicalHealth = Selection.default(),
      mentalEmotionalHealth = Selection.default(),
      neurodiversity = Selection.default(),
      locationTravel = Selection.default(),
      caringResponsibilities = Selection.default(),
      employmentResponsibilities = Selection.default(),
      diversity = Selection.default(),
      anythingElse = Selection.default(),
      needsAdditionalSupport = null,
    )
  }
}

data class NeedsInterpreterBffResponseDto(
  val refereeName: RefereeNameDto,
  val language: Selection? = null,
  val needsInterpreter: Boolean? = null,
) {
  companion object {
    fun from(person: Person, personAdditionalSupportNeeds: PersonAdditionalSupportNeeds): NeedsInterpreterBffResponseDto = NeedsInterpreterBffResponseDto(
      refereeName = RefereeNameDto(firstName = person.firstName, lastName = person.lastName),
      language = Selection.fromString(personAdditionalSupportNeeds.interpreterLanguage),
      needsInterpreter = personAdditionalSupportNeeds.interpreterNeeded,
    )
  }
}
