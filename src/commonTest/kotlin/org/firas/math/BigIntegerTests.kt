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
        assertEquals(3f * 0x4000_0000_0000_0000L * 4f, BigInteger.valueOf(3).shl(64).toFloat())
        assertEquals(5f * 0x4000_0000_0000_0000L * 4f, BigInteger.valueOf(5).shl(64).toFloat())
        assertEquals(6f * 0x4000_0000_0000_0000L * 4f, BigInteger.valueOf(6).shl(64).toFloat())
        assertEquals(7f * 0x4000_0000_0000_0000L * 4f, BigInteger.valueOf(7).shl(64).toFloat())
        assertEquals(9f * 0x4000_0000_0000_0000L * 4f, BigInteger.valueOf(9).shl(64).toFloat())
        assertEquals(10f * 0x4000_0000_0000_0000L * 4f, BigInteger.valueOf(10).shl(64).toFloat())

        val random = Random.Default
        for (i in 1..10000) {
            val l = random.nextLong(1L shl FloatConsts.SIGNIFICAND_WIDTH)
            assertEquals(l.toFloat(), BigInteger.valueOf(l).toFloat())
        }
    }

    @Test
    fun testToDouble() {
        assertEquals(3.0 * 0x4000_0000_0000_0000L * 4.0, BigInteger.valueOf(3).shl(64).toDouble())
        assertEquals(5.0 * 0x4000_0000_0000_0000L * 4.0, BigInteger.valueOf(5).shl(64).toDouble())
        assertEquals(6.0 * 0x4000_0000_0000_0000L * 4.0, BigInteger.valueOf(6).shl(64).toDouble())
        assertEquals(7.0 * 0x4000_0000_0000_0000L * 4.0, BigInteger.valueOf(7).shl(64).toDouble())
        assertEquals(9.0 * 0x4000_0000_0000_0000L * 4.0, BigInteger.valueOf(9).shl(64).toDouble())
        assertEquals(10.0 * 0x4000_0000_0000_0000L * 4.0, BigInteger.valueOf(10).shl(64).toDouble())

        val random = Random.Default
        for (i in 1..10000) {
            val l = random.nextLong(1L shl DoubleConsts.SIGNIFICAND_WIDTH)
            assertEquals(l.toDouble(), BigInteger.valueOf(l).toDouble())
        }
    }
}