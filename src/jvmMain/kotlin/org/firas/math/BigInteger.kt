@file:JvmName("BigIntegerJvmKt")

package org.firas.math

/**
 *
 * @author Wu Yuping
 */
actual typealias BigInteger = java.math.BigInteger

actual val bigIntegerZero: BigInteger = BigInteger.ZERO!!
actual val bigIntegerOne: BigInteger = BigInteger.ONE!!
actual val bigIntegerTen: BigInteger = BigInteger.TEN!!

actual fun longToBigInteger(value: Long): BigInteger = BigInteger.valueOf(value)
@JvmOverloads
actual fun stringToBigInteger(value: String, radix: Int): BigInteger {
    return if (Regex("^[+-]?0+$").matches(value)) bigIntegerZero
    else if (Regex("^[+-]?0*1$").matches(value)) bigIntegerOne
    else BigInteger(value, radix)
}