package uk.gov.justice.digital.hmpps.communitysupportapi.mapper

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.model.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAggregate
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.CRN
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createTestPersonAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toFormattedDateOfBirth
import java.time.LocalDate

class PersonAggregateMapperTest {

  @Test
  fun `toEntity should map PersonAggregate to Person entity`() {
    val additionalDetails = createTestPersonAdditionalDetails()

    val person = Person(
      identifier = PersonIdentifier.Crn(CRN),
      firstName = "John",
      lastName = "Smith",
      dateOfBirth = LocalDate.of(1985, 1, 1),
      sex = "Male",
    )

    val aggregate = PersonAggregate(
      person = person,
      additionalDetails = additionalDetails,
    )

    val personEntity = aggregate.toEntity()

    personEntity.identifier shouldBe "X123456"
    personEntity.firstName shouldBe "John"
    personEntity.lastName shouldBe "Smith"
    personEntity.dateOfBirth shouldBe LocalDate.of(1985, 1, 1)
    personEntity.gender shouldBe "Male"

    personEntity.additionalDetails shouldNotBe null
    personEntity.additionalDetails?.ethnicity shouldBe "White"
    personEntity.additionalDetails?.preferredLanguage shouldBe "English"
  }

  @Test
  fun `toPersonDto should map PersonAggregate to PersonDto correctly`() {
    val additionalDetails = createTestPersonAdditionalDetails()

    val person = Person(
      identifier = PersonIdentifier.Crn(CRN),
      firstName = "John",
      lastName = "Smith",
      dateOfBirth = LocalDate.of(1985, 1, 1),
      sex = "Male",
      title = "Mr",
      middleNames = "David",
    )

    val personAggregate = PersonAggregate(
      person = person,
      additionalDetails = additionalDetails,
    )

    val personDto = personAggregate.toPersonDto()

    personDto.personIdentifier shouldBe "X123456"
    personDto.title shouldBe "Mr"
    personDto.firstName shouldBe "John"
    personDto.middleNames shouldBe "David"
    personDto.lastName shouldBe "Smith"
    personDto.dateOfBirth shouldBe LocalDate.of(1985, 1, 1).toFormattedDateOfBirth()
    personDto.sex shouldBe "Male"
    personDto.additionalDetails?.ethnicity shouldBe "White"
    personDto.additionalDetails?.preferredLanguage shouldBe "English"
    personDto.additionalDetails?.genderIdentity shouldBe "Male"
    personDto.additionalDetails?.nationalities shouldBe listOf("Argentine", "Brazilian")
    personDto.additionalDetails?.mobileNumber shouldBe "07700900002"
    personDto.additionalDetails?.phoneNumber shouldBe "01234567890"
    personDto.additionalDetails?.addressType shouldBe "Friends/Family (settled) (verified)"
    personDto.additionalDetails?.addressStartDate shouldBe LocalDate.of(2005, 12, 1)
    personDto.additionalDetails?.addressNotes shouldBe "No notes"
    personDto.additionalDetails?.disability shouldBe true
  }
}
