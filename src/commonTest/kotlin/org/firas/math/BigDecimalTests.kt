package org.firas.math

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 * @author Wu Yuping
 */
class BigDecimalTests {

    @Test
    fun testToString() {
        val random = Random.Default
        for (i in 1..10000) {
            val a = random.nextLong(100L, Long.MAX_VALUE)
            val aa = BigDecimal.valueOf(a)

            val temp = a.toString()
            val expected = /* if (temp.endsWith("00"))
                temp.substring(0, temp.length - 2)
            else if (temp.endsWith("0"))
                temp.substring(0, temp.length - 2) + '.' + temp[temp.length - 2]
            else */
                temp.substring(0, temp.length - 2) + '.' + temp.substring(temp.length - 2)
            assertEquals(expected, aa.movePointLeft(2).toString())
            assertEquals(temp + "000", aa.movePointRight(3).toString())
        }
    }

    @Test
    fun testPlus() {
        val random = Random.Default
        for (i in 1..10000) {
            val a = random.nextInt().toLong()
            val aa = BigDecimal.valueOf(a)
            assertEquals(aa, aa + BigDecimal.ZERO)
            assertEquals(aa, BigDecimal.ZERO + aa)
        }
    }

    @Test
    fun testMinus() {
        val random = Random.Default
        for (i in 1..10000) {
            val a = random.nextInt().toLong()
            val aa = BigDecimal.valueOf(a)
            assertEquals(aa, aa - BigDecimal.ZERO)

            assertEquals(BigDecimal.ZERO, aa - aa)
        }
    }

    @Test
    fun testTimes() {
        val random = Random.Default
        for (i in 1..10000) {
            val a = random.nextInt().toLong()
            val aa = BigDecimal.valueOf(a)
            assertEquals(aa, aa * BigDecimal.ONE)
            assertEquals(aa, BigDecimal.ONE * aa)

            assertEquals(BigDecimal.ZERO, aa * BigDecimal.ZERO)
            assertEquals(BigDecimal.ZERO, BigDecimal.ZERO * aa)
        }
    }

    @Test
    fun testToFloat() {
        val random = Random.Default
        for (i in 1..10000) {
            val l = random.nextLong(1L shl FloatConsts.SIGNIFICAND_WIDTH)
            assertEquals(l.toFloat(), BigInteger.valueOf(l).toFloat())
        }
    }

    @Test
    fun testToDouble() {
        val random = Random.Default
        for (i in 1..10000) {
            val l = random.nextLong(1L shl DoubleConsts.SIGNIFICAND_WIDTH)
            assertEquals(l.toDouble(), BigInteger.valueOf(l).toDouble())
        }
    }
}
