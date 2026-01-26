package uk.gov.justice.digital.hmpps.communitysupportapi.testdata

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PhoneNumberDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.ContactDetailsDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.DeliusPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.HighlightDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.MappaDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.OffenderAliasDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.OffenderLanguagesDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.OffenderProfileDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.OtherIdsDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.ProbationStatusDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis.AlertsDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis.AliasesDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis.NomisPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toJson
import java.time.LocalDate
import kotlin.String

object ExternalApiResponse {

  // DELIUS PERSON DATA

  const val CRN = "X123456"

  fun createDeliusPersonDto(crn: String): DeliusPersonDto = DeliusPersonDto(
    previousSurname = "Johnson",
    offenderId = 123456L,
    title = "Mr",
    firstName = "John",
    middleNames = listOf("Michael"),
    surname = "Smith",
    dateOfBirth = LocalDate.of(1985, 1, 1),
    gender = "Male",
    otherIds = OtherIdsDto(
      crn = crn,
      pncNumber = "2012/0052494Q",
      croNumber = "123456/24A",
      niNumber = "AA123456A",
      nomsNumber = "G5555TT",
      immigrationNumber = null,
      mostRecentPrisonerNumber = "X123456",
      previousCrn = null,
    ),
    contactDetails = ContactDetailsDto(
      phoneNumbers = listOf(
        PhoneNumberDto(
          number = "07123456789",
          type = "TELEPHONE",
        ),
      ),
      emailAddresses = listOf("john.smith@example.com"),
      allowSMS = true,
      addresses = emptyList(),
    ),
    offenderProfile = OffenderProfileDto(
      ethnicity = "White",
      nationality = "British",
      secondaryNationality = null,
      notes = null,
      immigrationStatus = null,
      offenderLanguages = OffenderLanguagesDto(
        primaryLanguage = "English",
        otherLanguages = emptyList(),
        languageConcerns = null,
        requiresInterpreter = false,
      ),
      religion = "Christian",
      sexualOrientation = "Heterosexual",
      offenderDetails = null,
      remandStatus = "SENTENCED",
      previousConviction = null,
      riskColour = "GREEN",
      disabilities = emptyList(),
      provisions = emptyList(),
    ),
    offenderAliases = listOf(
      OffenderAliasDto(
        id = "ALIAS-1",
        dateOfBirth = LocalDate.of(1985, 1, 1),
        firstName = "Johnny",
        middleNames = emptyList(),
        surname = "Smith",
        gender = "Male",
      ),
    ),
    offenderManagers = emptyList(),
    softDeleted = false,
    currentDisposal = "Community Order",
    partitionArea = "N01",
    currentRestriction = false,
    restrictionMessage = null,
    currentExclusion = false,
    exclusionMessage = null,
    highlight = HighlightDto(
      surname = listOf("Smith"),
      offenderAliasesSurname = listOf("SMITH"),
    ),
    accessDenied = false,
    currentTier = "B2",
    activeProbationManagedSentence = true,
    mappa = MappaDto(
      level = 1,
      levelDescription = "Level 1",
      category = 0,
      categoryDescription = "None",
      startDate = LocalDate.of(2022, 1, 1),
      reviewDate = LocalDate.of(2023, 1, 1),
      team = null,
      officer = null,
      probationArea = null,
      notes = null,
    ),
    probationStatus = ProbationStatusDto(
      status = "CURRENT",
      previouslyKnownTerminationDate = null,
      inBreach = false,
      preSentenceActivity = false,
      awaitingPsr = false,
    ),
    age = 39,
  )

  val deliusPerson = createDeliusPersonDto(CRN)

  fun createDeliusPersonAdditionalDetails() = PersonAdditionalDetails(
    ethnicity = deliusPerson.offenderProfile?.ethnicity,
    preferredLanguage = deliusPerson.offenderProfile?.offenderLanguages?.primaryLanguage,
    neurodiverseConditions = null,
    religionOrBelief = deliusPerson.offenderProfile?.religion,
    transgender = null,
    sexualOrientation = deliusPerson.offenderProfile?.sexualOrientation,
    address = deliusPerson.contactDetails?.addresses?.firstOrNull()?.id?.toString(),
    phoneNumber = deliusPerson.contactDetails?.phoneNumbers?.firstOrNull()?.number,
    emailAddress = deliusPerson.contactDetails?.emailAddresses?.firstOrNull(),
  )

  fun deliusPersonJson(crn: String) = createDeliusPersonDto(crn).toJson()

  fun deliusPersonNotFoundJson() = """
        {
          "error": "Not Found",
          "status": 404,
          "message": "Person not found"
        }
  """.trimIndent()

  // NOMIS PERSON DATA

  const val PRISONER_NUMBER = "A1234BC"

  fun createNomisPersonDto(prisonerNumber: String): NomisPersonDto = NomisPersonDto(
    prisonerNumber = prisonerNumber,
    pncNumber = "12/394773H",
    croNumber = "29906/12J",
    bookingId = "2900924",
    bookingNumber = "38412A",
    title = "Mr",
    firstName = "John",
    lastName = "Smith",
    dateOfBirth = LocalDate.of(1985, 1, 1),
    gender = "Male",
    ethnicity = "White",
    nationality = "British",
    prisonId = "MDI",
    prisonName = "HMP Leeds",
    cellLocation = "A-1-002",
    csra = "HIGH",
    category = "C",
    legalStatus = "SENTENCED",
    recall = false,
    indeterminateSentence = false,
    sentenceStartDate = LocalDate.of(2020, 4, 3),
    releaseDate = LocalDate.of(2023, 5, 2),
    confirmedReleaseDate = LocalDate.of(2023, 5, 1),
    aliases = listOf(
      AliasesDto(
        title = "Mr",
        firstName = "Johnny",
        lastName = "Smith",
        dateOfBirth = LocalDate.of(1985, 1, 1),
        gender = "Male",
        ethnicity = "White",
        raceCode = "W1",
      ),
    ),
    alerts = listOf(
      AlertsDto(
        alertType = "H",
        alertCode = "HA",
        active = true,
        expired = false,
      ),
    ),
    phoneNumbers = listOf(
      PhoneNumberDto(
        type = "MOBILE",
        number = "07123456789",
      ),
    ),
  )

  val nomisPerson = createNomisPersonDto(PRISONER_NUMBER)

  fun createNomisPersonAdditionalDetails() = PersonAdditionalDetails(
    ethnicity = nomisPerson.ethnicity,
    preferredLanguage = nomisPerson.languages.firstOrNull()?.code,
    neurodiverseConditions = null,
    religionOrBelief = nomisPerson.religion,
    transgender = null,
    sexualOrientation = null,
    address = nomisPerson.addresses.firstOrNull()?.fullAddress,
    phoneNumber = nomisPerson.phoneNumbers.firstOrNull()?.number,
    emailAddress = nomisPerson.emailAddresses.firstOrNull()?.email,
  )

  fun nomisPersonJson(prisonerNumber: String) = createNomisPersonDto(prisonerNumber).toJson()

  fun nomisPersonNotFoundJson() = """
        {
          "error": "Not Found",
          "status": 404,
          "message": "Prisoner not found"
        }
  """.trimIndent()
}
