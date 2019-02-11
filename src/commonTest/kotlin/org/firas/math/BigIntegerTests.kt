package org.firas.math

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
}