@file:JvmName("BigIntegerKt")

package org.firas.math

import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads

/**
 *
 * @author Wu Yuping
 */
expect class BigInteger: Number, Comparable<BigInteger> {

    fun longValueExact(): Long
    fun intValueExact(): Int
    fun shortValueExact(): Short
    fun byteValueExact(): Byte

    fun signum(): Int
    fun bitLength(): Int

    fun min(other: BigInteger): BigInteger
    fun max(other: BigInteger): BigInteger

    fun testBit(n: Int): Boolean
    fun setBit(n: Int): BigInteger
    fun clearBit(n: Int): BigInteger
    fun flipBit(n: Int): BigInteger

    fun shiftLeft(n: Int): BigInteger
    fun shiftRight(n: Int): BigInteger
    fun and(other: BigInteger): BigInteger
    fun or(other: BigInteger): BigInteger
    fun xor(other: BigInteger): BigInteger
    fun not(): BigInteger

    fun negate(): BigInteger
    fun abs(): BigInteger

    fun add(augend: BigInteger): BigInteger
    fun subtract(subtrahend: BigInteger): BigInteger

    fun multiply(multiplicand: BigInteger): BigInteger
    fun divide(divisor: BigInteger): BigInteger
    fun mod(divisor: BigInteger): BigInteger
    fun remainder(divisor: BigInteger): BigInteger
}
inline operator fun BigInteger.unaryMinus(): BigInteger {
    return this.negate()
}
inline operator fun BigInteger.plus(augend: BigInteger): BigInteger {
    return this.add(augend)
}
inline operator fun BigInteger.minus(augend: BigInteger): BigInteger {
    return this.subtract(augend)
}
inline operator fun BigInteger.times(augend: BigInteger): BigInteger {
    return this.multiply(augend)
}
inline operator fun BigInteger.div(augend: BigInteger): BigInteger {
    return this.divide(augend)
}
inline operator fun BigInteger.rem(augend: BigInteger): BigInteger {
    return this.mod(augend)
}

inline infix fun BigInteger.shl(n: Int): BigInteger {
    return this.shiftLeft(n)
}
inline infix fun BigInteger.shr(n: Int): BigInteger {
    return this.shiftRight(n)
}

expect val bigIntegerZero: BigInteger
expect val bigIntegerOne: BigInteger
expect val bigIntegerTen: BigInteger

expect fun longToBigInteger(value: Long): BigInteger
@JvmOverloads
expect fun stringToBigInteger(value: String, radix: Int = 10): BigInteger