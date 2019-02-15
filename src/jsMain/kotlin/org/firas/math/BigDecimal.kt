package org.firas.math

/**
 *
 * @author Wu Yuping
 */
internal actual fun expandBigIntegerTenPowers(n: Int): BigInteger {
    return BigDecimal._expandBigIntegerTenPowers(n)
}

private val stringBuilderHelperSingleton = BigDecimal.Companion.StringBuilderHelper()
internal actual fun getStringBuilderHelper(): BigDecimal.Companion.StringBuilderHelper {
    return stringBuilderHelperSingleton
}