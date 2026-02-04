package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CaseListView
import java.util.UUID

interface CaseListViewRepository :
  JpaRepository<CaseListView, UUID>,
  JpaSpecificationExecutor<CaseListView>
