package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.PersonAdditionalSupportNeeds

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "selected",
  visible = true,
)
@JsonSubTypes(
  JsonSubTypes.Type(value = Selection.Yes::class, name = "Yes"),
  JsonSubTypes.Type(value = Selection.No::class, name = "No"),
  JsonSubTypes.Type(value = Selection.Unanswered::class, name = "Unanswered"),
)
sealed interface Selection {
  data class Yes(val value: String) : Selection
  data object No : Selection
  data object Unanswered : Selection
  companion object {
    fun fromString(value: String?): Selection = if (value == null) No else Yes(value)
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
      physicalHealth = Selection.Unanswered,
      mentalEmotionalHealth = Selection.Unanswered,
      neurodiversity = Selection.Unanswered,
      locationTravel = Selection.Unanswered,
      caringResponsibilities = Selection.Unanswered,
      employmentResponsibilities = Selection.Unanswered,
      diversity = Selection.Unanswered,
      anythingElse = Selection.Unanswered,
      needsAdditionalSupport = null,
    )
  }
}

data class NeedsInterpreterBffResponseDto(
  val refereeName: RefereeNameDto,
  val language: Selection,
) {
  companion object {
    fun from(person: Person, personAdditionalSupportNeeds: PersonAdditionalSupportNeeds): NeedsInterpreterBffResponseDto {
      val refereeName = RefereeNameDto(firstName = person.firstName, lastName = person.lastName)
      return when (personAdditionalSupportNeeds.interpreterNeeded) {
        true -> NeedsInterpreterBffResponseDto(refereeName, Selection.fromString(personAdditionalSupportNeeds.interpreterLanguage))
        false -> if (personAdditionalSupportNeeds.interpreterLanguage != null) {
          NeedsInterpreterBffResponseDto(refereeName, Selection.Unanswered)
        } else {
          NeedsInterpreterBffResponseDto(refereeName, Selection.No)
        }
        null -> NeedsInterpreterBffResponseDto(refereeName, Selection.Unanswered)
      }
    }
  }
}
