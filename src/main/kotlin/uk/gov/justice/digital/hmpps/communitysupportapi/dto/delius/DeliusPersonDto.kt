package uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius

import java.time.LocalDate

data class DeliusPersonDto(
  val previousSurname: String?,
  val offenderId: Long,
  val title: String? = null,
  val firstName: String,
  val middleNames: List<String> = emptyList(),
  val surname: String,
  val dateOfBirth: LocalDate,
  val gender: String = "Unknown",
  val otherIds: OtherIdsDto?,
  val contactDetails: ContactDetailsDto,
  val offenderProfile: OffenderProfileDto?,
  val offenderAliases: List<OffenderAliasDto> = emptyList(),
  val offenderManagers: List<OffenderManagerDto> = emptyList(),
  val softDeleted: Boolean,
  val currentDisposal: String?,
  val partitionArea: String?,
  val currentRestriction: Boolean,
  val restrictionMessage: String?,
  val currentExclusion: Boolean,
  val exclusionMessage: String?,
  val highlight: HighlightDto?,
  val accessDenied: Boolean,
  val currentTier: String?,
  val activeProbationManagedSentence: Boolean,
  val mappa: MappaDto?,
  val probationStatus: ProbationStatusDto?,
  val age: Int,
)
