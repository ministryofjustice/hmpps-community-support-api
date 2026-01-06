package uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis

data class NomisPersonDto(
  val prisonerNumber: String, // NOMS Number
  val firstName: String,
  val lastName: String,
  val dateOfBirth: String,
  val gender: String? = null,
  val contactDetails: ContactDetails,
  val offenderProfile: OffenderProfile,
  val offenderAliases: List<OffenderAlias> = emptyList(),
  val offenderManagers: List<OffenderManager> = emptyList(),
  val softDeleted: Boolean = false,
  val currentDisposal: String? = null,
  val partitionArea: String? = null,
  val currentRestriction: Boolean = false,
  val restrictionMessage: String? = null,
  val currentExclusion: Boolean = false,
  val exclusionMessage: String? = null,
  val highlight: Map<String, List<String>> = emptyMap(),
  val accessDenied: Boolean = false,
  val currentTier: String? = null,
  val activeProbationManagedSentence: Boolean = false,
  val mappa: Mappa? = null,
  val probationStatus: ProbationStatus? = null,
  val age: Int? = null
)
