package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.OffenceSentenceDto
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonFactory
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
    )

    val result = OffenceSentenceInfoBffResponseDto.from(person, offenceSentenceInfo)

    result.firstName shouldBe "Jane"
    result.lastName shouldBe "Doe"
    result.offenceSentenceInfo.sentenceEndDate shouldBe LocalDate.of(2026, 1, 1)
    result.offenceSentenceInfo.expectedReleaseDate shouldBe null
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
    )

    val result = OffenceSentenceInfoBffResponseDto.from(person, offenceSentenceInfo)

    result.firstName shouldBe "Jane"
    result.lastName shouldBe "Doe"
    result.offenceSentenceInfo.sentenceEndDate shouldBe null
    result.offenceSentenceInfo.expectedReleaseDate shouldBe LocalDate.of(2026, 2, 1)
  }
}
