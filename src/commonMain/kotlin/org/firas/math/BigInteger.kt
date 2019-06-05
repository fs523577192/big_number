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

import org.firas.lang.Character
import org.firas.util.Integers
import kotlin.js.JsName
import kotlin.jvm.JvmStatic
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
    internal var signum: Int = 0

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
    @JsName("BigInteger_initWithByteArray")
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
    @JsName("BigInteger_initWithSignAndByteArray")
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
    @JsName("BigInteger_initWithStringAndRadix")
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
            cursor += 1
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
        while (cursor < len) {
            group = value.substring(cursor, cursor + digitsPerInt[radix])
            cursor += digitsPerInt[radix]
            val groupVal = group.toInt(radix)
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
            cursor += 1
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
    @JsName("BigInteger_initWithSignAndMagnitude")
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
    @JsName("BigInteger_initWithSignAndIntArray")
    internal constructor(signum: Int, magnitude: IntArray) {
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
        if (this.mag.size >= MAX_MAG_LENGTH) {
            checkRange()
        }
    }

    /**
     * Constructs a BigInteger with the specified value, which may not be zero.
     */
    private constructor(longValue: Long) {
        var value = longValue
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
         * Initialize static constant array when class is loaded.
         *
         * Should place before ONE, TWO, TEN and NEGATIVE_ONE,
         * or posConst and negConst are still null when valueOf
         * is called
         */
        private const val MAX_CONSTANT = 16

        @JvmStatic
        private val posConst = Array(MAX_CONSTANT + 1) {
            BigInteger(intArrayOf(it), 1)
        }

        @JvmStatic
        private val negConst = Array(MAX_CONSTANT + 1) {
            BigInteger(intArrayOf(it), -1)
        }

        /**
         * The BigInteger constant zero.
         *
         * @since   Java 1.2
         */
        @JvmStatic
        val ZERO = BigInteger(IntArray(0), 0)

        /**
         * The BigInteger constant one.
         *
         * @since   Java 1.2
         */
        @JvmStatic
        val ONE = valueOf(1)

        /**
         * The BigInteger constant two.
         *
         * @since   Java 9
         */
        @JvmStatic
        val TWO = valueOf(2)

        /**
         * The BigInteger constant -1.  (Not exported.)
         */
        @JvmStatic
        private val NEGATIVE_ONE = valueOf(-1)

        /**
         * The BigInteger constant ten.
         *
         * @since   Java 1.5
         */
        @JvmStatic
        val TEN = valueOf(10)

        /**
         * This mask is used to obtain the value of an int as if it were unsigned.
         */
        internal const val LONG_MASK = 0xffffffffL

        /**
         * This constant limits `mag.length` of BigIntegers to the supported
         * range.
         */
        private const val MAX_MAG_LENGTH = Int.MAX_VALUE / Int.SIZE_BITS + 1 // (1 << 26)

        /**
         * The threshold value for using Schoenhage recursive base conversion. If
         * the number of ints in the number are larger than this value,
         * the Schoenhage algorithm will be used.  In practice, it appears that the
         * Schoenhage routine is faster for any threshold down to 2, and is
         * relatively flat for thresholds between 2-25, so this choice may be
         * varied within this range for very small effect.
         */
        private const val SCHOENHAGE_BASE_CONVERSION_THRESHOLD = 20

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
        @JvmStatic
        private val digitsPerLong = intArrayOf(0, 0, 62, 39, 31,
                27, 24, 22, 20, 19,
                18, 18, 17, 17, 16,
                16, 15, 15, 15, 14,
                14, 14, 14, 13, 13,
                13, 13, 13, 13, 12,
                12, 12, 12, 12, 12,
                12, 12)

        @JvmStatic
        private val longRadix = arrayOf<BigInteger>(ZERO, ZERO, // The first two item is useless
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
        @JvmStatic
        private val digitsPerInt = intArrayOf(0, 0, 30, 19, 15,
                13, 11, 11, 10, 9,
                9, 8, 8, 8, 8,
                7, 7, 7, 7, 7,
                7, 7, 6, 6, 6,
                6, 6, 6, 6, 6,
                6, 6, 6, 6, 6,
                6, 5)

        @JvmStatic
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
        @JvmStatic
        private val bitsPerDigit = longArrayOf(0, 0, 1024, 1624, 2048,
                2378, 2648, 2875, 3072, 3247,
                3402, 3543, 3672, 3790, 3899,
                4001, 4096, 4186, 4271, 4350,
                4426, 4498, 4567, 4633, 4696,
                4756, 4814, 4870, 4923, 4975,
                5025, 5074, 5120, 5166, 5210,
                5253, 5295)

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
        @JsName("fromLong")
        @JvmStatic
        fun valueOf(value: Long): BigInteger {
            // If -MAX_CONSTANT < val < MAX_CONSTANT, return stashed constant
            return when (value) {
                0L -> ZERO
                in 1..MAX_CONSTANT -> posConst[value.toInt()]
                in -MAX_CONSTANT..-1 -> negConst[(-value).toInt()]
                else -> BigInteger(value)
            }
        }

        /**
         * Translates the decimal String representation of a BigInteger into a
         * BigInteger.  The String representation consists of an optional minus
         * sign followed by a sequence of one or more decimal digits.  The
         * character-to-digit mapping is provided by `Character.digit`.
         * The String may not contain any extraneous characters (whitespace, for
         * example).
         *
         * @param str decimal String representation of BigInteger.
         * @throws NumberFormatException `str` is not a valid representation
         * of a BigInteger.
         * @see Character.digit
         */
        @JsName("fromString")
        @JvmStatic
        fun valueOf(str: String): BigInteger {
            return valueOf(str, 10)
        }

        /**
         * Translates the String representation of a BigInteger in the
         * specified radix into a BigInteger.  The String representation
         * consists of an optional minus or plus sign followed by a
         * sequence of one or more digits in the specified radix.  The
         * character-to-digit mapping is provided by `Character.digit`.  The String may not contain any extraneous
         * characters (whitespace, for example).
         *
         * @param str String representation of BigInteger.
         * @param radix radix to be used in interpreting `str`.
         * @throws NumberFormatException `str` is not a valid representation
         * of a BigInteger in the specified radix, or `radix` is
         * outside the range from [Character.MIN_RADIX] to
         * [Character.MAX_RADIX], inclusive.
         * @see Character.digit
         */
        @JsName("fromStringAndRadix")
        @JvmStatic
        fun valueOf(str: String, radix: Int): BigInteger {
            var cursor = 0
            val numDigits: Int
            val len = str.length

            if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
                throw NumberFormatException("Radix out of range")
            }
            if (len == 0) {
                throw NumberFormatException("Zero length BigInteger")
            }

            // Check for at most one leading sign
            var sign = 1
            val index1 = str.lastIndexOf('-')
            val index2 = str.lastIndexOf('+')
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
            while (cursor < len && Character.digit(str[cursor], radix) == 0) {
                cursor += 1
            }

            if (cursor == len) {
                return ZERO
            }

            numDigits = len - cursor

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
            val temp = cursor + firstGroupLen
            var group = str.substring(cursor, temp)
            cursor = temp
            magnitude[numWords - 1] = group.toInt(radix)
            if (magnitude[numWords - 1] < 0) {
                throw NumberFormatException("Illegal digit")
            }

            // Process remaining digit groups
            val superRadix = intRadix[radix]
            while (cursor < len) {
                val temp = cursor + digitsPerInt[radix]
                group = str.substring(cursor, temp)
                cursor = temp
                val groupVal = group.toInt(radix)
                if (groupVal < 0) {
                    throw NumberFormatException("Illegal digit")
                }
                destructiveMulAdd(magnitude, superRadix, groupVal)
            }
            // Required for cases where the array was overallocated.
            val mag = trustedStripLeadingZeroInts(magnitude)
            val result = BigInteger(sign, mag)
            if (mag.size >= MAX_MAG_LENGTH) {
                result.checkRange()
            }
            return result
        }

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
        @JsName("fromRandom")
        @JvmStatic
        fun fromRandom(numBits: Int, rnd: Random): BigInteger {
            return BigInteger(1, randomBits(numBits, rnd))
        }

        /**
         * Returns a BigInteger with the given two's complement representation.
         * Assumes that the input array will not be modified (the returned
         * BigInteger will reference the input array if feasible).
         */
        @JvmStatic
        private fun valueOf(value: IntArray): BigInteger {
            return if (value[0] > 0) BigInteger(value, 1) else BigInteger(value)
        }

        /**
         * The cache of powers of each radix.  This allows us to not have to
         * recalculate powers of radix^(2^n) more than once.  This speeds
         * Schoenhage recursive base conversion significantly.
         */
        @JvmStatic
        private var powerCache: Array<Array<BigInteger>> = Array(Character.MAX_RADIX + 1) {
            if (it < Character.MIN_RADIX) arrayOf() else arrayOf(BigInteger.valueOf(it.toLong()))
        }

        /** The cache of logarithms of radices for base conversion.  */
        @JvmStatic
        private var logCache: DoubleArray = DoubleArray(Character.MAX_RADIX + 1) {
            if (it < Character.MIN_RADIX) 0.0 else kotlin.math.ln(it.toDouble())
        }

        /** The natural log of 2.  This is used in computing cache indices.  */
        @JvmStatic
        private val LOG_TWO = kotlin.math.ln(2.0)

        /* zero[i] is a string of i consecutive zeros. */
        @JvmStatic
        private val zeros = Array(64) {
            "000000000000000000000000000000000000000000000000000000000000000".substring(0, it)
        }

        @JvmStatic
        private val bnExpModThreshTable = intArrayOf(7, 25, 81, 241, 673, 1793, Int.MAX_VALUE) // Sentinel

        /**
         * Package private method to return bit length for an integer.
         */
        @JsName("bitLengthForInt")
        @JvmStatic
        internal fun bitLengthForInt(n: Int): Int {
            return 32 - Integers.numberOfLeadingZeros(n)
        }

        /**
         * Calculate bitlength of contents of the first len elements an int array,
         * assuming there are no leading zero ints.
         */
        @JvmStatic
        private fun bitLength(value: IntArray, len: Int): Int {
            return if (len == 0) 0 else (len - 1 shl 5) + bitLengthForInt(value[0])
        }

        /**
         * Returns a copy of the input array stripped of any leading zero bytes.
         */
        @JvmStatic
        private fun stripLeadingZeroInts(value: IntArray): IntArray {
            val vlen = value.size

            // Find first nonzero byte
            var keep = 0
            while (keep < vlen && value[keep] == 0) {
                keep += 1
            }
            return value.copyOfRange(keep, vlen)
        }

        /**
         * Returns the input array stripped of any leading zero bytes.
         * Since the source is trusted the copying may be skipped.
         */
        @JsName("trustedStripLeadingZeroInts")
        @JvmStatic
        internal fun trustedStripLeadingZeroInts(value: IntArray): IntArray {
            val vlen = value.size

            // Find first nonzero byte
            var keep = 0
            while (keep < vlen && value[keep] == 0) {
                keep += 1
            }
            return if (keep == 0) value else value.copyOfRange(keep, vlen)
        }

        /**
         * Returns a copy of the input array stripped of any leading zero bytes.
         */
        @JvmStatic
        private fun stripLeadingZeroBytes(a: ByteArray): IntArray {
            val byteLength = a.size

            // Find first nonzero byte
            var keep = 0
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
        @JvmStatic
        private fun parseInt(source: CharArray, start: Int, end: Int): Int {
            var startIndex = start
            var result = Character.digit(source[startIndex], 10)
            startIndex += 1
            if (result == -1) {
                throw NumberFormatException(String(source))
            }
            for (index in startIndex until end) {
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
        @JvmStatic
        private fun makePositive(a: ByteArray): IntArray {
            val byteLength = a.size

            // Find first non-sign (0xff) byte of input
            var keep = 0
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
        } // private fun makePositive(a: ByteArray): IntArray

        /**
         * Takes an array a representing a negative 2's-complement number and
         * returns the minimal (no leading zero ints) unsigned whose value is -a.
         */
        @JvmStatic
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
        } // private fun makePositive(a: IntArray): IntArray

        // Multiply x array times word y in place, and add word z
        @JvmStatic
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

        @JvmStatic
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
         * Returns a magnitude array whose value is `(mag << n)`.
         * The shift distance, `n`, is considered unnsigned.
         * (Computes <tt>this * 2<sup>n</sup></tt>.)
         *
         * @param mag magnitude, the most-significant int (`mag[0]`) must be non-zero.
         * @param  n unsigned shift distance, in bits.
         * @return `mag << n`
         */
        @JvmStatic
        private fun shiftLeft(mag: IntArray, n: Int): IntArray {
            val nInts = n.ushr(5)
            val nBits = n and 0x1f
            val magLen = mag.size
            val newMag: IntArray?

            if (nBits == 0) {
                newMag = IntArray(magLen + nInts)
                mag.copyInto(newMag, 0, 0, magLen)
            } else {
                var i = 0
                val nBits2 = 32 - nBits
                val highBits = mag[0].ushr(nBits2)
                if (highBits != 0) {
                    newMag = IntArray(magLen + nInts + 1)
                    newMag[i] = highBits
                    i += 1
                } else {
                    newMag = IntArray(magLen + nInts)
                }
                var j = 0
                while (j < magLen - 1) {
                    newMag[i++] = mag[j++] shl nBits or mag[j].ushr(nBits2)
                }
                newMag[i] = mag[j] shl nBits
            }
            return newMag
        }

        /**
         * Left shift int array a up to len by n bits. Returns the array that
         * results from the shift since space may have to be reallocated.
         */
        @JvmStatic
        private fun leftShift(a: IntArray, len: Int, n: Int): IntArray {
            val nInts = n.ushr(5)
            val nBits = n and 0x1F
            val bitsInHighWord = bitLengthForInt(a[0])

            // If shift can be done without recopy, do so
            if (n <= 32 - bitsInHighWord) {
                AlgorithmUtils.primitiveLeftShift(a, len, nBits)
                return a
            } else { // Array must be resized
                return if (nBits <= 32 - bitsInHighWord) {
                    val result = IntArray(nInts + len)
                    a.copyInto(result, 0, 0, len)
                    AlgorithmUtils.primitiveLeftShift(result, result.size, nBits)
                    result
                } else {
                    val result = IntArray(nInts + len + 1)
                    a.copyInto(result, 0, 0, len)
                    AlgorithmUtils.primitiveRightShift(result, result.size, 32 - nBits)
                    result
                }
            } // private fun leftShift(a: IntArray, len: Int, n: Int): IntArray
        }

        /**
         * Adds the contents of the int array x and long value val. This
         * method allocates a new int array to hold the answer and returns
         * a reference to that array.  Assumes x.length &gt; 0 and val is
         * non-negative
         */
        @JvmStatic
        private fun add(x: IntArray, value: Long): IntArray {
            var sum: Long
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
        } // private fun add(x: IntArray, value: Long): IntArray

        /**
         * Adds the contents of the int arrays x and y. This method allocates
         * a new int array to hold the answer and returns a reference to that
         * array.
         */
        @JvmStatic
        private fun add(xArray: IntArray, yArray: IntArray): IntArray {
            var x = xArray
            var y = yArray
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
                xIndex -= 1
                sum = (x[xIndex].toLong() and LONG_MASK) + (y[0].toLong() and LONG_MASK)
                result[xIndex] = sum.toInt()
            } else {
                // Add common parts of both numbers
                while (yIndex > 0) {
                    xIndex -= 1
                    yIndex -= 1
                    sum = (x[xIndex].toLong() and LONG_MASK) +
                            (y[yIndex].toLong() and LONG_MASK) + sum.ushr(32)
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
        } // private fun add(x: IntArray, y: IntArray): IntArray

        @JvmStatic
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
                } else { // little.length == 2
                    var difference = (value.toInt().toLong() and LONG_MASK) -
                            (little[1].toLong() and LONG_MASK)
                    result[1] = difference.toInt()
                    difference = (highWord.toLong() and LONG_MASK) -
                            (little[0].toLong() and LONG_MASK) + (difference shr 32)
                    result[0] = difference.toInt()
                }
                return result
            }
        } // private fun subtract(value: Long, little: IntArray): IntArray

        /**
         * Subtracts the contents of the second argument (val) from the
         * first (big).  The first int array (big) must represent a larger number
         * than the second.  This method allocates the space necessary to hold the
         * answer.
         * assumes val &gt;= 0
         */
        @JvmStatic
        private fun subtract(big: IntArray, value: Long): IntArray {
            val highWord = value.ushr(32).toInt()
            var bigIndex = big.size
            val result = IntArray(bigIndex)
            var difference: Long

            if (highWord == 0) {
                difference = (big[--bigIndex].toLong() and LONG_MASK) - value
                result[bigIndex] = difference.toInt()
            } else {
                bigIndex -= 1
                difference = (big[bigIndex].toLong() and LONG_MASK) - (value and LONG_MASK)
                result[bigIndex] = difference.toInt()
                bigIndex -= 1
                difference = (big[bigIndex].toLong() and LONG_MASK) -
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
        } // private fun subtract(big: IntArray, value: Long): IntArray

        /**
         * Subtracts the contents of the second int arrays (little) from the
         * first (big).  The first int array (big) must represent a larger number
         * than the second.  This method allocates the space necessary to hold the
         * answer.
         */
        @JvmStatic
        private fun subtract(big: IntArray, little: IntArray): IntArray {
            var bigIndex = big.size
            val result = IntArray(bigIndex)
            var littleIndex = little.size
            var difference: Long = 0

            // Subtract common parts of both numbers
            while (littleIndex > 0) {
                bigIndex -= 1
                littleIndex -= 1
                difference = (big[bigIndex].toLong() and LONG_MASK) -
                        (little[littleIndex].toLong() and LONG_MASK) + (difference shr 32)
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
        } // private fun subtract(big: IntArray, little: IntArray): IntArray

        @JvmStatic
        private fun multiplyByInt(x: IntArray, y: Int, sign: Int): BigInteger {
            if (Integers.bitCount(y) == 1) {
                return BigInteger(shiftLeft(x, Integers.numberOfTrailingZeros(y)), sign)
            }
            val xlen = x.size
            var rmag = IntArray(xlen + 1)
            var carry: Long = 0
            val yl = y.toLong() and LONG_MASK
            var rstart = rmag.size - 1
            for (i in xlen - 1 downTo 0) {
                val product = (x[i].toLong() and LONG_MASK) * yl + carry
                rmag[rstart] = product.toInt()
                rstart -= 1
                carry = product.ushr(32)
            }
            if (carry == 0L) {
                rmag = rmag.copyOfRange(1, rmag.size)
            } else {
                rmag[rstart] = carry.toInt()
            }
            return BigInteger(rmag, sign)
        } // private fun multiplyByInt(x: IntArray, y: Int, sign: Int): BigInteger

        /**
         * Multiplies int arrays x and y to the specified lengths and places
         * the result into z. There will be no leading zeros in the resultant array.
         */
        @JvmStatic
        private fun multiplyToLen(x: IntArray, xlen: Int, y: IntArray, ylen: Int, z: IntArray?): IntArray {
            var z = z
            val xstart = xlen - 1
            val ystart = ylen - 1

            if (z == null || z.size < xlen + ylen) {
                z = IntArray(xlen + ylen)
            }
            var carry: Long = 0
            run {
                var j = ystart
                var k = ystart + 1 + xstart
                while (j >= 0) {
                    val product = (y[j].toLong() and LONG_MASK) * (x[xstart].toLong() and LONG_MASK) + carry
                    z[k] = product.toInt()
                    carry = product.ushr(32)
                    j -= 1
                    k -= 1
                }
            }
            z[xstart] = carry.toInt()

            for (i in xstart - 1 downTo 0) {
                carry = 0
                var j = ystart
                var k = ystart + 1 + i
                while (j >= 0) {
                    val product = (y[j].toLong() and LONG_MASK) * (x[i].toLong() and LONG_MASK) +
                            (z[k].toLong() and LONG_MASK) + carry
                    z[k] = product.toInt()
                    carry = product.ushr(32)
                    j -= 1
                    k -= 1
                }
                z[i] = carry.toInt()
            }
            return z
        } // private fun multiplyToLen(x: IntArray, xlen: Int, y: IntArray, ylen: Int, z: IntArray?): IntArray

        @JsName("javaIncrement")
        @JvmStatic
        internal fun javaIncrement(intArray: IntArray): IntArray {
            var integers = intArray
            var lastSum = 0
            var i = integers.size - 1
            while (i >= 0 && lastSum == 0) {
                integers[i] += 1
                lastSum = integers[i]
                i -= 1
            }
            if (lastSum == 0) {
                integers = IntArray(integers.size + 1)
                integers[0] = 1
            }
            return integers
        }

        /**
         * Converts the specified BigInteger to a string and appends to
         * `sb`.  This implements the recursive Schoenhage algorithm
         * for base conversions.
         *
         *
         * See Knuth, Donald,  _The Art of Computer Programming_, Vol. 2,
         * Answers to Exercises (4.4) Question 14.
         *
         * @param u      The number to convert to a string.
         * @param sb     The StringBuilder that will be appended to in place.
         * @param radix  The base to convert to.
         * @param digits The minimum number of digits to pad to.
         */
        @JvmStatic
        private fun toString(
            u: BigInteger, sb: StringBuilder, radix: Int,
            digits: Int
        ) {
            // If we're smaller than a certain threshold, use the smallToString
            // method, padding with leading zeroes when necessary.
            if (u.mag.size <= SCHOENHAGE_BASE_CONVERSION_THRESHOLD) {
                val s = u.smallToString(radix)

                // Pad with internal zeros if necessary.
                // Don't pad if we're at the beginning of the string.
                if (s.length < digits && sb.length > 0) {
                    for (i in s.length until digits) {
                        sb.append('0')
                    }
                }

                sb.append(s)
                return
            }

            val b = u.bitLength()
            val n: Int

            // Calculate a value for n in the equation radix^(2^n) = u
            // and subtract 1 from that value.  This is used to find the
            // cache index that contains the best value to divide u.
            n = kotlin.math.round(kotlin.math.ln(b * LOG_TWO / logCache[radix]) / LOG_TWO - 1.0).toInt()
            val v = getRadixConversionCache(radix, n)
            val results: Array<BigInteger> = u.divideAndRemainder(v)

            val expectedDigits = 1 shl n

            // Now recursively build the two halves of each number.
            toString(results[0], sb, radix, digits - expectedDigits)
            toString(results[1], sb, radix, expectedDigits)
        }

        /**
         * Returns the value radix^(2^exponent) from the cache.
         * If this value doesn't already exist in the cache, it is added.
         *
         *
         * This could be changed to a more complicated caching method using
         * `Future`.
         */
        @JvmStatic
        private fun getRadixConversionCache(radix: Int, exponent: Int): BigInteger {
            val cacheLine: Array<BigInteger> = powerCache[radix] // volatile read
            if (exponent < cacheLine.size) {
                return cacheLine[exponent]
            }

            val oldLength = cacheLine.size
            val cacheLine1 = cacheLine.copyOf(exponent + 1)
            for (i in oldLength..exponent) {
                cacheLine1[i] = cacheLine1[i - 1]!!.pow(2)
            }
            val cacheLine2 = cacheLine1.requireNoNulls()

            var pc = powerCache // volatile read again
            if (exponent >= pc[radix].size) {
                pc = pc.copyOf()
                pc[radix] = cacheLine2
                powerCache = pc // volatile write, publish
            }
            return cacheLine2[exponent]
        }

        @JvmStatic
        private fun reportOverflow() {
            throw ArithmeticException("BigInteger would overflow supported range")
        }
    } // companion object

    // --------
    // The following fields are stable variables. A stable variable's value
    // changes at most once from the default zero value to a non-zero stable
    // value. A stable value is calculated lazily on demand.

    /**
     * One plus the bitCount of this BigInteger. This is a stable variable.
     *
     * @see .bitCount
     */
    private var bitCountPlusOne: Int = 0

    /**
     * One plus the bitLength of this BigInteger. This is a stable variable.
     * (either value is acceptable).
     *
     * @see .bitLength
     */
    private var bitLengthPlusOne: Int = 0

    /**
     * Two plus the lowest set bit of this BigInteger. This is a stable variable.
     *
     * @see .getLowestSetBit
     */
    private var lowestSetBitPlusTwo: Int = 0

    /**
     * Two plus the index of the lowest-order int in the magnitude of this
     * BigInteger that contains a nonzero int. This is a stable variable. The
     * least significant int has int-number 0, the next int in order of
     * increasing significance has int-number 1, and so forth.
     *
     *
     * Note: never used for a BigInteger with a magnitude of zero.
     *
     * @see .firstNonzeroIntNum
     */
    private var firstNonzeroIntNumPlusTwo: Int = 0

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
        if (this.signum == 0) {
            return 0.0f
        }

        val exponent = (this.mag.size - 1).shl(5) + bitLengthForInt(this.mag[0]) - 1

        // exponent == floor(log2(abs(this)))
        if (exponent < Long.SIZE_BITS - 1) {
            return toLong().toFloat()
        } else if (exponent > FloatConsts.MAX_EXPONENT) {
            return if (this.signum > 0) Float.POSITIVE_INFINITY else Float.NEGATIVE_INFINITY
        }

        /*
         * We need the top SIGNIFICAND_WIDTH bits, including the "implicit"
         * one bit. To make rounding easier, we pick out the top
         * SIGNIFICAND_WIDTH + 1 bits, so we have one to help us round up or
         * down. twiceSignifFloor will contain the top SIGNIFICAND_WIDTH + 1
         * bits, and signifFloor the top SIGNIFICAND_WIDTH.
         *
         * It helps to consider the real number signif = abs(this) *
         * 2^(SIGNIFICAND_WIDTH - 1 - exponent).
         */
        val shift = exponent - FloatConsts.SIGNIFICAND_WIDTH

        var twiceSignifFloor: Int
        // twiceSignifFloor will be == abs().shiftRight(shift).intValue()
        // We do the shift into an int directly to improve performance.

        val nBits = shift and (Int.SIZE_BITS - 1)
        val nBits2 = Int.SIZE_BITS - nBits

        if (nBits == 0) {
            twiceSignifFloor = this.mag[0]
        } else {
            twiceSignifFloor = this.mag[0].ushr(nBits)
            if (twiceSignifFloor == 0) {
                twiceSignifFloor = this.mag[0].shl(nBits2) or this.mag[1].ushr(nBits)
            }
        }

        var f = twiceSignifFloor * (1L shl shift.and(Long.SIZE_BITS.shr(1) - 1)).toFloat()
        for (i in 1..shift.shr(5)) {
            f *= 1L.shl(Long.SIZE_BITS.shr(1)).toFloat()
        }
        return if (this.signum > 0) f else -f
    }

    override fun toDouble(): Double {
        if (this.signum == 0) {
            return 0.0
        }

        val exponent = (mag.size - 1).shl(5) + bitLengthForInt(mag[0]) - 1

        // exponent == floor(log2(abs(this))Double)
        if (exponent < Long.SIZE_BITS - 1) {
            return toLong().toDouble()
        } else if (exponent > DoubleConsts.MAX_EXPONENT) {
            return if (this.signum > 0) Double.POSITIVE_INFINITY else Double.NEGATIVE_INFINITY
        }

        /*
         * We need the top SIGNIFICAND_WIDTH bits, including the "implicit"
         * one bit. To make rounding easier, we pick out the top
         * SIGNIFICAND_WIDTH + 1 bits, so we have one to help us round up or
         * down. twiceSignifFloor will contain the top SIGNIFICAND_WIDTH + 1
         * bits, and signifFloor the top SIGNIFICAND_WIDTH.
         *
         * It helps to consider the real number signif = abs(this) *
         * 2^(SIGNIFICAND_WIDTH - 1 - exponent).
         */
        val shift = exponent - DoubleConsts.SIGNIFICAND_WIDTH

        val twiceSignifFloor: Long
        // twiceSignifFloor will be == abs().shiftRight(shift).longValue()
        // We do the shift into a long directly to improve performance.

        val nBits = shift and (Int.SIZE_BITS - 1)
        val nBits2 = Int.SIZE_BITS - nBits

        var highBits: Int
        var lowBits: Int
        if (nBits == 0) {
            highBits = this.mag[0]
            lowBits = this.mag[1]
        } else {
            highBits = this.mag[0].ushr(nBits)
            lowBits = this.mag[0].shl(nBits2) or this.mag[1].ushr(nBits)
            if (highBits == 0) {
                highBits = lowBits
                lowBits = this.mag[1].shl(nBits2) or this.mag[2].ushr(nBits)
            }
        }

        twiceSignifFloor = highBits.toLong().and(LONG_MASK).shl(32) or lowBits.toLong().and(LONG_MASK)

        var d = twiceSignifFloor * (1L shl shift.and(Long.SIZE_BITS.shr(1) - 1)).toDouble()
        for (i in 1..shift.shr(5)) {
            d *= 1L.shl(Long.SIZE_BITS.shr(1)).toDouble()
        }
        return if (this.signum > 0) d else -d
    }

    override fun toChar(): Char {
        return toInt().toChar()
    }

    /**
     * Converts this `BigInteger` to a `long`, checking
     * for lost information.  If the value of this `BigInteger`
     * is out of the range of the `long` type, then an
     * `ArithmeticException` is thrown.
     *
     * @return this `BigInteger` converted to a `long`.
     * @throws ArithmeticException if the value of `this` will
     * not exactly fit in a `long`.
     * @see BigInteger.toLong
     *
     * @since  Java 1.8
     */
    @JsName("longValueExact")
    fun longValueExact(): Long {
        return if (this.mag.size <= 2 && bitLength() <= 63)
            toLong()
        else
            throw ArithmeticException("BigInteger out of long range")
    }

    /**
     * Converts this `BigInteger` to an `int`, checking
     * for lost information.  If the value of this `BigInteger`
     * is out of the range of the `int` type, then an
     * `ArithmeticException` is thrown.
     *
     * @return this `BigInteger` converted to an `int`.
     * @throws ArithmeticException if the value of `this` will
     * not exactly fit in an `int`.
     * @see BigInteger.intValue
     *
     * @since  Java 1.8
     */
    @JsName("intValueExact")
    fun intValueExact(): Int {
        return if (this.mag.size <= 1 && bitLength() <= 31)
            toInt()
        else
            throw ArithmeticException("BigInteger out of int range")
    }

    /**
     * Converts this `BigInteger` to a `short`, checking
     * for lost information.  If the value of this `BigInteger`
     * is out of the range of the `short` type, then an
     * `ArithmeticException` is thrown.
     *
     * @return this `BigInteger` converted to a `short`.
     * @throws ArithmeticException if the value of `this` will
     * not exactly fit in a `short`.
     * @see BigInteger.shortValue
     *
     * @since  Java 1.8
     */
    @JsName("shortValueExact")
    fun shortValueExact(): Short {
        if (this.mag.size <= 1 && bitLength() <= 31) {
            val value = toInt()
            if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
                return toShort()
            }
        }
        throw ArithmeticException("BigInteger out of short range")
    }

    /**
     * Converts this `BigInteger` to a `byte`, checking
     * for lost information.  If the value of this `BigInteger`
     * is out of the range of the `byte` type, then an
     * `ArithmeticException` is thrown.
     *
     * @return this `BigInteger` converted to a `byte`.
     * @throws ArithmeticException if the value of `this` will
     * not exactly fit in a `byte`.
     * @see BigInteger.byteValue
     *
     * @since  Java 1.8
     */
    @JsName("byteValueExact")
    fun byteValueExact(): Byte {
        if (this.mag.size <= 1 && bitLength() <= 31) {
            val value = toInt()
            if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
                return toByte()
            }
        }
        throw ArithmeticException("BigInteger out of byte range")
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
    override operator fun compareTo(other: BigInteger): Int {
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
     * Returns the minimum of this BigInteger and `other`.
     *
     * @param  other value with which the minimum is to be computed.
     * @return the BigInteger whose value is the lesser of this BigInteger and
     * `other`.  If they are equal, either may be returned.
     */
    @JsName("min")
    fun min(other: BigInteger): BigInteger {
        return if (compareTo(other) < 0) this else other
    }

    /**
     * Returns the maximum of this BigInteger and `other`.
     *
     * @param  other value with which the maximum is to be computed.
     * @return the BigInteger whose value is the greater of this and
     * `other`.  If they are equal, either may be returned.
     */
    @JsName("max")
    fun max(other: BigInteger): BigInteger {
        return if (compareTo(other) > 0) this else other
    }

    /**
     * Compares this BigInteger with the specified Object for equality.
     *
     * @param  other Object to which this BigInteger is to be compared.
     * @return `true` if and only if the specified Object is a
     * BigInteger whose value is numerically equal to this BigInteger.
     */
    override fun equals(other: Any?): Boolean {
        // This test is just an optimization, which may or may not help
        if (other === this) {
            return true
        }
        if (other !is BigInteger) {
            return false
        }
        if (other.signum != this.signum) {
            return false
        }
        val m = this.mag
        val len = m.size
        val xm = other.mag
        if (len != xm.size) {
            return false
        }
        for (i in 0 until len) {
            if (xm[i] != m[i]) {
                return false
            }
        }
        return true
    }

    /**
     * Returns the hash code for this BigInteger.
     *
     * @return hash code for this BigInteger.
     */
    override fun hashCode(): Int {
        var hashCode = 0

        for (i in 0 until this.mag.size) {
            hashCode = 31 * hashCode + (this.mag[i].toLong() and LONG_MASK).toInt()
        }
        return hashCode * this.signum
    }

    /**
     * Returns the String representation of this BigInteger in the
     * given radix.  If the radix is outside the range from {@link
     * Character#MIN_RADIX} to {@link Character#MAX_RADIX} inclusive,
     * it will default to 10 (as is the case for
     * `Int.toString`.  The digit-to-character mapping
     * provided by {@code Character.forDigit} is used, and a minus
     * sign is prepended if appropriate.  (This representation is
     * compatible with the {@link #BigInteger(String, int) (String,
     * int)} constructor.)
     *
     * @param  radix  radix of the String representation.
     * @return String representation of this BigInteger in the given radix.
     * @see    Int#toString
     * @see    Character#forDigit
     * @see    #BigInteger(java.lang.String, int)
     */
    @JsName("toStringWithRadix")
    fun toString(radix: Int): String {
        if (this.signum == 0) {
            return "0"
        }
        var radix = radix
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
            radix = 10
        }
        // If it's small enough, use smallToString.
        if (mag.size <= SCHOENHAGE_BASE_CONVERSION_THRESHOLD) {
            return smallToString(radix)
        }
        // Otherwise use recursive toString, which requires positive arguments.
        // The results will be concatenated into this StringBuilder
        val sb = StringBuilder()
        return if (this.signum < 0) {
            toString(-this, sb, radix, 0)
            "-$sb"
        } else {
            toString(this, sb, radix, 0)
            sb.toString()
        }
    }

    /**
     * Returns the decimal String representation of this BigInteger.
     * The digit-to-character mapping provided by
     * `Character.forDigit` is used, and a minus sign is
     * prepended if appropriate.  (This representation is compatible
     * with the [(String)][.BigInteger] constructor, and
     * allows for String concatenation with Java's + operator.)
     *
     * @return decimal String representation of this BigInteger.
     * @see Character.forDigit
     *
     * @see .BigInteger
     */
    override fun toString(): String {
        return toString(10)
    }

    /** This method is used to perform toString when arguments are small. */
    private fun smallToString(radix: Int): String {
        if (this.signum == 0) {
            return "0"
        }

        // Compute upper bound on number of digit groups and allocate space
        val maxNumDigitGroups = (4 * this.mag.size + 6) / 7
        val digitGroup = Array<String>(maxNumDigitGroups) { "" }

        // Translate number to string, a digit group at a time
        var tmp = this.abs()
        var numGroups = 0
        while (tmp.signum != 0) {
            val d = longRadix[radix]

            val q = MutableBigInteger()
            val a = MutableBigInteger(tmp.mag)
            val b = MutableBigInteger(d.mag)
            val r = a.divide(b, q)
            val q2 = q.toBigInteger(tmp.signum * d.signum)
            val r2 = r.toBigInteger(tmp.signum * d.signum)

            digitGroup[numGroups] = r2.toLong().toString(radix)
            numGroups += 1
            tmp = q2
        }

        // Put sign (if any) and first digit group into result buffer
        val buf = StringBuilder(numGroups * digitsPerLong[radix] + 1)
        if (signum < 0) {
            buf.append('-')
        }
        buf.append(digitGroup[numGroups - 1])

        // Append remaining digit groups padded with leading zeros
        if (numGroups >= 2) {
            for (i in numGroups - 2..0) {
                // Prepend (any) leading zeros for this digit group
                val numLeadingZeros = digitsPerLong[radix] - digitGroup[i].length
                if (numLeadingZeros != 0) {
                    buf.append(zeros[numLeadingZeros])
                }
                buf.append(digitGroup[i])
            }
        }
        return buf.toString()
    }

    /**
     * Returns the number of bits in the minimal two's-complement
     * representation of this BigInteger, *excluding* a sign bit.
     * For positive BigIntegers, this is equivalent to the number of bits in
     * the ordinary binary representation.  For zero this method returns
     * `0`.  (Computes `(ceil(log2(this < 0 ? -this : this+1)))`.)
     *
     * @return number of bits in the minimal two's-complement
     * representation of this BigInteger, *excluding* a sign bit.
     */
    @JsName("bitLength")
    fun bitLength(): Int {
        var n = bitLengthPlusOne - 1
        if (n == -1) { // bitLength not initialized yet
            val m = mag
            val len = m.size
            if (len == 0) {
                n = 0 // offset by one to initialize
            } else {
                // Calculate the bit length of the magnitude
                val magBitLength = (len - 1 shl 5) + bitLengthForInt(mag[0])
                if (signum < 0) {
                    // Check if magnitude is a power of two
                    var pow2 = Integers.bitCount(mag[0]) == 1
                    var i = 1
                    while (i < len && pow2) {
                        pow2 = mag[i] == 0
                        i += 1
                    }

                    n = if (pow2) magBitLength - 1 else magBitLength
                } else {
                    n = magBitLength
                }
            }
            bitLengthPlusOne = n + 1
        }
        return n
    }

    /**
     * Returns the number of bits in the two's complement representation
     * of this BigInteger that differ from its sign bit.  This method is
     * useful when implementing bit-vector style sets atop BigIntegers.
     *
     * @return number of bits in the two's complement representation
     * of this BigInteger that differ from its sign bit.
     */
    @JsName("bitCount")
    fun bitCount(): Int {
        var bc = bitCountPlusOne - 1
        if (bc == -1) {  // bitCount not initialized yet
            bc = 0      // offset by one to initialize
            // Count the bits in the magnitude
            for (i in 0 until mag.size)
                bc += Integers.bitCount(mag[i])
            if (signum < 0) {
                // Count the trailing zeros in the magnitude
                var magTrailingZeroCount = 0
                var j: Int = mag.size - 1
                while (mag[j] == 0) {
                    magTrailingZeroCount += 32
                    j -= 1
                }
                magTrailingZeroCount += Integers.numberOfTrailingZeros(mag[j])
                bc += magTrailingZeroCount - 1
            }
            bitCountPlusOne = bc + 1
        }
        return bc
    }

    // ----==== Single Bit Operations ====----
    /**
     * Returns `true` if and only if the designated bit is set.
     * (Computes `((this & (1<<n)) != 0)`.)
     *
     * @param  n index of bit to test.
     * @return `true` if and only if the designated bit is set.
     * @throws ArithmeticException `n` is negative.
     */
    @JsName("testBit")
    fun testBit(n: Int): Boolean {
        if (n < 0) {
            throw ArithmeticException("Negative bit address")
        }
        return getInt(n.ushr(5)) and (1 shl (n and 31)) != 0
    }

    /**
     * Returns a BigInteger whose value is equivalent to this BigInteger
     * with the designated bit set.  (Computes `(this | (1<<n))`.)
     *
     * @param  n index of bit to set.
     * @return `this | (1<<n)`
     * @throws ArithmeticException `n` is negative.
     */
    @JsName("setBit")
    fun setBit(n: Int): BigInteger {
        if (n < 0) {
            throw ArithmeticException("Negative bit address")
        }
        val intNum = n.ushr(5)
        val result = IntArray(maxOf(intLength(), intNum + 2))

        for (i in result.indices) {
            result[result.size - i - 1] = getInt(i)
        }
        result[result.size - intNum - 1] = result[result.size - intNum - 1] or (1 shl (n and 31))

        return valueOf(result)
    }

    /**
     * Returns a BigInteger whose value is equivalent to this BigInteger
     * with the designated bit cleared.
     * (Computes `(this & ~(1<<n))`.)
     *
     * @param  n index of bit to clear.
     * @return `this & ~(1<<n)`
     * @throws ArithmeticException `n` is negative.
     */
    @JsName("clearBit")
    fun clearBit(n: Int): BigInteger {
        if (n < 0) {
            throw ArithmeticException("Negative bit address")
        }
        val intNum = n.ushr(5)
        val result = IntArray(maxOf(intLength(), (n + 1).ushr(5) + 1))

        for (i in result.indices) {
            result[result.size - i - 1] = getInt(i)
        }
        result[result.size - intNum - 1] = result[result.size - intNum - 1] and (1 shl (n and 31)).inv()

        return valueOf(result)
    }

    /**
     * Returns a BigInteger whose value is equivalent to this BigInteger
     * with the designated bit flipped.
     * (Computes `(this ^ (1<<n))`.)
     *
     * @param  n index of bit to flip.
     * @return `this ^ (1<<n)`
     * @throws ArithmeticException `n` is negative.
     */
    @JsName("flipBit")
    fun flipBit(n: Int): BigInteger {
        if (n < 0) {
            throw ArithmeticException("Negative bit address")
        }
        val intNum = n.ushr(5)
        val result = IntArray(maxOf(intLength(), intNum + 2))

        for (i in result.indices) {
            result[result.size - i - 1] = getInt(i)
        }
        result[result.size - intNum - 1] = result[result.size - intNum - 1] xor (1 shl (n and 31))

        return valueOf(result)
    }

    /**
     * Returns the index of the rightmost (lowest-order) one bit in this
     * BigInteger (the number of zero bits to the right of the rightmost
     * one bit).  Returns -1 if this BigInteger contains no one bits.
     * (Computes `(this == 0 ? -1 : log2(this & -this))`.)
     *
     * @return index of the rightmost one bit in this BigInteger.
     */
    @JsName("getLowestSetBit")
    fun getLowestSetBit(): Int {
        var lsb = lowestSetBitPlusTwo - 2
        if (lsb == -2) {  // lowestSetBit not initialized yet
            lsb = 0
            if (signum == 0) {
                lsb -= 1
            } else {
                // Search for lowest order nonzero int
                var i = 0
                var b: Int = getInt(i)
                while (b == 0) {
                    i += 1
                    b = getInt(i)
                }
                lsb += (i shl 5) + Integers.numberOfTrailingZeros(b)
            }
            lowestSetBitPlusTwo = lsb + 2
        }
        return lsb
    }

    // ----==== Shift Operations ====----
    /**
     * Returns a BigInteger whose value is `(this << n)`.
     * The shift distance, `n`, may be negative, in which case
     * this method performs a right shift.
     * (Computes <tt>floor(this * 2<sup>n</sup>)</tt>.)
     *
     * Rename from `shiftLeft` to `shl` to accord with `Int.shl` in Kotlin
     *
     * @param  n shift distance, in bits.
     * @return `this << n`
     * @see .shiftRight
     */
    infix fun shl(n: Int): BigInteger {
        return if (this.signum == 0) {
            ZERO
        } else if (n > 0) {
            BigInteger(shiftLeft(this.mag, n), this.signum)
        } else if (n == 0) {
            this
        } else {
            // Possible int overflow in (-n) is not a trouble,
            // because shiftRightImpl considers its argument unsigned
            shiftRightImpl(-n)
        }
    }

    /**
     * Returns a BigInteger whose value is `(this >> n)`.  Sign
     * extension is performed.  The shift distance, `n`, may be
     * negative, in which case this method performs a left shift.
     * (Computes <tt>floor(this / 2<sup>n</sup>)</tt>.)
     *
     * Rename from `shiftRight` to `shr` to accord with `Int.shr` in Kotlin
     *
     * @param  n shift distance, in bits.
     * @return `this >> n`
     * @see .shiftLeft
     */
    infix fun shr(n: Int): BigInteger {
        return if (this.signum == 0) {
            ZERO
        } else if (n > 0) {
            shiftRightImpl(n)
        } else if (n == 0) {
            this
        } else {
            // Possible int overflow in `-n` is not a trouble,
            // because shiftLeft considers its argument unsigned
            BigInteger(shiftLeft(this.mag, -n), this.signum)
        }
    }

    // ----==== Bitwise Operations ====----

    /**
     * Returns a BigInteger whose value is `(this & other)`.  (This
     * method returns a negative BigInteger if and only if this and other are
     * both negative.)
     *
     * @param other value to be AND'ed with this BigInteger.
     * @return `this & other`
     */
    infix fun and(other: BigInteger): BigInteger {
        val result = IntArray(maxOf(intLength(), other.intLength()))
        for (i in result.indices) {
            result[i] = getInt(result.size - i - 1) and other.getInt(result.size - i - 1)
        }
        return valueOf(result)
    }

    /**
     * Returns a BigInteger whose value is `(this | other)`.  (This method
     * returns a negative BigInteger if and only if either this or other is
     * negative.)
     *
     * @param other value to be OR'ed with this BigInteger.
     * @return `this | other`
     */
    infix fun or(other: BigInteger): BigInteger {
        val result = IntArray(maxOf(intLength(), other.intLength()))
        for (i in result.indices) {
            result[i] = getInt(result.size - i - 1) or other.getInt(result.size - i - 1)
        }
        return valueOf(result)
    }

    /**
     * Returns a BigInteger whose value is `(this ^ other)`.  (This method
     * returns a negative BigInteger if and only if exactly one of this and
     * other are negative.)
     *
     * @param other value to be XOR'ed with this BigInteger.
     * @return `this ^ other`
     */
    infix fun xor(other: BigInteger): BigInteger {
        val result = IntArray(maxOf(intLength(), other.intLength()))
        for (i in result.indices) {
            result[i] = getInt(result.size - i - 1) xor other.getInt(result.size - i - 1)
        }
        return valueOf(result)
    }

    /**
     * Returns a BigInteger whose value is `(~this)`.  (This method
     * returns a negative value if and only if this BigInteger is
     * non-negative.)
     *
     * @return `~this`
     */
    @JsName("not")
    operator fun not(): BigInteger {
        val result = IntArray(intLength())
        for (i in result.indices) {
            result[i] = getInt(result.size - i - 1).inv()
        }
        return valueOf(result)
    }

    /**
     * Returns a BigInteger whose value is `(this & ~other)`.  This
     * method, which is equivalent to `and(other.not())`, is provided as
     * a convenience for masking operations.  (This method returns a negative
     * BigInteger if and only if `this` is negative and `other` is
     * positive.)
     *
     * @param other value to be complemented and AND'ed with this BigInteger.
     * @return `this & ~other`
     */
    @JsName("andNot")
    fun andNot(other: BigInteger): BigInteger {
        val result = IntArray(maxOf(intLength(), other.intLength()))
        for (i in result.indices) {
            result[i] = getInt(result.size - i - 1) and other.getInt(result.size - i - 1).inv()
        }
        return valueOf(result)
    }

    /**
     * Returns a BigInteger whose value is `(-this)`.
     * Rename from `negate` to `unaryMinus` to accord with the `unaryMinus` operator in Kotlin
     *
     * @return `-this`
     */
    @JsName("unaryMinus")
    operator fun unaryMinus(): BigInteger {
        return BigInteger(this.mag, -this.signum)
    }

    /**
     * Returns a BigInteger whose value is the absolute value of this
     * BigInteger.
     *
     * @return `abs(this)`
     */
    @JsName("abs")
    fun abs(): BigInteger {
        return if (this.signum >= 0) this else -this
    }

    /**
     * Returns the signum function of this BigInteger.
     *
     * @return -1, 0 or 1 as the value of this BigInteger is negative, zero or
     * positive.
     */
    @JsName("signum")
    fun signum(): Int {
        return this.signum
    }

    /**
     * Returns a BigInteger whose value is `(this + addend)`.
     * Rename from `add` to `plus` to accord with `Int.plus` in Kotlin
     *
     * @param  addend value to be added to this BigInteger.
     * @return `this + addend`
     */
    @JsName("plus")
    operator fun plus(addend: BigInteger): BigInteger {
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
     * Returns a BigInteger whose value is `(this - val)`.
     * Rename from `subtract` to `minus` to accord with `Int.minus` in Kotlin
     *
     * @param  value value to be subtracted from this BigInteger.
     * @return `this - val`
     */
    @JsName("minus")
    operator fun minus(value: BigInteger): BigInteger {
        if (value.signum == 0) {
            return this
        }
        if (this.signum == 0) {
            return -value
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
     * Returns a BigInteger whose value is `(this * val)`.
     * Rename from `multiply` to `times` to accord with `Int.times` in Kotlin
     *
     * @param  value value to be multiplied by this BigInteger.
     * @return `this * value`
     */
    @JsName("times")
    operator fun times(value: BigInteger): BigInteger {
        if (value.signum == 0 || this.signum == 0) {
            return ZERO
        }
        val xlen = mag.size
        val ylen = value.mag.size

        if (xlen < AlgorithmUtils.KARATSUBA_THRESHOLD || ylen < AlgorithmUtils.KARATSUBA_THRESHOLD) {
            val resultSign = if (this.signum == value.signum) 1 else -1
            if (value.mag.size == 1) {
                return multiplyByInt(mag, value.mag[0], resultSign)
            }
            if (mag.size == 1) {
                return multiplyByInt(value.mag, mag[0], resultSign)
            }
            var result = multiplyToLen(mag, xlen,
                    value.mag, ylen, null)
            result = trustedStripLeadingZeroInts(result)
            return BigInteger(result, resultSign)
        } else {
            return if (xlen < AlgorithmUtils.TOOM_COOK_THRESHOLD && ylen < AlgorithmUtils.TOOM_COOK_THRESHOLD) {
                AlgorithmUtils.multiplyKaratsuba(this, value)
            } else {
                AlgorithmUtils.multiplyToomCook3(this, value)
            }
        }
    }

    /**
     * Package private methods used by BigDecimal code to multiply a BigInteger
     * with a long. Assumes v is not equal to INFLATED.
     */
    internal fun multiply(v: Long): BigInteger {
        var v = v
        if (v == 0L || signum == 0) {
            return ZERO
        }
        if (v == BigDecimal.INFLATED) {
            return times(BigInteger.valueOf(v))
        }
        val rsign = if (v > 0) signum else -signum
        if (v < 0) {
            v = -v
        }
        val dh = v.ushr(32)      // higher order bits
        val dl = v and LONG_MASK // lower order bits

        val xlen = this.mag.size
        val value = this.mag
        var rmag = if (dh == 0L) IntArray(xlen + 1) else IntArray(xlen + 2)
        var carry: Long = 0
        var rstart = rmag.size - 1
        for (i in xlen - 1 downTo 0) {
            val product = (value[i].toLong() and LONG_MASK) * dl + carry
            rmag[rstart--] = product.toInt()
            carry = product.ushr(32)
        }
        rmag[rstart] = carry.toInt()
        if (dh != 0L) {
            carry = 0
            rstart = rmag.size - 2
            for (i in xlen - 1 downTo 0) {
                val product = (value[i].toLong() and LONG_MASK) * dh +
                        (rmag[rstart].toLong() and LONG_MASK) + carry
                rmag[rstart--] = product.toInt()
                carry = product.ushr(32)
            }
            rmag[0] = carry.toInt()
        }
        if (carry == 0L) {
            rmag = rmag.copyOfRange(1, rmag.size)
        }
        return BigInteger(rmag, rsign)
    }

    /**
     * Returns a BigInteger whose value is `(this / divisor)`.
     * Rename from `divide` to `div` to accord with `Int.div` in Kotlin
     *
     * @param  `divisor` value by which this BigInteger is to be divided.
     * @return `this / divisor`
     * @throws ArithmeticException if `divisor` is zero.
     */
    @JsName("div")
    operator fun div(divisor: BigInteger): BigInteger {
        return if (divisor.mag.size < AlgorithmUtils.BURNIKEL_ZIEGLER_THRESHOLD ||
                this.mag.size - divisor.mag.size < AlgorithmUtils.BURNIKEL_ZIEGLER_OFFSET) {
            AlgorithmUtils.divideKnuth(this, divisor)
        } else {
            AlgorithmUtils.divideBurnikelZiegler(this, divisor)
        }
    }

    /**
     * Returns an array of two BigIntegers containing `(this / divisor)`
     * followed by `(this % divisor)`.
     *
     * @param  divisor value by which this BigInteger is to be divided, and the
     * remainder computed.
     * @return an array of two BigIntegers: the quotient `(this / divisor)`
     * is the initial element, and the remainder `(this % divisor)`
     * is the final element.
     * @throws ArithmeticException if `divisor` is zero.
     */
    @JsName("divideAndRemainder")
    fun divideAndRemainder(divisor: BigInteger): Array<BigInteger> {
        return if (divisor.mag.size < AlgorithmUtils.BURNIKEL_ZIEGLER_THRESHOLD ||
                this.mag.size - divisor.mag.size < AlgorithmUtils.BURNIKEL_ZIEGLER_OFFSET) {
            AlgorithmUtils.divideAndRemainderKnuth(this, divisor)
        } else {
            AlgorithmUtils.divideAndRemainderBurnikelZiegler(this, divisor)
        }
    }

    /**
     * Returns a BigInteger whose value is `(this mod m`).  This method
     * differs from `remainder` in that it always returns a
     * *non-negative* BigInteger.
     *
     * Rename from `remainder` to `rem` to accord with `Int.rem` in Kotlin
     *
     * @param  m the modulus.
     * @return `this mod m`
     * @throws ArithmeticException `m`  0
     * @see .remainder
     */
    @JsName("rem")
    operator fun rem(m: BigInteger): BigInteger {
        if (m.signum <= 0) {
            throw ArithmeticException("BigInteger: modulus not positive")
        }
        val result = this.remainder(m)
        return if (result.signum >= 0) result else result.plus(m)
    }

    /**
     * Returns a BigInteger whose value is `(this % divisor)`.
     *
     * @param  divisor value by which this BigInteger is to be divided, and the
     * remainder computed.
     * @return `this % divisor`
     * @throws ArithmeticException if `divisor` is zero.
     */
    @JsName("remainder")
    fun remainder(divisor: BigInteger): BigInteger {
        return if (divisor.mag.size < AlgorithmUtils.BURNIKEL_ZIEGLER_THRESHOLD ||
                this.mag.size - divisor.mag.size < AlgorithmUtils.BURNIKEL_ZIEGLER_OFFSET) {
            AlgorithmUtils.remainderKnuth(this, divisor)
        } else {
            AlgorithmUtils.remainderBurnikelZiegler(this, divisor)
        }
    }

    /**
     * Returns a BigInteger whose value is <tt>(this<sup>exponent</sup>)</tt>.
     * Note that `exponent` is an integer rather than a BigInteger.
     *
     * @param  exponent exponent to which this BigInteger is to be raised.
     * @return <tt>this<sup>exponent</sup></tt>
     * @throws ArithmeticException `exponent` is negative.  (This would
     * cause the operation to yield a non-integer value.)
     */
    @JsName("pow")
    fun pow(exponent: Int): BigInteger {
        if (exponent < 0) {
            throw ArithmeticException("Negative exponent")
        }
        if (this.signum == 0) {
            return if (exponent == 0) ONE else this
        }

        var partToSquare = this.abs()

        // Factor out powers of two from the base, as the exponentiation of
        // these can be done by left shifts only.
        // The remaining part can then be exponentiated faster.  The
        // powers of two will be multiplied back at the end.
        val powersOfTwo = partToSquare.getLowestSetBit()
        val bitsToShift = powersOfTwo.toLong() * exponent
        if (bitsToShift > Int.MAX_VALUE) {
            reportOverflow()
        }

        val remainingBits: Int

        // Factor the powers of two out quickly by shifting right, if needed.
        if (powersOfTwo > 0) {
            partToSquare = partToSquare.shr(powersOfTwo)
            remainingBits = partToSquare.bitLength()
            if (remainingBits == 1) {  // Nothing left but +/- 1?
                return if (this.signum < 0 && exponent.and(1) == 1) {
                    NEGATIVE_ONE.shl(powersOfTwo * exponent)
                } else {
                    ONE.shl(powersOfTwo * exponent)
                }
            }
        } else {
            remainingBits = partToSquare.bitLength()
            if (remainingBits == 1) { // Nothing left but +/- 1?
                return if (signum < 0 && exponent.and(1) == 1) {
                    NEGATIVE_ONE
                } else {
                    ONE
                }
            }
        }

        // This is a quick way to approximate the size of the result,
        // similar to doing log2[n] * exponent.  This will give an upper bound
        // of how big the result can be, and which algorithm to use.
        val scaleFactor = remainingBits.toLong() * exponent

        // Use slightly different algorithms for small and large operands.
        // See if the result will safely fit into a long. (Largest 2^63-1)
        if (partToSquare.mag.size == 1 && scaleFactor <= 62) {
            // Small number algorithm.  Everything fits into a long.
            val newSign = if (signum < 0 && exponent and 1 == 1) -1 else 1
            var result: Long = 1
            var baseToPow2 = partToSquare.mag[0].toLong() and LONG_MASK

            var workingExponent = exponent

            // Perform exponentiation using repeated squaring trick
            while (workingExponent != 0) {
                if (workingExponent and 1 == 1) {
                    result = result * baseToPow2
                }
                workingExponent = workingExponent ushr 1
                if (workingExponent != 0) {
                    baseToPow2 = baseToPow2 * baseToPow2
                }
            }

            // Multiply back the powers of two (quickly, by shifting left)
            return if (powersOfTwo > 0) {
                if (bitsToShift + scaleFactor <= 62) { // Fits in long?
                    valueOf((result shl bitsToShift.toInt()) * newSign)
                } else {
                    valueOf(result * newSign).shl(bitsToShift.toInt())
                }
            } else {
                valueOf(result * newSign)
            }
        } else {
            // Large number algorithm.  This is basically identical to
            // the algorithm above, but calls multiply() and square()
            // which may use more efficient algorithms for large numbers.
            var answer = ONE

            var workingExponent = exponent
            // Perform exponentiation using repeated squaring trick
            while (workingExponent != 0) {
                if (workingExponent and 1 == 1) {
                    answer *= partToSquare
                }
                workingExponent = workingExponent ushr 1
                if (workingExponent != 0) {
                    partToSquare = AlgorithmUtils.square(partToSquare)
                }
            }
            // Multiply back the (exponentiated) powers of two (quickly,
            // by shifting left)
            if (powersOfTwo > 0) {
                answer = answer.shl(powersOfTwo * exponent)
            }

            return if (signum < 0 && exponent and 1 == 1) {
                -answer
            } else {
                answer
            }
        }
    }

    /**
     * Returns a BigInteger whose value is the greatest common divisor of
     * `abs(this)` and `abs(val)`.  Returns 0 if
     * `this == 0 && val == 0`.
     *
     * @param  other value with which the GCD is to be computed.
     * @return `GCD(abs(this), abs(val))`
     */
    @JsName("gcd")
    fun gcd(other: BigInteger): BigInteger {
        if (other.signum == 0) {
            return this.abs()
        } else if (this.signum == 0) {
            return other.abs()
        }
        val a = MutableBigInteger(this)
        val b = MutableBigInteger(other)

        val result = a.hybridGCD(b)

        return result.toBigInteger(1)
    }

    /**
     * Returns a BigInteger whose value is
     * <tt>(this<sup>exponent</sup> mod m)</tt>.  (Unlike `pow`, this
     * method permits negative exponents.)
     *
     * @param  exponent the exponent.
     * @param  m the modulus.
     * @return <tt>this<sup>exponent</sup> mod m</tt>
     * @throws ArithmeticException `m`  0 or the exponent is
     * negative and this BigInteger is not *relatively
     * prime* to `m`.
     * @see .modInverse
     */
    @JsName("modPow")
    fun modPow(exponent: BigInteger, m: BigInteger): BigInteger {
        var exponent = exponent
        if (m.signum <= 0) {
            throw ArithmeticException("BigInteger: modulus not positive")
        }
        // Trivial cases
        if (exponent.signum == 0) {
            return if (m == ONE) ZERO else ONE
        }
        if (this == ONE) {
            return if (m == ONE) ZERO else ONE
        }
        if (this == ZERO && exponent.signum >= 0) {
            return ZERO
        }
        if (this == negConst[1] && !exponent.testBit(0)) {
            return if (m == ONE) ZERO else ONE
        }
        val invertResult = exponent.signum < 0
        if (invertResult) {
            exponent = -exponent
        }
        val base = if (this.signum < 0 || this >= m) this.rem(m) else this
        val result: BigInteger
        if (m.testBit(0)) { // odd modulus
            result = base.oddModPow(exponent, m)
        } else {
            /*
             * Even modulus.  Tear it into an "odd part" (m1) and power of two
             * (m2), exponentiate mod m1, manually exponentiate mod m2, and
             * use Chinese Remainder Theorem to combine results.
             */

            // Tear m apart into odd part (m1) and power of 2 (m2)
            val p = m.getLowestSetBit()   // Max pow of 2 that divides m

            val m1 = m.shr(p)  // m/2**p
            val m2 = ONE.shl(p) // 2**p

            // Calculate new base from m1
            val base2 = if (this.signum < 0 || this >= m1) this.rem(m1) else this

            // Caculate (base ** exponent) mod m1.
            val a1 = if (m1 == ONE)
                ZERO
            else
                base2.oddModPow(exponent, m1)

            // Calculate (this ** exponent) mod m2
            val a2 = AlgorithmUtils.modPow2(base, exponent, p)

            // Combine results using Chinese Remainder Theorem
            val y1 = m2.modInverse(m1)
            val y2 = m1.modInverse(m2)

            result = if (m.mag.size < MAX_MAG_LENGTH / 2) {
                (a1 * m2 * y1 + a2 * m1 * y2).rem(m)
            } else {
                val t1 = MutableBigInteger()
                MutableBigInteger(a1 * m2).multiply(MutableBigInteger(y1), t1)
                val t2 = MutableBigInteger()
                MutableBigInteger(a2 * m1).multiply(MutableBigInteger(y2), t2)
                t1 += t2
                val q = MutableBigInteger()
                t1.divide(MutableBigInteger(m), q).toBigInteger()
            }
        }

        return if (invertResult) result.modInverse(m) else result
    }

    /**
     * Returns a BigInteger whose value is `(this`<sup>-1</sup> `mod m)`.
     *
     * @param  m the modulus.
     * @return `this`<sup>-1</sup> `mod m`.
     * @throws ArithmeticException `m`  0, or this BigInteger
     * has no multiplicative inverse mod m (that is, this BigInteger
     * is not *relatively prime* to m).
     */
    @JsName("modInverse")
    fun modInverse(m: BigInteger): BigInteger {
        if (m.signum != 1) {
            throw ArithmeticException("BigInteger: modulus not positive")
        }
        if (m == ONE) {
            return ZERO
        }
        // Calculate (this mod m)
        var modVal = this
        if (this.signum < 0 || this.compareMagnitude(m) >= 0) {
            modVal = this.rem(m)
        }
        if (modVal == ONE) {
            return ONE
        }
        val a = MutableBigInteger(modVal)
        val b = MutableBigInteger(m)

        val result = a.mutableModInverse(b)
        return result.toBigInteger(1)
    }

    /**
     * Returns a BigInteger whose value is x to the power of y mod z.
     * Assumes: z is odd && x < z.
     */
    private fun oddModPow(y: BigInteger, z: BigInteger): BigInteger {
        /*
         * The algorithm is adapted from Colin Plumb's C library.
         *
         * The window algorithm:
         * The idea is to keep a running product of b1 = n^(high-order bits of exp)
         * and then keep appending exponent bits to it.  The following patterns
         * apply to a 3-bit window (k = 3):
         * To append   0: square
         * To append   1: square, multiply by n^1
         * To append  10: square, multiply by n^1, square
         * To append  11: square, square, multiply by n^3
         * To append 100: square, multiply by n^1, square, square
         * To append 101: square, square, square, multiply by n^5
         * To append 110: square, square, multiply by n^3, square
         * To append 111: square, square, square, multiply by n^7
         *
         * Since each pattern involves only one multiply, the longer the pattern
         * the better, except that a 0 (no multiplies) can be appended directly.
         * We precompute a table of odd powers of n, up to 2^k, and can then
         * multiply k bits of exponent at a time.  Actually, assuming random
         * exponents, there is on average one zero bit between needs to
         * multiply (1/2 of the time there's none, 1/4 of the time there's 1,
         * 1/8 of the time, there's 2, 1/32 of the time, there's 3, etc.), so
         * you have to do one multiply per k+1 bits of exponent.
         *
         * The loop walks down the exponent, squaring the result buffer as
         * it goes.  There is a wbits+1 bit lookahead buffer, buf, that is
         * filled with the upcoming exponent bits.  (What is read after the
         * end of the exponent is unimportant, but it is filled with zero here.)
         * When the most-significant bit of this buffer becomes set, i.e.
         * (buf & tblmask) != 0, we have to decide what pattern to multiply
         * by, and when to do it.  We decide, remember to do it in future
         * after a suitable number of squarings have passed (e.g. a pattern
         * of "100" in the buffer requires that we multiply by n^1 immediately;
         * a pattern of "110" calls for multiplying by n^3 after one more
         * squaring), clear the buffer, and continue.
         *
         * When we start, there is one more optimization: the result buffer
         * is implcitly one, so squaring it or multiplying by it can be
         * optimized away.  Further, if we start with a pattern like "100"
         * in the lookahead window, rather than placing n into the buffer
         * and then starting to square it, we have already computed n^2
         * to compute the odd-powers table, so we can place that into
         * the buffer and save a squaring.
         *
         * This means that if you have a k-bit window, to compute n^z,
         * where z is the high k bits of the exponent, 1/2 of the time
         * it requires no squarings.  1/4 of the time, it requires 1
         * squaring, ... 1/2^(k-1) of the time, it reqires k-2 squarings.
         * And the remaining 1/2^(k-1) of the time, the top k bits are a
         * 1 followed by k-1 0 bits, so it again only requires k-2
         * squarings, not k-1.  The average of these is 1.  Add that
         * to the one squaring we have to do to compute the table,
         * and you'll see that a k-bit window saves k-2 squarings
         * as well as reducing the multiplies.  (It actually doesn't
         * hurt in the case k = 1, either.)
         */
        // Special case for exponent of one
        if (y == ONE) {
            return this
        }
        // Special case for base of zero
        if (this.signum == 0) {
            return ZERO
        }
        val base = this.mag.copyOf()
        val exp = y.mag
        val mod = z.mag
        val modLen = mod.size

        // Select an appropriate window size
        var wbits = 0
        var ebits = bitLength(exp, exp.size)
        // if exponent is 65537 (0x10001), use minimum window size
        if (ebits != 17 || exp[0] != 65537) {
            while (ebits > bnExpModThreshTable[wbits]) {
                wbits += 1
            }
        }

        // Calculate appropriate table size
        val tblmask = 1 shl wbits

        // Allocate table for precomputed odd powers of base in Montgomery form
        val table = Array(tblmask) { IntArray(modLen) }

        // Compute the modular inverse
        val inv = -MutableBigInteger.inverseMod32(mod[modLen - 1])

        // Convert base to Montgomery form
        var a = leftShift(base, base.size, modLen shl 5)

        val q = MutableBigInteger()
        val a2 = MutableBigInteger(a)
        val b2 = MutableBigInteger(mod)

        val r = a2.divide(b2, q)
        table[0] = r.toIntArray()

        // Pad table[0] with leading zeros so its length is at least modLen
        if (table[0].size < modLen) {
            val offset = modLen - table[0].size
            val t2 = IntArray(modLen)
            for (i in 0 until table[0].size)
                t2[i + offset] = table[0][i]
            table[0] = t2
        }

        // Set b to the square of the base
        var b = AlgorithmUtils.squareToLen(table[0], modLen, null)
        b = AlgorithmUtils.montReduce(b, mod, modLen, inv)

        // Set t to high half of b
        var t = b.copyOf(modLen)

        // Fill in the table with odd powers of the base
        for (i in 1 until tblmask) {
            val prod = multiplyToLen(t, modLen, table[i - 1], modLen, null)
            table[i] = AlgorithmUtils.montReduce(prod, mod, modLen, inv)
        }

        // Pre load the window that slides over the exponent
        var bitpos = 1 shl (ebits - 1 and 32 - 1)

        var buf = 0
        var elen = exp.size
        var eIndex = 0
        for (i in 0 .. wbits) {
            buf = buf shl 1 or if (exp[eIndex] and bitpos != 0) 1 else 0
            bitpos = bitpos ushr 1
            if (bitpos == 0) {
                eIndex += 1
                bitpos = 1 shl 32 - 1
                elen -= 1
            }
        }

        // The first iteration, which is hoisted out of the main loop
        ebits -= 1
        var isone = true

        var multpos = ebits - wbits
        while (buf and 1 == 0) {
            buf = buf ushr 1
            multpos += 1
        }

        var mult = table[buf.ushr(1)]

        buf = 0
        if (multpos == ebits) {
            isone = false
        }
        // The main loop
        while (true) {
            ebits -= 1
            // Advance the window
            buf = buf shl 1

            if (elen != 0) {
                buf = buf or if (exp[eIndex] and bitpos != 0) 1 else 0
                bitpos = bitpos ushr 1
                if (bitpos == 0) {
                    eIndex += 1
                    bitpos = 1 shl 32 - 1
                    elen -= 1
                }
            }

            // Examine the window for pending multiplies
            if (buf and tblmask != 0) {
                multpos = ebits - wbits
                while (buf and 1 == 0) {
                    buf = buf ushr 1
                    multpos += 1
                }
                mult = table[buf.ushr(1)]
                buf = 0
            }

            // Perform multiply
            if (ebits == multpos) {
                if (isone) {
                    b = mult.copyOf()
                    isone = false
                } else {
                    t = b
                    a = multiplyToLen(t, modLen, mult, modLen, a)
                    a = AlgorithmUtils.montReduce(a, mod, modLen, inv)
                    t = a
                    a = b
                    b = t
                }
            }

            // Check if done
            if (ebits == 0) {
                break
            }
            // Square the input
            if (!isone) {
                t = b
                a = AlgorithmUtils.squareToLen(t, modLen, a)
                a = AlgorithmUtils.montReduce(a, mod, modLen, inv)
                t = a
                a = b
                b = t
            }
        }

        // Convert result out of Montgomery form and return
        var t2 = IntArray(2 * modLen)
        b.copyInto(t2, modLen, 0, modLen)

        b = AlgorithmUtils.montReduce(t2, mod, modLen, inv)

        t2 = b.copyOf(modLen)

        return BigInteger(1, t2)
    } // private fun oddModPow(y: BigInteger, z: BigInteger): BigInteger

    /**
     * Returns the integer square root of this BigInteger.  The integer square
     * root of the corresponding mathematical integer `n` is the largest
     * mathematical integer `s` such that `s*s <= n`.  It is equal
     * to the value of `floor(sqrt(n))`, where `sqrt(n)` denotes the
     * real square root of `n` treated as a real.  Note that the integer
     * square root will be less than the real square root if the latter is not
     * representable as an integral value.
     *
     * @return the integer square root of `this`
     * @throws ArithmeticException if `this` is negative.  (The square
     * root of a negative integer `val` is
     * `(i * sqrt(-val))` where *i* is the
     * *imaginary unit* and is equal to
     * `sqrt(-1)`.)
     * @since  Java 9
     */
    @JsName("sqrt")
    fun sqrt(): BigInteger {
        if (this.signum < 0) {
            throw ArithmeticException("Negative BigInteger")
        }

        return MutableBigInteger(this.mag).sqrt().toBigInteger()
    }

    /**
     * Returns an array of two BigIntegers containing the integer square root
     * `s` of `this` and its remainder `this - s*s`,
     * respectively.
     *
     * @return an array of two BigIntegers with the integer square root at
     * offset 0 and the remainder at offset 1
     * @throws ArithmeticException if `this` is negative.  (The square
     * root of a negative integer `val` is
     * `(i * sqrt(-val))` where *i* is the
     * *imaginary unit* and is equal to
     * `sqrt(-1)`.)
     * @see .sqrt
     * @since  Java 9
     */
    @JsName("sqrtAndRemainder")
    fun sqrtAndRemainder(): Array<BigInteger> {
        val s = sqrt()
        val r = this - AlgorithmUtils.square(s)
        if (r < BigInteger.ZERO) {
            throw ArithmeticException("Cannot get the square root of a negative number")
        }
        return arrayOf(s, r)
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
     * Returns a BigInteger whose value is `(this >> n)`. The shift
     * distance, `n`, is considered unsigned.
     * (Computes <tt>floor(this * 2<sup>-n</sup>)</tt>.)
     *
     * @param  n unsigned shift distance, in bits.
     * @return `this >> n`
     */
    private fun shiftRightImpl(n: Int): BigInteger {
        val nInts = n.ushr(5)
        val nBits = n and 0x1f
        val magLen = mag.size
        var newMag: IntArray?

        // Special case: entire contents shifted off the end
        if (nInts >= magLen)
            return if (signum >= 0) ZERO else negConst[1]

        if (nBits == 0) {
            val newMagLen = magLen - nInts
            newMag = mag.copyOf(newMagLen)
        } else {
            var i = 0
            val highBits = mag[0].ushr(nBits)
            if (highBits != 0) {
                newMag = IntArray(magLen - nInts)
                newMag[i] = highBits
                i += 1
            } else {
                newMag = IntArray(magLen - nInts - 1)
            }

            val nBits2 = 32 - nBits
            var j = 0
            while (j < magLen - nInts - 1)
                newMag[i++] = mag[j++] shl nBits2 or mag[j].ushr(nBits)
        }

        if (this.signum < 0) {
            // Find out whether any one-bits were shifted off the end.
            var onesLost = false
            var i = magLen - 1
            val j = magLen - nInts
            while (i >= j && !onesLost) {
                onesLost = this.mag[i] != 0
                i -= 1
            }
            if (!onesLost && nBits != 0) {
                onesLost = this.mag[magLen - nInts - 1] shl 32 - nBits != 0
            }
            if (onesLost) {
                newMag = javaIncrement(newMag)
            }
        }

        return BigInteger(newMag, signum)
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