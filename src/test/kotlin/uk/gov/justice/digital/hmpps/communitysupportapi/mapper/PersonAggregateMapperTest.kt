package uk.gov.justice.digital.hmpps.communitysupportapi.mapper

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.model.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAggregate
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.CRN
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createDeliusPersonAdditionalDetails
import java.time.LocalDate
import java.util.UUID

class PersonAggregateMapperTest {

  @Test
  fun `toEntity should map PersonAggregate to Person entity`() {
    val additionalDetails = createDeliusPersonAdditionalDetails()

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
    val additionalDetails = createDeliusPersonAdditionalDetails()

    val person = Person(
      identifier = PersonIdentifier.Crn(CRN),
      firstName = "John",
      lastName = "Smith",
      dateOfBirth = LocalDate.of(1985, 1, 1),
      sex = "Male",
    )

    val personAggregate = PersonAggregate(
      person = person,
      additionalDetails = additionalDetails,
    )

    val personId = UUID.randomUUID()

    val personDto = personAggregate.toPersonDto()

    personDto.personIdentifier shouldBe "X123456"
    personDto.firstName shouldBe "John"
    personDto.lastName shouldBe "Smith"
    personDto.dateOfBirth shouldBe LocalDate.of(1985, 1, 1)
    personDto.sex shouldBe "Male"
    personDto.additionalDetails?.ethnicity shouldBe "White"
    personDto.additionalDetails?.preferredLanguage shouldBe "English"
  }
}
