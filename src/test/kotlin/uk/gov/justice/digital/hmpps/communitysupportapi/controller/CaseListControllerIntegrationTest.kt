package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralCaseListDto
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralProviderAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralProviderAssignmentFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralUserFactory
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.time.OffsetDateTime
import java.util.UUID

class CaseListControllerIntegrationTest : IntegrationTestBase() {

  @MockitoBean
  private lateinit var userMapper: UserMapper

  @Autowired
  private lateinit var personRepository: PersonRepository

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var communityServiceProviderRepository: CommunityServiceProviderRepository

  @Autowired
  private lateinit var serviceProviderRepository: ServiceProviderRepository

  @Autowired
  private lateinit var referralProviderAssignmentRepository: ReferralProviderAssignmentRepository

  @Autowired
  private lateinit var referralUserRepository: ReferralUserRepository

  @Nested
  @DisplayName("GET /bff/case-list/unassigned")
  inner class UnassignedCaseListEndpoint {

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
      testDataCleaner.refreshMaterializedView()
    }

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri("/bff/case-list/unassigned")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("/bff/case-list/unassigned")
        .headers(
          setAuthorisation(
            "AUTH_ADM",
            listOf(),
            listOf("read"),
          ),
        )
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri("/bff/case-list/unassigned")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return empty list when user has no service provider access`() {
      val testUserId = UUID.randomUUID().toString()
      val testUser = ReferralUserFactory()
        .withHmppsAuthId(testUserId)
        .withHmppsAuthUsername("test-user")
        .create()

      ensureReferralUser(testUser.id, testUser.hmppsAuthId, testUser.hmppsAuthUsername)

      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val serviceProvider = serviceProviderRepository.findAll().first()

      // Stub ManageUsers API to return service provider group
      // This allows ServiceProviderAccessScopeMapper to find the provider
      stubManageUsersGetUserGroups(
        testUserId,
        listOf(
          "INT_SP_${serviceProvider.authGroupId}" to "Test Provider Group",
        ),
      )

      val response = webTestClient
        .method(HttpMethod.GET)
        .uri("/bff/case-list/unassigned")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(
          setAuthorisation(
            username = "test-user",
            roles = listOf("ROLE_IPB_FRONTEND_RW"),
          ),
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody(object : ParameterizedTypeReference<List<ReferralCaseListDto>>() {})
        .returnResult().responseBody!!

      // Then - should return empty list (no cases assigned)
      assertThat(response).isEmpty()
    }

    @Test
    fun `should return unassigned cases when user has service provider access`() {
      // Given - set up test user
      val testUserUUID = UUID.randomUUID()
      val testUserId = testUserUUID.toString()
      val testUser = ReferralUserFactory()
        .withHmppsAuthId(testUserId)
        .withHmppsAuthUsername("test-user")
        .create()

      ensureReferralUser(testUserUUID, testUserId, testUser.hmppsAuthUsername)

      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      val serviceProvider = serviceProviderRepository.findAll().first()
      val communityServiceProvider = communityServiceProviderRepository.findAll()
        .first { it.serviceProvider.id == serviceProvider.id }

      stubManageUsersGetUserGroups(
        testUserId,
        listOf(
          "INT_SP_${serviceProvider.authGroupId}" to "Test Provider Group",
        ),
      )

      val person = personRepository.save(
        PersonFactory()
          .withFirstName("John")
          .withLastName("Doe")
          .withIdentifier("CRN12345")
          .create(),
      )

      val referral = ReferralFactory()
        .withPersonId(person.id)
        .withCrn(person.identifier)
        .withReferenceNumber("REF-001")
        .withSubmittedEvent(actorId = testUserUUID)
        .create()
      val savedReferral = referralRepository.save(referral)

      val providerAssignment = ReferralProviderAssignmentFactory()
        .withReferral(savedReferral)
        .withCommunityServiceProvider(communityServiceProvider)
        .create()
      referralProviderAssignmentRepository.save(providerAssignment)

      // Refresh materialized view to include new data
      testDataCleaner.refreshMaterializedView()

      // When
      val response = webTestClient
        .method(HttpMethod.GET)
        .uri("/bff/case-list/unassigned")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(
          setAuthorisation(
            username = "test-user",
            roles = listOf("ROLE_IPB_FRONTEND_RW"),
          ),
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody(object : ParameterizedTypeReference<List<ReferralCaseListDto>>() {})
        .returnResult().responseBody!!

      // Then
      assertThat(response).hasSize(1)
      assertThat(response[0].referralId).isEqualTo(savedReferral.id)
      assertThat(response[0].personName).isEqualTo("Doe, John")
      assertThat(response[0].personIdentifier).isEqualTo("CRN12345")
      assertThat(response[0].dateReceived).isNotNull()
    }

    @Test
    fun `should return multiple unassigned cases with pagination`() {
      // Given - set up test user
      val testUserUUID = UUID.randomUUID()
      val testUserId = testUserUUID.toString()
      val testUser = ReferralUserFactory()
        .withHmppsAuthId(testUserId)
        .withHmppsAuthUsername("test-user")
        .create()

      ensureReferralUser(testUserUUID, testUserId, testUser.hmppsAuthUsername)

      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val serviceProvider = serviceProviderRepository.findAll().first()
      val communityServiceProvider = communityServiceProviderRepository.findAll()
        .first { it.serviceProvider.id == serviceProvider.id }

      // Stub ManageUsers API to return service provider group with INT_SP_ prefix
      stubManageUsersGetUserGroups(
        testUserId,
        listOf(
          "INT_SP_${serviceProvider.authGroupId}" to "Test Provider Group",
        ),
      )

      val persons = (1..3).map { index ->
        personRepository.save(
          PersonFactory()
            .withFirstName("FirstName$index")
            .withLastName("LastName$index")
            .withIdentifier("CRN0000$index")
            .create(),
        )
      }

      persons.mapIndexed { index, person ->
        val referral = ReferralFactory()
          .withPersonId(person.id)
          .withCrn(person.identifier)
          .withReferenceNumber("REF-00${index + 1}")
          .withCreatedAt(OffsetDateTime.now().minusDays(index.toLong()))
          .withSubmittedEvent(testUserUUID, OffsetDateTime.now().minusDays(index.toLong()))
          .create()
        val savedReferral = referralRepository.save(referral)

        referralProviderAssignmentRepository.save(
          ReferralProviderAssignmentFactory()
            .withReferral(savedReferral)
            .withCommunityServiceProvider(communityServiceProvider)
            .create(),
        )
        savedReferral
      }

      // Refresh materialized view to include new data
      testDataCleaner.refreshMaterializedView()

      // When - request first page with size 2
      val response = webTestClient
        .method(HttpMethod.GET)
        .uri("/bff/case-list/unassigned?page=0&size=2")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(
          setAuthorisation(
            username = "test-user",
            roles = listOf("ROLE_IPB_FRONTEND_RW"),
          ),
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody(object : ParameterizedTypeReference<List<ReferralCaseListDto>>() {})
        .returnResult().responseBody!!

      // Then
      assertThat(response).hasSize(2)
      response.forEach { caseDto ->
        assertThat(caseDto.referralId).isNotNull()
        assertThat(caseDto.personName).isNotBlank()
        assertThat(caseDto.personIdentifier).startsWith("CRN")
        assertThat(caseDto.dateReceived).isNotNull()
      }
    }

    @Test
    fun `should return unassigned cases sorted by dateReceived in ascending order`() {
      // Given - set up test user
      val testUserUUID = UUID.randomUUID()
      val testUserId = testUserUUID.toString()
      val testUser = ReferralUserFactory()
        .withHmppsAuthId(testUserId)
        .withHmppsAuthUsername("test-user")
        .create()

      ensureReferralUser(testUserUUID, testUserId, testUser.hmppsAuthUsername)

      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val serviceProvider = serviceProviderRepository.findAll().first()
      val communityServiceProvider = communityServiceProviderRepository.findAll()
        .first { it.serviceProvider.id == serviceProvider.id }

      // Stub ManageUsers API to return service provider group with INT_SP_ prefix
      stubManageUsersGetUserGroups(
        testUserId,
        listOf(
          "INT_SP_${serviceProvider.authGroupId}" to "Test Provider Group",
        ),
      )

      val olderPerson = personRepository.save(
        PersonFactory()
          .withFirstName("Older")
          .withLastName("Person")
          .withIdentifier("CRN_OLDER")
          .withGender("Male")
          .create(),
      )

      val newerPerson = personRepository.save(
        PersonFactory()
          .withFirstName("Newer")
          .withLastName("Person")
          .withIdentifier("CRN_NEWER")
          .withGender("Female")
          .create(),
      )

      val olderReferralTime = OffsetDateTime.now().minusDays(5)
      val olderReferral = ReferralFactory()
        .withPersonId(olderPerson.id)
        .withCrn(olderPerson.identifier)
        .withReferenceNumber("REF-OLDER")
        .withCreatedAt(olderReferralTime)
        .withSubmittedEvent(testUserUUID, olderReferralTime)
        .create()
      val savedOlderReferral = referralRepository.save(olderReferral)
      referralProviderAssignmentRepository.save(
        ReferralProviderAssignmentFactory()
          .withReferral(savedOlderReferral)
          .withCommunityServiceProvider(communityServiceProvider)
          .create(),
      )

      val newerReferralTime = OffsetDateTime.now().minusDays(1)
      val newerReferral = ReferralFactory()
        .withPersonId(newerPerson.id)
        .withCrn(newerPerson.identifier)
        .withReferenceNumber("REF-NEWER")
        .withCreatedAt(newerReferralTime)
        .withSubmittedEvent(testUserUUID, newerReferralTime)
        .create()
      val savedNewerReferral = referralRepository.save(newerReferral)
      referralProviderAssignmentRepository.save(
        ReferralProviderAssignmentFactory()
          .withReferral(savedNewerReferral)
          .withCommunityServiceProvider(communityServiceProvider)
          .create(),
      )

      // Refresh materialized view to include new data
      testDataCleaner.refreshMaterializedView()

      // When - request with ascending sort
      val response = webTestClient
        .method(HttpMethod.GET)
        .uri("/bff/case-list/unassigned?sortBy=dateReceived&sortDirection=ASC")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(
          setAuthorisation(
            username = "test-user",
            roles = listOf("ROLE_IPB_FRONTEND_RW"),
          ),
        )
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody(object : ParameterizedTypeReference<List<ReferralCaseListDto>>() {})
        .returnResult().responseBody!!

      // Then - older referral should come first
      assertThat(response).hasSize(2)
      assertThat(response[0].personIdentifier).isEqualTo("CRN_OLDER")
      assertThat(response[1].personIdentifier).isEqualTo("CRN_NEWER")
      assertThat(response[0].dateReceived).isBefore(response[1].dateReceived)
    }
  }

  private fun ensureReferralUser(id: UUID, hmppsAuthId: String, username: String) {
    if (!referralUserRepository.existsById(id)) {
      referralUserRepository.save(
        ReferralUserFactory()
          .withId(id)
          .withHmppsAuthId(hmppsAuthId)
          .withHmppsAuthUsername(username)
          .withAuthSource("auth")
          .create(),
      )
    }
  }
}
