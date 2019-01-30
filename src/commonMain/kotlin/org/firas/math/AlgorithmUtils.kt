/*
 * Migrated from the source code of OpenJDK/jdk11 by Wu Yuping
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
/*
 * Portions Copyright (c) 1995  Colin Plumb.  All rights reserved.
 */
package org.firas.math

/**
 *
 * @author Wu Yuping
 * @version 1.0.0
 * @since 1.0.0
 */
internal class AlgorithmUtils private constructor() {

    companion object {
        /**
         * The threshold value for using Karatsuba multiplication.  If the number
         * of ints in both mag arrays are greater than this number, then
         * Karatsuba multiplication will be used.   This value is found
         * experimentally to work well.
         */
        internal val KARATSUBA_THRESHOLD = 80

        /**
         * The threshold value for using 3-way Toom-Cook multiplication.
         * If the number of ints in each mag array is greater than the
         * Karatsuba threshold, and the number of ints in at least one of
         * the mag arrays is greater than this threshold, then Toom-Cook
         * multiplication will be used.
         */
        internal val TOOM_COOK_THRESHOLD = 240

        /**
         * The threshold value for using Karatsuba squaring.  If the number
         * of ints in the number are larger than this value,
         * Karatsuba squaring will be used.   This value is found
         * experimentally to work well.
         */
        internal val KARATSUBA_SQUARE_THRESHOLD = 128

        /**
         * The threshold value for using Toom-Cook squaring.  If the number
         * of ints in the number are larger than this value,
         * Toom-Cook squaring will be used.   This value is found
         * experimentally to work well.
         */
        internal val TOOM_COOK_SQUARE_THRESHOLD = 216

        /**
         * The threshold value for using Burnikel-Ziegler division.  If the number
         * of ints in the divisor are larger than this value, Burnikel-Ziegler
         * division may be used.  This value is found experimentally to work well.
         */
        internal val BURNIKEL_ZIEGLER_THRESHOLD = 80

        /**
         * The offset value for using Burnikel-Ziegler division.  If the number
         * of ints in the divisor exceeds the Burnikel-Ziegler threshold, and the
         * number of ints in the dividend is greater than the number of ints in the
         * divisor plus this value, Burnikel-Ziegler division will be used.  This
         * value is found experimentally to work well.
         */
        internal val BURNIKEL_ZIEGLER_OFFSET = 40

        /**
         * The threshold value for using Schoenhage recursive base conversion. If
         * the number of ints in the number are larger than this value,
         * the Schoenhage algorithm will be used.  In practice, it appears that the
         * Schoenhage routine is faster for any threshold down to 2, and is
         * relatively flat for thresholds between 2-25, so this choice may be
         * varied within this range for very small effect.
         */
        private val SCHOENHAGE_BASE_CONVERSION_THRESHOLD = 20

        /**
         * Multiplies two BigIntegers using the Karatsuba multiplication
         * algorithm.  This is a recursive divide-and-conquer algorithm which is
         * more efficient for large numbers than what is commonly called the
         * "grade-school" algorithm used in multiplyToLen.  If the numbers to be
         * multiplied have length n, the "grade-school" algorithm has an
         * asymptotic complexity of O(n^2).  In contrast, the Karatsuba algorithm
         * has complexity of O(n^(log2(3))), or O(n^1.585).  It achieves this
         * increased performance by doing 3 multiplies instead of 4 when
         * evaluating the product.  As it has some overhead, should be used when
         * both numbers are larger than a certain threshold (found
         * experimentally).
         *
         * See:  http://en.wikipedia.org/wiki/Karatsuba_algorithm
         */
        internal fun multiplyKaratsuba(x: BigInteger, y: BigInteger): BigInteger {
            val xlen = x.mag.size
            val ylen = y.mag.size

            // The number of ints in each half of the number.
            val half = (maxOf(xlen, ylen) + 1) / 2

            // xl and yl are the lower halves of x and y respectively,
            // xh and yh are the upper halves.
            val xl = getLower(x, half)
            val xh = getUpper(x, half)
            val yl = getLower(y, half)
            val yh = getUpper(y, half)

            val p1 = xh * yh  // p1 = xh*yh
            val p2 = xl * yl  // p2 = xl*yl

            val p3 = (xh + xl) * (yh + yl)

            // result = p1 * 2^(32*2*half) + (p3 - p1 - p2) * 2^(32*half) + p2
            val result = p1.shl(32 * half) + (p3 - p1 - p2).shl(32 * half) + p2

            return if (x.signum != y.signum) {
                result.negate()
            } else {
                result
            }
        }

        /**
         * Multiplies two BigIntegers using a 3-way Toom-Cook multiplication
         * algorithm.  This is a recursive divide-and-conquer algorithm which is
         * more efficient for large numbers than what is commonly called the
         * "grade-school" algorithm used in multiplyToLen.  If the numbers to be
         * multiplied have length n, the "grade-school" algorithm has an
         * asymptotic complexity of O(n^2).  In contrast, 3-way Toom-Cook has a
         * complexity of about O(n^1.465).  It achieves this increased asymptotic
         * performance by breaking each number into three parts and by doing 5
         * multiplies instead of 9 when evaluating the product.  Due to overhead
         * (additions, shifts, and one division) in the Toom-Cook algorithm, it
         * should only be used when both numbers are larger than a certain
         * threshold (found experimentally).  This threshold is generally larger
         * than that for Karatsuba multiplication, so this algorithm is generally
         * only used when numbers become significantly larger.
         *
         * The algorithm used is the "optimal" 3-way Toom-Cook algorithm outlined
         * by Marco Bodrato.
         *
         * See: http://bodrato.it/toom-cook/
         * http://bodrato.it/papers/#WAIFI2007
         *
         * "Towards Optimal Toom-Cook Multiplication for Univariate and
         * Multivariate Polynomials in Characteristic 2 and 0." by Marco BODRATO;
         * In C.Carlet and B.Sunar, Eds., "WAIFI'07 proceedings", p. 116-133,
         * LNCS #4547. Springer, Madrid, Spain, June 21-22, 2007.
         *
         */
        internal fun multiplyToomCook3(a: BigInteger, b: BigInteger): BigInteger {
            val alen = a.mag.size
            val blen = b.mag.size

            val largest = maxOf(alen, blen)

            // k is the size (in ints) of the lower-order slices.
            val k = (largest + 2) / 3   // Equal to ceil(largest/3)

            // r is the size (in ints) of the highest-order slice.
            val r = largest - 2 * k

            // Obtain slices of the numbers. a2 and b2 are the most significant
            // bits of the numbers a and b, and a0 and b0 the least significant.
            val a0: BigInteger
            val a1: BigInteger
            val a2: BigInteger
            val b0: BigInteger
            val b1: BigInteger
            val b2: BigInteger
            a2 = getToomSlice(a, k, r, 0, largest)
            a1 = getToomSlice(a, k, r, 1, largest)
            a0 = getToomSlice(a, k, r, 2, largest)
            b2 = getToomSlice(b, k, r, 0, largest)
            b1 = getToomSlice(b, k, r, 1, largest)
            b0 = getToomSlice(b, k, r, 2, largest)

            val v0: BigInteger
            val v1: BigInteger
            val v2: BigInteger
            val vm1: BigInteger
            val vinf: BigInteger
            var t1: BigInteger
            var t2: BigInteger
            var tm1: BigInteger
            var da1: BigInteger
            var db1: BigInteger

            v0 = a0 * b0
            da1 = a2 + a0
            db1 = b2 + b0
            vm1 = (da1 - a1) * (db1 - b1)
            da1 += a1
            db1 += b1
            v1 = da1 * db1
            v2 = da1.plus(a2).shl(1).minus(a0).times(
                    db1.plus(b2).shl(1).minus(b0))
            vinf = a2 * b2

            // The algorithm requires two divisions by 2 and one by 3.
            // All divisions are known to be exact, that is, they do not produce
            // remainders, and all results are positive.  The divisions by 2 are
            // implemented as right shifts which are relatively efficient, leaving
            // only an exact division by 3, which is done by a specialized
            // linear-time algorithm.
            t2 = exactDivideBy3(v2.minus(vm1))
            tm1 = v1.minus(vm1).shr(1)
            t1 = v1.minus(v0)
            t2 = t2.minus(t1).shr(1)
            t1 = t1.minus(tm1).minus(vinf)
            t2 = t2.minus(vinf.shl(1))
            tm1 = tm1.minus(t2)

            // Number of bits to shift left.
            val ss = k * 32

            val result = vinf.shl(ss).plus(t2).shl(ss).plus(t1).shl(ss).plus(tm1).shl(ss).plus(v0)

            return if (a.signum != b.signum) result.negate() else result
        }

        /**
         * Returns a slice of a BigInteger for use in Toom-Cook multiplication.
         *
         * @param lowerSize The size of the lower-order bit slices.
         * @param upperSize The size of the higher-order bit slices.
         * @param slice The index of which slice is requested, which must be a
         * number from 0 to size-1. Slice 0 is the highest-order bits, and slice
         * size-1 are the lowest-order bits. Slice 0 may be of different size than
         * the other slices.
         * @param fullsize The size of the larger integer array, used to align
         * slices to the appropriate position when multiplying different-sized
         * numbers.
         */
        private fun getToomSlice(value: BigInteger, lowerSize: Int, upperSize: Int,
                                 slice: Int, fullsize: Int): BigInteger {
            var start: Int
            val end: Int
            val sliceSize: Int
            val offset: Int

            val len = value.mag.size
            offset = fullsize - len

            if (slice == 0) {
                start = 0 - offset
                end = upperSize - 1 - offset
            } else {
                start = upperSize + (slice - 1) * lowerSize - offset
                end = start + lowerSize - 1
            }

            if (start < 0) {
                start = 0
            }
            if (end < 0) {
                return BigInteger.ZERO
            }

            sliceSize = end - start + 1
            if (sliceSize <= 0) {
                return BigInteger.ZERO
            }

            // While performing Toom-Cook, all slices are positive and
            // the sign is adjusted when the final number is composed.
            if (start == 0 && sliceSize >= len) {
                return value.abs()
            }

            val intSlice = IntArray(sliceSize)
            value.mag.copyInto(intSlice, 0, start, start + sliceSize)

            return BigInteger(BigInteger.trustedStripLeadingZeroInts(intSlice), 1)
        }

        /**
         * Returns a BigInteger whose value is `(value<sup>2</sup>)`.
         *
         * @return `this<sup>2</sup>`
         */
        internal fun square(value: BigInteger): BigInteger {
            if (value.signum == 0) {
                return BigInteger.ZERO
            }
            val len = value.mag.size

            if (len < KARATSUBA_SQUARE_THRESHOLD) {
                val z = squareToLen(value.mag, len, null)
                return BigInteger(BigInteger.trustedStripLeadingZeroInts(z), 1)
            } else {
                return if (len < TOOM_COOK_SQUARE_THRESHOLD) {
                    squareKaratsuba(value)
                } else {
                    squareToomCook3(value)
                }
            }
        }

        /**
         * Squares the contents of the int array x. The result is placed into the
         * int array z.  The contents of x are not changed.
         */
        internal fun squareToLen(x: IntArray, len: Int, z: IntArray?): IntArray {
            var z = z
            /*
             * The algorithm used here is adapted from Colin Plumb's C library.
             * Technique: Consider the partial products in the multiplication
             * of "abcde" by itself:
             *
             *               a  b  c  d  e
             *            *  a  b  c  d  e
             *          ==================
             *              ae be ce de ee
             *           ad bd cd dd de
             *        ac bc cc cd ce
             *     ab bb bc bd be
             *  aa ab ac ad ae
             *
             * Note that everything above the main diagonal:
             *              ae be ce de = (abcd) * e
             *           ad bd cd       = (abc) * d
             *        ac bc             = (ab) * c
             *     ab                   = (a) * b
             *
             * is a copy of everything below the main diagonal:
             *                       de
             *                 cd ce
             *           bc bd be
             *     ab ac ad ae
             *
             * Thus, the sum is 2 * (off the diagonal) + diagonal.
             *
             * This is accumulated beginning with the diagonal (which
             * consist of the squares of the digits of the input), which is then
             * divided by two, the off-diagonal added, and multiplied by two
             * again.  The low bit is simply a copy of the low bit of the
             * input, so it doesn't need special care.
             */
            val zlen = len shl 1
            if (z == null || z.size < zlen) {
                z = IntArray(zlen)
            }
            // Store the squares, right shifted one bit (i.e., divided by 2)
            var lastProductLowWord = 0
            var j = 0
            var i = 0
            while (j < len) {
                val piece = x[j].toLong() and BigInteger.LONG_MASK
                val product = piece * piece
                z[i++] = lastProductLowWord shl 31 or product.ushr(33).toInt()
                z[i++] = product.ushr(1).toInt()
                lastProductLowWord = product.toInt()
                j += 1
            }

            // Add in off-diagonal sums
            i = len
            var offset = 1
            while (i > 0) {
                var t = x[i - 1]
                t = mulAdd(z, x, offset, i - 1, t)
                addOne(z, offset - 1, i, t)
                i -= 1
                offset += 2
            }

            // Shift back up and set low bit
            primitiveLeftShift(z, zlen, 1)
            z[zlen - 1] = z[zlen - 1] or (x[len - 1] and 1)

            return z
        }

        /**
         * Multiply an array by one word k and add to result, return the carry
         */
        internal fun mulAdd(out: IntArray, inArray: IntArray, offset: Int, len: Int, k: Int): Int {
            var offset = offset
            val kLong = k.toLong() and BigInteger.LONG_MASK
            var carry: Long = 0

            offset = out.size - offset - 1
            for (j in len - 1 downTo 0) {
                val product = (inArray[j].toLong() and BigInteger.LONG_MASK) * kLong +
                        (out[offset].toLong() and BigInteger.LONG_MASK) + carry
                out[offset] = product.toInt()
                offset -= 1
                carry = product.ushr(32)
            }
            return carry.toInt()
        }

        /**
         * Add one word to the number a mlen words into a. Return the resulting
         * carry.
         */
        fun addOne(a: IntArray, offset: Int, mlen: Int, carry: Int): Int {
            var offset = a.size - 1 - mlen - offset
            var mlen = mlen
            val t = (a[offset].toLong() and BigInteger.LONG_MASK) + (carry.toLong() and BigInteger.LONG_MASK)

            a[offset] = t.toInt()
            if (t.ushr(32) == 0L) {
                return 0
            }
            while (--mlen >= 0) {
                offset -= 1
                if (offset < 0) { // Carry out of number
                    return 1
                } else {
                    a[offset] += 1
                    if (a[offset] != 0) {
                        return 0
                    }
                }
            }
            return 1
        }

        // shifts a up to len right n bits assumes no leading zeros, 0<n<32
        internal fun primitiveRightShift(a: IntArray, len: Int, n: Int) {
            val n2 = 32 - n
            var i = len - 1
            var c = a[i]
            while (i > 0) {
                val b = c
                c = a[i - 1]
                a[i] = c shl n2 or b.ushr(n)
                i -= 1
            }
            a[0] = a[0] ushr n
        }

        // shifts a up to len left n bits assumes no leading zeros, 0<=n<32
        internal fun primitiveLeftShift(a: IntArray, len: Int, n: Int) {
            if (len == 0 || n == 0) {
                return
            }
            val n2 = 32 - n
            var i = 0
            var c = a[i]
            val m = i + len - 1
            while (i < m) {
                val b = c
                c = a[i + 1]
                a[i] = b shl n or c.ushr(n2)
                i += 1
            }
            a[len - 1] = a[len - 1] shl n
        }

        /**
         * Montgomery reduce n, modulo mod.  This reduces modulo mod and divides
         * by 2^(32*mlen). Adapted from Colin Plumb's C library.
         */
        internal fun montReduce(n: IntArray, mod: IntArray, mlen: Int, inv: Int): IntArray {
            var c = 0
            var len = mlen
            var offset = 0

            do {
                val nEnd = n[n.size - 1 - offset]
                val carry = mulAdd(n, mod, offset, mlen, inv * nEnd)
                c += addOne(n, offset, mlen, carry)
                offset += 1
            } while (--len > 0)

            while (c > 0) {
                c += subN(n, mod, mlen)
            }
            while (intArrayCmpToLen(n, mod, mlen) >= 0) {
                subN(n, mod, mlen)
            }
            return n
        }

        /*
         * Returns -1, 0 or +1 as big-endian unsigned int array arg1 is less than,
         * equal to, or greater than arg2 up to length len.
         */
        private fun intArrayCmpToLen(arg1: IntArray, arg2: IntArray, len: Int): Int {
            for (i in 0 until len) {
                val b1 = arg1[i].toLong() and BigInteger.LONG_MASK
                val b2 = arg2[i].toLong() and BigInteger.LONG_MASK
                if (b1 < b2) {
                    return -1
                }
                if (b1 > b2) {
                    return 1
                }
            }
            return 0
        }

        /**
         * Subtracts two numbers of same length, returning borrow.
         */
        private fun subN(a: IntArray, b: IntArray, length: Int): Int {
            var len = length
            var sum: Long = 0

            while (--len >= 0) {
                sum = (a[len].toLong() and BigInteger.LONG_MASK) -
                        (b[len].toLong() and BigInteger.LONG_MASK) + (sum shr 32)
                a[len] = sum.toInt()
            }

            return (sum shr 32).toInt()
        }

        /**
         * Squares a BigInteger using the Karatsuba squaring algorithm.  It should
         * be used when both numbers are larger than a certain threshold (found
         * experimentally).  It is a recursive divide-and-conquer algorithm that
         * has better asymptotic performance than the algorithm used in
         * squareToLen.
         */
        private fun squareKaratsuba(value: BigInteger): BigInteger {
            val half = (value.mag.size + 1) / 2

            val xl = getLower(value, half)
            val xh = getUpper(value, half)

            val xhs = square(xh)  // xhs = xh^2
            val xls = square(xl)  // xls = xl^2

            // xh^2 << 64  +  (((xl+xh)^2 - (xh^2 + xl^2)) << 32) + xl^2
            return square(xhs.shl(half * 32).plus(xl.plus(xh)).minus(xhs.plus(xls))).shl(half * 32).plus(xls)
        }

        /**
         * Squares a BigInteger using the 3-way Toom-Cook squaring algorithm.  It
         * should be used when both numbers are larger than a certain threshold
         * (found experimentally).  It is a recursive divide-and-conquer algorithm
         * that has better asymptotic performance than the algorithm used in
         * squareToLen or squareKaratsuba.
         */
        private fun squareToomCook3(value: BigInteger): BigInteger {
            val len = value.mag.size

            // k is the size (in ints) of the lower-order slices.
            val k = (len + 2) / 3   // Equal to ceil(largest/3)

            // r is the size (in ints) of the highest-order slice.
            val r = len - 2 * k

            // Obtain slices of the numbers. a2 is the most significant
            // bits of the number, and a0 the least significant.
            val a0: BigInteger
            val a1: BigInteger
            val a2: BigInteger
            a2 = getToomSlice(value, k, r, 0, len)
            a1 = getToomSlice(value, k, r, 1, len)
            a0 = getToomSlice(value, k, r, 2, len)
            var t1: BigInteger
            var t2: BigInteger
            var tm1: BigInteger

            val v0 = square(a0)
            var da1 = a2.plus(a0)
            val vm1 = square(da1.minus(a1))
            da1 = da1.plus(a1)
            val v1 = square(da1)
            val vinf = square(a2)
            val v2 = square(da1.plus(a2).shl(1).minus(a0))

            // The algorithm requires two divisions by 2 and one by 3.
            // All divisions are known to be exact, that is, they do not produce
            // remainders, and all results are positive.  The divisions by 2 are
            // implemented as right shifts which are relatively efficient, leaving
            // only a division by 3.
            // The division by 3 is done by an optimized algorithm for this case.
            t2 = exactDivideBy3(v2.minus(vm1))
            tm1 = v1.minus(vm1).shr(1)
            t1 = v1.minus(v0)
            t2 = t2.minus(t1).shr(1)
            t1 = t1.minus(tm1).minus(vinf)
            t2 = t2.minus(vinf.shl(1))
            tm1 = tm1.minus(t2)

            // Number of bits to shift left.
            val ss = k * 32

            return vinf.shl(ss).plus(t2).shl(ss).plus(t1).shl(ss).plus(tm1).shl(ss).plus(v0)
        }

        /**
         * Returns a new BigInteger representing n lower ints of the number.
         * This is used by Karatsuba multiplication and Karatsuba squaring.
         */
        private fun getLower(value: BigInteger, n: Int): BigInteger {
            val len = value.mag.size

            if (len <= n) {
                return value.abs()
            }

            val lowerInts = IntArray(n)
            value.mag.copyInto(lowerInts, 0, len - n, len)

            return BigInteger(BigInteger.trustedStripLeadingZeroInts(lowerInts), 1)
        }

        /**
         * Returns a new BigInteger representing mag.length-n upper
         * ints of the number.  This is used by Karatsuba multiplication and
         * Karatsuba squaring.
         */
        private fun getUpper(value: BigInteger, n: Int): BigInteger {
            val len = value.mag.size

            if (len <= n) {
                return BigInteger.ZERO
            }

            val upperLen = len - n
            val upperInts = IntArray(upperLen)
            value.mag.copyInto(upperInts, 0, 0, upperLen)

            return BigInteger(BigInteger.trustedStripLeadingZeroInts(upperInts), 1)
        }

        /**
         * Does an exact division (that is, the remainder is known to be zero)
         * of the specified number by 3.  This is used in Toom-Cook
         * multiplication.  This is an efficient algorithm that runs in linear
         * time.  If the argument is not exactly divisible by 3, results are
         * undefined.  Note that this is expected to be called with positive
         * arguments only.
         */
        private fun exactDivideBy3(value: BigInteger): BigInteger {
            val len = value.mag.size
            var result = IntArray(len)
            var x: Long
            var w: Long
            var q: Long
            var borrow: Long
            borrow = 0L
            for (i in len - 1 downTo 0) {
                x = value.mag[i].toLong() and BigInteger.LONG_MASK
                w = x - borrow
                if (borrow > x) {      // Did we make the number go negative?
                    borrow = 1L
                } else {
                    borrow = 0L
                }

                // 0xAAAAAAAB is the modular inverse of 3 (mod 2^32).  Thus,
                // the effect of this is to divide by 3 (mod 2^32).
                // This is much faster than division on most architectures.
                q = w * 0xAAAAAAABL and BigInteger.LONG_MASK
                result[i] = q.toInt()

                // Now check the borrow. The second check can of course be
                // eliminated if the first fails.
                if (q >= 0x55555556L) {
                    borrow++
                    if (q >= 0xAAAAAAABL)
                        borrow++
                }
            }
            result = BigInteger.trustedStripLeadingZeroInts(result)
            return BigInteger(result, value.signum)
        }

        /**
         * Returns a BigInteger whose value is `(this / val)` using an O(n^2) algorithm from Knuth.
         *
         * @param  dividend the dividend
         * @param  divisor value by which the dividend is to be divided.
         * @return `dividend / divisor`
         * @throws ArithmeticException if `divisor` is zero.
         * @see MutableBigInteger.divideKnuth
         */
        internal fun divideKnuth(dividend: BigInteger, divisor: BigInteger): BigInteger {
            val q = MutableBigInteger()
            val a = MutableBigInteger(dividend.mag)
            val b = MutableBigInteger(divisor.mag)

            a.divideKnuth(b, q, false)
            return q.toBigInteger(dividend.signum * divisor.signum)
        }

        internal fun remainderKnuth(dividend: BigInteger, divisor: BigInteger): BigInteger {
            val q = MutableBigInteger()
            val a = MutableBigInteger(dividend.mag)
            val b = MutableBigInteger(divisor.mag)

            return a.divideKnuth(b, q)!!.toBigInteger(dividend.signum)
        }

        internal fun divideAndRemainderKnuth(dividend: BigInteger, divisor: BigInteger): Array<BigInteger> {
            val q = MutableBigInteger()
            val a = MutableBigInteger(dividend.mag)
            val b = MutableBigInteger(divisor.mag)
            val r = a.divideKnuth(b, q)
            return arrayOf(q.toBigInteger(if (dividend.signum == divisor.signum) 1 else -1),
                    r!!.toBigInteger(dividend.signum))
        }

        /**
         * Calculates `dividend / divisor` using the Burnikel-Ziegler algorithm.
         * @param  dividend the dividend
         * @param  divisor the divisor
         * @return `dividend / divisor`
         */
        internal fun divideBurnikelZiegler(dividend: BigInteger, divisor: BigInteger): BigInteger {
            return divideAndRemainderBurnikelZiegler(dividend, divisor)[0]
        }

        /**
         * Calculates `dividend % divisor` using the Burnikel-Ziegler algorithm.
         * @param  dividend the dividend
         * @param  divisor the divisor
         * @return `dividend % divisor`
         */
        internal fun remainderBurnikelZiegler(dividend: BigInteger, divisor: BigInteger): BigInteger {
            return divideAndRemainderBurnikelZiegler(dividend, divisor)[1]
        }

        /**
         * Computes `dividend / divisor` and `dividend % divisor` using the
         * Burnikel-Ziegler algorithm.
         * @param  dividend the dividend
         * @param  divisor the divisor
         * @return an array containing the quotient and remainder
         */
        internal fun divideAndRemainderBurnikelZiegler(
                dividend: BigInteger, divisor: BigInteger): Array<BigInteger> {
            val q = MutableBigInteger()
            val r = MutableBigInteger(dividend).divideAndRemainderBurnikelZiegler(MutableBigInteger(divisor), q)
            val qBigInt = if (q.isZero()) BigInteger.ZERO
                    else q.toBigInteger(dividend.signum * divisor.signum)
            val rBigInt = if (r.isZero()) BigInteger.ZERO else r.toBigInteger(dividend.signum)
            return arrayOf(qBigInt, rBigInt)
        }

        /**
         * Uses the extended Euclidean algorithm to compute the modInverse of base
         * mod a modulus that is a power of 2. The modulus is 2^k.
         */
        internal fun euclidModInverse(value: MutableBigInteger, k: Int): MutableBigInteger {
            var b = MutableBigInteger(1)
            b.leftShift(k)
            val mod = MutableBigInteger(b)

            var a = MutableBigInteger(value)
            var q = MutableBigInteger()
            var r = b.divide(a, q)

            var swapper = b
            // swap b & r
            b = r
            r = swapper

            val t1 = MutableBigInteger(q)
            val t0 = MutableBigInteger(1)
            var temp = MutableBigInteger()

            while (!b.isOne()) {
                r = a.divide(b, q)

                if (r.intLen == 0) {
                    throw ArithmeticException("BigInteger not invertible.")
                }
                swapper = r
                a = swapper

                if (q.intLen == 1) {
                    t1.mul(q.value[q.offset], temp)
                } else {
                    q.multiply(t1, temp)
                }
                swapper = q
                q = temp
                temp = swapper
                t0.add(q)

                if (a.isOne()) {
                    return t0
                }
                r = b.divide(a, q)

                if (r.intLen == 0) {
                    throw ArithmeticException("BigInteger not invertible.")
                }

                swapper = b
                b = r

                if (q.intLen == 1) {
                    t0.mul(q.value[q.offset], temp)
                } else {
                    q.multiply(t0, temp)
                }
                swapper = q
                q = temp
                temp = swapper

                t1.add(q)
            }
            mod.subtract(t1)
            return mod
        }

        /**
         * Returns a BigInteger whose value is (this ** exponent) mod (2**p)
         */
        internal fun modPow2(value: BigInteger, exponent: BigInteger, p: Int): BigInteger {
            /*
             * Perform exponentiation using repeated squaring trick, chopping off
             * high order bits as indicated by modulus.
             */
            var result = BigInteger.ONE
            var baseToPow2 = mod2(value, p)
            var expOffset = 0

            var limit = exponent.bitLength()

            if (value.testBit(0)) {
                limit = if (p - 1 < limit) p - 1 else limit
            }
            while (expOffset < limit) {
                if (exponent.testBit(expOffset)) {
                    result = mod2(result * baseToPow2, p)
                }
                expOffset += 1
                if (expOffset < limit) {
                    baseToPow2 = mod2(square(baseToPow2), p)
                }
            }

            return result
        }

        /**
         * Returns a BigInteger whose value is this mod(2**p).
         * Assumes that this `BigInteger >= 0` and `p > 0`.
         */
        private fun mod2(value: BigInteger, p: Int): BigInteger {
            if (value.bitLength() <= p) {
                return value
            }
            // Copy remaining ints of mag
            val numInts = (p + 31).ushr(5)
            val mag = IntArray(numInts)
            value.mag.copyInto(mag, 0, value.mag.size - numInts, value.mag.size)

            // Mask out any excess bits
            val excessBits = (numInts shl 5) - p
            mag[0] = mag[0] and ((1L shl 32 - excessBits) - 1).toInt()

            return if (mag[0] == 0) BigInteger(1, mag) else BigInteger(mag, 1)
        }
    }
}