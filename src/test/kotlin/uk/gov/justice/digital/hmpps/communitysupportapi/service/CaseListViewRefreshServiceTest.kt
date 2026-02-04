package uk.gov.justice.digital.hmpps.communitysupportapi.service

import jakarta.persistence.EntityManager
import jakarta.persistence.Query
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class CaseListViewRefreshServiceTest {

  @Mock
  lateinit var entityManager: EntityManager

  @Mock
  lateinit var query: Query

  lateinit var caseListViewRefreshService: CaseListViewRefreshService

  @BeforeEach
  fun setUp() {
    caseListViewRefreshService = CaseListViewRefreshService(entityManager)
  }

  @Test
  fun `should refresh materialized view concurrently`() {
    whenever(entityManager.createNativeQuery("REFRESH MATERIALIZED VIEW CONCURRENTLY case_list_view"))
      .thenReturn(query)
    whenever(query.executeUpdate()).thenReturn(0)

    caseListViewRefreshService.refreshMaterializedView()

    verify(entityManager).createNativeQuery("REFRESH MATERIALIZED VIEW CONCURRENTLY case_list_view")
    verify(query).executeUpdate()
  }
}
