package uk.gov.justice.digital.hmpps.communitysupportapi.util

import kotlin.random.Random

object ReferralReferenceTestUtil {
  fun randomReferralReference(): String {
    val letters = ('A'..'Z').toList()
    val prefix = (1..2).map { letters.random(Random) }.joinToString("")
    val numbers = (1..4).map { Random.nextInt(10) }.joinToString("")
    val suffix = (1..2).map { letters.random(Random) }.joinToString("")
    return "$prefix$numbers$suffix"
  }
}
