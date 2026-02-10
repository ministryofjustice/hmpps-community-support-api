package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralCaseListDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.CaseListTestFixture
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralProviderAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.util.RestPageImpl
import java.time.OffsetDateTime

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
  private lateinit var referralUserAssignmentRepository: ReferralUserAssignmentRepository

  @Autowired
  private lateinit var referralUserRepository: ReferralUserRepository

  private lateinit var fixture: CaseListTestFixture

  @BeforeEach
  override fun setup() {
    fixture = CaseListTestFixture(
      personRepository,
      referralRepository,
      referralProviderAssignmentRepository,
      referralUserRepository,
      referralUserAssignmentRepository,
      serviceProviderRepository,
      communityServiceProviderRepository,
      userMapper,
    )
    fixture.initialiseProviders()

    testDataCleaner.cleanAllTables()
    testDataCleaner.refreshMaterializedView()
  }

  @Nested
  @DisplayName("GET /bff/case-list/unassigned")
  inner class UnassignedCaseListEndpoint {

    fun getUnassignedCases(testUser: ReferralUser, uri: String = "/bff/case-list/unassigned"): RestPageImpl<ReferralCaseListDto> = webTestClient
      .get()
      .uri(uri)
      .headers(setAuthorisation(username = testUser.hmppsAuthUsername, roles = listOf("ROLE_IPB_FRONTEND_RW")))
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody(object : ParameterizedTypeReference<RestPageImpl<ReferralCaseListDto>>() {})
      .returnResult().responseBody!!

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
      val testUser = fixture.createTestUser()

      stubManageUsersGetUserGroups(
        testUser.hmppsAuthId,
        listOf("INT_SP_${fixture.serviceProvider.authGroupId}" to "Test Provider Group"),
      )

      val response = getUnassignedCases(testUser)

      assertThat(response).isEmpty()
    }

    @Test
    fun `should return unassigned cases when user has service provider access`() {
      val testUser = fixture.createTestUser()
      val person = fixture.createPerson(firstName = "John", lastName = "Doe", crn = "CRN12345")
      val referral = fixture.createReferral(person = person, referenceNumber = "REF-001", submittedBy = testUser)

      stubManageUsersGetUserGroups(
        testUser.hmppsAuthId,
        listOf("INT_SP_${fixture.serviceProvider.authGroupId}" to "Test Provider Group"),
      )

      fixture.assignToCommunityServiceProvider(referral)
      testDataCleaner.refreshMaterializedView()

      val response = getUnassignedCases(testUser)

      assertThat(response).hasSize(1)
      assertThat(response.content[0].referralId).isEqualTo(referral.id)
      assertThat(response.content[0].personName).isEqualTo("Doe, John")
      assertThat(response.content[0].personIdentifier).isEqualTo("CRN12345")
      assertThat(response.content[0].date).isNotNull()
    }

    @Test
    fun `should return multiple unassigned cases with pagination`() {
      val testUser = fixture.createTestUser()
      val persons = fixture.createPersons(count = 3)

      stubManageUsersGetUserGroups(
        testUser.hmppsAuthId,
        listOf("INT_SP_${fixture.serviceProvider.authGroupId}" to "Test Provider Group"),
      )

      persons.forEachIndexed { index, person ->
        val referral = fixture.createReferral(
          person = person,
          referenceNumber = "REF-00${index + 1}",
          submittedBy = testUser,
          createdAt = OffsetDateTime.now().minusDays(index.toLong()),
        )

        fixture.assignToCommunityServiceProvider(referral)
      }

      testDataCleaner.refreshMaterializedView()

      val response = getUnassignedCases(
        testUser,
        "/bff/case-list/unassigned?page=0&size=2",
      )

      assertThat(response).hasSize(2)
      response.forEach { caseDto ->
        assertThat(caseDto.referralId).isNotNull()
        assertThat(caseDto.personName).isNotBlank()
        assertThat(caseDto.personIdentifier).startsWith("CRN")
        assertThat(caseDto.date).isNotNull()
      }
    }

    @Test
    fun `should return unassigned cases sorted by date received in ascending order`() {
      val testUser = fixture.createTestUser()
      val olderPerson = fixture.createPerson("Older", "Person", "CRN_OLDER")
      val newerPerson = fixture.createPerson("Newer", "Person", "CRN_NEWER")
      val olderReferral = fixture.createReferral(
        person = olderPerson,
        referenceNumber = "REF_OLDER",
        submittedBy = testUser,
        createdAt = OffsetDateTime.now().minusDays(5),
      )
      val newerReferral = fixture.createReferral(
        person = newerPerson,
        referenceNumber = "REF_NEWER",
        submittedBy = testUser,
        createdAt = OffsetDateTime.now().minusDays(1),
      )

      stubManageUsersGetUserGroups(
        testUser.hmppsAuthId,
        listOf("INT_SP_${fixture.serviceProvider.authGroupId}" to "Test Provider Group"),
      )

      fixture.assignToCommunityServiceProvider(olderReferral)
      fixture.assignToCommunityServiceProvider(newerReferral)

      testDataCleaner.refreshMaterializedView()

      val response = getUnassignedCases(
        testUser,
        "/bff/case-list/unassigned?sortBy=dateReceived&sortDirection=ASC",
      )

      assertThat(response).hasSize(2)
      assertThat(response.content[0].personIdentifier).isEqualTo("CRN_OLDER")
      assertThat(response.content[1].personIdentifier).isEqualTo("CRN_NEWER")
      assertThat(response.content[0].date).isBefore(response.content[1].date)
    }
  }

  @Nested
  @DisplayName("GET /bff/case-list/in-progress")
  inner class InProgressCaseListEndpoint {

    fun getInProgressCases(testUser: ReferralUser, uri: String = "/bff/case-list/in-progress"): RestPageImpl<ReferralCaseListDto> = webTestClient
      .get()
      .uri(uri)
      .headers(setAuthorisation(username = testUser.hmppsAuthUsername, roles = listOf("ROLE_IPB_FRONTEND_RW")))
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody(object : ParameterizedTypeReference<RestPageImpl<ReferralCaseListDto>>() {})
      .returnResult().responseBody!!

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri("/bff/case-list/in-progress")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("/bff/case-list/in-progress")
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
        .uri("/bff/case-list/in-progress")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return empty in-progress page when user has no service provider access`() {
      val testUser = fixture.createTestUser()

      stubManageUsersGetUserGroups(
        testUser.hmppsAuthId,
        listOf("INT_SP_${fixture.serviceProvider.authGroupId}" to "Test Provider Group"),
      )

      testDataCleaner.refreshMaterializedView()

      val response = getInProgressCases(testUser)

      assertThat(response).isEmpty()
    }

    @Test
    fun `should return in-progress cases when user has service provider access`() {
      val testUser = fixture.createTestUser()
      val person = fixture.createPerson(firstName = "John", lastName = "Doe", crn = "CRN12345")
      val referral = fixture.createReferral(person = person, referenceNumber = "REF-001", submittedBy = testUser)
      val caseWorkers = fixture.createCaseWorkers("CaseWorker One", "CaseWorker Two", "CaseWorker Three")

      stubManageUsersGetUserGroups(
        testUser.hmppsAuthId,
        listOf("INT_SP_${fixture.serviceProvider.authGroupId}" to "Test Provider Group"),
      )

      fixture.assignToCommunityServiceProvider(referral)
      fixture.assignCaseWorkers(referral, caseWorkers)

      testDataCleaner.refreshMaterializedView()

      val response = getInProgressCases(testUser)

      assertThat(response).hasSize(1)
      assertThat(response.content[0].referralId).isEqualTo(referral.id)
      assertThat(response.content[0].personName).isEqualTo("Doe, John")
      assertThat(response.content[0].personIdentifier).isEqualTo("CRN12345")
      assertThat(response.content[0].date).isNotNull()
      assertThat(response.content[0].caseWorkers).containsExactly("CaseWorker One", "CaseWorker Two", "CaseWorker Three")
    }

    @Test
    fun `should return multiple in-progress cases with pagination`() {
      val testUser = fixture.createTestUser()
      val persons = fixture.createPersons(count = 3)
      val caseWorkers = fixture.createCaseWorkers("CaseWorker One", "CaseWorker Two")

      stubManageUsersGetUserGroups(
        testUser.hmppsAuthId,
        listOf("INT_SP_${fixture.serviceProvider.authGroupId}" to "Test Provider Group"),
      )

      persons.forEachIndexed { index, person ->
        val createdAt = OffsetDateTime.now().minusDays(index.toLong())

        fixture.createInProgressReferral(
          person = person,
          referenceNumber = "REF-00${index + 1}",
          submittedBy = testUser,
          caseWorkers = caseWorkers,
          createdAt = createdAt,
        )
      }

      testDataCleaner.refreshMaterializedView()

      val response = getInProgressCases(
        testUser,
        "/bff/case-list/in-progress?page=0&size=2",
      )

      assertThat(response).hasSize(2)

      response.forEach { caseDto ->
        assertThat(caseDto.referralId).isNotNull()
        assertThat(caseDto.personName).isNotBlank()
        assertThat(caseDto.personIdentifier).startsWith("CRN")
        assertThat(caseDto.date).isNotNull()
        assertThat(caseDto.caseWorkers).containsExactly("CaseWorker One", "CaseWorker Two")
      }
    }

    @Test
    fun `should return in-progress cases sorted by date received in ascending order`() {
      val testUser = fixture.createTestUser()
      val olderCreatedAt = OffsetDateTime.now().minusDays(5)
      val newerCreatedAt = OffsetDateTime.now().minusDays(1)
      val olderPerson = fixture.createPerson(firstName = "Older", lastName = "Person", crn = "CRN_OLDER")
      val newerPerson = fixture.createPerson(firstName = "Newer", lastName = "Person", crn = "CRN_NEWER")
      val olderReferral = fixture.createReferral(
        person = olderPerson,
        referenceNumber = "REF_OLDER",
        submittedBy = testUser,
        createdAt = olderCreatedAt,
      )
      val newerReferral = fixture.createReferral(
        person = newerPerson,
        referenceNumber = "REF_NEWER",
        submittedBy = testUser,
        createdAt = newerCreatedAt,
      )

      val caseWorkers = fixture.createCaseWorkers("CaseWorker One", "CaseWorker Two")

      stubManageUsersGetUserGroups(
        testUser.hmppsAuthId,
        listOf("INT_SP_${fixture.serviceProvider.authGroupId}" to "Test Provider Group"),
      )

      fixture.assignToCommunityServiceProvider(olderReferral)
      fixture.assignToCommunityServiceProvider(newerReferral)

      fixture.assignCaseWorkers(olderReferral, caseWorkers)
      fixture.assignCaseWorkers(newerReferral, caseWorkers)

      testDataCleaner.refreshMaterializedView()

      val response = getInProgressCases(
        testUser,
        "/bff/case-list/in-progress?sortBy=dateReceived&sortDirection=ASC",
      )

      assertThat(response).hasSize(2)
      assertThat(response.content[0].personIdentifier).isEqualTo("CRN_OLDER")
      assertThat(response.content[1].personIdentifier).isEqualTo("CRN_NEWER")
      assertThat(response.content[0].date).isBefore(response.content[1].date)
    }
  }
}
