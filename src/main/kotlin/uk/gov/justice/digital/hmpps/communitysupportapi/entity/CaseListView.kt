package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Immutable
@Table(name = "case_list_view")
class CaseListView(
  @Id
  @Column(name = "referral_id")
  val referralId: UUID,

  @Column(name = "person_name")
  val personName: String,

  @Column(name = "person_identifier")
  val personIdentifier: String,

  @Column(name = "date_received")
  val dateReceived: OffsetDateTime,

  @Column(name = "date_assigned")
  val dateAssigned: OffsetDateTime?,

  @Column(name = "community_service_provider_id")
  val communityServiceProviderId: UUID,

  @Column(name = "service_provider_id")
  val serviceProviderId: UUID,

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "case_workers", columnDefinition = "text[]")
  val caseWorkers: List<String> = emptyList(),
)
