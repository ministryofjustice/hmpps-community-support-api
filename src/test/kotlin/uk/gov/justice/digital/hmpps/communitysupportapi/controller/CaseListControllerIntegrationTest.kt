package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod.GET
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PageResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralCaseListDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CommunityServiceProvider
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ServiceProvider
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralProviderAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ServiceProviderRepository
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

  private lateinit var referralHelper: ReferralTestSupport

  private lateinit var serviceProvider: ServiceProvider

  private lateinit var communityServiceProvider: CommunityServiceProvider

  @BeforeEach
  override fun setup() {
    referralHelper = ReferralTestSupport(
      personRepository,
      referralRepository,
      referralProviderAssignmentRepository,
      referralUserRepository,
      referralUserAssignmentRepository,
      serviceProviderRepository,
      communityServiceProviderRepository,
      userMapper,
    )

    val providers = referralHelper.getProviders()
    serviceProvider = providers.first
    communityServiceProvider = providers.second

    testDataCleaner.cleanAllTables()
    testDataCleaner.refreshMaterializedView()
  }

  @Nested
  @DisplayName("GET /bff/case-list/unassigned")
  inner class UnassignedCaseListEndpoint {

    fun getUnassignedCases(testUser: ReferralUser, uri: String = "/bff/case-list/unassigned"): PageResponse<ReferralCaseListDto> = webTestClient.get()
      .uri(uri)
      .headers(
        setAuthorisation(
          username = testUser.hmppsAuthUsername,
          roles = listOf("ROLE_IPB_FRONTEND_RW"),
        ),
      )
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody(object : ParameterizedTypeReference<PageResponse<ReferralCaseListDto>>() {})
      .returnResult().responseBody!!

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(GET, "/bff/case-list/unassigned")
    }

    @Test
    fun `should return forbidden if no role`() {
      assertForbiddenNoRole(GET, "/bff/case-list/unassigned")
    }

    @Test
    fun `should return forbidden if wrong role`() {
      assertForbiddenWrongRole(GET, "/bff/case-list/unassigned")
    }

    @Test
    fun `should return empty list when user has no service provider access`() {
      val testUser = referralHelper.createTestUser()

      stubManageUsersGetUserGroups(
        testUser.hmppsAuthId,
        listOf("INT_SP_${serviceProvider.authGroupId}" to "Test Provider Group"),
      )

      val response = getUnassignedCases(testUser)

      assertThat(response.content).isEmpty()
    }

    @Test
    fun `should return unassigned cases when user has service provider access`() {
      val testUser = referralHelper.createTestUser()
      val person = referralHelper.createPerson(identifier = "CRN12345")
      val referral = referralHelper.createReferral(person = person, submittedBy = testUser)

      stubManageUsersGetUserGroups(
        testUser.hmppsAuthId,
        listOf("INT_SP_${serviceProvider.authGroupId}" to "Test Provider Group"),
      )

      referralHelper.assignToCommunityServiceProvider(referral, communityServiceProvider)

      testDataCleaner.refreshMaterializedView()

      val response = getUnassignedCases(testUser)

      assertThat(response.content).hasSize(1)
      assertThat(response.content[0].referralId).isEqualTo(referral.id)
      assertThat(response.content[0].personName).isEqualTo("Smith, John")
      assertThat(response.content[0].personIdentifier).isEqualTo("CRN12345")
      assertThat(response.content[0].date).isNotNull()
    }

    @Test
    fun `should return multiple unassigned cases with pagination`() {
      val testUser = referralHelper.createTestUser()
      val persons = referralHelper.createPersons(count = 3)

      stubManageUsersGetUserGroups(
        testUser.hmppsAuthId,
        listOf("INT_SP_${serviceProvider.authGroupId}" to "Test Provider Group"),
      )

      persons.forEachIndexed { index, person ->
        val referral = referralHelper.createReferral(
          person = person,
          referenceNumber = "AB123${index + 1}CD",
          submittedBy = testUser,
          createdAt = OffsetDateTime.now().minusDays(index.toLong()),
        )

        referralHelper.assignToCommunityServiceProvider(referral, communityServiceProvider)
      }

      testDataCleaner.refreshMaterializedView()

      val response = getUnassignedCases(
        testUser,
        "/bff/case-list/unassigned?page=0&size=2",
      )

      assertThat(response.content).hasSize(2)
      response.content.forEach { caseDto ->
        assertThat(caseDto.referralId).isNotNull()
        assertThat(caseDto.personName).isNotBlank()
        assertThat(caseDto.personIdentifier).startsWith("CRN")
        assertThat(caseDto.date).isNotNull()
      }
    }

    @Test
    fun `should return unassigned cases sorted by date received in ascending order`() {
      val testUser = referralHelper.createTestUser()
      val olderPerson = referralHelper.createPerson("Older", "Person", "CRN_OLDER")
      val newerPerson = referralHelper.createPerson("Newer", "Person", "CRN_NEWER")
      val olderReferral = referralHelper.createReferral(
        person = olderPerson,
        referenceNumber = "REF_OLDER",
        submittedBy = testUser,
        createdAt = OffsetDateTime.now().minusDays(5),
      )
      val newerReferral = referralHelper.createReferral(
        person = newerPerson,
        referenceNumber = "REF_NEWER",
        submittedBy = testUser,
        createdAt = OffsetDateTime.now().minusDays(1),
      )

      stubManageUsersGetUserGroups(
        testUser.hmppsAuthId,
        listOf("INT_SP_${serviceProvider.authGroupId}" to "Test Provider Group"),
      )

      referralHelper.assignToCommunityServiceProvider(olderReferral, communityServiceProvider)
      referralHelper.assignToCommunityServiceProvider(newerReferral, communityServiceProvider)

      testDataCleaner.refreshMaterializedView()

      val response = getUnassignedCases(
        testUser,
        "/bff/case-list/unassigned?sortBy=dateReceived&sortDirection=ASC",
      )

      assertThat(response.content).hasSize(2)
      assertThat(response.content[0].personIdentifier).isEqualTo("CRN_OLDER")
      assertThat(response.content[1].personIdentifier).isEqualTo("CRN_NEWER")
      assertThat(response.content[0].date).isNotBlank()
      assertThat(response.content[1].date).isNotBlank()
    }
  }

  @Nested
  @DisplayName("GET /bff/case-list/in-progress")
  inner class InProgressCaseListEndpoint {

    fun getInProgressCases(testUser: ReferralUser, uri: String = "/bff/case-list/in-progress"): PageResponse<ReferralCaseListDto> = webTestClient.get()
      .uri(uri)
      .headers(
        setAuthorisation(
          username = testUser.hmppsAuthUsername,
          roles = listOf("ROLE_IPB_FRONTEND_RW"),
        ),
      )
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody(object : ParameterizedTypeReference<PageResponse<ReferralCaseListDto>>() {})
      .returnResult().responseBody!!

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(GET, "/bff/case-list/in-progress")
    }

    @Test
    fun `should return forbidden if no role`() {
      assertForbiddenNoRole(GET, "/bff/case-list/in-progress")
    }

    @Test
    fun `should return forbidden if wrong role`() {
      assertForbiddenWrongRole(GET, "/bff/case-list/in-progress")
    }

    @Test
    fun `should return empty in-progress page when user has no service provider access`() {
      val testUser = referralHelper.createTestUser()

      stubManageUsersGetUserGroups(
        testUser.hmppsAuthId,
        listOf("INT_SP_${serviceProvider.authGroupId}" to "Test Provider Group"),
      )

      testDataCleaner.refreshMaterializedView()

      val response = getInProgressCases(testUser)

      assertThat(response.content).isEmpty()
    }

    @Test
    fun `should return in-progress cases when user has service provider access`() {
      val testUser = referralHelper.createTestUser()
      val person = referralHelper.createPerson(identifier = "CRN12345")
      val referral = referralHelper.createReferral(person = person, submittedBy = testUser)
      val caseWorkers = referralHelper.createCaseWorkers("CaseWorker One", "CaseWorker Two", "CaseWorker Three")

      stubManageUsersGetUserGroups(
        testUser.hmppsAuthId,
        listOf("INT_SP_${serviceProvider.authGroupId}" to "Test Provider Group"),
      )

      referralHelper.assignToCommunityServiceProvider(referral, communityServiceProvider)
      referralHelper.assignCaseWorkers(referral, caseWorkers)

      testDataCleaner.refreshMaterializedView()

      val response = getInProgressCases(testUser)

      assertThat(response.content).hasSize(1)
      assertThat(response.content[0].referralId).isEqualTo(referral.id)
      assertThat(response.content[0].personName).isEqualTo("Smith, John")
      assertThat(response.content[0].personIdentifier).isEqualTo("CRN12345")
      assertThat(response.content[0].date).isNotNull()
      assertThat(response.content[0].caseWorkers).containsExactly("CaseWorker One", "CaseWorker Two", "CaseWorker Three")
    }

    @Test
    fun `should return multiple in-progress cases with pagination`() {
      val testUser = referralHelper.createTestUser()
      val persons = referralHelper.createPersons(count = 3)
      val caseWorkers = referralHelper.createCaseWorkers("CaseWorker One", "CaseWorker Two")

      stubManageUsersGetUserGroups(
        testUser.hmppsAuthId,
        listOf("INT_SP_${serviceProvider.authGroupId}" to "Test Provider Group"),
      )

      persons.forEachIndexed { index, person ->
        val createdAt = OffsetDateTime.now().minusDays(index.toLong())

        referralHelper.createInProgressReferral(
          person = person,
          referenceNumber = "AB123${index + 1}CD",
          submittedBy = testUser,
          caseWorkers = caseWorkers,
          communityServiceProvider = communityServiceProvider,
          createdAt = createdAt,
        )
      }

      testDataCleaner.refreshMaterializedView()

      val response = getInProgressCases(
        testUser,
        "/bff/case-list/in-progress?page=0&size=2",
      )

      assertThat(response.content).hasSize(2)

      response.content.forEach { caseDto ->
        assertThat(caseDto.referralId).isNotNull()
        assertThat(caseDto.personName).isNotBlank()
        assertThat(caseDto.personIdentifier).startsWith("CRN")
        assertThat(caseDto.date).isNotNull()
        assertThat(caseDto.caseWorkers).containsExactly("CaseWorker One", "CaseWorker Two")
      }
    }

    @Test
    fun `should return in-progress cases sorted by date received in ascending order`() {
      val testUser = referralHelper.createTestUser()
      val olderCreatedAt = OffsetDateTime.now().minusDays(5)
      val newerCreatedAt = OffsetDateTime.now().minusDays(1)
      val olderPerson = referralHelper.createPerson(firstName = "Older", lastName = "Person", identifier = "CRN_OLDER")
      val newerPerson = referralHelper.createPerson(firstName = "Newer", lastName = "Person", identifier = "CRN_NEWER")
      val olderReferral = referralHelper.createReferral(
        person = olderPerson,
        referenceNumber = "REF_OLDER",
        submittedBy = testUser,
        createdAt = olderCreatedAt,
      )
      val newerReferral = referralHelper.createReferral(
        person = newerPerson,
        referenceNumber = "REF_NEWER",
        submittedBy = testUser,
        createdAt = newerCreatedAt,
      )

      val caseWorkers = referralHelper.createCaseWorkers("CaseWorker One", "CaseWorker Two")

      stubManageUsersGetUserGroups(
        testUser.hmppsAuthId,
        listOf("INT_SP_${serviceProvider.authGroupId}" to "Test Provider Group"),
      )

      referralHelper.assignToCommunityServiceProvider(olderReferral, communityServiceProvider)
      referralHelper.assignToCommunityServiceProvider(newerReferral, communityServiceProvider)

      referralHelper.assignCaseWorkers(olderReferral, caseWorkers)
      referralHelper.assignCaseWorkers(newerReferral, caseWorkers)

      testDataCleaner.refreshMaterializedView()

      val response = getInProgressCases(
        testUser,
        "/bff/case-list/in-progress?sortBy=dateReceived&sortDirection=ASC",
      )

      assertThat(response.content).hasSize(2)
      assertThat(response.content[0].personIdentifier).isEqualTo("CRN_OLDER")
      assertThat(response.content[1].personIdentifier).isEqualTo("CRN_NEWER")
      assertThat(response.content[0].date).isNotBlank()
      assertThat(response.content[1].date).isNotBlank()
    }
  }
}
