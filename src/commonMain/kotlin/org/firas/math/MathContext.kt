package org.firas.math

/**
 *
 * @author Wu Yuping
 */
expect class MathContext {

    constructor(precision: Int)
    constructor(precision: Int, roundingMode: RoundingMode)

    fun getPrecision(): Int
    fun getRoundingMode(): RoundingMode
}