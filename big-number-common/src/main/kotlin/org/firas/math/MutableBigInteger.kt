/*
 * Migrated from the source code of OpenJDK/jdk8
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
        internal val KNUTH_POW2_THRESH_LEN = 6

        /**
         * The minimum number of trailing zero ints for cancelling powers of two
         * before dividing.
         * If the dividend and divisor don't share at least this many zero ints
         * at the end, `divideKnuth` does not eliminate common powers
         * of two from the dividend and divisor.
         */
        internal val KNUTH_POW2_THRESH_ZEROS = 3
    }

    /**
     * The offset into the value array where the magnitude of this
     * MutableBigInteger begins.
     */
    internal var offset = 0

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
    internal fun clear() {
        this.intLen = 0
        this.offset = 0
        Arrays.fill(this.value, 0)
    }

    /**
     * Set a MutableBigInteger to zero, removing its offset.
     */
    internal fun reset() {
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
    internal fun setInt(index: Int, value: Int) {
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
    internal fun copyValue(src: MutableBigInteger) {
        copyValue(src.value, src.offset, src.intLen)
    }

    /**
     * Sets this MutableBigInteger's value array to a copy of the specified
     * array. The intLen is set to the length of the specified array.
    */
    internal fun copyValue(value: IntArray) {
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
    internal fun safeRightShift(n: Int) {
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
    internal fun safeLeftShift(n: Int) {
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
    fun subtract(b: MutableBigInteger): Int {
        var a = this
        var b = b

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
            result[rstart--] = diff.toInt()
        }
        // Subtract remainder of longer number
        while (x > 0) {
            x -= 1
            diff = (a.value[x+a.offset].toLong() and LONG_MASK) - (-(diff shr 32))
            result[rstart--] = diff.toInt()
        }

        this.value = result
        this.intLen = resultLen
        this.offset = this.value.size - resultLen
        normalize()
        return sign
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
    fun add(addend: MutableBigInteger) {
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
    }

    /**
     * Adds the value of `addend` shifted `n` ints to the left.
     * Has the same effect as `addend.leftShift(32*ints); add(addend);`
     * but doesn't change the value of `addend`.
     */
    fun addShifted(addend: MutableBigInteger, n: Int) {
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
            val bval = if (y + addend.offset < addend.value.size) addend.value[y + addend.offset] else 0
            sum = (bval.toLong() and LONG_MASK) + carry
            result[rstart--] = sum.toInt()
            carry = sum.ushr(32)
        }

        if (carry > 0) { // Result must grow in length
            resultLen++
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
    }

    /**
     * Like [.addShifted] but `this.intLen` must
     * not be greater than `n`. In other words, concatenates `this`
     * and `addend`.
     */
    fun addDisjoint(addend: MutableBigInteger, n: Int) {
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
    internal fun addLower(addend: MutableBigInteger, n: Int) {
        val a = MutableBigInteger(addend)
        if (a.offset + a.intLen >= n) {
            a.offset = a.offset + a.intLen - n
            a.intLen = n
        }
        a.normalize()
        add(a)
    }
}