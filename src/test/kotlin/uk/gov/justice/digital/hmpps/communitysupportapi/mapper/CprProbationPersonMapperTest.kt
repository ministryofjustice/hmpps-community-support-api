package uk.gov.justice.digital.hmpps.communitysupportapi.mapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.CRN
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createCprProbationPersonAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createCprProbationPersonDto
import java.time.LocalDate

class CprProbationPersonMapperTest {

  @Test
  fun `maps CPR probation Person dto to domain Person with CRN identifier`() {
    val cprPersonDto = createCprProbationPersonDto(CRN)
    val expectedIdentifier = PersonIdentifier.Crn(CRN)
    val person = cprPersonDto.toProbationPerson()

    assertEquals(expectedIdentifier, person.identifier)
    assertEquals("John", person.firstName)
    assertEquals("Smith", person.lastName)
    assertEquals("Male", person.sex)
    assertEquals("Mr", person.title)
    assertEquals("David", person.middleNames)
  }

  @Test
  fun `maps CPR probation Person dto to PersonAdditionalDetails`() {
    val expectedAdditionalDetails = createCprProbationPersonAdditionalDetails()
    val cprPersonDto = createCprProbationPersonDto(CRN)
    val personAdditionalDetails = cprPersonDto.toAdditionalDetails()

    assertEquals(expectedAdditionalDetails, personAdditionalDetails)
  }

  @Test
  fun `preferred language is null since CPR does not provide language data`() {
    val personAdditionalDetails = createCprProbationPersonDto(CRN).toAdditionalDetails()
    assertNull(personAdditionalDetails.preferredLanguage)
  }

  @Test
  fun `maps gender identity from sex field`() {
    val personAdditionalDetails = createCprProbationPersonDto(CRN).toAdditionalDetails()
    assertEquals("Male", personAdditionalDetails.genderIdentity)
  }

  @Test
  fun `maps multiple nationalities from CPR`() {
    val personAdditionalDetails = createCprProbationPersonDto(CRN).toAdditionalDetails()
    assertEquals(listOf("Argentine", "Brazilian"), personAdditionalDetails.nationalities)
  }

  @Test
  fun `maps phone number and mobile number separately`() {
    val personAdditionalDetails = createCprProbationPersonDto(CRN).toAdditionalDetails()
    assertEquals("01234567890", personAdditionalDetails.phoneNumber)
    assertEquals("07700900002", personAdditionalDetails.mobileNumber)
  }

  @Test
  fun `maps address type from active address usage`() {
    val personAdditionalDetails = createCprProbationPersonDto(CRN).toAdditionalDetails()
    assertEquals("Friends/Family (settled) (verified)", personAdditionalDetails.addressType)
  }

  @Test
  fun `maps address start date from address`() {
    val personAdditionalDetails = createCprProbationPersonDto(CRN).toAdditionalDetails()
    assertEquals(LocalDate.of(2005, 12, 1), personAdditionalDetails.addressStartDate)
  }

  @Test
  fun `maps address notes from address comment`() {
    val personAdditionalDetails = createCprProbationPersonDto(CRN).toAdditionalDetails()
    assertEquals("No notes", personAdditionalDetails.addressNotes)
  }

  @Test
  fun `maps disability flag from CPR`() {
    val personAdditionalDetails = createCprProbationPersonDto(CRN).toAdditionalDetails()
    assertEquals(true, personAdditionalDetails.disability)
  }

  @Test
  fun `maps title from CPR`() {
    val person = createCprProbationPersonDto(CRN).toProbationPerson()
    assertEquals("Mr", person.title)
  }

  @Test
  fun `maps middle names from CPR`() {
    val person = createCprProbationPersonDto(CRN).toProbationPerson()
    assertEquals("David", person.middleNames)
  }

  @Test
  fun `maps prison numbers as empty list for probation person`() {
    val person = createCprProbationPersonDto(CRN).toProbationPerson()
    assertEquals(emptyList<String>(), person.prisonNumbers)
  }
}
