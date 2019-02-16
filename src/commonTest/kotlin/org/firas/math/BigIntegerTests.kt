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
    fun testToString() {
        assertEquals(Long.MAX_VALUE.toString() + "00",
                BigInteger.valueOf(Long.MAX_VALUE).times(BigInteger.valueOf(100)).toString())
        val random = Random.Default
        for (i in 1..10000) {
            val l = random.nextLong()
            val bi = BigInteger.valueOf(l)
            val str = l.toString()
            assertEquals(str, bi.toString())

            assertEquals(bi, BigInteger.valueOf(str))
        }
    }

    @Test
    fun testPlus() {
        assertEquals(BigInteger.TWO, BigInteger.ONE + BigInteger.ONE)

        val random = Random.Default
        for (i in 1..10000) {
            val a = random.nextInt().toLong()
            val b = random.nextInt().toLong()
            val aa = BigInteger.valueOf(a)
            val bb = BigInteger.valueOf(b)
            assertEquals(a + b, aa.plus(bb).toLong())
            assertEquals(a + b, bb.plus(aa).toLong())

            assertEquals(aa, aa + BigInteger.ZERO)
            assertEquals(aa, BigInteger.ZERO + aa)
        }
    }

    @Test
    fun testMinus() {
        assertEquals(BigInteger.ZERO, BigInteger.ZERO - BigInteger.ZERO)
        assertEquals(BigInteger.ONE, BigInteger.ONE - BigInteger.ZERO)
        assertEquals(BigInteger.ZERO, BigInteger.ONE - BigInteger.ONE)
        assertEquals(BigInteger.ONE, BigInteger.TWO - BigInteger.ONE)

        val random = Random.Default
        for (i in 1..10000) {
            val a = random.nextInt().toLong()
            val b = random.nextInt().toLong()
            val aa = BigInteger.valueOf(a)
            val bb = BigInteger.valueOf(b)
            assertEquals(a - b, aa.minus(bb).toLong())
            assertEquals(aa - bb, bb.minus(aa).unaryMinus())

            assertEquals(aa, aa - BigInteger.ZERO)

            assertEquals(BigInteger.ZERO, aa - aa)
        }
    }

    @Test
    fun testTimes() {
        val random = Random.Default
        for (i in 1..10000) {
            val a = random.nextInt().toLong()
            val b = random.nextInt().toLong()
            val aa = BigInteger.valueOf(a)
            val bb = BigInteger.valueOf(b)
            assertEquals(a * b, aa.times(bb).toLong())
            assertEquals(a * b, bb.times(aa).toLong())

            assertEquals(aa, aa * BigInteger.ONE)
            assertEquals(aa, BigInteger.ONE * aa)

            assertEquals(BigInteger.ZERO, aa * BigInteger.ZERO)
            assertEquals(BigInteger.ZERO, BigInteger.ZERO * aa)
        }
    }

    @Test
    fun testDiv() {

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