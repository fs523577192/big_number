package org.firas.math

/**
 *
 * @author Wu Yuping
 */
@JsName("expandBigIntegerTenPowers")
internal actual fun expandBigIntegerTenPowers(n: Int): BigInteger {
    return BigDecimal._expandBigIntegerTenPowers(n)
}

private val stringBuilderHelperSingleton = BigDecimal.Companion.StringBuilderHelper()
@JsName("getStringBuilderHelper")
internal actual fun getStringBuilderHelper(): BigDecimal.Companion.StringBuilderHelper {
    return stringBuilderHelperSingleton
}