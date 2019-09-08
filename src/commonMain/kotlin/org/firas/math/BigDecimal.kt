@file:JvmName("BigDecimalKt")

package org.firas.math

import kotlin.jvm.JvmName

expect class BigDecimal: Number, Comparable<BigDecimal> {

    fun signum(): Int
    fun scale(): Int
    fun precision(): Int

    fun min(other: BigDecimal): BigDecimal
    fun max(other: BigDecimal): BigDecimal

    fun negate(): BigDecimal
    fun abs(): BigDecimal

    fun add(augend: BigDecimal): BigDecimal
    fun subtract(subtrahend: BigDecimal): BigDecimal

    fun setScale(newScale: Int): BigDecimal
    fun setScale(newScale: Int, roundingMode: RoundingMode): BigDecimal
    fun movePointLeft(n: Int): BigDecimal
    fun movePointRight(n: Int): BigDecimal

    fun multiply(multiplicand: BigDecimal): BigDecimal
    fun divide(divisor: BigDecimal): BigDecimal
    fun remainder(divisor: BigDecimal): BigDecimal

    fun toBigInteger(): BigInteger
    fun toBigIntegerExact(): BigInteger
}

inline operator fun BigDecimal.unaryMinus(): BigDecimal {
    return this.negate()
}
inline operator fun BigDecimal.plus(augend: BigDecimal): BigDecimal {
    return this.add(augend)
}
inline operator fun BigDecimal.minus(augend: BigDecimal): BigDecimal {
    return this.subtract(augend)
}
inline operator fun BigDecimal.times(augend: BigDecimal): BigDecimal {
    return this.multiply(augend)
}
inline operator fun BigDecimal.div(augend: BigDecimal): BigDecimal {
    return this.divide(augend)
}
inline operator fun BigDecimal.rem(augend: BigDecimal): BigDecimal {
    return this.remainder(augend)
}

expect val bigDecimalZero: BigDecimal
expect val bigDecimalOne: BigDecimal

expect fun longToBigDecimal(value: Long): BigDecimal
expect fun doubleToBigDecimal(value: Double): BigDecimal
expect fun stringToBigDecimal(value: String): BigDecimal
expect fun bigIntegerToBigDecimal(value: BigInteger): BigDecimal