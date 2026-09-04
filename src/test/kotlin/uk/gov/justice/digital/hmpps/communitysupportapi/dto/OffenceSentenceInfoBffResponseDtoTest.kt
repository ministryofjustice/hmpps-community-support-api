package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.OffenceSentenceDto
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toFormattedDateOfBirthLong
import java.time.LocalDate

class OffenceSentenceInfoBffResponseDtoTest {
  @Test
  fun `from should document intended future behavior pending nDelius integration by returning sentenceEndDate for non-custody cases`() {
    val person = PersonFactory()
      .withFirstName("Jane")
      .withLastName("Doe")
      .create()
    val offenceSentenceInfo = OffenceSentenceDto(
      sentenceEndDate = LocalDate.of(2026, 1, 1),
      expectedReleaseDate = null,
      hasLicenceConditionsOrZones = true,
      licenceConditionsOrZonesDetails = "Do not enter exclusion zone",
    )

    val result = OffenceSentenceInfoBffResponseDto.from(person, "X123456", offenceSentenceInfo)

    result.firstName shouldBe "Jane"
    result.lastName shouldBe "Doe"
    result.crn shouldBe "X123456"
    result.dateOfBirth shouldBe person.dateOfBirth.toFormattedDateOfBirthLong()
    result.offenceSentenceInfo.sentenceEndDate shouldBe LocalDate.of(2026, 1, 1)
    result.offenceSentenceInfo.expectedReleaseDate shouldBe null
    result.offenceSentenceInfo.hasLicenceConditionsOrZones shouldBe true
    result.offenceSentenceInfo.licenceConditionsOrZonesDetails shouldBe "Do not enter exclusion zone"
  }

  @Test
  fun `from should document intended future behavior pending nDelius integration by returning expectedReleaseDate for custody cases`() {
    val person = PersonFactory()
      .withFirstName("Jane")
      .withLastName("Doe")
      .create()
    val offenceSentenceInfo = OffenceSentenceDto(
      sentenceEndDate = null,
      expectedReleaseDate = LocalDate.of(2026, 2, 1),
      hasLicenceConditionsOrZones = false,
      licenceConditionsOrZonesDetails = null,
    )

    val result = OffenceSentenceInfoBffResponseDto.from(person, "X123456", offenceSentenceInfo)

    result.firstName shouldBe "Jane"
    result.lastName shouldBe "Doe"
    result.crn shouldBe "X123456"
    result.dateOfBirth shouldBe person.dateOfBirth.toFormattedDateOfBirthLong()
    result.offenceSentenceInfo.sentenceEndDate shouldBe null
    result.offenceSentenceInfo.expectedReleaseDate shouldBe LocalDate.of(2026, 2, 1)
    result.offenceSentenceInfo.hasLicenceConditionsOrZones shouldBe false
    result.offenceSentenceInfo.licenceConditionsOrZonesDetails shouldBe null
  }
}
