package uk.gov.justice.digital.hmpps.communitysupportapi.repository.specification

import org.springframework.data.jpa.domain.Specification
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CaseListView
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataServiceProvider
import java.time.OffsetDateTime
import java.util.UUID

object CaseListViewSpecifications {

  fun hasServiceProviderIn(referenceDataServiceProviders: Set<ReferenceDataServiceProvider>): Specification<CaseListView> = Specification { root, _, criteriaBuilder ->
    if (referenceDataServiceProviders.isEmpty()) {
      criteriaBuilder.disjunction() // Returns false, no results
    } else {
      val serviceProviderIds = referenceDataServiceProviders.map { it.id }
      root.get<UUID>("serviceProviderId").`in`(serviceProviderIds)
    }
  }

  fun createdBy(referralUserId: UUID): Specification<CaseListView> = Specification { root, _, criteriaBuilder ->
    criteriaBuilder.equal(root.get<UUID>("createdBy"), referralUserId)
  }

  fun isUnassigned(): Specification<CaseListView> = Specification { root, _, criteriaBuilder ->
    criteriaBuilder.isNull(root.get<OffsetDateTime>("dateAssigned"))
  }

  fun isAssigned(): Specification<CaseListView> = Specification { root, _, criteriaBuilder ->
    criteriaBuilder.isNotNull(root.get<OffsetDateTime>("dateAssigned"))
  }
}
