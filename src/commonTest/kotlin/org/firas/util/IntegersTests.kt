package org.firas.util

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 *
 * @author Wu Yuping
 */
class IntegersTests {

    @Test
    fun testInt() {
        val random = Random.Default
        for (i in 1..10000) {
            val a = random.nextInt()
            val b = Integers.reverse(a)
            assertEquals(a, Integers.reverse(b))
            assertEquals(Integers.bitCount(a), Integers.bitCount(b))
            assertEquals(Integers.numberOfLeadingZeros(a), Integers.numberOfTrailingZeros(b))
        }
    }

    @Test
    fun testLong() {
        val random = Random.Default
        for (i in 1..10000) {
            val a = random.nextLong()
            val b = Integers.reverse(a)
            assertEquals(a, Integers.reverse(b))
            assertEquals(Integers.bitCount(a), Integers.bitCount(b))
            assertEquals(Integers.numberOfLeadingZeros(a), Integers.numberOfTrailingZeros(b))
        }
    }
}