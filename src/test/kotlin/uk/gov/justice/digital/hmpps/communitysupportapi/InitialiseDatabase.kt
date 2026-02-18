package uk.gov.justice.digital.hmpps.communitysupportapi

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
class InitialiseDatabase {

  // This is needed to initialize the database for schema spy
  @Test
  fun `initialises database`() {
    println("Database has been initialised by IntegrationTestBase")
  }
}
