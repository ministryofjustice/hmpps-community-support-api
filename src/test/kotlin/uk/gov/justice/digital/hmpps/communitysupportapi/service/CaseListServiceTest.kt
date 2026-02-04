package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.ServiceProviderAccessScope
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.ServiceProviderAccessScopeMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CaseListView
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ServiceProvider
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CaseListViewRepository
import uk.gov.justice.hmpps.kotlin.auth.AuthSource
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.time.OffsetDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class CaseListServiceTest {

  @Mock
  lateinit var caseListViewRepository: CaseListViewRepository

  @Mock
  lateinit var authenticationHolder: HmppsAuthenticationHolder

  @Mock
  lateinit var userMapper: UserMapper

  @Mock
  lateinit var serviceProviderAccessScopeMapper: ServiceProviderAccessScopeMapper

  lateinit var caseListService: CaseListService

  @BeforeEach
  fun setUp() {
    caseListService = CaseListService(caseListViewRepository, authenticationHolder, userMapper, serviceProviderAccessScopeMapper)
  }

  private val pageable: Pageable = PageRequest.of(0, 10)

  @Test
  fun `should throw AuthenticationCredentialsNotFoundException when username is null`() {
    // Given
    whenever(authenticationHolder.username).thenReturn(null)

    // When & Then
    assertThatThrownBy { caseListService.getUnassignedCases(pageable) }
      .isInstanceOf(AuthenticationCredentialsNotFoundException::class.java)
      .hasMessage("No authenticated user found")
    verify(userMapper, never()).fromToken(any<HmppsAuthenticationHolder>())
    verify(serviceProviderAccessScopeMapper, never()).fromUser(any())
    verify(caseListViewRepository, never()).findAll(any<Specification<CaseListView>>(), any<Pageable>())
  }

  @Test
  fun `should throw AuthenticationCredentialsNotFoundException when username is blank`() {
    // Given
    whenever(authenticationHolder.username).thenReturn("")

    // When & Then
    assertThatThrownBy { caseListService.getUnassignedCases(pageable) }
      .isInstanceOf(AuthenticationCredentialsNotFoundException::class.java)
      .hasMessage("No authenticated user found")
    verify(userMapper, never()).fromToken(any<HmppsAuthenticationHolder>())
    verify(serviceProviderAccessScopeMapper, never()).fromUser(any())
    verify(caseListViewRepository, never()).findAll(any<Specification<CaseListView>>(), any<Pageable>())
  }

  @Test
  fun `should throw AuthenticationCredentialsNotFoundException when username is whitespace`() {
    // Given
    whenever(authenticationHolder.username).thenReturn("   ")

    // When & Then
    assertThatThrownBy { caseListService.getUnassignedCases(pageable) }
      .isInstanceOf(AuthenticationCredentialsNotFoundException::class.java)
      .hasMessage("No authenticated user found")
    verify(userMapper, never()).fromToken(any<HmppsAuthenticationHolder>())
    verify(serviceProviderAccessScopeMapper, never()).fromUser(any())
    verify(caseListViewRepository, never()).findAll(any<Specification<CaseListView>>(), any<Pageable>())
  }

  @Test
  fun `should return empty page when user is a Delius user`() {
    // Given
    whenever(authenticationHolder.username).thenReturn("delius-user")
    whenever(authenticationHolder.authSource).thenReturn(AuthSource.DELIUS)

    // When
    val result = caseListService.getUnassignedCases(pageable)

    // Then
    assertThat(result.isEmpty).isTrue()
    verify(userMapper, never()).fromToken(any<HmppsAuthenticationHolder>())
    verify(serviceProviderAccessScopeMapper, never()).fromUser(any())
    verify(caseListViewRepository, never()).findAll(any<Specification<CaseListView>>(), any<Pageable>())
  }

  @Test
  fun `should return empty page when user has no service provider access`() {
    // Given
    val referralUser = createReferralUser()
    val emptyAccessScope = ServiceProviderAccessScope(emptySet())
    val emptyPage: Page<CaseListView> = Page.empty()

    whenever(authenticationHolder.username).thenReturn("test-user")
    whenever(authenticationHolder.authSource).thenReturn(AuthSource.AUTH)
    whenever(userMapper.fromToken(authenticationHolder)).thenReturn(referralUser)
    whenever(serviceProviderAccessScopeMapper.fromUser(referralUser)).thenReturn(emptyAccessScope)
    whenever(caseListViewRepository.findAll(any<Specification<CaseListView>>(), any<Pageable>())).thenReturn(emptyPage)

    // When
    val result = caseListService.getUnassignedCases(pageable)

    // Then
    assertThat(result.isEmpty).isTrue()
    verify(userMapper).fromToken(authenticationHolder)
    verify(serviceProviderAccessScopeMapper).fromUser(referralUser)
    verify(caseListViewRepository).findAll(any<Specification<CaseListView>>(), any<Pageable>())
  }

  @Test
  fun `should return unassigned cases filtered by service providers`() {
    // Given
    val referralUser = createReferralUser()
    val serviceProvider = createServiceProvider()
    val accessScope = ServiceProviderAccessScope(setOf(serviceProvider))
    val caseListViews = listOf(createCaseListView(serviceProvider.id))
    val expectedPage: Page<CaseListView> = PageImpl(caseListViews, pageable, caseListViews.size.toLong())

    whenever(authenticationHolder.username).thenReturn("test-user")
    whenever(authenticationHolder.authSource).thenReturn(AuthSource.AUTH)
    whenever(userMapper.fromToken(authenticationHolder)).thenReturn(referralUser)
    whenever(serviceProviderAccessScopeMapper.fromUser(referralUser)).thenReturn(accessScope)
    whenever(caseListViewRepository.findAll(any<Specification<CaseListView>>(), eq(pageable))).thenReturn(expectedPage)

    // When
    val result = caseListService.getUnassignedCases(pageable)

    // Then
    assertThat(result.totalElements).isEqualTo(1)
    assertThat(result.content[0].referralId).isEqualTo(caseListViews[0].referralId)
    verify(userMapper).fromToken(authenticationHolder)
    verify(serviceProviderAccessScopeMapper).fromUser(referralUser)
    verify(caseListViewRepository).findAll(any<Specification<CaseListView>>(), eq(pageable))
  }

  @Test
  fun `should return multiple cases when user has access to multiple service providers`() {
    // Given
    val referralUser = createReferralUser()
    val serviceProvider1 = createServiceProvider(UUID.randomUUID(), "SP_1", "Provider 1")
    val serviceProvider2 = createServiceProvider(UUID.randomUUID(), "SP_2", "Provider 2")
    val accessScope = ServiceProviderAccessScope(setOf(serviceProvider1, serviceProvider2))

    val caseListViews = listOf(
      createCaseListView(serviceProvider1.id),
      createCaseListView(serviceProvider2.id),
    )
    val expectedPage: Page<CaseListView> = PageImpl(caseListViews, pageable, caseListViews.size.toLong())

    whenever(authenticationHolder.username).thenReturn("test-user")
    whenever(authenticationHolder.authSource).thenReturn(AuthSource.AUTH)
    whenever(userMapper.fromToken(authenticationHolder)).thenReturn(referralUser)
    whenever(serviceProviderAccessScopeMapper.fromUser(referralUser)).thenReturn(accessScope)
    whenever(caseListViewRepository.findAll(any<Specification<CaseListView>>(), eq(pageable))).thenReturn(expectedPage)

    // When
    val result = caseListService.getUnassignedCases(pageable)

    // Then
    assertThat(result.totalElements).isEqualTo(2)
    verify(caseListViewRepository).findAll(any<Specification<CaseListView>>(), eq(pageable))
  }

  private fun createReferralUser(
    id: UUID = UUID.randomUUID(),
    hmppsAuthId: String = UUID.randomUUID().toString(),
    hmppsAuthUsername: String = "test-user",
    authSource: String = "auth",
  ) = ReferralUser(
    id = id,
    hmppsAuthId = hmppsAuthId,
    hmppsAuthUsername = hmppsAuthUsername,
    authSource = authSource,
  )

  private fun createServiceProvider(
    id: UUID = UUID.randomUUID(),
    authGroupId: String = "INT_SP_TEST",
    name: String = "Test Provider",
  ) = ServiceProvider(
    id = id,
    authGroupId = authGroupId,
    name = name,
  )

  private fun createCaseListView(
    serviceProviderId: UUID,
    referralId: UUID = UUID.randomUUID(),
    personName: String = "Test, Person",
    personIdentifier: String = "X123456",
    dateReceived: OffsetDateTime = OffsetDateTime.now(),
    communityServiceProviderId: UUID = UUID.randomUUID(),
    assignedUserId: UUID? = null,
  ) = CaseListView(
    referralId = referralId,
    personName = personName,
    personIdentifier = personIdentifier,
    dateReceived = dateReceived,
    communityServiceProviderId = communityServiceProviderId,
    serviceProviderId = serviceProviderId,
    assignedUserId = assignedUserId,
  )
}
