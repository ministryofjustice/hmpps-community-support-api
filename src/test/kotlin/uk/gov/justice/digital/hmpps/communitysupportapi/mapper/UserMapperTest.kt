package uk.gov.justice.digital.hmpps.communitysupportapi.mapper

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.UserDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.User
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.UserType
import java.util.UUID

class UserMapperTest {

  @Test
  fun `toEntity should map UserDto to an Internal User entity`() {
    val id = UUID.randomUUID()

    val userDto = UserDto(
      id = id,
      hmppsAuthId = "hmppsAuthId",
      hmppsAuthUsername = "hmppsAuthUsername",
      firstName = "John",
      lastName = "Smith",
      emailAddress = "johnsmith@email.com",
    )

    val userEntity = userDto.toEntity()

    userEntity.id shouldBe id
    userEntity.hmppsAuthId shouldBe "hmppsAuthId"
    userEntity.hmppsAuthUsername shouldBe "hmppsAuthUsername"
    userEntity.firstName shouldBe "John"
    userEntity.lastName shouldBe "Smith"
    userEntity.emailAddress shouldBe "johnsmith@email.com"
    userEntity.userType shouldBe UserType.INTERNAL
    userEntity.lastSynchronisedAt shouldNotBe null
  }

  @Test
  fun `toEntity should map UserDto to an External User entity`() {
    val id = UUID.randomUUID()

    val userDto = UserDto(
      id = id,
      firstName = "John",
      lastName = "Smith",
      emailAddress = "johnsmith@email.com",
    )

    val userEntity = userDto.toEntity()

    userEntity.id shouldBe id
    userEntity.hmppsAuthId shouldBe null
    userEntity.hmppsAuthUsername shouldBe null
    userEntity.firstName shouldBe "John"
    userEntity.lastName shouldBe "Smith"
    userEntity.emailAddress shouldBe "johnsmith@email.com"
    userEntity.userType shouldBe UserType.EXTERNAL
    userEntity.lastSynchronisedAt shouldNotBe null
  }

  @Test
  fun `toDto should map User to UserDto (Internal User)`() {
    val id = UUID.randomUUID()

    val user = User(
      id = id,
      hmppsAuthId = "hmppsAuthId",
      hmppsAuthUsername = "hmppsAuthUsername",
      firstName = "John",
      lastName = "Smith",
      emailAddress = "johnsmith@email.com",
    )

    val userDto = user.toDto()

    userDto.id shouldBe id
    userDto.hmppsAuthId shouldBe "hmppsAuthId"
    userDto.hmppsAuthUsername shouldBe "hmppsAuthUsername"
    userDto.firstName shouldBe "John"
    userDto.lastName shouldBe "Smith"
    userDto.emailAddress shouldBe "johnsmith@email.com"
    userDto.userType shouldBe UserType.INTERNAL
  }

  @Test
  fun `toDto should map User to UserDto (External User)`() {
    val id = UUID.randomUUID()

    val user = User(
      id = id,
      firstName = "John",
      lastName = "Smith",
      emailAddress = "johnsmith@email.com",
    )

    val userDto = user.toDto()

    userDto.id shouldBe id
    userDto.hmppsAuthId shouldBe null
    userDto.hmppsAuthUsername shouldBe null
    userDto.firstName shouldBe "John"
    userDto.lastName shouldBe "Smith"
    userDto.emailAddress shouldBe "johnsmith@email.com"
    userDto.userType shouldBe UserType.EXTERNAL
  }
}
