package org.firas.math

/**
 *
 * @author Wu Yuping
 */
internal actual fun expandBigIntegerTenPowers(n: Int): BigInteger {
    return BigDecimal._expandBigIntegerTenPowers(n)
}
