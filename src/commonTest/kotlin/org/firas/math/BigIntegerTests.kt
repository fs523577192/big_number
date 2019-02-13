package org.firas.math

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 *
 * @author Wu Yuping
 * @version 1.0.0
 * @since 1.0.0
 */
class BigIntegerTests {

    @Test
    fun testPlus() {
        assertEquals(BigInteger.ZERO, BigInteger.ZERO + BigInteger.ZERO)
        assertEquals(BigInteger.ONE, BigInteger.ZERO + BigInteger.ONE)
        assertEquals(BigInteger.ONE, BigInteger.ONE + BigInteger.ZERO)
        assertEquals(BigInteger.TWO, BigInteger.ONE + BigInteger.ONE)
    }

    @Test
    fun testMinus() {
        assertEquals(BigInteger.ZERO, BigInteger.ZERO - BigInteger.ZERO)
        assertEquals(BigInteger.ONE, BigInteger.ONE - BigInteger.ZERO)
        assertEquals(BigInteger.ZERO, BigInteger.ONE - BigInteger.ONE)
        assertEquals(BigInteger.ONE, BigInteger.TWO - BigInteger.ONE)
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