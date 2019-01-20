/*
 * Migrated from the source code of OpenJDK/jdk8 by Wu Yuping
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

import org.firas.lang.Character
import org.firas.util.Integers
import kotlin.math.absoluteValue
import kotlin.random.Random

/**
 * Immutable arbitrary-precision integers.  All operations behave as if
 * BigIntegers were represented in two's-complement notation (like Java's
 * primitive integer types).  BigInteger provides analogues to all of Java's
 * primitive integer operators, and all relevant methods from java.lang.Math.
 * Additionally, BigInteger provides operations for modular arithmetic, GCD
 * calculation, primality testing, prime generation, bit manipulation,
 * and a few other miscellaneous operations.
 *
 * <p>Semantics of arithmetic operations exactly mimic those of Java's integer
 * arithmetic operators, as defined in <i>The Java Language Specification</i>.
 * For example, division by zero throws an `ArithmeticException`, and
 * division of a negative by a positive yields a negative (or zero) remainder.
 * All of the details in the Spec concerning overflow are ignored, as
 * BigIntegers are made as large as necessary to accommodate the results of an
 * operation.
 *
 * <p>Semantics of shift operations extend those of Java's shift operators
 * to allow for negative shift distances.  A right-shift with a negative
 * shift distance results in a left shift, and vice-versa.  The unsigned
 * right shift operator (`>>>`) is omitted, as this operation makes
 * little sense in combination with the "infinite word size" abstraction
 * provided by this class.
 *
 * <p>Semantics of bitwise logical operations exactly mimic those of Java's
 * bitwise integer operators.  The binary operators (`and`,
 * `or`, `xor`) implicitly perform sign extension on the shorter
 * of the two operands prior to performing the operation.
 *
 * <p>Comparison operations perform signed integer comparisons, analogous to
 * those performed by Java's relational and equality operators.
 *
 * <p>Modular arithmetic operations are provided to compute residues, perform
 * exponentiation, and compute multiplicative inverses.  These methods always
 * return a non-negative result, between `0` and `(modulus - 1)`,
 * inclusive.
 *
 * <p>Bit operations operate on a single bit of the two's-complement
 * representation of their operand.  If necessary, the operand is sign-
 * extended so that it contains the designated bit.  None of the single-bit
 * operations can produce a BigInteger with a different sign from the
 * BigInteger being operated on, as they affect only a single bit, and the
 * "infinite word size" abstraction provided by this class ensures that there
 * are infinitely many "virtual sign bits" preceding each BigInteger.
 *
 * <p>For the sake of brevity and clarity, pseudo-code is used throughout the
 * descriptions of BigInteger methods.  The pseudo-code expression
 * `(i + j)` is shorthand for "a BigInteger whose value is
 * that of the BigInteger `i` plus that of the BigInteger `j`."
 * The pseudo-code expression `(i == j)` is shorthand for
 * "`true` if and only if the BigInteger `i` represents the same
 * value as the BigInteger `j`."  Other pseudo-code expressions are
 * interpreted similarly.
 *
 * <p>All methods and constructors in this class throw
 * `NullPointerException` when passed
 * a null object reference for any input parameter.
 *
 * BigInteger must support values in the range
 * -2<sup>`Integer.MAX_VALUE`</sup> (exclusive) to
 * +2<sup>`Integer.MAX_VALUE`</sup> (exclusive)
 * and may support values outside of that range.
 *
 * The range of probable prime values is limited and may be less than
 * the full supported positive range of `BigInteger`.
 * The range must be at least 1 to 2<sup>500000000</sup>.
 *
 * @implNote
 * BigInteger constructors and operations throw `ArithmeticException` when
 * the result is out of the supported range of
 * -2<sup>`Integer.MAX_VALUE`</sup> (exclusive) to
 * +2<sup>`Integer.MAX_VALUE`</sup> (exclusive).
 *
 * @see     BigDecimal
 * @author  Josh Bloch
 * @author  Michael McCloskey
 * @author  Alan Eliasen
 * @author  Timothy Buktu
 * @author  Wu Yuping
 * @since JDK1.1
 */
class BigInteger: Number, Comparable<BigInteger> {

    /**
     * The signum of this BigInteger: -1 for negative, 0 for zero, or
     * 1 for positive.  Note that the BigInteger zero <i>must</i> have
     * a signum of 0.  This is necessary to ensures that there is exactly one
     * representation for each BigInteger value.
     *
     * @serial
     */
    private var signum: Int = 0

    /**
     * The magnitude of this BigInteger, in <i>big-endian</i> order: the
     * zeroth element of this array is the most-significant int of the
     * magnitude.  The magnitude must be "minimal" in that the most-significant
     * int ({@code mag[0]}) must be non-zero.  This is necessary to
     * ensure that there is exactly one representation for each BigInteger
     * value.  Note that this implies that the BigInteger zero has a
     * zero-length mag array.
     */
    internal var mag: IntArray = intArrayOf()

    // ----==== Constructors ====----
    /**
     * Translates a byte array containing the two's-complement binary
     * representation of a BigInteger into a BigInteger.  The input array is
     * assumed to be in *big-endian* byte-order: the most significant
     * byte is in the zeroth element.
     *
     * @param  value big-endian two's-complement binary representation of
     * BigInteger.
     * @throws NumberFormatException value is zero bytes long.
     */
    constructor(value: ByteArray) {
        if (value.size == 0) {
            throw NumberFormatException("Zero length BigInteger")
        }
        if (value[0] < 0) {
            this.mag = makePositive(value)
            this.signum = -1
        } else {
            this.mag = stripLeadingZeroBytes(value)
            this.signum = if (this.mag.size == 0) 0 else 1
        }
        if (this.mag.size >= MAX_MAG_LENGTH) {
            checkRange()
        }
    }

    /**
     * Translates the sign-magnitude representation of a BigInteger into a
     * BigInteger.  The sign is represented as an integer signum value: -1 for
     * negative, 0 for zero, or 1 for positive.  The magnitude is a byte array
     * in *big-endian* byte-order: the most significant byte is in the
     * zeroth element.  A zero-length magnitude array is permissible, and will
     * result in a BigInteger value of 0, whether signum is -1, 0 or 1.
     *
     * @param  signum signum of the number (-1 for negative, 0 for zero, 1
     * for positive).
     * @param  magnitude big-endian binary representation of the magnitude of
     * the number.
     * @throws NumberFormatException `signum` is not one of the three
     * legal values (-1, 0, and 1), or `signum` is 0 and
     * `magnitude` contains one or more non-zero bytes.
     */
    constructor(signum: Int, magnitude: ByteArray) {
        this.mag = stripLeadingZeroBytes(magnitude)

        if (signum < -1 || signum > 1) {
            throw NumberFormatException("Invalid signum value")
        }
        if (this.mag.size == 0) {
            this.signum = 0
        } else {
            if (signum == 0) {
                throw NumberFormatException("signum-magnitude mismatch")
            }
            this.signum = signum
        }
        if (this.mag.size >= MAX_MAG_LENGTH) {
            checkRange()
        }
    } // constructor(signum: Int, magnitude: ByteArray)

    /**
     * Constructs a randomly generated BigInteger, uniformly distributed over
     * the range 0 to (2<sup>`numBits`</sup> - 1), inclusive.
     * The uniformity of the distribution assumes that a fair source of random
     * bits is provided in `rnd`.  Note that this constructor always
     * constructs a non-negative BigInteger.
     *
     * @param  numBits maximum bitLength of the new BigInteger.
     * @param  rnd source of randomness to be used in computing the new
     * BigInteger.
     * @throws IllegalArgumentException `numBits` is negative.
     * @see .bitLength
     */
    constructor(numBits: Int, rnd: Random); this(1, randomBits(numBits, rnd))

    /**
     * Translates the String representation of a BigInteger in the
     * specified radix into a BigInteger.  The String representation
     * consists of an optional minus or plus sign followed by a
     * sequence of one or more digits in the specified radix.  The
     * character-to-digit mapping is provided by `Character.digit`.  The String may not contain any extraneous
     * characters (whitespace, for example).
     *
     * @param value String representation of BigInteger.
     * @param radix radix to be used in interpreting `value`.
     * @throws NumberFormatException `value` is not a valid representation
     * of a BigInteger in the specified radix, or `radix` is
     * outside the range from [Character.MIN_RADIX] to
     * [Character.MAX_RADIX], inclusive.
     * @see Character.digit
     */
    internal constructor(value: String, radix: Int) {
        var cursor = 0
        val numDigits: Int
        val len = value.length

        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
            throw NumberFormatException("Radix out of range")
        }
        if (len == 0) {
            throw NumberFormatException("Zero length BigInteger")
        }

        // Check for at most one leading sign
        var sign = 1
        val index1 = value.lastIndexOf('-')
        val index2 = value.lastIndexOf('+')
        if (index1 >= 0) {
            if (index1 != 0 || index2 >= 0) {
                throw NumberFormatException("Illegal embedded sign character")
            }
            sign = -1
            cursor = 1
        } else if (index2 >= 0) {
            if (index2 != 0) {
                throw NumberFormatException("Illegal embedded sign character")
            }
            cursor = 1
        }
        if (cursor == len)
            throw NumberFormatException("Zero length BigInteger")

        // Skip leading zeros and compute number of digits in magnitude
        while (cursor < len && Character.digit(value[cursor], radix) == 0) {
            cursor++
        }

        if (cursor == len) {
            this.signum = 0
            this.mag = ZERO.mag
            return
        }

        numDigits = len - cursor
        this.signum = sign

        // Pre-allocate array of expected size. May be too large but can
        // never be too small. Typically exact.
        val numBits = (numDigits * bitsPerDigit[radix]).ushr(10) + 1
        if (numBits + 31 >= 1L shl 32) {
            reportOverflow()
        }
        val numWords = (numBits + 31).toInt().ushr(5)
        val magnitude = IntArray(numWords)

        // Process first (potentially short) digit group
        var firstGroupLen = numDigits % digitsPerInt[radix]
        if (firstGroupLen == 0) {
            firstGroupLen = digitsPerInt[radix]
        }
        var group = value.substring(cursor, cursor + firstGroupLen)
        cursor += firstGroupLen
        magnitude[numWords - 1] = group.toInt(radix)
        if (magnitude[numWords - 1] < 0)
            throw NumberFormatException("Illegal digit")

        // Process remaining digit groups
        val superRadix = intRadix[radix]
        var groupVal = 0
        while (cursor < len) {
            group = value.substring(cursor, cursor + digitsPerInt[radix])
            cursor += digitsPerInt[radix]
            groupVal = group.toInt(radix)
            if (groupVal < 0)
                throw NumberFormatException("Illegal digit")
            destructiveMulAdd(magnitude, superRadix, groupVal)
        }
        // Required for cases where the array was overallocated.
        this.mag = trustedStripLeadingZeroInts(magnitude)
        if (this.mag.size >= MAX_MAG_LENGTH) {
            checkRange()
        }
    } // internal constructor(value: String, radix: Int)

    /*
     * Constructs a new BigInteger using a char array with radix=10.
     * Sign is precalculated outside and not allowed in the val.
     */
    internal constructor(value: CharArray, sign: Int, len: Int) {
        var cursor = 0
        val numDigits: Int

        // Skip leading zeros and compute number of digits in magnitude
        while (cursor < len && Character.digit(value[cursor], 10) == 0) {
            cursor++
        }
        if (cursor == len) {
            signum = 0
            mag = ZERO.mag
            return
        }

        numDigits = len - cursor
        this.signum = sign
        // Pre-allocate array of expected size
        val numWords: Int
        if (len < 10) {
            numWords = 1
        } else {
            val numBits = (numDigits * bitsPerDigit[10]).ushr(10) + 1
            if (numBits + 31 >= 1L shl 32) {
                reportOverflow()
            }
            numWords = (numBits + 31).toInt().ushr(5)
        }
        val magnitude = IntArray(numWords)

        // Process first (potentially short) digit group
        var firstGroupLen = numDigits % digitsPerInt[10]
        if (firstGroupLen == 0) {
            firstGroupLen = digitsPerInt[10]
        }
        magnitude[numWords - 1] = parseInt(value, cursor, cursor + firstGroupLen)
        cursor += firstGroupLen

        // Process remaining digit groups
        while (cursor < len) {
            val groupVal = parseInt(value, cursor, cursor + digitsPerInt[10])
            cursor += digitsPerInt[10]
            destructiveMulAdd(magnitude, intRadix[10], groupVal)
        }
        this.mag = trustedStripLeadingZeroInts(magnitude)
        if (this.mag.size >= MAX_MAG_LENGTH) {
            checkRange()
        }
    } // internal constructor(value: CharArray, sign: Int, len: Int)

    /**
     * This internal constructor differs from its public cousin
     * with the arguments reversed in two ways: it assumes that its
     * arguments are correct, and it doesn't copy the magnitude array.
     */
    internal constructor(magnitude: IntArray, signum: Int) {
        this.signum = if (magnitude.size == 0) 0 else signum
        this.mag = magnitude
        if (mag.size >= MAX_MAG_LENGTH) {
            checkRange()
        }
    }

    /**
     * This private constructor translates an int array containing the
     * two's-complement binary representation of a BigInteger into a
     * BigInteger. The input array is assumed to be in <i>big-endian</i>
     * int-order: the most significant int is in the zeroth element.
     */
    private constructor(value: IntArray) {
        if (value.size == 0) {
            throw NumberFormatException("Zero length BigInteger")
        }

        if (value[0] < 0) {
            this.mag = makePositive(value)
            this.signum = -1
        } else {
            this.mag = trustedStripLeadingZeroInts(value)
            this.signum = if (this.mag.size == 0) 0 else 1
        }
        if (this.mag.size >= MAX_MAG_LENGTH) {
            checkRange()
        }
    } // private constructor(value: IntArray)

    /**
     * A constructor for internal use that translates the sign-magnitude
     * representation of a BigInteger into a BigInteger. It checks the
     * arguments and copies the magnitude so this constructor would be
     * safe for external use.
     */
    private constructor(signum: Int, magnitude: IntArray) {
        this.mag = stripLeadingZeroInts(magnitude)

        if (signum < -1 || signum > 1) {
            throw NumberFormatException("Invalid signum value")
        }

        if (this.mag.size == 0) {
            this.signum = 0
        } else {
            if (signum == 0) {
                throw NumberFormatException("signum-magnitude mismatch")
            }
            this.signum = signum
        }
        if (this.mag.size >= MAX_MAG_LENGTH) {
            checkRange()
        }
    } // private constructor(signum: Int, magnitude: IntArray)

    /**
     * This private constructor is for internal use and assumes that its
     * arguments are correct.
     */
    private constructor(magnitude: ByteArray, signum: Int) {
        this.signum = if (magnitude.size == 0) 0 else signum
        this.mag = stripLeadingZeroBytes(magnitude)
        if (mag.size >= MAX_MAG_LENGTH) {
            checkRange()
        }
    }

    /**
     * Constructs a BigInteger with the specified value, which may not be zero.
     */
    private constructor(value: Long) {
        var value = value
        if (value < 0) {
            value = -value
            this.signum = -1
        } else {
            this.signum = 1
        }

        val highWord = value.ushr(32).toInt()
        if (highWord == 0) {
            this.mag = IntArray(1)
            this.mag[0] = value.toInt()
        } else {
            this.mag = IntArray(2)
            this.mag[0] = highWord
            this.mag[1] = value.toInt()
        }
    } // private constructor(Long)

    companion object {
        /**
         * The BigInteger constant zero.
         *
         * @since   1.2
         */
        val ZERO = BigInteger(IntArray(0), 0)

        /**
         * The BigInteger constant one.
         *
         * @since   1.2
         */
        val ONE = valueOf(1)

        /**
         * The BigInteger constant two.  (Not exported.)
         */
        private val TWO = valueOf(2)

        /**
         * The BigInteger constant -1.  (Not exported.)
         */
        private val NEGATIVE_ONE = valueOf(-1)

        /**
         * The BigInteger constant ten.
         *
         * @since   1.5
         */
        val TEN = valueOf(10)

        // ----==== Static Factory Methods ====----
        /**
         * Returns a BigInteger whose value is equal to that of the
         * specified `long`.  This "static factory method" is
         * provided in preference to a (`long`) constructor
         * because it allows for reuse of frequently used BigIntegers.
         *
         * @param  value value of the BigInteger to return.
         * @return a BigInteger with the specified value.
         */
        fun valueOf(value: Long): BigInteger {
            // If -MAX_CONSTANT < val < MAX_CONSTANT, return stashed constant
            if (value == 0L) {
                return ZERO
            }
            if (value > 0 && value <= MAX_CONSTANT) {
                return posConst[value.toInt()]!!
            } else if (value < 0 && value >= -MAX_CONSTANT) {
                return negConst[(-value).toInt()]!!
            }
            return BigInteger(value)
        }

        /**
         * Returns a BigInteger with the given two's complement representation.
         * Assumes that the input array will not be modified (the returned
         * BigInteger will reference the input array if feasible).
         */
        private fun valueOf(value: IntArray): BigInteger {
            return if (value[0] > 0) BigInteger(value, 1) else BigInteger(value)
        }

        /**
         * This mask is used to obtain the value of an int as if it were unsigned.
         */
        internal val LONG_MASK = 0xffffffffL

        /**
         * This constant limits `mag.length` of BigIntegers to the supported
         * range.
         */
        private val MAX_MAG_LENGTH = Int.MAX_VALUE / Int.SIZE_BITS + 1 // (1 << 26)

        /*
         * The following two arrays are used for fast String conversions.  Both
         * are indexed by radix.  The first is the number of digits of the given
         * radix that can fit in a Java long without "going negative", i.e., the
         * highest integer n such that radix**n < 2**63.  The second is the
         * "long radix" that tears each number into "long digits", each of which
         * consists of the number of digits in the corresponding element in
         * digitsPerLong (longRadix[i] = i**digitPerLong[i]).  Both arrays have
         * nonsense values in their 0 and 1 elements, as radixes 0 and 1 are not
         * used.
         */
        private val digitsPerLong = intArrayOf(0, 0, 62, 39, 31,
                27, 24, 22, 20, 19,
                18, 18, 17, 17, 16,
                16, 15, 15, 15, 14,
                14, 14, 14, 13, 13,
                13, 13, 13, 13, 12,
                12, 12, 12, 12, 12,
                12, 12)

        private val longRadix = arrayOf<BigInteger?>(null, null,
                valueOf(0x4000000000000000L),
                valueOf(0x383d9170b85ff80bL),
                valueOf(0x4000000000000000L),
                valueOf(0x6765c793fa10079dL),
                valueOf(0x41c21cb8e1000000L),
                valueOf(0x3642798750226111L),
                valueOf(0x1000000000000000L),
                valueOf(0x12bf307ae81ffd59L),
                valueOf(0xde0b6b3a7640000L),
                valueOf(0x4d28cb56c33fa539L),
                valueOf(0x1eca170c00000000L),
                valueOf(0x780c7372621bd74dL),
                valueOf(0x1e39a5057d810000L),
                valueOf(0x5b27ac993df97701L),
                valueOf(0x1000000000000000L),
                valueOf(0x27b95e997e21d9f1L),
                valueOf(0x5da0e1e53c5c8000L),
                valueOf(0xb16a458ef403f19L),
                valueOf(0x16bcc41e90000000L),
                valueOf(0x2d04b7fdd9c0ef49L),
                valueOf(0x5658597bcaa24000L),
                valueOf(0x6feb266931a75b7L),
                valueOf(0xc29e98000000000L),
                valueOf(0x14adf4b7320334b9L),
                valueOf(0x226ed36478bfa000L),
                valueOf(0x383d9170b85ff80bL),
                valueOf(0x5a3c23e39c000000L),
                valueOf(0x4e900abb53e6b71L),
                valueOf(0x7600ec618141000L),
                valueOf(0xaee5720ee830681L),
                valueOf(0x1000000000000000L),
                valueOf(0x172588ad4f5f0981L),
                valueOf(0x211e44f7d02c1000L),
                valueOf(0x2ee56725f06e5c71L),
                valueOf(0x41c21cb8e1000000L))

        /*
     * These two arrays are the integer analogue of above.
     */
        private val digitsPerInt = intArrayOf(0, 0, 30, 19, 15,
                13, 11, 11, 10, 9,
                9, 8, 8, 8, 8,
                7, 7, 7, 7, 7,
                7, 7, 6, 6, 6,
                6, 6, 6, 6, 6,
                6, 6, 6, 6, 6,
                6, 5)

        private val intRadix = intArrayOf(0, 0,
                0x40000000,
                0x4546b3db,
                0x40000000,
                0x48c27395,
                0x159fd800,
                0x75db9c97,
                0x40000000,
                0x17179149,
                0x3b9aca00,
                0xcc6db61,
                0x19a10000,
                0x309f1021,
                0x57f6c100,
                0xa2f1b6f,
                0x10000000,
                0x18754571,
                0x247dbc80,
                0x3547667b,
                0x4c4b4000,
                0x6b5a6e1d,
                0x6c20a40,
                0x8d2d931,
                0xb640000,
                0xe8d4a51,
                0x1269ae40,
                0x17179149,
                0x1cb91000,
                0x23744899,
                0x2b73a840,
                0x34e63b41,
                0x40000000,
                0x4cfa3cc1,
                0x5c13d840,
                0x6d91b519,
                0x39aa400)

        // bitsPerDigit in the given radix times 1024
        // Rounded up to avoid underallocation.
        private val bitsPerDigit = longArrayOf(0, 0, 1024, 1624, 2048,
                2378, 2648, 2875, 3072, 3247,
                3402, 3543, 3672, 3790, 3899,
                4001, 4096, 4186, 4271, 4350,
                4426, 4498, 4567, 4633, 4696,
                4756, 4814, 4870, 4923, 4975,
                5025, 5074, 5120, 5166, 5210,
                5253, 5295)

        /**
         * Initialize static constant array when class is loaded.
         */
        private val MAX_CONSTANT = 16
        private val posConst = arrayOfNulls<BigInteger>(MAX_CONSTANT + 1)
        private val negConst = arrayOfNulls<BigInteger>(MAX_CONSTANT + 1)

        /**
         * The cache of powers of each radix.  This allows us to not have to
         * recalculate powers of radix^(2^n) more than once.  This speeds
         * Schoenhage recursive base conversion significantly.
         */
        private var powerCache: Array<Array<BigInteger>>? = null

        /** The cache of logarithms of radices for base conversion.  */
        private var logCache: DoubleArray? = null

        /** The natural log of 2.  This is used in computing cache indices.  */
        private val LOG_TWO = kotlin.math.ln(2.0)

        /* zero[i] is a string of i consecutive zeros. */
        private val zeros = arrayOfNulls<String>(64)

        init {
            for (i in 1 .. MAX_CONSTANT) {
                val magnitude = IntArray(1)
                magnitude[0] = i
                posConst[i] = BigInteger(magnitude, 1)
                negConst[i] = BigInteger(magnitude, -1)
            }

            /*
             * Initialize the cache of radix^(2^x) values used for base conversion
             * with just the very first value.  Additional values will be created
             * on demand.
             */
            powerCache = Array<Array<BigInteger>>(Character.MAX_RADIX + 1) {
                i -> if (i < Character.MIN_RADIX) arrayOf() else arrayOf(BigInteger.valueOf(i.toLong()))
            }
            logCache = DoubleArray(Character.MAX_RADIX + 1) {
                i -> if (i < Character.MIN_RADIX) 0.0 else kotlin.math.ln(i.toDouble())
            }

            zeros[63] = "000000000000000000000000000000000000000000000000000000000000000"
            for (i in 0 .. 62) {
                zeros[i] = zeros[63]!!.substring(0, i)
            }
        }

        /**
         * Package private method to return bit length for an integer.
         */
        internal fun bitLengthForInt(n: Int): Int {
            return 32 - Integers.numberOfLeadingZeros(n)
        }

        /**
         * Calculate bitlength of contents of the first len elements an int array,
         * assuming there are no leading zero ints.
         */
        private fun bitLength(value: IntArray, len: Int): Int {
            return if (len == 0) 0 else (len - 1 shl 5) + bitLengthForInt(value[0])
        }

        /**
         * Returns a copy of the input array stripped of any leading zero bytes.
         */
        private fun stripLeadingZeroInts(value: IntArray): IntArray {
            val vlen = value.size

            // Find first nonzero byte
            var keep: Int = 0
            while (keep < vlen && value[keep] == 0) {
                keep += 1
            }
            return value.copyOfRange(keep, vlen)
        }

        /**
         * Returns the input array stripped of any leading zero bytes.
         * Since the source is trusted the copying may be skipped.
         */
        private fun trustedStripLeadingZeroInts(value: IntArray): IntArray {
            val vlen = value.size

            // Find first nonzero byte
            var keep: Int = 0
            while (keep < vlen && value[keep] == 0) {
                keep += 1
            }
            return if (keep == 0) value else value.copyOfRange(keep, vlen)
        }

        /**
         * Returns a copy of the input array stripped of any leading zero bytes.
         */
        private fun stripLeadingZeroBytes(a: ByteArray): IntArray {
            val byteLength = a.size

            // Find first nonzero byte
            var keep: Int = 0
            while (keep < byteLength && a[keep].toInt() == 0) {
                keep += 1
            }

            // Allocate new array and copy relevant part of input array
            val intLength = (byteLength - keep + 3).ushr(2)
            val result = IntArray(intLength)
            var b = byteLength - 1
            for (i in intLength - 1 downTo 0) {
                result[i] = a[b--].toInt() and 0xff
                val bytesRemaining = b - keep + 1
                val bytesToTransfer = minOf(3, bytesRemaining)
                var j = 8
                while (j <= bytesToTransfer shl 3) {
                    result[i] = result[i] or (a[b--].toInt() and 0xff shl j)
                    j += 8
                }
            }
            return result
        }

        /**
         * Create an integer with the digits between the two indexes
         * Assumes start < end. The result may be negative, but it
         * is to be treated as an unsigned value.
         */
        private fun parseInt(source: CharArray, start: Int, end: Int): Int {
            var start = start
            var result = Character.digit(source[start++], 10)
            if (result == -1) {
                throw NumberFormatException(String(source))
            }
            for (index in start until end) {
                val nextVal = Character.digit(source[index], 10)
                if (nextVal == -1) {
                    throw NumberFormatException(String(source))
                }
                result = 10 * result + nextVal
            }
            return result
        }

        /**
         * Takes an array a representing a negative 2's-complement number and
         * returns the minimal (no leading zero bytes) unsigned whose value is -a.
         */
        private fun makePositive(a: ByteArray): IntArray {
            val byteLength = a.size

            // Find first non-sign (0xff) byte of input
            var keep: Int = 0
            while (keep < byteLength && a[keep].toInt() == -1) {
                keep += 1
            }


            /* Allocate output array.  If all non-sign bytes are 0x00, we must
             * allocate space for one extra output byte. */
            var k: Int = keep
            while (k < byteLength && a[k].toInt() == 0) {
                k += 1
            }

            val extraByte = if (k == byteLength) 1 else 0
            val intLength = (byteLength - keep + extraByte + 3).ushr(2)
            val result = IntArray(intLength)

            /* Copy one's complement of input into output, leaving extra
             * byte (if it exists) == 0x00 */
            var b = byteLength - 1
            for (i in intLength - 1 downTo 0) {
                result[i] = a[b--].toInt() and 0xff
                var numBytesToTransfer = minOf(3, b - keep + 1)
                if (numBytesToTransfer < 0)
                    numBytesToTransfer = 0
                var j = 8
                while (j <= 8 * numBytesToTransfer) {
                    result[i] = result[i] or (a[b--].toInt() and 0xff shl j)
                    j += 8
                }

                // Mask indicates which bits must be complemented
                val mask = (-1).ushr(8 * (3 - numBytesToTransfer))
                result[i] = result[i].inv() and mask
            }

            // Add one to one's complement to generate two's complement
            for (i in result.indices.reversed()) {
                result[i] = ((result[i].toLong() and LONG_MASK) + 1).toInt()
                if (result[i] != 0) {
                    break
                }
            }

            return result
        }

        /**
         * Takes an array a representing a negative 2's-complement number and
         * returns the minimal (no leading zero ints) unsigned whose value is -a.
         */
        private fun makePositive(a: IntArray): IntArray {
            // Find first non-sign (0xffffffff) int of input
            var keep: Int = 0
            while (keep < a.size && a[keep] == -1) {
                keep += 1
            }

            /* Allocate output array.  If all non-sign ints are 0x00, we must
             * allocate space for one extra output int. */
            var j: Int = keep
            while (j < a.size && a[j] == 0) {
                j += 1
            }
            val extraInt = if (j == a.size) 1 else 0
            val result = IntArray(a.size - keep + extraInt)

            /* Copy one's complement of input into output, leaving extra
             * int (if it exists) == 0x00 */
            for (i in keep until a.size) {
                result[i - keep + extraInt] = a[i].inv()
            }
            // Add one to one's complement to generate two's complement
            var i = result.size - 1
            while (++result[i] == 0) {
                i -= 1
            }

            return result
        }

        // Multiply x array times word y in place, and add word z
        private fun destructiveMulAdd(x: IntArray, y: Int, z: Int) {
            // Perform the multiplication word by word
            val ylong = y.toLong() and LONG_MASK
            val zlong = z.toLong() and LONG_MASK
            val len = x.size

            var carry: Long = 0
            for (i in len - 1 downTo 0) {
                val product = ylong * (x[i].toLong() and LONG_MASK) + carry
                x[i] = product.toInt()
                carry = product.ushr(32)
            }

            // Perform the addition
            var sum = (x[len - 1].toLong() and LONG_MASK) + zlong
            x[len - 1] = sum.toInt()
            carry = sum.ushr(32)
            for (i in len - 2 downTo 0) {
                sum = (x[i].toLong() and LONG_MASK) + carry
                x[i] = sum.toInt()
                carry = sum.ushr(32)
            }
        } // private fun destructiveMulAdd(x: IntArray, y: Int, z: Int)

        private fun randomBits(numBits: Int, rnd: Random): ByteArray {
            if (numBits < 0) {
                throw IllegalArgumentException("numBits must be non-negative")
            }
            val numBytes = ((numBits.toLong() + 7) / 8).toInt() // avoid overflow
            val randomBits = ByteArray(numBytes)

            // Generate random bytes and mask out any excess bits
            if (numBytes > 0) {
                rnd.nextBytes(randomBits)
                val excessBits = 8 * numBytes - numBits
                randomBits[0] = (randomBits[0].toInt() and ((1 shl 8 - excessBits) - 1)).toByte()
            }
            return randomBits
        }

        /**
         * Adds the contents of the int array x and long value val. This
         * method allocates a new int array to hold the answer and returns
         * a reference to that array.  Assumes x.length &gt; 0 and val is
         * non-negative
         */
        private fun add(x: IntArray, value: Long): IntArray {
            val y: IntArray
            var sum: Long = 0
            var xIndex = x.size
            val result: IntArray
            val highWord = value.ushr(32).toInt()
            if (highWord == 0) {
                result = IntArray(xIndex)
                sum = (x[--xIndex].toLong() and LONG_MASK) + value
                result[xIndex] = sum.toInt()
            } else {
                if (xIndex == 1) {
                    result = IntArray(2)
                    sum = value + (x[0].toLong() and LONG_MASK)
                    result[1] = sum.toInt()
                    result[0] = sum.ushr(32).toInt()
                    return result
                } else {
                    result = IntArray(xIndex)
                    sum = (x[--xIndex].toLong() and LONG_MASK) + (value and LONG_MASK)
                    result[xIndex] = sum.toInt()
                    sum = (x[--xIndex].toLong() and LONG_MASK) +
                            (highWord.toLong() and LONG_MASK) + sum.ushr(32)
                    result[xIndex] = sum.toInt()
                }
            }
            // Copy remainder of longer number while carry propagation is required
            var carry = sum.ushr(32) != 0L
            while (xIndex > 0 && carry) {
                xIndex -= 1
                result[xIndex] = x[xIndex] + 1
                carry = result[xIndex] == 0
            }
            // Copy remainder of longer number
            while (xIndex > 0) {
                xIndex -= 1
                result[xIndex] = x[xIndex]
            }
            // Grow result if necessary
            if (carry) {
                val bigger = IntArray(result.size + 1)
                result.copyInto(bigger, 1, 0, result.size)
                bigger[0] = 0x01
                return bigger
            }
            return result
        }

        /**
         * Adds the contents of the int arrays x and y. This method allocates
         * a new int array to hold the answer and returns a reference to that
         * array.
         */
        private fun add(x: IntArray, y: IntArray): IntArray {
            var x = x
            var y = y
            // If x is shorter, swap the two arrays
            if (x.size < y.size) {
                val tmp = x
                x = y
                y = tmp
            }

            var xIndex = x.size
            var yIndex = y.size
            val result = IntArray(xIndex)
            var sum: Long = 0
            if (yIndex == 1) {
                sum = (x[--xIndex].toLong() and LONG_MASK) + (y[0].toLong() and LONG_MASK)
                result[xIndex] = sum.toInt()
            } else {
                // Add common parts of both numbers
                while (yIndex > 0) {
                    sum = (x[--xIndex].toLong() and LONG_MASK) +
                            (y[--yIndex].toLong() and LONG_MASK) + sum.ushr(32)
                    result[xIndex] = sum.toInt()
                }
            }
            // Copy remainder of longer number while carry propagation is required
            var carry = sum.ushr(32) != 0L
            while (xIndex > 0 && carry) {
                xIndex -= 1
                result[xIndex] = x[xIndex] + 1
                carry = result[xIndex] == 0
            }

            // Copy remainder of longer number
            while (xIndex > 0) {
                xIndex -= 1
                result[xIndex] = x[xIndex]
            }

            // Grow result if necessary
            if (carry) {
                val bigger = IntArray(result.size + 1)
                result.copyInto(bigger, 1, 0, result.size)
                bigger[0] = 0x01
                return bigger
            }
            return result
        }

        private fun subtract(value: Long, little: IntArray): IntArray {
            val highWord = value.ushr(32).toInt()
            if (highWord == 0) {
                val result = IntArray(1)
                result[0] = (value - (little[0].toLong() and LONG_MASK)).toInt()
                return result
            } else {
                val result = IntArray(2)
                if (little.size == 1) {
                    val difference = (value.toInt().toLong() and LONG_MASK) -
                            (little[0].toLong() and LONG_MASK)
                    result[1] = difference.toInt()
                    // Subtract remainder of longer number while borrow propagates
                    val borrow = difference shr 32 != 0L
                    if (borrow) {
                        result[0] = highWord - 1
                    } else {        // Copy remainder of longer number
                        result[0] = highWord
                    }
                    return result
                } else { // little.length == 2
                    var difference = (value.toInt().toLong() and LONG_MASK) -
                            (little[1].toLong() and LONG_MASK)
                    result[1] = difference.toInt()
                    difference = (highWord.toLong() and LONG_MASK) -
                            (little[0].toLong() and LONG_MASK) + (difference shr 32)
                    result[0] = difference.toInt()
                    return result
                }
            }
        }

        /**
         * Subtracts the contents of the second argument (val) from the
         * first (big).  The first int array (big) must represent a larger number
         * than the second.  This method allocates the space necessary to hold the
         * answer.
         * assumes val &gt;= 0
         */
        private fun subtract(big: IntArray, value: Long): IntArray {
            val highWord = value.ushr(32).toInt()
            var bigIndex = big.size
            val result = IntArray(bigIndex)
            var difference: Long = 0

            if (highWord == 0) {
                difference = (big[--bigIndex].toLong() and LONG_MASK) - value
                result[bigIndex] = difference.toInt()
            } else {
                difference = (big[--bigIndex].toLong() and LONG_MASK) - (value and LONG_MASK)
                result[bigIndex] = difference.toInt()
                difference = (big[--bigIndex].toLong() and LONG_MASK) -
                        (highWord.toLong() and LONG_MASK) + (difference shr 32)
                result[bigIndex] = difference.toInt()
            }

            // Subtract remainder of longer number while borrow propagates
            var borrow = difference shr 32 != 0L
            while (bigIndex > 0 && borrow) {
                bigIndex -= 1
                result[bigIndex] = big[bigIndex] - 1
                borrow = result[bigIndex] == -1
            }

            // Copy remainder of longer number
            while (bigIndex > 0) {
                bigIndex -= 1
                result[bigIndex] = big[bigIndex]
            }
            return result
        }

        /**
         * Subtracts the contents of the second int arrays (little) from the
         * first (big).  The first int array (big) must represent a larger number
         * than the second.  This method allocates the space necessary to hold the
         * answer.
         */
        private fun subtract(big: IntArray, little: IntArray): IntArray {
            var bigIndex = big.size
            val result = IntArray(bigIndex)
            var littleIndex = little.size
            var difference: Long = 0

            // Subtract common parts of both numbers
            while (littleIndex > 0) {
                difference = (big[--bigIndex].toLong() and LONG_MASK) -
                        (little[--littleIndex].toLong() and LONG_MASK) + (difference shr 32)
                result[bigIndex] = difference.toInt()
            }

            // Subtract remainder of longer number while borrow propagates
            var borrow = difference shr 32 != 0L
            while (bigIndex > 0 && borrow) {
                bigIndex -= 1
                result[bigIndex] = big[bigIndex] - 1
                borrow = result[bigIndex] == -1
            }

            // Copy remainder of longer number
            while (bigIndex > 0) {
                bigIndex -= 1
                result[bigIndex] = big[bigIndex]
            }
            return result
        }

        private fun reportOverflow() {
            throw ArithmeticException("BigInteger would overflow supported range")
        }
    } // companion object

    /**
     * Converts this BigInteger to an `int`.  This
     * conversion is analogous to a
     * *narrowing primitive conversion* from `long` to
     * `int` as defined in section 5.1.3 of
     * <cite>The Java Language Specification</cite>:
     * if this BigInteger is too big to fit in an
     * `int`, only the low-order 32 bits are returned.
     * Note that this conversion can lose information about the
     * overall magnitude of the BigInteger value as well as return a
     * result with the opposite sign.
     *
     * @return this BigInteger converted to an `int`.
     * @see .intValueExact
     */
    override fun toInt(): Int {
        return getInt(0)
    }

    /**
     * Converts this BigInteger to a `long`.  This
     * conversion is analogous to a
     * *narrowing primitive conversion* from `long` to
     * `int` as defined in section 5.1.3 of
     * <cite>The Java Language Specification</cite>:
     * if this BigInteger is too big to fit in a
     * `long`, only the low-order 64 bits are returned.
     * Note that this conversion can lose information about the
     * overall magnitude of the BigInteger value as well as return a
     * result with the opposite sign.
     *
     * @return this BigInteger converted to a `long`.
     * @see .longValueExact
     */
    override fun toLong(): Long {
        var result: Long = 0

        for (i in 1 downTo 0) {
            result = (result shl 32) + (getInt(i).toLong() and LONG_MASK)
        }
        return result
    }

    override fun toShort(): Short {
        return toInt().toShort()
    }

    override fun toByte(): Byte {
        return toInt().toByte()
    }

    override fun toFloat(): Float {
        return toLong().toFloat()
    }

    override fun toDouble(): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toChar(): Char {
        return toInt().toChar()
    }

    /**
     * Compares this BigInteger with the specified BigInteger.  This
     * method is provided in preference to individual methods for each
     * of the six boolean comparison operators (&lt;, ==,
     * &gt;, &gt;=, !=, &lt;=).  The suggested
     * idiom for performing these comparisons is: `(x.compareTo(y)` &lt;*op*&gt; `0)`, where
     * &lt;*op*&gt; is one of the six comparison operators.
     *
     * @param  other BigInteger to which this BigInteger is to be compared.
     * @return -1, 0 or 1 as this BigInteger is numerically less than, equal
     * to, or greater than `other`.
     */
    override fun compareTo(other: BigInteger): Int {
        if (signum == other.signum) {
            return when (signum) {
                1 -> compareMagnitude(other)
                -1 -> other.compareMagnitude(this)
                else -> 0
            }
        }
        return if (signum > other.signum) 1 else -1
    }

    /**
     * Returns the number of bits in the minimal two's-complement
     * representation of this BigInteger, *excluding* a sign bit.
     * For positive BigIntegers, this is equivalent to the number of bits in
     * the ordinary binary representation.  (Computes
     * `(ceil(log2(this < 0 ? -this : this+1)))`.)
     *
     * @return number of bits in the minimal two's-complement
     * representation of this BigInteger, *excluding* a sign bit.
     */
    fun bitLength(): Int {
        val m = this.mag
        val len = m.size
        if (len == 0) {
            return 0 // offset by one to initialize
        } else {
            // Calculate the bit length of the magnitude
            val magBitLength = (len - 1 shl 5) + bitLengthForInt(this.mag[0])
            if (this.signum < 0) {
                // Check if magnitude is a power of two
                var pow2 = Integers.bitCount(mag[0]) == 1
                var i = 1
                while (i < len && pow2) {
                    pow2 = this.mag[i] == 0
                    i += 1
                }

                return if (pow2) magBitLength - 1 else magBitLength
            } else {
                return magBitLength
            }
        }
    }

    /**
     * Returns a BigInteger whose value is `(-this)`.
     *
     * @return `-this`
     */
    fun negate(): BigInteger {
        return BigInteger(this.mag, -this.signum)
    }

    /**
     * Returns a BigInteger whose value is the absolute value of this
     * BigInteger.
     *
     * @return `abs(this)`
     */
    fun abs(): BigInteger {
        return if (this.signum >= 0) this else this.negate()
    }

    /**
     * Returns the signum function of this BigInteger.
     *
     * @return -1, 0 or 1 as the value of this BigInteger is negative, zero or
     * positive.
     */
    fun signum(): Int {
        return this.signum
    }

    /**
     * Returns a BigInteger whose value is `(this + val)`.
     *
     * @param  addend value to be added to this BigInteger.
     * @return `this + val`
     */
    fun add(addend: BigInteger): BigInteger {
        if (addend.signum == 0) {
            return this
        }
        if (this.signum == 0) {
            return addend
        }
        if (addend.signum == this.signum) {
            return BigInteger(add(this.mag, addend.mag), this.signum)
        }
        val cmp = compareMagnitude(addend)
        if (cmp == 0) {
            return ZERO
        }
        var resultMag = if (cmp > 0) subtract(this.mag, addend.mag)
                else subtract(addend.mag, this.mag)
        resultMag = trustedStripLeadingZeroInts(resultMag)

        return BigInteger(resultMag, if (cmp == this.signum) 1 else -1)
    }

    /**
     * Returns a BigInteger whose value is `(this - val)`.
     *
     * @param  value value to be subtracted from this BigInteger.
     * @return `this - val`
     */
    fun subtract(value: BigInteger): BigInteger {
        if (value.signum == 0) {
            return this
        }
        if (this.signum == 0) {
            return value.negate()
        }
        if (value.signum != this.signum) {
            return BigInteger(add(this.mag, value.mag), this.signum)
        }
        val cmp = compareMagnitude(value)
        if (cmp == 0) {
            return ZERO
        }
        var resultMag: IntArray = if (cmp > 0) subtract(this.mag, value.mag)
                else subtract(value.mag, this.mag)
        resultMag = trustedStripLeadingZeroInts(resultMag)
        return BigInteger(resultMag, if (cmp == this.signum) 1 else -1)
    }

    /**
     * Package private methods used by BigDecimal code to add a BigInteger
     * with a long. Assumes val is not equal to INFLATED.
     */
    internal fun add(value: Long): BigInteger {
        if (value == 0L) {
            return this
        }
        if (this.signum == 0) {
            return valueOf(value)
        }
        if (Integers.signum(value) == this.signum) {
            return BigInteger(add(this.mag, value.absoluteValue), this.signum)
        }
        val cmp = compareMagnitude(value)
        if (cmp == 0) {
            return ZERO
        }
        var resultMag = if (cmp > 0) subtract(this.mag, value.absoluteValue)
                else subtract(value.absoluteValue, this.mag)
        resultMag = trustedStripLeadingZeroInts(resultMag)
        return BigInteger(resultMag, if (cmp == signum) 1 else -1)
    }

    /**
     * Compares the magnitude array of this BigInteger with the specified
     * BigInteger's. This is the version of compareTo ignoring sign.
     *
     * @param value BigInteger whose magnitude array to be compared.
     * @return -1, 0 or 1 as this magnitude array is less than, equal to or
     * greater than the magnitude array for the specified BigInteger's.
     */
    internal fun compareMagnitude(value: BigInteger): Int {
        val m1 = this.mag
        val len1 = m1.size
        val m2 = value.mag
        val len2 = m2.size
        if (len1 < len2) {
            return -1
        }
        if (len1 > len2) {
            return 1
        }
        for (i in 0 until len1) {
            val a = m1[i]
            val b = m2[i]
            if (a != b) {
                return if (a.toLong() and LONG_MASK < b.toLong() and LONG_MASK) -1 else 1
            }
        }
        return 0
    }

    /**
     * Version of compareMagnitude that compares magnitude with long value.
     * val can't be Long.MIN_VALUE.
     */
    internal fun compareMagnitude(value: Long): Int {
        var longValue = value
        if (longValue == Long.MIN_VALUE) {
            throw AssertionError("value == " + Long.MIN_VALUE)
        }
        val m1 = this.mag
        val len = m1.size
        if (len > 2) {
            return 1
        }
        if (longValue < 0) {
            longValue = -longValue
        }
        val highWord = longValue.ushr(32).toInt()
        if (highWord == 0) {
            if (len < 1) {
                return -1
            }
            if (len > 1) {
                return 1
            }
            val a = m1[0]
            val b = longValue.toInt()
            return if (a != b) {
                if (a.toLong() and LONG_MASK < b.toLong() and LONG_MASK) -1 else 1
            } else 0
        } else {
            if (len < 2) {
                return -1
            }
            var a = m1[0]
            var b = highWord
            if (a != b) {
                return if (a.toLong() and LONG_MASK < b.toLong() and LONG_MASK) -1 else 1
            }
            a = m1[1]
            b = longValue.toInt()
            return if (a != b) {
                if (a.toLong() and LONG_MASK < b.toLong() and LONG_MASK) -1 else 1
            } else 0
        }
    }

    /**
     * Throws an `ArithmeticException` if the `BigInteger` would be
     * out of the supported range.
     *
     * @throws ArithmeticException if `this` exceeds the supported range.
     */
    private fun checkRange() {
        if (this.mag.size > MAX_MAG_LENGTH || this.mag.size == MAX_MAG_LENGTH && mag[0] < 0) {
            reportOverflow()
        }
    }

    /**
     * Returns the length of the two's complement representation in ints,
     * including space for at least one sign bit.
     */
    private fun intLength(): Int {
        return bitLength().ushr(5) + 1
    }

    /* Returns sign bit */
    private fun signBit(): Int {
        return if (signum < 0) 1 else 0
    }

    /* Returns an int of sign bits */
    private fun signInt(): Int {
        return if (signum < 0) -1 else 0
    }

    /**
     * Returns the specified int of the little-endian two's complement
     * representation (int 0 is the least significant).  The int number can
     * be arbitrarily high (values are logically preceded by infinitely many
     * sign ints).
     */
    private fun getInt(n: Int): Int {
        if (n < 0) {
            return 0
        }
        if (n >= this.mag.size) {
            return signInt()
        }
        val magInt = this.mag[this.mag.size - n - 1]

        return if (this.signum >= 0)
            magInt
        else
            if (n <= firstNonzeroIntNum()) -magInt else magInt.inv()
    }

    /**
     * Returns the index of the int that contains the first nonzero int in the
     * little-endian binary representation of the magnitude (int 0 is the
     * least significant). If the magnitude is zero, return value is undefined.
     */
    private fun firstNonzeroIntNum(): Int {
        val mlen = this.mag.size
        var i = mlen - 1
        while (i >= 0 && this.mag[i] == 0) {
            i -= 1
        }
        return mlen - i - 1
    }

}