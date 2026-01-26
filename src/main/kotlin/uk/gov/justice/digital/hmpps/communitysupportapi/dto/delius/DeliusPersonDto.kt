package uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import java.time.LocalDate

data class DeliusPersonDto(
  val previousSurname: String?,
  val offenderId: Long,
  val title: String? = null,
  val firstName: String,
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val middleNames: List<String> = emptyList(),
  val surname: String,
  val dateOfBirth: LocalDate,
  val gender: String = "Unknown",
  val otherIds: OtherIdsDto?,
  val contactDetails: ContactDetailsDto?,
  val offenderProfile: OffenderProfileDto?,
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val offenderAliases: List<OffenderAliasDto> = emptyList(),
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val offenderManagers: List<OffenderManagerDto> = emptyList(),
  val softDeleted: Boolean? = null,
  val currentDisposal: String?,
  val partitionArea: String?,
  val currentRestriction: Boolean? = null,
  val restrictionMessage: String?,
  val currentExclusion: Boolean? = null,
  val exclusionMessage: String?,
  val highlight: HighlightDto?,
  val accessDenied: Boolean? = null,
  val currentTier: String?,
  val activeProbationManagedSentence: Boolean? = null,
  val mappa: MappaDto?,
  val probationStatus: ProbationStatusDto?,
  val age: Int,
)
