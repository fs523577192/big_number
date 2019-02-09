package org.firas.math

/**
 *
 * @author Wu Yuping
 */
internal actual fun expandBigIntegerTenPowers(n: Int): BigInteger {
    synchronized(BigDecimal::class.java) {
        return BigDecimal._expandBigIntegerTenPowers(n)
    }
}
