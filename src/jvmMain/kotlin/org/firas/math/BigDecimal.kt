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

private val threadLocalStringBuilderHelper = ThreadLocal.withInitial {
    BigDecimal.Companion.StringBuilderHelper()
}
internal actual fun getStringBuilderHelper(): BigDecimal.Companion.StringBuilderHelper {
    return threadLocalStringBuilderHelper.get()
}
