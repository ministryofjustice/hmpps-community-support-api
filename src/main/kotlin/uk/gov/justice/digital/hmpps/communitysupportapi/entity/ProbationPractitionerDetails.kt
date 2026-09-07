package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "probation_practitioner_details")
class ProbationPractitionerDetails(
  @Id
  val id: UUID,

  @Column(name = "referral_id", nullable = false)
  val referralId: UUID,

  @Column(name = "name", nullable = false)
  var name: String,

  @Column(name = "job_role")
  var jobRole: String? = null,

  @Column(name = "email_address")
  var emailAddress: String? = null,

  @Column(name = "pdu")
  var pdu: String? = null,

  @Column(name = "probation_office")
  var probationOffice: String? = null,

  @Column(name = "team_phone_number")
  var teamPhoneNumber: String? = null,

  @Column(name = "phone_number")
  var phoneNumber: String? = null,

  @Column(name = "pp_details_found_and_correct")
  var ppDetailsFoundAndCorrect: Boolean? = null,

  @Column(name = "updated_at", nullable = false)
  var updatedAt: OffsetDateTime,

  @Column(name = "updated_by", nullable = false)
  var updatedBy: UUID,
)
