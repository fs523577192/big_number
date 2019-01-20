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

            val p1 = xh.multiply(yh)  // p1 = xh*yh
            val p2 = xl.multiply(yl)  // p2 = xl*yl

            // p3=(xh+xl)*(yh+yl)
            val p3 = xh.add(xl).multiply(yh.add(yl))

            // result = p1 * 2^(32*2*half) + (p3 - p1 - p2) * 2^(32*half) + p2
            val result = p1.shiftLeft(32 * half).add(p3.subtract(p1).subtract(p2)).shiftLeft(32 * half).add(p2)

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

            v0 = a0.multiply(b0)
            da1 = a2.add(a0)
            db1 = b2.add(b0)
            vm1 = da1.subtract(a1).multiply(db1.subtract(b1))
            da1 = da1.add(a1)
            db1 = db1.add(b1)
            v1 = da1.multiply(db1)
            v2 = da1.add(a2).shiftLeft(1).subtract(a0).multiply(
                    db1.add(b2).shiftLeft(1).subtract(b0))
            vinf = a2.multiply(b2)

            // The algorithm requires two divisions by 2 and one by 3.
            // All divisions are known to be exact, that is, they do not produce
            // remainders, and all results are positive.  The divisions by 2 are
            // implemented as right shifts which are relatively efficient, leaving
            // only an exact division by 3, which is done by a specialized
            // linear-time algorithm.
            t2 = exactDivideBy3(v2.subtract(vm1))
            tm1 = v1.subtract(vm1).shiftRight(1)
            t1 = v1.subtract(v0)
            t2 = t2.subtract(t1).shiftRight(1)
            t1 = t1.subtract(tm1).subtract(vinf)
            t2 = t2.subtract(vinf.shiftLeft(1))
            tm1 = tm1.subtract(t2)

            // Number of bits to shift left.
            val ss = k * 32

            val result = vinf.shiftLeft(ss).add(t2).shiftLeft(ss).add(t1).shiftLeft(ss).add(tm1).shiftLeft(ss).add(v0)

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
        private fun square(value: BigInteger): BigInteger {
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
        private fun squareToLen(x: IntArray, len: Int, z: IntArray?): IntArray {
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
            return square(xhs.shiftLeft(half * 32).add(xl.add(xh)).subtract(xhs.add(xls))).shiftLeft(half * 32).add(xls)
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
            var da1 = a2.add(a0)
            val vm1 = square(da1.subtract(a1))
            da1 = da1.add(a1)
            val v1 = square(da1)
            val vinf = square(a2)
            val v2 = square(da1.add(a2).shiftLeft(1).subtract(a0))

            // The algorithm requires two divisions by 2 and one by 3.
            // All divisions are known to be exact, that is, they do not produce
            // remainders, and all results are positive.  The divisions by 2 are
            // implemented as right shifts which are relatively efficient, leaving
            // only a division by 3.
            // The division by 3 is done by an optimized algorithm for this case.
            t2 = exactDivideBy3(v2.subtract(vm1))
            tm1 = v1.subtract(vm1).shiftRight(1)
            t1 = v1.subtract(v0)
            t2 = t2.subtract(t1).shiftRight(1)
            t1 = t1.subtract(tm1).subtract(vinf)
            t2 = t2.subtract(vinf.shiftLeft(1))
            tm1 = tm1.subtract(t2)

            // Number of bits to shift left.
            val ss = k * 32

            return vinf.shiftLeft(ss).add(t2).shiftLeft(ss).add(t1).shiftLeft(ss).add(tm1).shiftLeft(ss).add(v0)
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
        private fun remainderBurnikelZiegler(dividend: BigInteger, divisor: BigInteger): BigInteger {
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
         * Computes Jacobi(p,n).
         * Assumes n positive, odd, n>=3.
         */
        private fun jacobiSymbol(p: Int, n: BigInteger): Int {
            var p = p
            if (p == 0) {
                return 0
            }
            // Algorithm and comments adapted from Colin Plumb's C library.
            var j = 1
            var u = n.mag[n.mag.size - 1]

            // Make p positive
            if (p < 0) {
                p = -p
                val n8 = u and 7
                if (n8 == 3 || n8 == 7) {
                    j = -j // 3 (011) or 7 (111) mod 8
                }
            }

            // Get rid of factors of 2 in p
            while (p and 3 == 0) {
                p = p shr 2
            }
            if (p and 1 == 0) {
                p = p shr 1
                if (u xor (u shr 1) and 2 != 0) {
                    j = -j // 3 (011) or 5 (101) mod 8
                }
            }
            if (p == 1) {
                return j
            }
            // Then, apply quadratic reciprocity
            if (p and u and 2 != 0) {
                // p = u = 3 (mod 4)?
                j = -j
            }
            // And reduce u mod p
            u = n.mod(BigInteger.valueOf(p.toLong())).intValue()

            // Now compute Jacobi(u,p), u < p
            while (u != 0) {
                while (u and 3 == 0) {
                    u = u shr 2
                }
                if (u and 1 == 0) {
                    u = u shr 1
                    if (p xor (p shr 1) and 2 != 0) {
                        j = -j     // 3 (011) or 5 (101) mod 8
                    }
                }
                if (u == 1) {
                    return j
                }
                // Now both u and p are odd, so use quadratic reciprocity
                if (!(u < p)) {
                    throw AssertionError()
                }
                val t = u
                u = p
                p = t
                if (u and p and 2 != 0) {
                    // u = p = 3 (mod 4)?
                    j = -j
                }
                // Now u >= p, so it can be reduced
                u %= p
            }
            return 0
        } // private fun jacobiSymbol(p: Int, n: BigInteger): Int

        private fun lucasLehmerSequence(z: Int, k: BigInteger, n: BigInteger): BigInteger {
            val d = BigInteger.valueOf(z.toLong())
            var u = BigInteger.ONE
            var u2: BigInteger
            var v = BigInteger.ONE
            var v2: BigInteger

            for (i in k.bitLength() - 2 downTo 0) {
                u2 = u.multiply(v).mod(n)

                v2 = square(v).add(d.multiply(square(u))).mod(n)
                if (v2.testBit(0))
                    v2 = v2.subtract(n)

                v2 = v2.shiftRight(1)

                u = u2
                v = v2
                if (k.testBit(i)) {
                    u2 = u.add(v).mod(n)
                    if (u2.testBit(0))
                        u2 = u2.subtract(n)

                    u2 = u2.shiftRight(1)
                    v2 = v.add(d.multiply(u)).mod(n)
                    if (v2.testBit(0))
                        v2 = v2.subtract(n)
                    v2 = v2.shiftRight(1)

                    u = u2
                    v = v2
                }
            }
            return u
        } // private fun lucasLehmerSequence(z: Int, k: BigInteger, n: BigInteger): BigInteger
    }
}