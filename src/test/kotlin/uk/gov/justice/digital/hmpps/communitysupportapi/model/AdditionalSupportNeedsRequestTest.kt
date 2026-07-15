package uk.gov.justice.digital.hmpps.communitysupportapi.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AdditionalSupportNeedsRequestTest {

  @Test
  fun `normalised should keep details when additional support is required`() {
    val request = AdditionalSupportNeedsRequest(
      physicalHealth = "Physical",
      mentalEmotionalHealth = "Mental",
      neurodiversity = "Neuro",
      locationTravel = "Travel",
      caringResponsibilities = "Caring",
      employmentResponsibilities = "Employment",
      diversity = "Diversity",
      anythingElse = "Anything else",
      needsAdditionalSupport = true,
    )

    val normalised = request.normaliseAgainstNeedsAdditionalSupport()

    assertThat(normalised).isSameAs(request)
  }

  @Test
  fun `normalised should clear details when additional support is not required`() {
    val request = AdditionalSupportNeedsRequest(
      physicalHealth = "Physical",
      mentalEmotionalHealth = "Mental",
      neurodiversity = "Neuro",
      locationTravel = "Travel",
      caringResponsibilities = "Caring",
      employmentResponsibilities = "Employment",
      diversity = "Diversity",
      anythingElse = "Anything else",
      needsAdditionalSupport = false,
    )

    val normalised = request.normaliseAgainstNeedsAdditionalSupport()

    assertThat(normalised.needsAdditionalSupport).isFalse()
    assertThat(normalised.physicalHealth).isNull()
    assertThat(normalised.mentalEmotionalHealth).isNull()
    assertThat(normalised.neurodiversity).isNull()
    assertThat(normalised.locationTravel).isNull()
    assertThat(normalised.caringResponsibilities).isNull()
    assertThat(normalised.employmentResponsibilities).isNull()
    assertThat(normalised.diversity).isNull()
    assertThat(normalised.anythingElse).isNull()
  }
}
