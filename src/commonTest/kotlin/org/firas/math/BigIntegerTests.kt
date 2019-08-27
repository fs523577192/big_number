package org.firas.math

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 *
 * @author Wu Yuping
 */
class BigIntegerTests {

    @Test
    fun testToString() {
        assertEquals(Long.MAX_VALUE.toString() + "00",
                longToBigInteger(Long.MAX_VALUE).multiply(longToBigInteger(100L)).toString())
        val random = Random.Default
        for (i in 1..10000) {
            val l = random.nextLong()
            val bi = longToBigInteger(l)
            val str = l.toString()
            assertEquals(str, bi.toString())

            assertEquals(bi, stringToBigInteger(str))
        }
    }

    @Test
    fun testPlus() {
        // assertEquals(BigInteger.TWO, bigIntegerOne + bigIntegerOne)

        val random = Random.Default
        for (i in 1..10000) {
            val a = random.nextInt().toLong()
            val b = random.nextInt().toLong()
            val aa = longToBigInteger(a)
            val bb = longToBigInteger(b)
            assertEquals(a + b, aa.add(bb).toLong())
            assertEquals(a + b, bb.add(aa).toLong())

            assertEquals(aa, aa + bigIntegerZero)
            assertEquals(aa, bigIntegerZero + aa)
        }
    }

    @Test
    fun testMinus() {
        assertEquals(bigIntegerZero, bigIntegerZero - bigIntegerZero)
        assertEquals(bigIntegerOne, bigIntegerOne - bigIntegerZero)
        assertEquals(bigIntegerZero, bigIntegerOne - bigIntegerOne)
        // assertEquals(bigIntegerOne, BigInteger.TWO - bigIntegerOne)

        val random = Random.Default
        for (i in 1..10000) {
            val a = random.nextInt().toLong()
            val b = random.nextInt().toLong()
            val aa = longToBigInteger(a)
            val bb = longToBigInteger(b)
            assertEquals(a - b, aa.subtract(bb).toLong())
            assertEquals(aa - bb, bb.subtract(aa).negate())

            assertEquals(aa, aa - bigIntegerZero)

            assertEquals(bigIntegerZero, aa - aa)
        }
    }

    @Test
    fun testTimes() {
        val random = Random.Default
        for (i in 1..10000) {
            val a = random.nextInt().toLong()
            val b = random.nextInt().toLong()
            val aa = longToBigInteger(a)
            val bb = longToBigInteger(b)
            assertEquals(a * b, aa.times(bb).toLong())
            assertEquals(a * b, bb.times(aa).toLong())

            assertEquals(aa, aa * bigIntegerOne)
            assertEquals(aa, bigIntegerOne * aa)

            assertEquals(bigIntegerZero, aa * bigIntegerZero)
            assertEquals(bigIntegerZero, bigIntegerZero * aa)
        }
    }

    @Test
    fun testDiv() {

    }

    @Test
    fun testToFloat() {
        assertEquals(3f * 0x4000_0000_0000_0000L * 4f, longToBigInteger(3).shiftLeft(64).toFloat())
        assertEquals(5f * 0x4000_0000_0000_0000L * 4f, longToBigInteger(5).shiftLeft(64).toFloat())
        assertEquals(6f * 0x4000_0000_0000_0000L * 4f, longToBigInteger(6).shiftLeft(64).toFloat())
        assertEquals(7f * 0x4000_0000_0000_0000L * 4f, longToBigInteger(7).shiftLeft(64).toFloat())
        assertEquals(9f * 0x4000_0000_0000_0000L * 4f, longToBigInteger(9).shiftLeft(64).toFloat())
        assertEquals(10f * 0x4000_0000_0000_0000L * 4f, longToBigInteger(10).shiftLeft(64).toFloat())

        val random = Random.Default
        for (i in 1..10000) {
            val l = random.nextLong(1L shl FloatConsts.SIGNIFICAND_WIDTH)
            assertEquals(l.toFloat(), longToBigInteger(l).toFloat())
        }
    }

    @Test
    fun testToDouble() {
        assertEquals(3.0 * 0x4000_0000_0000_0000L * 4.0, longToBigInteger(3).shiftLeft(64).toDouble())
        assertEquals(5.0 * 0x4000_0000_0000_0000L * 4.0, longToBigInteger(5).shiftLeft(64).toDouble())
        assertEquals(6.0 * 0x4000_0000_0000_0000L * 4.0, longToBigInteger(6).shiftLeft(64).toDouble())
        assertEquals(7.0 * 0x4000_0000_0000_0000L * 4.0, longToBigInteger(7).shiftLeft(64).toDouble())
        assertEquals(9.0 * 0x4000_0000_0000_0000L * 4.0, longToBigInteger(9).shiftLeft(64).toDouble())
        assertEquals(10.0 * 0x4000_0000_0000_0000L * 4.0, longToBigInteger(10).shiftLeft(64).toDouble())

        val random = Random.Default
        for (i in 1..10000) {
            val l = random.nextLong(1L shl DoubleConsts.SIGNIFICAND_WIDTH)
            assertEquals(l.toDouble(), longToBigInteger(l).toDouble())
        }
    }
}