@file:JvmName("BigDecimalJvmKt")

package org.firas.math

actual typealias BigDecimal = java.math.BigDecimal

actual val bigDecimalZero: BigDecimal = BigDecimal.ZERO!!
actual val bigDecimalOne: BigDecimal = BigDecimal.ONE!!

actual fun longToBigDecimal(value: Long): BigDecimal = BigDecimal.valueOf(value)
actual fun doubleToBigDecimal(value: Double): BigDecimal = BigDecimal.valueOf(value)
actual fun stringToBigDecimal(value: String): BigDecimal {
    return if (Regex("^[+-]?(0+(\\.0+)?|\\.0+)$").matches(value)) bigDecimalZero
            else if (Regex("^[+-]?0*1(\\.0+)?$").matches(value)) bigDecimalOne
            else BigDecimal(value)
}
actual fun bigIntegerToBigDecimal(value: BigInteger): BigDecimal {
    return if (bigIntegerZero == value) bigDecimalZero
            else if (bigIntegerOne == value) bigDecimalOne
            else BigDecimal(value)
}