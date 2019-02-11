/*
 * Migrated from the source code of OpenJDK/jdk11
 *
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.firas.math

import org.firas.math.BigDecimal.Companion.INFLATED
import org.firas.math.BigInteger.Companion.LONG_MASK
import org.firas.util.Arrays
import org.firas.util.Integers
import kotlin.math.absoluteValue

/**
 * A class used to represent multiprecision integers that makes efficient
 * use of allocated space by allowing a number to occupy only part of
 * an array so that the arrays do not have to be reallocated as often.
 * When performing an operation with many iterations the array used to
 * hold a number is only reallocated when necessary and does not have to
 * be the same size as the number it represents. A mutable number allows
 * calculations to occur on the same number without having to create
 * a new number for every step of the calculation as occurs with
 * BigIntegers.
 *
 * @see     BigInteger
 * @author  Michael McCloskey
 * @author  Timothy Buktu
 * @author  Wu Yuping
 * @since   Java 1.3
 */
internal open class MutableBigInteger private constructor(
    /**
     * Holds the magnitude of this MutableBigInteger in big endian order.
     * The magnitude may start at an offset into the value array, and it may
     * end before the length of the value array.
     */
    internal var value: IntArray,

    /**
     * The number of ints of the value array that are currently used
     * to hold the magnitude of this MutableBigInteger. The magnitude starts
     * at an offset and offset + intLen may be less than value.length.
     */
    internal var intLen: Int
) {

    /**
     * The default constructor. An empty MutableBigInteger is created with
     * a one word capacity.
     */
    internal constructor(): this(IntArray(1), 0)

    /**
     * Construct a new MutableBigInteger with a magnitude specified by
     * the int val.
     */
    internal constructor(value: Int): this(IntArray(1), 1) {
        this.value[0] = value
    }

    /**
     * Construct a new MutableBigInteger with the specified value array
     * up to the length of the array supplied.
     */
    internal constructor(value: IntArray): this(value, value.size)

    /**
     * Construct a new MutableBigInteger with a magnitude equal to the
     * specified MutableBigInteger.
     */
    internal constructor(value: MutableBigInteger):
            this(value.value.copyOfRange(value.offset, value.offset + value.intLen), value.intLen)

    /**
     * Construct a new MutableBigInteger with a magnitude equal to the
     * specified BigInteger.
     */
    internal constructor(value: BigInteger): this(value.mag.copyOf(), value.mag.size)

    companion object {
        // Constants
        /**
         * MutableBigInteger with one element value array with the value 1. Used by
         * BigDecimal divideAndRound to increment the quotient. Use this constant
         * only when the method is not going to modify this object.
         */
        internal val ONE = MutableBigInteger(1)

        /**
         * The minimum `intLen` for cancelling powers of two before
         * dividing.
         * If the number of ints is less than this threshold,
         * `divideKnuth` does not eliminate common powers of two from
         * the dividend and divisor.
         */
        internal const val KNUTH_POW2_THRESH_LEN = 6

        /**
         * The minimum number of trailing zero ints for cancelling powers of two
         * before dividing.
         * If the dividend and divisor don't share at least this many zero ints
         * at the end, `divideKnuth` does not eliminate common powers
         * of two from the dividend and divisor.
         */
        internal const val KNUTH_POW2_THRESH_ZEROS = 3

        private fun copyAndShift(src: IntArray, srcFrom: Int, srcLen: Int,
                                 dst: IntArray, dstFrom: Int, shift: Int) {
            var srcFrom = srcFrom
            val n2 = 32 - shift
            var c = src[srcFrom]
            for (i in 0 until srcLen - 1) {
                val b = c
                srcFrom += 1
                c = src[srcFrom]
                dst[dstFrom + i] = b shl shift or c.ushr(n2)
            }
            dst[dstFrom + srcLen - 1] = c shl shift
        }

        /**
         * A primitive used for division by long.
         * Specialized version of the method divadd.
         * dh is a high part of the divisor, dl is a low part
         */
        private fun divaddLong(dh: Int, dl: Int, result: IntArray, offset: Int): Int {
            var carry: Long = 0

            var sum = (dl.toLong() and LONG_MASK) + (result[1 + offset].toLong() and LONG_MASK)
            result[1 + offset] = sum.toInt()

            sum = (dh.toLong() and LONG_MASK) + (result[offset].toLong() and LONG_MASK) + carry
            result[offset] = sum.toInt()
            carry = sum.ushr(32)
            return carry.toInt()
        }

        /**
         * This method is used for division by long.
         * Specialized version of the method sulsub.
         * dh is a high part of the divisor, dl is a low part
         */
        private fun mulsubLong(q: IntArray, dh: Int, dl: Int, x: Int, offset: Int): Int {
            var offset = offset
            val xLong = x.toLong() and LONG_MASK
            offset += 2
            var product = (dl.toLong() and LONG_MASK) * xLong
            var difference = q[offset] - product
            q[offset] = difference.toInt()
            offset -= 1
            var carry = product.ushr(32) + if (difference and LONG_MASK > product.inv() and LONG_MASK)
                1
            else
                0
            product = (dh.toLong() and LONG_MASK) * xLong + carry
            difference = q[offset] - product
            q[offset] = difference.toInt()
            carry = product.ushr(32) + if (difference and LONG_MASK > product.inv() and LONG_MASK)
                1
            else
                0
            return carry.toInt()
        }

        /**
         * Compare two longs as if they were unsigned.
         * Returns true iff one is bigger than two.
         */
        private fun unsignedLongCompare(one: Long, two: Long): Boolean {
            return one + Long.MIN_VALUE > two + Long.MIN_VALUE
        }

        /**
         * Returns the multiplicative inverse of val mod 2^32.  Assumes val is odd.
         */
        internal fun inverseMod32(value: Int): Int {
            // Newton's iteration!
            var t = value
            t *= 2 - value * t
            t *= 2 - value * t
            t *= 2 - value * t
            t *= 2 - value * t
            return t
        }

        /**
         * Calculate the multiplicative inverse of 2^k mod mod, where mod is odd.
         */
        internal fun modInverseBP2(mod: MutableBigInteger, k: Int): MutableBigInteger {
            // Copy the mod to protect original
            return fixup(MutableBigInteger(1), MutableBigInteger(mod), k)
        }

        /**
         * Calculate GCD of a and b interpreted as unsigned integers.
         */
        internal fun binaryGcd(a: Int, b: Int): Int {
            var a = a
            var b = b
            if (b == 0) {
                return a
            }
            if (a == 0) {
                return b
            }
            // Right shift a & b till their last bits equal to 1.
            val aZeros = Integers.numberOfTrailingZeros(a)
            val bZeros = Integers.numberOfTrailingZeros(b)
            a = a ushr aZeros
            b = b ushr bZeros

            val t = if (aZeros < bZeros) aZeros else bZeros

            while (a != b) {
                if (a + -0x80000000 > b + -0x80000000) {  // a > b as unsigned
                    a -= b
                    a = a ushr Integers.numberOfTrailingZeros(a)
                } else {
                    b -= a
                    b = b ushr Integers.numberOfTrailingZeros(b)
                }
            }
            return a shl t
        }

        /**
         * The Fixup Algorithm
         * Calculates X such that X = C * 2^(-k) (mod P)
         * Assumes C<P and P is odd.
         */
        internal fun fixup(c: MutableBigInteger, p: MutableBigInteger,
                  k: Int): MutableBigInteger {
            val temp = MutableBigInteger()
            // Set r to the multiplicative inverse of p mod 2^32
            val r = -inverseMod32(p.value[p.offset+p.intLen-1])

            val numWords = k shr 5
            for (i in 0 until numWords) {
                // V = R * c (mod 2^j)
                val v = r * c.value[c.offset + c.intLen-1]
                // c = c + (v * p)
                p.mul(v, temp)
                c.add(temp)
                // c = c / 2^j
                c.intLen -= 1
            }
            val numBits = k and 0x1f
            if (numBits != 0) {
                // V = R * c (mod 2^j)
                var v = r * c.value[c.offset + c.intLen-1]
                v = v and ((1 shl numBits) - 1)
                // c = c + (v * p)
                p.mul(v, temp)
                c.add(temp)
                // c = c / 2^j
                c.rightShift(numBits)
            }

            // In theory, c may be greater than p at this point (Very rare!)
            while (c.compare(p) >= 0) {
                c.subtract(p)
            }
            return c
        }
    }

    /**
     * The offset into the value array where the magnitude of this
     * MutableBigInteger begins.
     */
    internal var offset = 0

    /**
     * Convert this MutableBigInteger to a BigInteger object.
     */
    internal fun toBigInteger(sign: Int): BigInteger {
        return if (this.intLen == 0 || sign == 0) BigInteger.ZERO else BigInteger(getMagnitudeArray(), sign)
    }

    /**
     * Converts this number to a nonnegative `BigInteger`.
     */
    internal fun toBigInteger(): BigInteger {
        normalize()
        return toBigInteger(if (isZero()) 0 else 1)
    }

    /**
     * Convert this MutableBigInteger to BigDecimal object with the specified sign
     * and scale.
     */
    fun toBigDecimal(sign: Int, scale: Int): BigDecimal {
        if (this.intLen == 0 || sign == 0) {
            return BigDecimal.zeroValueOf(scale)
        }
        val mag = getMagnitudeArray()
        val len = mag.size
        val d = mag[0]
        // If this MutableBigInteger can't be fit into long, we need to
        // make a BigInteger object for the resultant BigDecimal object.
        if (len > 2 || d < 0 && len == 2)
            return BigDecimal(BigInteger(mag, sign), INFLATED, scale, 0)
        val v = if (len == 2)
            mag[1].toLong().and(LONG_MASK) or (d.toLong().and(LONG_MASK) shl 32)
        else
            d.toLong() and LONG_MASK
        return BigDecimal.valueOf(if (sign == -1) -v else v, scale)
    }

    /**
     * Makes this number an `n`-int number all of whose bits are ones.
     * Used by Burnikel-Ziegler division.
     * @param n number of ints in the `value` array
     * @return a number equal to `((1<<(32*n)))-1`
     */
    private fun ones(n: Int) {
        if (n > this.value.size) {
            this.value = IntArray(n)
        }
        Arrays.fill(this.value, -1)
        this.offset = 0
        this.intLen = n
    }

    /**
     * Internal helper method to return the magnitude array. The caller is not
     * supposed to modify the returned array.
     */
    private fun getMagnitudeArray(): IntArray {
        return if (this.offset > 0 || this.value.size != this.intLen)
                this.value.copyOfRange(this.offset, this.offset + this.intLen) else this.value
    }

    /**
     * Convert this MutableBigInteger to a long value. The caller has to make
     * sure this MutableBigInteger can be fit into long.
     */
    private fun toLong(): Long {
        if (this.intLen > 2) {
            throw AssertionError("this MutableBigInteger exceeds the range of long")
        }
        if (this.intLen == 0)
            return 0
        val d = this.value[this.offset].toLong() and LONG_MASK
        return if (this.intLen == 2) d shl 32 or (this.value[this.offset + 1].toLong() and LONG_MASK) else d
    }

    /**
     * This is for internal use in converting from a MutableBigInteger
     * object into a long value given a specified sign.
     * returns INFLATED if value is not fit into long
     */
    internal fun toCompactValue(sign: Int): Long {
        if (this.intLen == 0 || sign == 0) {
            return 0L
        }
        val mag = getMagnitudeArray()
        val len = mag.size
        val d = mag[0]
        // If this MutableBigInteger can not be fitted into long, we need to
        // make a BigInteger object for the resultant BigDecimal object.
        if (len > 2 || d < 0 && len == 2) {
            return INFLATED
        }
        val v = if (len == 2)
            mag[1].toLong() and LONG_MASK or (d.toLong() and LONG_MASK shl 32)
        else
            d.toLong() and LONG_MASK
        return if (sign == -1) -v else v
    }

    /**
     * Clear out a MutableBigInteger for reuse.
     */
    private fun clear() {
        this.intLen = 0
        this.offset = 0
        Arrays.fill(this.value, 0)
    }

    /**
     * Set a MutableBigInteger to zero, removing its offset.
     */
    private fun reset() {
        this.intLen = 0
        this.offset = this.intLen
    }

    /**
     * Compare the magnitude of two MutableBigIntegers. Returns -1, 0 or 1
     * as this MutableBigInteger is numerically less than, equal to, or
     * greater than <tt>b</tt>.
     */
    internal fun compare(b: MutableBigInteger): Int {
        val blen = b.intLen
        if (intLen < blen) {
            return -1
        }
        if (intLen > blen) {
            return 1
        }

        // Add Integer.MIN_VALUE to make the comparison act as unsigned integer
        // comparison.
        val bval = b.value
        var i = offset
        var j = b.offset
        while (i < intLen + offset) {
            val b1 = value[i] + -0x80000000
            val b2 = bval[j] + -0x80000000
            if (b1 < b2) {
                return -1
            }
            if (b1 > b2) {
                return 1
            }
            i += 1
            j += 1
        }
        return 0
    }

    /**
     * Returns a value equal to what `b.leftShift(32*ints); return compare(b);`
     * would return, but doesn't change the value of `b`.
     */
    private fun compareShifted(b: MutableBigInteger, ints: Int): Int {
        val blen = b.intLen
        val alen = intLen - ints
        if (alen < blen) {
            return -1
        }
        if (alen > blen) {
            return 1
        }

        // Add Integer.MIN_VALUE to make the comparison act as unsigned integer
        // comparison.
        val bval = b.value
        var i = offset
        var j = b.offset
        while (i < alen + offset) {
            val b1 = value[i] + -0x80000000
            val b2 = bval[j] + -0x80000000
            if (b1 < b2) {
                return -1
            }
            if (b1 > b2) {
                return 1
            }
            i += 1
            j += 1
        }
        return 0
    }

    /**
     * Compare this against half of a MutableBigInteger object (Needed for
     * remainder tests).
     * Assumes no leading unnecessary zeros, which holds for results
     * from divide().
     */
    internal fun compareHalf(b: MutableBigInteger): Int {
        val blen = b.intLen
        val len = intLen
        if (len <= 0) {
            return if (blen <= 0) 0 else -1
        }
        if (len > blen) {
            return 1
        }
        if (len < blen - 1) {
            return -1
        }
        val bval = b.value
        var bstart = 0
        var carry = 0
        // Only 2 cases left:len == blen or len == blen - 1
        if (len != blen) { // len == blen - 1
            if (bval[bstart] == 1) {
                bstart += 1
                carry = -0x80000000
            } else {
                return -1
            }
        }
        // compare values with right-shifted values of b,
        // carrying shifted-out bits across words
        val value = this.value
        var i = this.offset
        var j = bstart
        while (i < len + this.offset) {
            val bv = bval[j++]
            val hb = (bv.ushr(1) + carry).toLong() and LONG_MASK
            val v = value[i++].toLong() and LONG_MASK
            if (v != hb) {
                return if (v < hb) -1 else 1
            }
            carry = bv and 1 shl 31 // carray will be either 0x80000000 or 0
        }
        return if (carry == 0) 0 else -1
    }

    /**
     * Return the index of the lowest set bit in this MutableBigInteger. If the
     * magnitude of this MutableBigInteger is zero, -1 is returned.
     */
    private fun getLowestSetBit(): Int {
        if (this.intLen == 0) {
            return -1
        }
        var j: Int = this.intLen - 1
        while (j > 0 && this.value[j + this.offset] == 0) {
            j -= 1
        }
        val b = this.value[j + this.offset]
        return if (b == 0) -1 else (this.intLen - 1 - j shl 5) + Integers.numberOfTrailingZeros(b)
    }

    /**
     * Return the int in use in this MutableBigInteger at the specified
     * index. This method is not used because it is not inlined on all
     * platforms.
     */
    private fun getInt(index: Int): Int {
        return this.value[this.offset + index]
    }

    /**
     * Return a long which is equal to the unsigned this.value of the int in
     * use in this MutableBigInteger at the specified index. This method is
     * not used because it is not inlined on all platforms.
     */
    private fun getLong(index: Int): Long {
        return this.value[this.offset + index].toLong() and LONG_MASK
    }

    /**
     * Ensure that the MutableBigInteger is in normal form, specifically
     * making sure that there are no leading zeros, and that if the
     * magnitude is zero, then intLen is zero.
     */
    internal fun normalize() {
        if (this.intLen == 0) {
            this.offset = 0
            return
        }

        var index = this.offset
        if (this.value[index] != 0)
            return

        val indexBound = index + this.intLen
        do {
            index += 1
        } while (index < indexBound && this.value[index] == 0)

        val numZeros = index - this.offset
        this.intLen -= numZeros
        this.offset = if (this.intLen == 0) 0 else this.offset + numZeros
    }

    /**
     * If this MutableBigInteger cannot hold len words, increase the size
     * of the value array to len words.
     */
    private fun ensureCapacity(len: Int) {
        if (this.value.size < len) {
            this.value = IntArray(len)
            this.offset = 0
            this.intLen = len
        }
    }

    /**
     * Convert this MutableBigInteger into an int array with no leading
     * zeros, of a length that is equal to this MutableBigInteger's intLen.
     */
    internal fun toIntArray(): IntArray {
        val result = IntArray(this.intLen)
        for (i in 0 until this.intLen) {
            result[i] = this.value[this.offset + i]
        }
        return result
    }

    /**
     * Sets the int at index+offset in this MutableBigInteger to val.
     * This does not get inlined on all platforms so it is not used
     * as often as originally intended.
     */
    private fun setInt(index: Int, value: Int) {
        this.value[this.offset + index] = value
    }

    /**
     * Sets this MutableBigInteger's value array to the specified array.
     * The intLen is set to the specified length.
     */
    internal fun setValue(value: IntArray, length: Int) {
        this.value = value
        this.intLen = length
        this.offset = 0
    }

    /**
     * Sets this MutableBigInteger's value array to a copy of the specified
     * array. The intLen is set to the length of the new array.
     */
    private fun copyValue(src: MutableBigInteger) {
        copyValue(src.value, src.offset, src.intLen)
    }

    /**
     * Sets this MutableBigInteger's value array to a copy of the specified
     * array. The intLen is set to the length of the specified array.
    */
    private fun copyValue(value: IntArray) {
        copyValue(value, 0, value.size)
    }

    private fun copyValue(value: IntArray, offset: Int, len: Int) {
        if (this.value.size < len) {
            this.value = IntArray(len)
        }
        value.copyInto(this.value, 0, offset, offset + len)
        this.intLen = len
        this.offset = 0
    }

    /**
     * Returns true iff this MutableBigInteger has a value of one.
     */
    internal fun isOne(): Boolean {
        return this.intLen == 1 && this.value[this.offset] == 1
    }

    /**
     * Returns true iff this MutableBigInteger has a this.value of zero.
     */
    internal fun isZero(): Boolean {
        return this.intLen == 0
    }

    /**
     * Returns true iff this MutableBigInteger is even.
     */
    internal fun isEven(): Boolean {
        return this.intLen == 0 || this.value[this.offset + this.intLen - 1].and(1) == 0
    }

    /**
     * Returns true iff this MutableBigInteger is odd.
     */
    internal fun isOdd(): Boolean {
        return if (isZero()) false else this.value[this.offset + this.intLen - 1].and(1) == 1
    }

    /**
     * Returns true iff this MutableBigInteger is in normal form. A
     * MutableBigInteger is in normal form if it has no leading zeros
     * after the offset, and intLen + offset <= value.length.
     */
    internal fun isNormal(): Boolean {
        if (this.intLen + this.offset > this.value.size) {
            return false
        }
        return if (this.intLen == 0) true else this.value[this.offset] != 0
    }

    /**
     * Like [.rightShift] but `n` can be greater than the length of the number.
     */
    private fun safeRightShift(n: Int) {
        if (n / 32 >= this.intLen) {
            reset()
        } else {
            rightShift(n)
        }
    }

    /**
     * Right shift this MutableBigInteger n bits. The MutableBigInteger is left
     * in normal form.
     */
    internal fun rightShift(n: Int) {
        if (this.intLen == 0) {
            return
        }
        val nInts = n.ushr(5)
        val nBits = n and 0x1F
        this.intLen -= nInts
        if (nBits == 0) {
            return
        }
        val bitsInHighWord = BigInteger.bitLengthForInt(this.value[this.offset])
        if (nBits >= bitsInHighWord) {
            this.primitiveLeftShift(32 - nBits)
            this.intLen -= 1
        } else {
            primitiveRightShift(nBits)
        }
    }

    /**
     * Like [.leftShift] but `n` can be zero.
     */
    private fun safeLeftShift(n: Int) {
        if (n > 0) {
            leftShift(n)
        }
    }

    /**
     * Left shift this MutableBigInteger n bits.
     */
    internal fun leftShift(n: Int) {
        /*
         * If there is enough storage space in this MutableBigInteger already
         * the available space will be used. Space to the right of the used
         * ints in the value array is faster to utilize, so the extra space
         * will be taken from the right if possible.
         */
        if (this.intLen == 0) {
            return
        }
        val nInts = n.ushr(5)
        val nBits = n and 0x1F
        val bitsInHighWord = BigInteger.bitLengthForInt(this.value[this.offset])

        // If shift can be done without moving words, do so
        if (n <= 32 - bitsInHighWord) {
            primitiveLeftShift(nBits)
            return
        }

        var newLen = this.intLen + nInts + 1
        if (nBits <= 32 - bitsInHighWord) {
            newLen -= 1
        }
        if (this.value.size < newLen) {
            // The array must grow
            val result = IntArray(newLen)
            for (i in 0 until this.intLen)
                result[i] = this.value[this.offset + i]
            setValue(result, newLen)
        } else if (this.value.size - this.offset >= newLen) {
            // Use space on right
            for (i in 0 until newLen - this.intLen)
                this.value[this.offset + this.intLen + i] = 0
        } else {
            // Must use space on left
            for (i in 0 until this.intLen) {
                this.value[i] = this.value[offset + i]
            }
            for (i in this.intLen until newLen) {
                this.value[i] = 0
            }
            this.offset = 0
        }
        this.intLen = newLen
        if (nBits == 0) {
            return
        }
        if (nBits <= 32 - bitsInHighWord) {
            primitiveLeftShift(nBits)
        } else {
            primitiveRightShift(32 - nBits)
        }
    }

    /**
     * Subtracts the smaller of this and b from the larger and places the
     * result into this MutableBigInteger.
     */
    internal fun subtract(other: MutableBigInteger): Int {
        var a = this
        var b = other

        var result = value
        val sign = a.compare(b)

        if (sign == 0) {
            reset()
            return 0
        }
        if (sign < 0) {
            val tmp = a
            a = b
            b = tmp
        }

        val resultLen = a.intLen
        if (result.size < resultLen) {
            result = IntArray(resultLen)
        }
        var diff = 0L
        var x = a.intLen
        var y = b.intLen
        var rstart = result.size - 1

        // Subtract common parts of both numbers
        while (y > 0) {
            x -= 1
            y -= 1

            diff = (a.value[x+a.offset].toLong() and LONG_MASK) -
                   (b.value[y+b.offset].toLong() and LONG_MASK) - (-(diff shr 32))
            result[rstart] = diff.toInt()
            rstart -= 1
        }
        // Subtract remainder of longer number
        while (x > 0) {
            x -= 1
            diff = (a.value[x+a.offset].toLong() and LONG_MASK) - (-(diff shr 32))
            result[rstart] = diff.toInt()
            rstart -= 1
        }

        this.value = result
        this.intLen = resultLen
        this.offset = this.value.size - resultLen
        normalize()
        return sign
    } // internal fun subtract(other: MutableBigInteger): Int

    /**
     * Subtracts the smaller of a and b from the larger and places the result
     * into the larger. Returns 1 if the answer is in a, -1 if in b, 0 if no
     * operation was performed.
     */
    private fun difference(other: MutableBigInteger): Int {
        var a = this
        var b = other
        val sign = a.compare(b)
        if (sign == 0) {
            return 0
        }
        if (sign < 0) {
            val tmp = a
            a = b
            b = tmp
        }

        var diff = 0L
        var x = a.intLen
        var y = b.intLen

        // Subtract common parts of both numbers
        while (y > 0) {
            x -= 1
            y -= 1
            diff = (a.value[a.offset+ x].toLong() and LONG_MASK) -
                (b.value[b.offset+ y].toLong() and LONG_MASK) - (-(diff shr 32)).toInt()
            a.value[a.offset+x] = diff.toInt()
        }
        // Subtract remainder of longer number
        while (x > 0) {
            x -= 1
            diff = (a.value[a.offset+ x].toLong() and LONG_MASK) - (-(diff shr 32)).toInt()
            a.value[a.offset+x] = diff.toInt()
        }

        a.normalize()
        return sign
    }

    /**
     * Multiply the contents of two MutableBigInteger objects. The result is
     * placed into MutableBigInteger z. The contents of y are not changed.
     */
    internal fun multiply(y: MutableBigInteger, z: MutableBigInteger) {
        val xLen = intLen
        val yLen = y.intLen
        val newLen = xLen + yLen

        // Put z into an appropriate state to receive product
        if (z.value.size < newLen) {
            z.value = IntArray(newLen)
        }
        z.offset = 0
        z.intLen = newLen

        // The first iteration is hoisted out of the loop to avoid extra add
        var carry: Long = 0
        run {
            var j = yLen - 1
            var k = yLen + xLen - 1
            while (j >= 0) {
                val product = (y.value[j + y.offset].toLong() and LONG_MASK) *
                        (this.value[xLen - 1 + this.offset].toLong() and LONG_MASK) + carry
                z.value[k] = product.toInt()
                carry = product.ushr(32)
                j -= 1
                k -= 1
            }
        }
        z.value[xLen - 1] = carry.toInt()

        // Perform the multiplication word by word
        for (i in xLen - 2 downTo 0) {
            carry = 0
            var j = yLen - 1
            var k = yLen + i
            while (j >= 0) {
                val product = (y.value[j + y.offset].toLong() and LONG_MASK) *
                        (this.value[i + this.offset].toLong() and LONG_MASK) +
                        (z.value[k].toLong() and LONG_MASK) + carry
                z.value[k] = product.toInt()
                carry = product.ushr(32)
                j -= 1
                k -= 1
            }
            z.value[i] = carry.toInt()
        }

        // Remove leading zeros from product
        z.normalize()
    }

    /**
     * Multiply the contents of this MutableBigInteger by the word y. The
     * result is placed into z.
     */
    internal fun mul(y: Int, z: MutableBigInteger) {
        if (y == 1) {
            z.copyValue(this)
            return
        }

        if (y == 0) {
            z.clear()
            return
        }

        // Perform the multiplication word by word
        val ylong = y.toLong() and LONG_MASK
        val zval = if (z.value.size < intLen + 1)
            IntArray(intLen + 1)
        else
            z.value
        var carry: Long = 0
        for (i in intLen - 1 downTo 0) {
            val product = ylong * (value[i + offset].toLong() and LONG_MASK) + carry
            zval[i + 1] = product.toInt()
            carry = product.ushr(32)
        }

        if (carry == 0L) {
            z.offset = 1
            z.intLen = intLen
        } else {
            z.offset = 0
            z.intLen = intLen + 1
            zval[0] = carry.toInt()
        }
        z.value = zval
    }

    /**
     * Internally used  to calculate the quotient of this div v and places the
     * quotient in the provided MutableBigInteger object and the remainder is
     * returned.
     *
     * @return the remainder of the division will be returned.
     */
    internal fun divide(v: Long, quotient: MutableBigInteger): Long {
        var v = v
        if (v == 0L) {
            throw ArithmeticException("BigInteger divide by zero")
        }
        // Dividend is zero
        if (this.intLen == 0) {
            quotient.offset = 0
            quotient.intLen = quotient.offset
            return 0
        }
        if (v < 0) {
            v = -v
        }
        val d = v.ushr(32).toInt()
        quotient.clear()
        // Special case on word divisor
        return if (d == 0)
            divideOneWord(v.toInt(), quotient).toLong() and LONG_MASK
        else {
            divideLongMagnitude(v, quotient).toLong()
        }
    }

    /**
     * @see .divideKnuth
     */
    internal fun divideKnuth(b: MutableBigInteger, quotient: MutableBigInteger): MutableBigInteger? {
        return divideKnuth(b, quotient, true)
    }

    /**
     * Calculates the quotient of this div b and places the quotient in the
     * provided MutableBigInteger objects and the remainder object is returned.
     *
     */
    internal fun divide(b: MutableBigInteger, quotient: MutableBigInteger): MutableBigInteger {
        return divide(b, quotient, true)!!
    }

    internal fun divide(b: MutableBigInteger, quotient: MutableBigInteger,
                        needRemainder: Boolean): MutableBigInteger? {
        return if (b.intLen < AlgorithmUtils.BURNIKEL_ZIEGLER_THRESHOLD ||
                intLen - b.intLen < AlgorithmUtils.BURNIKEL_ZIEGLER_OFFSET) {
            divideKnuth(b, quotient, needRemainder)
        } else {
            divideAndRemainderBurnikelZiegler(b, quotient)
        }
    }

    /**
     * Calculates the quotient of this div b and places the quotient in the
     * provided MutableBigInteger objects and the remainder object is returned.
     *
     * Uses Algorithm D in Knuth section 4.3.1.
     * Many optimizations to that algorithm have been adapted from the Colin
     * Plumb C library.
     * It special cases one word divisors for speed. The content of b is not
     * changed.
     *
     */
    internal fun divideKnuth(b: MutableBigInteger, quotient: MutableBigInteger,
                    needRemainder: Boolean): MutableBigInteger? {
        var b = b
        if (b.intLen == 0) {
            throw ArithmeticException("BigInteger divide by zero")
        }
        // Dividend is zero
        if (this.intLen == 0) {
            quotient.intLen = 0
            quotient.offset = 0
            return if (needRemainder) MutableBigInteger() else null
        }

        val cmp = compare(b)
        // Dividend less than divisor
        if (cmp < 0) {
            quotient.intLen = 0
            quotient.offset = 0
            return if (needRemainder) MutableBigInteger(this) else null
        }
        // Dividend equal to divisor
        if (cmp == 0) {
            quotient.value[0] = 1
            quotient.intLen = 1
            quotient.offset = 0
            return if (needRemainder) MutableBigInteger() else null
        }

        quotient.clear()
        // Special case one word divisor
        if (b.intLen == 1) {
            val r = divideOneWord(b.value[b.offset], quotient)
            return if(needRemainder) {
                if (r == 0) MutableBigInteger() else MutableBigInteger(r)
            } else {
                null
            }
        }

        // Cancel common powers of two if we're above the KNUTH_POW2_* thresholds
        if (intLen >= KNUTH_POW2_THRESH_LEN) {
            val trailingZeroBits = minOf(getLowestSetBit(), b.getLowestSetBit())
            if (trailingZeroBits >= KNUTH_POW2_THRESH_ZEROS*32) {
                val a = MutableBigInteger(this)
                b = MutableBigInteger(b)
                a.rightShift(trailingZeroBits)
                b.rightShift(trailingZeroBits)
                val r = a.divideKnuth(b, quotient)
                r!!.leftShift(trailingZeroBits)
                return r
            }
        }

        return divideMagnitude(b, quotient, needRemainder)
    } // internal fun divideKnuth

    /**
     * Computes `this/b` and `this%b` using the
     * [ Burnikel-Ziegler algorithm](http://cr.yp.to/bib/1998/burnikel.ps).
     * This method implements algorithm 3 from pg. 9 of the Burnikel-Ziegler paper.
     * The parameter beta was chosen to b 2<sup>32</sup> so almost all shifts are
     * multiples of 32 bits.<br></br>
     * `this` and `b` must be nonnegative.
     * @param b the divisor
     * @param quotient output parameter for `this/b`
     * @return the remainder
     */
    internal fun divideAndRemainderBurnikelZiegler(
            b: MutableBigInteger, quotient: MutableBigInteger): MutableBigInteger {
        val r = intLen
        val s = b.intLen

        // Clear the quotient
        quotient.intLen = 0
        quotient.offset = quotient.intLen

        if (r < s) {
            return this
        } else {
            // Unlike Knuth division, we don't check for common powers of two here because
            // BZ already runs faster if both numbers contain powers of two and cancelling them has no
            // additional benefit.

            // step 1: let m = min{2^k | (2^k)*BURNIKEL_ZIEGLER_THRESHOLD > s}
            val m = 1 shl 32 - Integers.numberOfLeadingZeros(s / AlgorithmUtils.BURNIKEL_ZIEGLER_THRESHOLD)

            val j = (s + m - 1) / m      // step 2a: j = ceil(s/m)
            val n = j * m            // step 2b: block length in 32-bit units
            val n32 = 32L * n         // block length in bits
            val sigma = maxOf(0, n32 - b.bitLength()).toInt()   // step 3: sigma = max{T | (2^T)*B < beta^n}
            val bShifted = MutableBigInteger(b)
            bShifted.safeLeftShift(sigma)   // step 4a: shift b so its length is a multiple of n
            safeLeftShift(sigma)     // step 4b: shift this by the same amount

            // step 5: t is the number of blocks needed to accommodate this plus one additional bit
            var t = ((bitLength() + n32) / n32).toInt()
            if (t < 2) {
                t = 2
            }

            // step 6: conceptually split this into blocks a[t-1], ..., a[0]
            val a1 = getBlock(t - 1, t, n)   // the most significant block of this

            // step 7: z[t-2] = [a[t-1], a[t-2]]
            var z = getBlock(t - 2, t, n)    // the second to most significant block
            z.addDisjoint(a1, n)   // z[t-2]

            // do schoolbook division on blocks, dividing 2-block numbers by 1-block numbers
            val qi = MutableBigInteger()
            var ri: MutableBigInteger
            for (i in t - 2 downTo 1) {
                // step 8a: compute (qi,ri) such that z=b*qi+ri
                ri = z.divide2n1n(bShifted, qi)!!

                // step 8b: z = [ri, a[i-1]]
                z = getBlock(i - 1, t, n)   // a[i-1]
                z.addDisjoint(ri, n)
                quotient.addShifted(qi, i * n)   // update q (part of step 9)
            }
            // final iteration of step 8: do the loop one more time for i=0 but leave z unchanged
            ri = z.divide2n1n(bShifted, qi)!!
            quotient.add(qi)

            ri.rightShift(sigma)   // step 9: this and b were shifted, so shift back
            return ri
        }
    } // internal fun divideAndRemainderBurnikelZiegler

    /**
     * This method implements algorithm 1 from pg. 4 of the Burnikel-Ziegler paper.
     * It divides a 2n-digit number by a n-digit number.<br></br>
     * The parameter beta is 2<sup>32</sup> so all shifts are multiples of 32 bits.
     * <br></br>
     * `this` must be a nonnegative number such that `this.bitLength() <= 2*b.bitLength()`
     * @param b a positive number such that `b.bitLength()` is even
     * @param quotient output parameter for `this/b`
     * @return `this%b`
     */
    private fun divide2n1n(b: MutableBigInteger, quotient: MutableBigInteger): MutableBigInteger? {
        val n = b.intLen

        // step 1: base case
        if (n % 2 != 0 || n < AlgorithmUtils.BURNIKEL_ZIEGLER_THRESHOLD) {
            return divideKnuth(b, quotient)
        }

        // step 2: view this as [a1,a2,a3,a4] where each ai is n/2 ints or less
        val aUpper = MutableBigInteger(this)
        aUpper.safeRightShift(32 * (n / 2))   // aUpper = [a1,a2,a3]
        keepLower(n / 2)   // this = a4

        // step 3: q1=aUpper/b, r1=aUpper%b
        val q1 = MutableBigInteger()
        val r1 = aUpper.divide3n2n(b, q1)

        // step 4: quotient=[r1,this]/b, r2=[r1,this]%b
        addDisjoint(r1, n / 2)   // this = [r1,this]
        val r2 = divide3n2n(b, quotient)

        // step 5: let quotient=[q1,quotient] and return r2
        quotient.addDisjoint(q1, n / 2)
        return r2
    } // private fun divide2n1n(b: MutableBigInteger, quotient: MutableBigInteger): MutableBigInteger?

    /**
     * This method implements algorithm 2 from pg. 5 of the Burnikel-Ziegler paper.
     * It divides a 3n-digit number by a 2n-digit number.<br/>
     * The parameter beta is 2<sup>32</sup> so all shifts are multiples of 32 bits.<br/>
     * <br/>
     * `this` must be a nonnegative number such that `2*this.bitLength() <= 3*b.bitLength()`
     * @param quotient output parameter for `this/b`
     * @return `this%b`
     */
    private fun divide3n2n(b: MutableBigInteger, quotient: MutableBigInteger): MutableBigInteger {
        val n = b.intLen / 2   // half the length of b in ints

        // step 1: view this as [a1,a2,a3] where each ai is n ints or less; let a12=[a1,a2]
        val a12 = MutableBigInteger(this)
        a12.safeRightShift(32*n)

        // step 2: view b as [b1,b2] where each bi is n ints or less
        val b1 = MutableBigInteger(b)
        b1.safeRightShift(n * 32)
        val b2 = b.getLower(n)

        val r: MutableBigInteger
        val d: MutableBigInteger
        if (compareShifted(b, n) < 0) {
            // step 3a: if a1<b1, let quotient=a12/b1 and r=a12%b1
            r = a12.divide2n1n(b1, quotient)!!

            // step 4: d=quotient*b2
            d = MutableBigInteger(quotient.toBigInteger().times(b2))
        } else {
            // step 3b: if a1>=b1, let quotient=beta^n-1 and r=a12-b1*2^n+b1
            quotient.ones(n)
            a12.add(b1)
            b1.leftShift(32*n)
            a12.subtract(b1)
            r = a12

            // step 4: d=quotient*b2=(b2 << 32*n) - b2
            d = MutableBigInteger(b2)
            d.leftShift(32 * n)
            d.subtract(MutableBigInteger(b2))
        }

        // step 5: r = r*beta^n + a3 - d (paper says a4)
        // However, don't subtract d until after the while loop so r doesn't become negative
        r.leftShift(32 * n)
        r.addLower(this, n)

        // step 6: add b until r>=d
        while (r.compare(d) < 0) {
            r.add(b)
            quotient.subtract(MutableBigInteger.ONE)
        }
        r.subtract(d)

        return r
    } // private fun divide3n2n(b: MutableBigInteger, quotient: MutableBigInteger): MutableBigInteger

    /**
     * Divide this MutableBigInteger by the divisor.
     * The quotient will be placed into the provided quotient object &
     * the remainder object is returned.
     */
    private fun divideMagnitude(div: MutableBigInteger,
                                quotient: MutableBigInteger,
                                needRemainder: Boolean): MutableBigInteger? {
        // assert div.intLen > 1
        // D1 normalize the divisor
        val shift = Integers.numberOfLeadingZeros(div.value[div.offset])
        // Copy divisor value to protect divisor
        val dlen = div.intLen
        val divisor: IntArray
        val rem: MutableBigInteger // Remainder starts as dividend with space for a leading zero
        if (shift > 0) {
            divisor = IntArray(dlen)
            copyAndShift(div.value, div.offset, dlen, divisor, 0, shift)
            if (Integers.numberOfLeadingZeros(value[offset]) >= shift) {
                val remarr = IntArray(intLen + 1)
                rem = MutableBigInteger(remarr)
                rem.intLen = intLen
                rem.offset = 1
                copyAndShift(value, offset, intLen, remarr, 1, shift)
            } else {
                val remarr = IntArray(intLen + 2)
                rem = MutableBigInteger(remarr)
                rem.intLen = intLen + 1
                rem.offset = 1
                var rFrom = offset
                var c = 0
                val n2 = 32 - shift
                var i = 1
                while (i < this.intLen + 1) {
                    val b = c
                    c = this.value[rFrom]
                    remarr[i] = b shl shift or c.ushr(n2)
                    i += 1
                    rFrom += 1
                }
                remarr[intLen + 1] = c shl shift
            }
        } else {
            divisor = div.value.copyOfRange(div.offset, div.offset + div.intLen)
            rem = MutableBigInteger(IntArray(intLen + 1))
            this.value.copyInto(rem.value, 1, this.offset, this.offset + this.intLen)
            rem.intLen = this.intLen
            rem.offset = 1
        }

        val nlen = rem.intLen

        // Set the quotient size
        val limit = nlen - dlen + 1
        if (quotient.value.size < limit) {
            quotient.value = IntArray(limit)
            quotient.offset = 0
        }
        quotient.intLen = limit
        val q = quotient.value


        // Must insert leading 0 in rem if its length did not change
        if (rem.intLen == nlen) {
            rem.offset = 0
            rem.value[0] = 0
            rem.intLen += 1
        }

        val dh = divisor[0]
        val dhLong = dh.toLong() and LONG_MASK
        val dl = divisor[1]

        // D2 Initialize j
        for (j in 0 until limit - 1) {
            // D3 Calculate qhat
            // estimate qhat
            var qhat: Int
            var qrem: Int
            var skipCorrection = false
            val nh = rem.value[j + rem.offset]
            val nh2 = nh + -0x80000000
            val nm = rem.value[j + 1 + rem.offset]

            if (nh == dh) {
                qhat = 0.inv()
                qrem = nh + nm
                skipCorrection = qrem + -0x80000000 < nh2
            } else {
                val nChunk = nh.toLong() shl 32 or (nm.toLong() and LONG_MASK)
                if (nChunk >= 0) {
                    qhat = (nChunk / dhLong).toInt()
                    qrem = (nChunk - qhat * dhLong).toInt()
                } else {
                    val tmp = divWord(nChunk, dh)
                    qhat = (tmp and LONG_MASK).toInt()
                    qrem = tmp.ushr(32).toInt()
                }
            }

            if (qhat == 0) {
                continue
            }
            if (!skipCorrection) { // Correct qhat
                val nl = rem.value[j + 2 + rem.offset].toLong() and LONG_MASK
                var rs = qrem.toLong() and LONG_MASK shl 32 or nl
                var estProduct = (dl.toLong() and LONG_MASK) * (qhat.toLong() and LONG_MASK)

                if (unsignedLongCompare(estProduct, rs)) {
                    qhat -= 1
                    qrem = ((qrem.toLong() and LONG_MASK) + dhLong).toInt()
                    if (qrem.toLong() and LONG_MASK >= dhLong) {
                        estProduct -= dl.toLong() and LONG_MASK
                        rs = qrem.toLong() and LONG_MASK shl 32 or nl
                        if (unsignedLongCompare(estProduct, rs)) {
                            qhat -= 1
                        }
                    }
                }
            }

            // D4 Multiply and subtract
            rem.value[j + rem.offset] = 0
            val borrow = mulsub(rem.value, divisor, qhat, dlen, j + rem.offset)

            // D5 Test remainder
            if (borrow + -0x80000000 > nh2) {
                // D6 Add back
                divadd(divisor, rem.value, j + 1 + rem.offset)
                qhat -= 1
            }

            // Store the quotient digit
            q[j] = qhat
        } // D7 loop on j
        // D3 Calculate qhat
        // estimate qhat
        var qhat: Int
        var qrem: Int
        var skipCorrection = false
        val nh = rem.value[limit - 1 + rem.offset]
        val nh2 = nh + -0x80000000
        val nm = rem.value[limit + rem.offset]

        if (nh == dh) {
            qhat = 0.inv()
            qrem = nh + nm
            skipCorrection = qrem + -0x80000000 < nh2
        } else {
            val nChunk = nh.toLong() shl 32 or (nm.toLong() and LONG_MASK)
            if (nChunk >= 0) {
                qhat = (nChunk / dhLong).toInt()
                qrem = (nChunk - qhat * dhLong).toInt()
            } else {
                val tmp = divWord(nChunk, dh)
                qhat = (tmp and LONG_MASK).toInt()
                qrem = tmp.ushr(32).toInt()
            }
        }
        if (qhat != 0) {
            if (!skipCorrection) { // Correct qhat
                val nl = rem.value[limit + 1 + rem.offset].toLong() and LONG_MASK
                var rs = qrem.toLong() and LONG_MASK shl 32 or nl
                var estProduct = (dl.toLong() and LONG_MASK) * (qhat.toLong() and LONG_MASK)

                if (unsignedLongCompare(estProduct, rs)) {
                    qhat -= 1
                    qrem = ((qrem.toLong() and LONG_MASK) + dhLong).toInt()
                    if (qrem.toLong() and LONG_MASK >= dhLong) {
                        estProduct -= dl.toLong() and LONG_MASK
                        rs = qrem.toLong() and LONG_MASK shl 32 or nl
                        if (unsignedLongCompare(estProduct, rs)) {
                            qhat -= 1
                        }
                    }
                }
            }

            // D4 Multiply and subtract
            rem.value[limit - 1 + rem.offset] = 0
            val borrow: Int = if (needRemainder) {
                mulsub(rem.value, divisor, qhat, dlen, limit - 1 + rem.offset)
            } else {
                mulsubBorrow(rem.value, divisor, qhat, dlen, limit - 1 + rem.offset)
            }
            // D5 Test remainder
            if (borrow + -0x80000000 > nh2) {
                // D6 Add back
                if (needRemainder) {
                    divadd(divisor, rem.value, limit - 1 + 1 + rem.offset)
                }
                qhat -= 1
            }

            // Store the quotient digit
            q[limit - 1] = qhat
        }

        if (needRemainder) {
            // D8 Unnormalize
            if (shift > 0) {
                rem.rightShift(shift)
            }
            rem.normalize()
        }
        quotient.normalize()
        return if (needRemainder) rem else null
    } // private fun divideMagnitude

    /**
     * This method is used for division of an n word dividend by a one word
     * divisor. The quotient is placed into quotient. The one word divisor is
     * specified by divisor.
     *
     * @return the remainder of the division is returned.
     */
    internal fun divideOneWord(divisor: Int, quotient: MutableBigInteger): Int {
        val divisorLong = divisor.toLong() and LONG_MASK

        // Special case of one word dividend
        if (this.intLen == 1) {
            val dividendValue = this.value[this.offset].toLong() and LONG_MASK
            val q = (dividendValue / divisorLong).toInt()
            val r = (dividendValue - q * divisorLong).toInt()
            quotient.value[0] = q
            quotient.intLen = if (q == 0) 0 else 1
            quotient.offset = 0
            return r
        }

        if (quotient.value.size < this.intLen) {
            quotient.value = IntArray(this.intLen)
        }
        quotient.offset = 0
        quotient.intLen = this.intLen

        // Normalize the divisor
        val shift = Integers.numberOfLeadingZeros(divisor)

        var rem = this.value[this.offset]
        var remLong = rem.toLong() and LONG_MASK
        if (remLong < divisorLong) {
            quotient.value[0] = 0
        } else {
            quotient.value[0] = (remLong / divisorLong).toInt()
            rem = (remLong - quotient.value[0] * divisorLong).toInt()
            remLong = rem.toLong() and LONG_MASK
        }
        var xlen = this.intLen
        while (--xlen > 0) {
            val dividendEstimate = remLong shl 32 or
                    (this.value[this.offset + this.intLen - xlen].toLong() and LONG_MASK)
            val q: Int
            if (dividendEstimate >= 0) {
                q = (dividendEstimate / divisorLong).toInt()
                rem = (dividendEstimate - q * divisorLong).toInt()
            } else {
                val tmp = divWord(dividendEstimate, divisor)
                q = (tmp and LONG_MASK).toInt()
                rem = tmp.ushr(32).toInt()
            }
            quotient.value[intLen - xlen] = q
            remLong = rem.toLong() and LONG_MASK
        }

        quotient.normalize()
        // Unnormalize
        return if (shift > 0) rem % divisor else rem
    } // internal fun divideOneWord(divisor: Int, quotient: MutableBigInteger): Int

    /**
     * Divide this MutableBigInteger by the divisor represented by positive long
     * value. The quotient will be placed into the provided quotient object &
     * the remainder object is returned.
     */
    private fun divideLongMagnitude(ldivisor: Long, quotient: MutableBigInteger): MutableBigInteger {
        var ldivisor = ldivisor
        // Remainder starts as dividend with space for a leading zero
        val rem = MutableBigInteger(IntArray(this.intLen + 1))
        this.value.copyInto(rem.value, 1, this.offset, this.offset + this.intLen)
        rem.intLen = this.intLen
        rem.offset = 1

        val nlen = rem.intLen

        val limit = nlen - 2 + 1
        if (quotient.value.size < limit) {
            quotient.value = IntArray(limit)
            quotient.offset = 0
        }
        quotient.intLen = limit
        val q = quotient.value

        // D1 normalize the divisor
        val shift = Integers.numberOfLeadingZeros(ldivisor)
        if (shift > 0) {
            ldivisor = ldivisor shl shift
            rem.leftShift(shift)
        }

        // Must insert leading 0 in rem if its length did not change
        if (rem.intLen == nlen) {
            rem.offset = 0
            rem.value[0] = 0
            rem.intLen += 1
        }

        val dh = ldivisor.ushr(32).toInt()
        val dhLong = dh.toLong() and LONG_MASK
        val dl = (ldivisor and LONG_MASK).toInt()

        // D2 Initialize j
        for (j in 0 until limit) {
            // D3 Calculate qhat
            // estimate qhat
            var qhat: Int
            var qrem: Int
            var skipCorrection = false
            val nh = rem.value[j + rem.offset]
            val nh2 = nh + -0x80000000
            val nm = rem.value[j + 1 + rem.offset]

            if (nh == dh) {
                qhat = 0.inv()
                qrem = nh + nm
                skipCorrection = qrem + -0x80000000 < nh2
            } else {
                val nChunk = nh.toLong().shl(32) or (nm.toLong() and LONG_MASK)
                if (nChunk >= 0) {
                    qhat = (nChunk / dhLong).toInt()
                    qrem = (nChunk - qhat * dhLong).toInt()
                } else {
                    val tmp = divWord(nChunk, dh)
                    qhat = (tmp and LONG_MASK).toInt()
                    qrem = tmp.ushr(32).toInt()
                }
            }

            if (qhat == 0) {
                continue
            }
            if (!skipCorrection) { // Correct qhat
                val nl = rem.value[j + 2 + rem.offset].toLong() and LONG_MASK
                var rs = qrem.toLong().and(LONG_MASK).shl(32) or nl
                var estProduct = (dl.toLong() and LONG_MASK) * (qhat.toLong() and LONG_MASK)

                if (unsignedLongCompare(estProduct, rs)) {
                    qhat -= 1
                    qrem = ((qrem.toLong() and LONG_MASK) + dhLong).toInt()
                    if (qrem.toLong() and LONG_MASK >= dhLong) {
                        estProduct -= dl.toLong().and(LONG_MASK)
                        rs = qrem.toLong().and(LONG_MASK).shl(32) or nl
                        if (unsignedLongCompare(estProduct, rs)) {
                            qhat -= 1
                        }
                    } // if (qrem.toLong() and LONG_MASK >= dhLong)
                }
            } // if (!skipCorrection) { // Correct qhat

            // D4 Multiply and subtract
            rem.value[j + rem.offset] = 0
            val borrow = mulsubLong(rem.value, dh, dl, qhat, j + rem.offset)

            // D5 Test remainder
            if (borrow + -0x80000000 > nh2) {
                // D6 Add back
                divaddLong(dh, dl, rem.value, j + 1 + rem.offset)
                qhat -= 1
            }

            // Store the quotient digit
            q[j] = qhat
        } // D7 loop on j

        // D8 Unnormalize
        if (shift > 0) {
            rem.rightShift(shift)
        }
        quotient.normalize()
        rem.normalize()
        return rem
    }

    /**
     * This method divides a long quantity by an int to estimate
     * qhat for two multi precision numbers. It is used when
     * the signed value of n is less than zero.
     * Returns long value where high 32 bits contain remainder value and
     * low 32 bits contain quotient value.
     */
    private fun divWord(n: Long, d: Int): Long {
        val dLong = d.toLong() and LONG_MASK
        var r: Long
        var q: Long
        if (dLong == 1L) {
            q = n.toInt().toLong()
            r = 0
            return r shl 32 or (q and LONG_MASK)
        }

        // Approximate the quotient and remainder
        q = n.ushr(1) / dLong.ushr(1)
        r = n - q * dLong

        // Correct the approximation
        while (r < 0) {
            r += dLong
            q -= 1
        }
        while (r >= dLong) {
            r -= dLong
            q += 1
        }
        // n - q*dlong == r && 0 <= r <dLong, hence we're done.
        return r shl 32 or (q and LONG_MASK)
    }

    /**
     * Returns the modInverse of this mod p. This and p are not affected by
     * the operation.
     */
    internal fun mutableModInverse(p: MutableBigInteger): MutableBigInteger {
        // Modulus is odd, use Schroeppel's algorithm
        if (p.isOdd()) {
            return modInverse(p)
        }
        // Base and modulus are even, throw exception
        if (isEven())
            throw ArithmeticException("BigInteger not invertible.")

        // Get even part of modulus expressed as a power of 2
        val powersOf2 = p.getLowestSetBit()

        // Construct odd part of modulus
        val oddMod = MutableBigInteger(p)
        oddMod.rightShift(powersOf2)

        if (oddMod.isOne())
            return modInverseMP2(powersOf2)

        // Calculate 1/a mod oddMod
        val oddPart = modInverse(oddMod)

        // Calculate 1/a mod evenMod
        val evenPart = modInverseMP2(powersOf2)

        // Combine the results using Chinese Remainder Theorem
        val y1 = modInverseBP2(oddMod, powersOf2)
        val y2 = oddMod.modInverseMP2(powersOf2)

        val temp1 = MutableBigInteger()
        val temp2 = MutableBigInteger()
        val result = MutableBigInteger()

        oddPart.leftShift(powersOf2)
        oddPart.multiply(y1, result)

        evenPart.multiply(oddMod, temp1)
        temp1.multiply(y2, temp2)

        result.add(temp2)
        return result.divide(p, temp1)
    }

    /**
     * Calculate the multiplicative inverse of this mod mod, where mod is odd.
     * This and mod are not changed by the calculation.
     *
     * This method implements an algorithm due to Richard Schroeppel, that uses
     * the same intermediate representation as Montgomery Reduction
     * ("Montgomery Form").  The algorithm is described in an unpublished
     * manuscript entitled "Fast Modular Reciprocals."
     */
    private fun modInverse(mod: MutableBigInteger): MutableBigInteger {
        val p = MutableBigInteger(mod)
        var f = MutableBigInteger(this)
        var g = MutableBigInteger(p)
        var c = SignedMutableBigInteger(1)
        var d = SignedMutableBigInteger()

        var k = 0
        // Right shift f k times until odd, left shift d k times
        if (f.isEven()) {
            val trailingZeros = f.getLowestSetBit()
            f.rightShift(trailingZeros)
            d.leftShift(trailingZeros)
            k = trailingZeros
        }

        // The Almost Inverse Algorithm
        while (!f.isOne()) {
            // If gcd(f, g) != 1, number is not invertible modulo mod
            if (f.isZero()) {
                throw ArithmeticException("BigInteger not invertible.")
            }
            // If f < g exchange f, g and c, d
            if (f.compare(g) < 0) {
                val temp = f
                f = g
                g = temp
                val sTemp = d
                d = c
                c = sTemp
            }

            // If f == g (mod 4)
            if (((f.value[f.offset + f.intLen - 1] xor
                 g.value[g.offset + g.intLen - 1]) and 3) == 0) {
                f.subtract(g)
                c.signedSubtract(d)
            } else { // If f != g (mod 4)
                f.add(g)
                c.signedAdd(d)
            }

            // Right shift f k times until odd, left shift d k times
            val trailingZeros = f.getLowestSetBit()
            f.rightShift(trailingZeros)
            d.leftShift(trailingZeros)
            k += trailingZeros
        }

        while (c.sign < 0) {
            c.signedAdd(p)
        }
        return fixup(c, p, k)
    }

    /*
     * Calculate the multiplicative inverse of this mod 2^k.
     */
    private fun modInverseMP2(k: Int): MutableBigInteger {
        if (isEven())
            throw ArithmeticException("Non-invertible. (GCD != 1)")

        if (k > 64) {
            return AlgorithmUtils.euclidModInverse(this, k)
        }
        var t = inverseMod32(value[offset + intLen - 1])

        if (k < 33) {
            t = if (k == 32) t else t and (1 shl k) - 1
            return MutableBigInteger(t)
        }

        var pLong = value[offset + intLen - 1].toLong() and LONG_MASK
        if (intLen > 1) {
            pLong = pLong or (value[offset + intLen - 2].toLong() shl 32)
        }
        var tLong = t.toLong() and LONG_MASK
        tLong = tLong * (2 - pLong * tLong)  // 1 more Newton iter step
        tLong = if (k == 64) tLong else tLong and (1L shl k) - 1

        val result = MutableBigInteger(IntArray(2))
        result.value[0] = tLong.ushr(32).toInt()
        result.value[1] = tLong.toInt()
        result.intLen = 2
        result.normalize()
        return result
    }

    /**
     * Calculate GCD of this and b. This and b are changed by the computation.
     */
    internal fun hybridGCD(other: MutableBigInteger): MutableBigInteger {
        var b = other
        // Use Euclid's algorithm until the numbers are approximately the
        // same length, then use the binary GCD algorithm to find the GCD.
        var a = this
        val q = MutableBigInteger()

        while (b.intLen != 0) {
            if ((a.intLen - b.intLen).absoluteValue < 2) {
                return a.binaryGCD(b)
            }
            val r = a.divide(b, q)
            a = b
            b = r
        }
        return a
    }

    /**
     * Calculate GCD of this and v.
     * Assumes that this and v are not zero.
     */
    private fun binaryGCD(v: MutableBigInteger): MutableBigInteger {
        var other = v
        // Algorithm B from Knuth section 4.5.2
        var u = this
        val r = MutableBigInteger()

        // step B1
        val s1 = u.getLowestSetBit()
        val s2 = other.getLowestSetBit()
        val k = if (s1 < s2) s1 else s2
        if (k != 0) {
            u.rightShift(k)
            other.rightShift(k)
        }

        // step B2
        val uOdd = k == s1
        var t = if (uOdd) other else u
        var tsign = if (uOdd) -1 else 1

        val lb: Int = t.getLowestSetBit()
        while (lb >= 0) {
            // steps B3 and B4
            t.rightShift(lb)
            // step B5
            if (tsign > 0) {
                u = t
            } else {
                other = t
            }
            // Special case one word numbers
            if (u.intLen < 2 && other.intLen < 2) {
                var x = u.value[u.offset]
                val y = other.value[other.offset]
                x = binaryGcd(x, y)
                r.value[0] = x
                r.intLen = 1
                r.offset = 0
                if (k > 0) {
                    r.leftShift(k)
                }
                return r
            }

            // step B6
            tsign = u.difference(other)
            if (tsign == 0) {
                break
            }
            t = if (tsign >= 0) u else other
        }

        if (k > 0) {
            u.leftShift(k)
        }
        return u
    } // private fun binaryGCD(v: MutableBigInteger): MutableBigInteger

    /**
     * Calculate the integer square root {@code floor(sqrt(this))} where
     * {@code sqrt(.)} denotes the mathematical square root. The contents of
     * {@code this} are <b>not</b> changed. The value of {@code this} is assumed
     * to be non-negative.
     *
     * @implNote The implementation is based on the material in Henry S. Warren,
     * Jr., <i>Hacker's Delight (2nd ed.)</i> (Addison Wesley, 2013), 279-282.
     *
     * @throws ArithmeticException if the value returned by {@code bitLength()}
     * overflows the range of {@code int}.
     * @return the integer square root of {@code this}
     * @since Java 9
     */
    internal fun sqrt(): MutableBigInteger {
        // Special cases.
        if (this.isZero()) {
            return MutableBigInteger(0)
        } else if (this.value.size == 1
                && (this.value[0].toLong() and LONG_MASK) < 4) { // result is unity
            return MutableBigInteger(1)
        }

        if (bitLength() <= 63) {
            // Initial estimate is the square root of the positive long value.
            val v = BigInteger(this.value, 1).longValueExact()
            var xk = kotlin.math.floor(kotlin.math.sqrt(v.toDouble())).toLong()

            // Refine the estimate.
            do {
                val xk1 = (xk + v/xk)/2

                // Terminate when non-decreasing.
                if (xk1 >= xk) {
                    return MutableBigInteger(intArrayOf(
                        (xk ushr 32).toInt(), (xk and LONG_MASK).toInt()
                    ))
                }

                xk = xk1
            } while (true)
        } else {
            // Set up the initial estimate of the iteration.

            // Obtain the bitLength > 63.
            val bitLength = this.bitLength().toInt()
            if (bitLength.toLong() != this.bitLength()) {
                throw ArithmeticException("bitLength() integer overflow")
            }

            // Determine an even valued right shift into positive long range.
            var shift = bitLength - 63
            if (shift % 2 == 1) {
                shift += 1
            }

            // Shift the value into positive long range.
            var xk = MutableBigInteger(this)
            xk.rightShift(shift)
            xk.normalize()

            // Use the square root of the shifted value as an approximation.
            val d = BigInteger(xk.value, 1).toDouble()
            val bi = BigInteger.valueOf(kotlin.math.ceil(kotlin.math.sqrt(d)).toLong())
            xk = MutableBigInteger(bi.mag)

            // Shift the approximate square root back into the original range.
            xk.leftShift(shift / 2)

            // Refine the estimate.
            val xk1 = MutableBigInteger()
            do {
                // xk1 = (xk + n/xk)/2
                this.divide(xk, xk1, false)
                xk1.add(xk)
                xk1.rightShift(1)

                // Terminate when non-decreasing.
                if (xk1.compare(xk) >= 0) {
                    return xk
                }

                // xk = xk1
                xk.copyValue(xk1)

                xk1.reset()
            } while (true)
        }
    } // internal fun sqrt(): MutableBigInteger

    /**
     * Returns a `BigInteger` equal to the `n`
     * low ints of this number.
     */
    private fun getLower(n: Int): BigInteger {
        return if (isZero()) {
            BigInteger.ZERO
        } else if (this.intLen < n) {
            toBigInteger(1)
        } else {
            // strip zeros
            var len = n
            while (len > 0 && this.value[this.offset + this.intLen - len] == 0) {
                len -= 1
            }
            val sign = if (len > 0) 1 else 0
            BigInteger(this.value.copyOfRange(this.offset + this.intLen - len,
                    this.offset + this.intLen), sign)
        }
    }

    /**
     * Returns a `MutableBigInteger` containing `blockLength` ints from
     * `this` number, starting at `index*blockLength`.<br></br>
     * Used by Burnikel-Ziegler division.
     * @param index the block index
     * @param numBlocks the total number of blocks in `this` number
     * @param blockLength length of one block in units of 32 bits
     * @return
     */
    private fun getBlock(index: Int, numBlocks: Int, blockLength: Int): MutableBigInteger {
        val blockStart = index * blockLength
        if (blockStart >= intLen) {
            return MutableBigInteger()
        }

        val blockEnd: Int = if (index == numBlocks - 1) {
            intLen
        } else {
            (index + 1) * blockLength
        }
        if (blockEnd > intLen) {
            return MutableBigInteger()
        }

        val newVal = this.value.copyOfRange(this.offset + this.intLen - blockEnd,
                this.offset + this.intLen - blockStart)
        return MutableBigInteger(newVal)
    }

    /**
     * @see BigInteger.bitLength
     */
    private fun bitLength(): Long {
        return if (this.intLen == 0) 0 else
            this.intLen * 32L - Integers.numberOfLeadingZeros(this.value[this.offset])
    }

    /**
     * A primitive used for division. This method adds in one multiple of the
     * divisor a back to the dividend result at a specified offset. It is used
     * when qhat was estimated too large, and must be adjusted.
     */
    private fun divadd(a: IntArray, result: IntArray, offset: Int): Int {
        var carry: Long = 0

        for (j in a.indices.reversed()) {
            val sum = (a[j].toLong() and LONG_MASK) +
                    (result[j + offset].toLong() and LONG_MASK) + carry
            result[j + offset] = sum.toInt()
            carry = sum.ushr(32)
        }
        return carry.toInt()
    }

    /**
     * This method is used for division. It multiplies an n word input a by one
     * word input x, and subtracts the n word product from q. This is needed
     * when subtracting qhat*divisor from dividend.
     */
    private fun mulsub(q: IntArray, a: IntArray, x: Int, len: Int, offset: Int): Int {
        var offset = offset
        val xLong = x.toLong() and LONG_MASK
        var carry: Long = 0
        offset += len

        for (j in len - 1 downTo 0) {
            val product = (a[j].toLong() and LONG_MASK) * xLong + carry
            val difference = q[offset] - product
            q[offset--] = difference.toInt()
            carry = product.ushr(32) +
                    if (difference and LONG_MASK > product.toInt().inv().toLong() and LONG_MASK) 1 else 0
        }
        return carry.toInt()
    }

    /**
     * The method is the same as mulsun, except the fact that q array is not
     * updated, the only result of the method is borrow flag.
     */
    private fun mulsubBorrow(q: IntArray, a: IntArray, x: Int, len: Int, offset: Int): Int {
        var offset = offset
        val xLong = x.toLong() and LONG_MASK
        var carry: Long = 0
        offset += len
        for (j in len - 1 downTo 0) {
            val product = (a[j].toLong() and LONG_MASK) * xLong + carry
            val difference = q[offset--] - product
            carry = product.ushr(32) +
                    if (difference and LONG_MASK > product.toInt().inv().toLong() and LONG_MASK) 1 else 0
        }
        return carry.toInt()
    }

    /**
     * Right shift this MutableBigInteger n bits, where n is
     * less than 32.
     * Assumes that intLen > 0, n > 0 for speed
     */
    private fun primitiveRightShift(n: Int) {
        val n2 = 32 - n
        var i = this.offset + this.intLen - 1
        var c = this.value[i]
        while (i > this.offset) {
            val b = c
            c = this.value[i - 1]
            this.value[i] = c shl n2 or b.ushr(n)
            i -= 1
        }
        this.value[this.offset] = this.value[this.offset] ushr n
    }

    /**
     * Left shift this MutableBigInteger n bits, where n is
     * less than 32.
     * Assumes that intLen > 0, n > 0 for speed
     */
    private fun primitiveLeftShift(n: Int) {
        val n2 = 32 - n
        var i = this.offset
        var c = this.value[i]
        val m = i + intLen - 1
        while (i < m) {
            val b = c
            c = this.value[i + 1]
            this.value[i] = b shl n or c.ushr(n2)
            i += 1
        }
        this.value[this.offset + this.intLen - 1] = this.value[this.offset + this.intLen - 1] shl n
    }

    /**
     * Discards all ints whose index is greater than `n`.
     */
    private fun keepLower(n: Int) {
        if (intLen >= n) {
            offset += intLen - n
            intLen = n
        }
    }

    /**
     * Adds the contents of two MutableBigInteger objects.The result
     * is placed within this MutableBigInteger.
     * The contents of the addend are not changed.
     */
    internal fun add(addend: MutableBigInteger) {
        var x = this.intLen
        var y = addend.intLen
        var resultLen = if (this.intLen > addend.intLen) this.intLen else addend.intLen
        var result = if (this.value.size < resultLen) IntArray(resultLen) else this.value

        var rstart = result.size - 1
        var sum: Long
        var carry: Long = 0

        // Add common parts of both numbers
        while (x > 0 && y > 0) {
            x -= 1
            y -= 1
            sum = (this.value[x + offset].toLong() and LONG_MASK) +
                    (addend.value[y + addend.offset].toLong() and LONG_MASK) + carry
            result[rstart--] = sum.toInt()
            carry = sum.ushr(32)
        }

        // Add remainder of the longer number
        while (x > 0) {
            x -= 1
            if (carry == 0L && result === this.value && rstart == x + this.offset) {
                return
            }
            sum = (this.value[x + this.offset].toLong() and LONG_MASK) + carry
            result[rstart--] = sum.toInt()
            carry = sum.ushr(32)
        }
        while (y > 0) {
            y -= 1
            sum = (addend.value[y + addend.offset].toLong() and LONG_MASK) + carry
            result[rstart--] = sum.toInt()
            carry = sum.ushr(32)
        }

        if (carry > 0) { // Result must grow in length
            resultLen += 1
            if (result.size < resultLen) {
                val temp = IntArray(resultLen)
                // Result one word longer from carry-out; copy low-order
                // bits into new result.
                result.copyInto(temp, 1, 0, result.size)
                temp[0] = 1
                result = temp
            } else {
                result[rstart] = 1
            }
        }

        this.value = result
        this.intLen = resultLen
        this.offset = result.size - resultLen
    } // internal fun add(addend: MutableBigInteger)

    /**
     * Adds the value of `addend` shifted `n` ints to the left.
     * Has the same effect as `addend.leftShift(32*ints); add(addend);`
     * but doesn't change the value of `addend`.
     */
    private fun addShifted(addend: MutableBigInteger, n: Int) {
        if (addend.isZero()) {
            return
        }

        var x = this.intLen
        var y = addend.intLen + n
        var resultLen = if (this.intLen > y) this.intLen else y
        var result = if (this.value.size < resultLen) IntArray(resultLen) else this.value

        var rstart = result.size - 1
        var sum: Long
        var carry: Long = 0

        // Add common parts of both numbers
        while (x > 0 && y > 0) {
            x -= 1
            y -= 1
            val bval = if (y + addend.offset < addend.value.size) addend.value[y + addend.offset] else 0
            sum = (this.value[x + this.offset].toLong() and LONG_MASK) +
                    (bval.toLong() and LONG_MASK) + carry
            result[rstart] = sum.toInt()
            rstart -= 1
            carry = sum.ushr(32)
        }

        // Add remainder of the longer number
        while (x > 0) {
            x -= 1
            if (carry == 0L && result === this.value && rstart == x + this.offset) {
                return
            }
            sum = (this.value[x + this.offset].toLong() and LONG_MASK) + carry
            result[rstart] = sum.toInt()
            rstart -= 1
            carry = sum.ushr(32)
        }
        while (y > 0) {
            y -= 1
            val bval = if (y + addend.offset < addend.value.size) addend.value[y + addend.offset] else 0
            sum = (bval.toLong() and LONG_MASK) + carry
            result[rstart] = sum.toInt()
            rstart -= 1
            carry = sum.ushr(32)
        }

        if (carry > 0) { // Result must grow in length
            resultLen += 1
            if (result.size < resultLen) {
                val temp = IntArray(resultLen)
                // Result one word longer from carry-out; copy low-order
                // bits into new result.
                result.copyInto(temp, 1, 0, result.size)
                temp[0] = 1
                result = temp
            } else {
                result[rstart] = 1
            }
        }

        this.value = result
        this.intLen = resultLen
        this.offset = result.size - resultLen
    } // private fun addShifted(addend: MutableBigInteger, n: Int)

    /**
     * Like [.addShifted] but `this.intLen` must
     * not be greater than `n`. In other words, concatenates `this`
     * and `addend`.
     */
    private fun addDisjoint(addend: MutableBigInteger, n: Int) {
        if (addend.isZero()) {
            return
        }
        val x = this.intLen
        var y = addend.intLen + n
        val resultLen = if (this.intLen > y) this.intLen else y
        val result: IntArray
        if (this.value.size < resultLen) {
            result = IntArray(resultLen)
        } else {
            result = this.value
            Arrays.fill(value, offset + intLen, value.size, 0)
        }

        var rstart = result.size - 1

        // copy from this if needed
        this.value.copyInto(result, rstart + 1 - x, this.offset, this.offset + x)
        y -= x
        rstart -= x

        val len = minOf(y, addend.value.size - addend.offset)
        addend.value.copyInto(result, rstart + 1 - y, addend.offset, addend.offset + len)

        // zero the gap
        for (i in rstart + 1 - y + len until rstart + 1) {
            result[i] = 0
        }

        this.value = result
        this.intLen = resultLen
        this.offset = result.size - resultLen
    }

    /**
     * Adds the low `n` ints of `addend`.
     */
    private fun addLower(addend: MutableBigInteger, n: Int) {
        val a = MutableBigInteger(addend)
        if (a.offset + a.intLen >= n) {
            a.offset = a.offset + a.intLen - n
            a.intLen = n
        }
        a.normalize()
        add(a)
    }

}