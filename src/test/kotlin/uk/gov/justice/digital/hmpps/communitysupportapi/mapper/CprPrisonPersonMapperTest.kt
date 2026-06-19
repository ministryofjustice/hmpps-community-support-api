package uk.gov.justice.digital.hmpps.communitysupportapi.mapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createCprPrisonPersonAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createCprPrisonPersonDto
import java.time.LocalDate

class CprPrisonPersonMapperTest {

  @Test
  fun `maps CPR prison Person dto to domain Person with PrisonerNumber identifier`() {
    val cprPersonDto = createCprPrisonPersonDto(PRISONER_NUMBER)
    val expectedIdentifier = PersonIdentifier.PrisonerNumber(PRISONER_NUMBER)
    val person = cprPersonDto.toPrisonPerson()

    assertEquals(expectedIdentifier, person.identifier)
    assertEquals("John", person.firstName)
    assertEquals("Smith", person.lastName)
    assertEquals("Male", person.sex)
    assertEquals("Mr", person.title)
    assertEquals("James", person.middleNames)
  }

  @Test
  fun `maps CPR prison Person dto to PersonAdditionalDetails`() {
    val expectedAdditionalDetails = createCprPrisonPersonAdditionalDetails()
    val cprPersonDto = createCprPrisonPersonDto(PRISONER_NUMBER)
    val personAdditionalDetails = cprPersonDto.toAdditionalDetails()

    assertEquals(expectedAdditionalDetails, personAdditionalDetails)
  }

  @Test
  fun `preferred language is null since CPR does not provide language data`() {
    val personAdditionalDetails = createCprPrisonPersonDto(PRISONER_NUMBER).toAdditionalDetails()
    assertNull(personAdditionalDetails.preferredLanguage)
  }

  @Test
  fun `maps gender identity from sex field`() {
    val personAdditionalDetails = createCprPrisonPersonDto(PRISONER_NUMBER).toAdditionalDetails()
    assertEquals("Male", personAdditionalDetails.genderIdentity)
  }

  @Test
  fun `maps nationality from CPR`() {
    val personAdditionalDetails = createCprPrisonPersonDto(PRISONER_NUMBER).toAdditionalDetails()
    assertEquals(listOf("British"), personAdditionalDetails.nationalities)
  }

  @Test
  fun `maps phone number and mobile number separately`() {
    val personAdditionalDetails = createCprPrisonPersonDto(PRISONER_NUMBER).toAdditionalDetails()
    assertEquals("01234567890", personAdditionalDetails.phoneNumber)
    assertEquals("07700900002", personAdditionalDetails.mobileNumber)
  }

  @Test
  fun `maps address type from active address usage`() {
    val personAdditionalDetails = createCprPrisonPersonDto(PRISONER_NUMBER).toAdditionalDetails()
    assertEquals("Home", personAdditionalDetails.addressType)
  }

  @Test
  fun `maps address start date from address`() {
    val personAdditionalDetails = createCprPrisonPersonDto(PRISONER_NUMBER).toAdditionalDetails()
    assertEquals(LocalDate.of(2020, 4, 3), personAdditionalDetails.addressStartDate)
  }

  @Test
  fun `address notes are null when no comment provided`() {
    val personAdditionalDetails = createCprPrisonPersonDto(PRISONER_NUMBER).toAdditionalDetails()
    assertNull(personAdditionalDetails.addressNotes)
  }

  @Test
  fun `maps disability flag from CPR`() {
    val personAdditionalDetails = createCprPrisonPersonDto(PRISONER_NUMBER).toAdditionalDetails()
    assertEquals(false, personAdditionalDetails.disability)
  }

  @Test
  fun `maps title from CPR`() {
    val person = createCprPrisonPersonDto(PRISONER_NUMBER).toPrisonPerson()
    assertEquals("Mr", person.title)
  }

  @Test
  fun `maps middle names from CPR`() {
    val person = createCprPrisonPersonDto(PRISONER_NUMBER).toPrisonPerson()
    assertEquals("James", person.middleNames)
  }

  @Test
  fun `maps prison numbers from CPR identifiers`() {
    val person = createCprPrisonPersonDto(PRISONER_NUMBER).toPrisonPerson()
    assertEquals(listOf(PRISONER_NUMBER), person.prisonNumbers)
  }
}
