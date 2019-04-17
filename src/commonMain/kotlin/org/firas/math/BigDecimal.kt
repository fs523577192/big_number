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
 * Portions Copyright IBM Corporation, 2001. All Rights Reserved.
 */
package org.firas.math

import org.firas.lang.Character
import org.firas.util.Integers
import kotlin.math.absoluteValue


/**
 * Immutable, arbitrary-precision signed decimal numbers.  A
 * `BigDecimal` consists of an arbitrary precision integer
 * <i>unscaled value</i> and a 32-bit integer <i>scale</i>.  If zero
 * or positive, the scale is the number of digits to the right of the
 * decimal point.  If negative, the unscaled value of the number is
 * multiplied by ten to the power of the negation of the scale.  The
 * value of the number represented by the `BigDecimal` is
 * therefore <tt>(unscaledValue &times; 10<sup>-scale</sup>)</tt>.
 *
 * <p>The `BigDecimal` class provides operations for
 * arithmetic, scale manipulation, rounding, comparison, hashing, and
 * format conversion.  The {@link #toString} method provides a
 * canonical representation of a `BigDecimal`.
 *
 * <p>The `BigDecimal` class gives its user complete control
 * over rounding behavior.  If no rounding mode is specified and the
 * exact result cannot be represented, an exception is thrown;
 * otherwise, calculations can be carried out to a chosen precision
 * and rounding mode by supplying an appropriate {@link MathContext}
 * object to the operation.  In either case, eight <em>rounding
 * modes</em> are provided for the control of rounding.  Using the
 * integer fields in this class (such as {@link #ROUND_HALF_UP}) to
 * represent rounding mode is largely obsolete; the enumeration values
 * of the `RoundingMode` `enum`, (such as {@link
 * RoundingMode#HALF_UP}) should be used instead.
 *
 * <p>When a `MathContext` object is supplied with a precision
 * setting of 0 (for example, {@link MathContext#UNLIMITED}),
 * arithmetic operations are exact, as are the arithmetic methods
 * which take no `MathContext` object.  (This is the only
 * behavior that was supported in releases prior to 5.)  As a
 * corollary of computing the exact result, the rounding mode setting
 * of a `MathContext` object with a precision setting of 0 is
 * not used and thus irrelevant.  In the case of divide, the exact
 * quotient could have an infinitely long decimal expansion; for
 * example, 1 divided by 3.  If the quotient has a nonterminating
 * decimal expansion and the operation is specified to return an exact
 * result, an `ArithmeticException` is thrown.  Otherwise, the
 * exact result of the division is returned, as done for other
 * operations.
 *
 * <p>When the precision setting is not 0, the rules of
 * `BigDecimal` arithmetic are broadly compatible with selected
 * modes of operation of the arithmetic defined in ANSI X3.274-1996
 * and ANSI X3.274-1996/AM 1-2000 (section 7.4).  Unlike those
 * standards, `BigDecimal` includes many rounding modes, which
 * were mandatory for division in `BigDecimal` releases prior
 * to 5.  Any conflicts between these ANSI standards and the
 * `BigDecimal` specification are resolved in favor of
 * `BigDecimal`.
 *
 * <p>Since the same numerical value can have different
 * representations (with different scales), the rules of arithmetic
 * and rounding must specify both the numerical result and the scale
 * used in the result's representation.
 *
 *
 * <p>In general the rounding modes and precision setting determine
 * how operations return results with a limited number of digits when
 * the exact result has more digits (perhaps infinitely many in the
 * case of division) than the number of digits returned.
 *
 * First, the
 * total number of digits to return is specified by the
 * `MathContext`'s `precision` setting; this determines
 * the result's <i>precision</i>.  The digit count starts from the
 * leftmost nonzero digit of the exact result.  The rounding mode
 * determines how any discarded trailing digits affect the returned
 * result.
 *
 * <p>For all arithmetic operators , the operation is carried out as
 * though an exact intermediate result were first calculated and then
 * rounded to the number of digits specified by the precision setting
 * (if necessary), using the selected rounding mode.  If the exact
 * result is not returned, some digit positions of the exact result
 * are discarded.  When rounding increases the magnitude of the
 * returned result, it is possible for a new digit position to be
 * created by a carry propagating to a leading {@literal "9"} digit.
 * For example, rounding the value 999.9 to three digits rounding up
 * would be numerically equal to one thousand, represented as
 * 100&times;10<sup>1</sup>.  In such cases, the new {@literal "1"} is
 * the leading digit position of the returned result.
 *
 * <p>Besides a logical exact result, each arithmetic operation has a
 * preferred scale for representing a result.  The preferred
 * scale for each operation is listed in the table below.
 *
 * <table border>
 * <caption><b>Preferred Scales for Results of Arithmetic Operations
 * </b></caption>
 * <tr><th>Operation</th><th>Preferred Scale of Result</th></tr>
 * <tr><td>Add</td><td>max(addend.scale(), augend.scale())</td>
 * <tr><td>Subtract</td><td>max(minuend.scale(), subtrahend.scale())</td>
 * <tr><td>Multiply</td><td>multiplier.scale() + multiplicand.scale()</td>
 * <tr><td>Divide</td><td>dividend.scale() - divisor.scale()</td>
 * </table>
 *
 * These scales are the ones used by the methods which return exact
 * arithmetic results; except that an exact divide may have to use a
 * larger scale since the exact result may have more digits.  For
 * example, `1/32` is `0.03125`.
 *
 * <p>Before rounding, the scale of the logical exact intermediate
 * result is the preferred scale for that operation.  If the exact
 * numerical result cannot be represented in `precision`
 * digits, rounding selects the set of digits to return and the scale
 * of the result is reduced from the scale of the intermediate result
 * to the least scale which can represent the `precision`
 * digits actually returned.  If the exact result can be represented
 * with at most `precision` digits, the representation
 * of the result with the scale closest to the preferred scale is
 * returned.  In particular, an exactly representable quotient may be
 * represented in fewer than `precision` digits by removing
 * trailing zeros and decreasing the scale.  For example, rounding to
 * three digits using the {@linkplain RoundingMode#FLOOR floor}
 * rounding mode, <br>
 *
 * `19/100 = 0.19   // integer=19,  scale=2` <br>
 *
 * but<br>
 *
 * `21/110 = 0.190  // integer=190, scale=3` <br>
 *
 * <p>Note that for add, subtract, and multiply, the reduction in
 * scale will equal the number of digit positions of the exact result
 * which are discarded. If the rounding causes a carry propagation to
 * create a new high-order digit position, an additional digit of the
 * result is discarded than when no new digit position is created.
 *
 * <p>Other methods may have slightly different rounding semantics.
 * For example, the result of the `pow` method using the
 * {@linkplain #pow(int, MathContext) specified algorithm} can
 * occasionally differ from the rounded mathematical result by more
 * than one unit in the last place, one <i>{@linkplain #ulp() ulp}</i>.
 *
 * <p>Two types of operations are provided for manipulating the scale
 * of a `BigDecimal`: scaling/rounding operations and decimal
 * point motion operations.  Scaling/rounding operations ({@link
 * #setScale setScale} and {@link #round round}) return a
 * `BigDecimal` whose value is approximately (or exactly) equal
 * to that of the operand, but whose scale or precision is the
 * specified value; that is, they increase or decrease the precision
 * of the stored number with minimal effect on its value.  Decimal
 * point motion operations ({@link #movePointLeft movePointLeft} and
 * {@link #movePointRight movePointRight}) return a
 * `BigDecimal` created from the operand by moving the decimal
 * point a specified distance in the specified direction.
 *
 * <p>For the sake of brevity and clarity, pseudo-code is used
 * throughout the descriptions of `BigDecimal` methods.  The
 * pseudo-code expression `(i + j)` is shorthand for "a
 * `BigDecimal` whose value is that of the `BigDecimal`
 * `i` added to that of the `BigDecimal`
 * `j`." The pseudo-code expression `(i == j)` is
 * shorthand for "`true` if and only if the
 * `BigDecimal` `i` represents the same value as the
 * `BigDecimal` `j`." Other pseudo-code expressions
 * are interpreted similarly.  Square brackets are used to represent
 * the particular `BigInteger` and scale pair defining a
 * `BigDecimal` value; for example [19, 2] is the
 * `BigDecimal` numerically equal to 0.19 having a scale of 2.
 *
 * <p>Note: care should be exercised if `BigDecimal` objects
 * are used as keys in a {@link java.util.SortedMap SortedMap} or
 * elements in a {@link java.util.SortedSet SortedSet} since
 * `BigDecimal`'s <i>natural ordering</i> is <i>inconsistent
 * with equals</i>.  See [Comparable], {@link
 * java.util.SortedMap} or {@link java.util.SortedSet} for more
 * information.
 *
 * <p>All methods and constructors for this class throw
 * `NullPointerException` when passed a `null` object
 * reference for any input parameter.
 *
 * @see     BigInteger
 * @see     MathContext
 * @see     RoundingMode
 * @see     java.util.SortedMap
 * @see     java.util.SortedSet
 * @author  Josh Bloch
 * @author  Mike Cowlishaw
 * @author  Joseph D. Darcy
 * @author  Sergey V. Kuksenko
 * @author  Wu Yuping
 */
class BigDecimal
/**
 * Trusted package private constructor.
 * Trusted simply means if val is INFLATED, intVal could not be null and
 * if intVal is null, val could not be INFLATED.
 */
internal constructor(
    /**
     * The unscaled value of this BigDecimal, as returned by [ ][.unscaledValue].
     *
     * @serial
     * @see .unscaledValue
     */
    private val intVal: BigInteger?,

    /**
     * If the absolute value of the significand of this BigDecimal is
     * less than or equal to `Long.MAX_VALUE`, the value can be
     * compactly stored in this field and used in computations.
     */
    // @kotlin.jvm.Transient
    private val intCompact: Long,

    /**
     * The scale of this BigDecimal, as returned by [.scale].
     *
     * @serial
     * @see .scale
     */
    private val scale: Int, // Note: this may have any value, so
    // calculations must be done in longs

    /**
     * The number of decimal digits in this BigDecimal, or 0 if the
     * number of digits are not known (lookaside information).  If
     * nonzero, the value is guaranteed correct.  Use the precision()
     * method to obtain and set the value if it might be 0.  This
     * field is mutable until set nonzero.
     *
     * @since  1.5
     */
    // @kotlin.jvm.Transient
    private var precision: Int
): Number(), Comparable<BigDecimal> {
    /**
     * Used to store the canonical string representation, if computed.
     */
    // @kotlin.jvm.Transient
    private var stringCache: String? = null

    /**
     * Translates a `BigInteger` into a `BigDecimal`.
     * The scale of the `BigDecimal` is zero.
     *
     * @param intVal `BigInteger` value to be converted to
     * `BigDecimal`.
     */
    constructor(intVal: BigInteger): this(intVal, compactValFor(intVal), 0, 0)

    /**
     * Translates a `BigInteger` unscaled value and an
     * `int` scale into a `BigDecimal`.  The value of
     * the `BigDecimal` is
     * <tt>(unscaledVal &times; 10<sup>-scale</sup>)</tt>.
     *
     * @param unscaledVal unscaled value of the `BigDecimal`.
     * @param scale scale of the `BigDecimal`.
     */
    constructor(unscaledVal: BigInteger, scale: Int): this(unscaledVal, compactValFor(unscaledVal), scale, 0)

    /**
     * Translates an `Int` into a `BigDecimal`. The
     * scale of the `BigDecimal` is zero.
     *
     * @param intVal `Int` value to be converted to
     * `BigDecimal`
     * @since Java 1.5
     */
    constructor(intVal: Int): this(null, intVal.toLong(), 0, 0)

    /**
     * Translates an `Long` into a `BigDecimal`. The
     * scale of the `BigDecimal` is zero.
     *
     * @param longVal `Long` value to be converted to
     * `BigDecimal`
     * @since Java 1.5
     */
    constructor(longVal: Long): this(if (INFLATED == longVal) INFLATED_BIGINT else null, longVal, 0, 0)

    companion object {
        // ----==== Static Factory Methods ====----
        /**
         * Translates a `long` unscaled value and an
         * `int` scale into a `BigDecimal`.  This
         * &quot;static factory method&quot; is provided in preference to
         * a (`long`, `int`) constructor because it
         * allows for reuse of frequently used `BigDecimal` values..
         *
         * @param unscaledVal unscaled value of the `BigDecimal`.
         * @param scale scale of the `BigDecimal`.
         * @return a `BigDecimal` whose value is
         * <tt>(unscaledVal  10<sup>-scale</sup>)</tt>.
         */
        fun valueOf(unscaledVal: Long, scale: Int): BigDecimal {
            if (scale == 0)
                return valueOf(unscaledVal)
            else if (unscaledVal == 0L) {
                return zeroValueOf(scale)
            }
            return BigDecimal(
                if (unscaledVal == INFLATED)
                    INFLATED_BIGINT
                else
                    null,
                unscaledVal, scale, 0
            )
        }

        /**
         * Translates a `long` value into a `BigDecimal`
         * with a scale of zero.  This &quot;static factory method&quot;
         * is provided in preference to a (`long`) constructor
         * because it allows for reuse of frequently used
         * `BigDecimal` values.
         *
         * @param longVal value of the `BigDecimal`.
         * @return a `BigDecimal` whose value is `longVal`.
         */
        fun valueOf(longVal: Long): BigDecimal {
            if (longVal >= 0 && longVal < zeroThroughTen.size)
                return zeroThroughTen[longVal.toInt()]
            else if (longVal != INFLATED)
                return BigDecimal(null, longVal, 0, 0)
            return BigDecimal(INFLATED_BIGINT, longVal, 0, 0)
        }

        /**
         * Translates the string representation of a `BigDecimal`
         * into a `BigDecimal`.  The string representation consists
         * of an optional sign, `'+'` (` '&#92;u002B'`) or
         * `'-'` (`'&#92;u002D'`), followed by a sequence of
         * zero or more decimal digits ("the integer"), optionally
         * followed by a fraction, optionally followed by an exponent.
         *
         *
         * The fraction consists of a decimal point followed by zero
         * or more decimal digits.  The string must contain at least one
         * digit in either the integer or the fraction.  The number formed
         * by the sign, the integer and the fraction is referred to as the
         * *significand*.
         *
         *
         * The exponent consists of the character `'e'`
         * (`'&#92;u0065'`) or `'E'` (`'&#92;u0045'`)
         * followed by one or more decimal digits.  The value of the
         * exponent must lie between -[Integer.MAX_VALUE] ([ ][Integer.MIN_VALUE]+1) and [Integer.MAX_VALUE], inclusive.
         *
         *
         * More formally, the strings this constructor accepts are
         * described by the following grammar:
         * <blockquote>
         * <dl>
         * <dt>*BigDecimalString:*
        </dt> * <dd>*Sign<sub>opt</sub> Significand Exponent<sub>opt</sub>*
        </dd> * <dt>*Sign:*
        </dt> * <dd>`+`
        </dd> * <dd>`-`
        </dd> * <dt>*Significand:*
        </dt> * <dd>*IntegerPart* `.` *FractionPart<sub>opt</sub>*
        </dd> * <dd>`.` *FractionPart*
        </dd> * <dd>*IntegerPart*
        </dd> * <dt>*IntegerPart:*
        </dt> * <dd>*Digits*
        </dd> * <dt>*FractionPart:*
        </dt> * <dd>*Digits*
        </dd> * <dt>*Exponent:*
        </dt> * <dd>*ExponentIndicator SignedInteger*
        </dd> * <dt>*ExponentIndicator:*
        </dt> * <dd>`e`
        </dd> * <dd>`E`
        </dd> * <dt>*SignedInteger:*
        </dt> * <dd>*Sign<sub>opt</sub> Digits*
        </dd> * <dt>*Digits:*
        </dt> * <dd>*Digit*
        </dd> * <dd>*Digits Digit*
        </dd> * <dt>*Digit:*
        </dt> * <dd>any character for which [Character.isDigit]
         * returns `true`, including 0, 1, 2 ...
        </dd></dl> *
        </blockquote> *
         *
         *
         * The scale of the returned `BigDecimal` will be the
         * number of digits in the fraction, or zero if the string
         * contains no decimal point, subject to adjustment for any
         * exponent; if the string contains an exponent, the exponent is
         * subtracted from the scale.  The value of the resulting scale
         * must lie between `Integer.MIN_VALUE` and
         * `Integer.MAX_VALUE`, inclusive.
         *
         *
         * The character-to-digit mapping is provided by [ ][java.lang.Character.digit] set to convert to radix 10.  The
         * String may not contain any extraneous characters (whitespace,
         * for example).
         *
         *
         * **Examples:**<br></br>
         * The value of the returned `BigDecimal` is equal to
         * *significand*  10<sup>&nbsp;*exponent*</sup>.
         * For each string on the left, the resulting representation
         * [`BigInteger`, `scale`] is shown on the right.
         * <pre>
         * "0"            [0,0]
         * "0.00"         [0,2]
         * "123"          [123,0]
         * "-123"         [-123,0]
         * "1.23E3"       [123,-1]
         * "1.23E+3"      [123,-1]
         * "12.3E+7"      [123,-6]
         * "12.0"         [120,1]
         * "12.3"         [123,1]
         * "0.00123"      [123,5]
         * "-1.23E-12"    [-123,14]
         * "1234.5E-4"    [12345,5]
         * "0E+7"         [0,-7]
         * "-0"           [0,0]
        </pre> *
         *
         * @apiNote For values other than `float` and
         * `double` NaN and Infinity, this constructor is
         * compatible with the values returned by [Float.toString]
         * and [Double.toString].  This is generally the preferred
         * way to convert a `float` or `double` into a
         * BigDecimal, as it doesn't suffer from the unpredictability of
         * the [.BigDecimal] constructor.
         *
         * @param str String representation of `BigDecimal`.
         *
         * @throws NumberFormatException if `str` is not a valid
         * representation of a `BigDecimal`.
         */
        fun valueOf(str: String): BigDecimal {
            return valueOf(Character.stringToCharArray(str), 0, str.length)
        }

        /**
         * Translates the string representation of a `BigDecimal`
         * into a `BigDecimal`, accepting the same strings as the
         * [.BigDecimal] constructor, with rounding
         * according to the context settings.
         *
         * @param  str string representation of a `BigDecimal`.
         * @param  mc the context to use.
         * @throws ArithmeticException if the result is inexact but the
         * rounding mode is `UNNECESSARY`.
         * @throws NumberFormatException if `str` is not a valid
         * representation of a BigDecimal.
         * @since  1.5
         */
        fun valueOf(str: String, mc: MathContext): BigDecimal {
            return valueOf(Character.stringToCharArray(str), 0, str.length, mc)
        }

        /**
         * Translates a character array representation of a
         * `BigDecimal` into a `BigDecimal`, accepting the
         * same sequence of characters as the [.BigDecimal]
         * constructor, while allowing a sub-array to be specified.
         *
         * @implNote If the sequence of characters is already available
         * within a character array, using this constructor is faster than
         * converting the `char` array to string and using the
         * `BigDecimal(String)` constructor.
         *
         * @param  `in` `Char` array that is the source of characters.
         * @param  offset first character in the array to inspect.
         * @param  len number of characters to consider.
         * @throws NumberFormatException if `in` is not a valid
         * representation of a `BigDecimal` or the defined subarray
         * is not wholly within `in`.
         * @since  Java 1.5
         */
        fun valueOf(chars: CharArray, offset: Int, len: Int): BigDecimal {
            return valueOf(chars, offset, len, MathContext.UNLIMITED)
        }

        /**
         * Translates a character array representation of a
         * `BigDecimal` into a `BigDecimal`, accepting the
         * same sequence of characters as the [.BigDecimal]
         * constructor, while allowing a sub-array to be specified and
         * with rounding according to the context settings.
         *
         * @implNote If the sequence of characters is already available
         * within a character array, using this constructor is faster than
         * converting the `char` array to string and using the
         * `BigDecimal(String)` constructor.
         *
         * @param  `in` `char` array that is the source of characters.
         * @param  offset first character in the array to inspect.
         * @param  len number of characters to consider.
         * @param  mc the context to use.
         * @throws ArithmeticException if the result is inexact but the
         * rounding mode is `UNNECESSARY`.
         * @throws NumberFormatException if `in` is not a valid
         * representation of a `BigDecimal` or the defined subarray
         * is not wholly within `in`.
         * @since  Java 1.5
         */
        fun valueOf(chars: CharArray, offset: Int, len: Int, mc: MathContext): BigDecimal {
            var offset = offset
            var len = len
            // protect against huge length.
            if (offset + len > chars.size || offset < 0) {
                throw NumberFormatException("Bad offset or len arguments for char[] input.")
            }
            // This is the primary string to BigDecimal constructor; all
            // incoming strings end up here; it uses explicit (inline)
            // parsing for speed and generates at most one intermediate
            // (temporary) object (a char[] array) for non-compact case.

            // Use locals for all fields values until completion
            var prec = 0                 // record precision value
            var scl = 0                  // record scale value
            var rs: Long = 0             // the compact value in long
            var rb: BigInteger? = null   // the inflated value in BigInteger
            // use array bounds checking to handle too-long, len == 0,
            // bad offset, etc.
            try {
                // handle the sign
                var isneg = false          // assume positive
                if (chars[offset] == '-') {
                    isneg = true               // leading minus means negative
                    offset += 1
                    len -= 1
                } else if (chars[offset] == '+') { // leading + allowed
                    offset += 1
                    len -= 1
                }

                // should now be at numeric part of the significand
                var dot = false             // true when there is a '.'
                var exp: Long = 0                    // exponent
                var c: Char                          // current character
                val isCompact = len <= MAX_COMPACT_DIGITS
                // integer significand array & idx is the index to it. The array
                // is ONLY used when we can't use a compact representation.
                var idx = 0
                if (isCompact) {
                    // First compact case, we need not to preserve the character
                    // and we can just compute the value in place.
                    while (len > 0) {
                        c = chars[offset]
                        if (c == '0') { // have zero
                            if (prec == 0)
                                prec = 1
                            else if (rs != 0L) {
                                rs *= 10
                                prec += 1
                            } // else digit is a redundant leading zero
                            if (dot) {
                                scl += 1
                            }
                        } else if (c in '1'..'9') { // have digit
                            val digit = c - '0'
                            if (prec != 1 || rs != 0L)
                                prec += 1 // prec unchanged if preceded by 0s
                            rs = rs * 10 + digit
                            if (dot) {
                                scl += 1
                            }
                        } else if (c == '.') {   // have dot
                            // have dot
                            if (dot) {
                                // two dots
                                throw NumberFormatException("Character array" + " contains more than one decimal point.")
                            }
                            dot = true
                        } else if (Character.isDigit(c)) { // slow path
                            val digit = Character.digit(c, 10)
                            if (digit == 0) {
                                if (prec == 0)
                                    prec = 1
                                else if (rs != 0L) {
                                    rs *= 10
                                    prec += 1
                                } // else digit is a redundant leading zero
                            } else {
                                if (prec != 1 || rs != 0L) {
                                    prec += 1 // prec unchanged if preceded by 0s
                                }
                                rs = rs * 10 + digit
                            }
                            if (dot) {
                                scl += 1
                            }
                        } else if (c == 'e' || c == 'E') {
                            exp = parseExp(chars, offset, len)
                            // Next test is required for backwards compatibility
                            if (exp.toInt().toLong() != exp) {
                                // overflow
                                throw NumberFormatException("Exponent overflow.")
                            }
                            break // [saves a test]
                        } else {
                            throw NumberFormatException(
                                "Character " + c
                                        + " is neither a decimal digit number, decimal point, nor"
                                        + " \"e\" notation exponential mark."
                            )
                        }
                        offset += 1
                        len -= 1
                    }
                    if (prec == 0) {
                        // no digits found
                        throw NumberFormatException("No digits found.")
                    }
                    // Adjust scale if exp is not zero.
                    if (exp != 0L) { // had significant exponent
                        scl = adjustScale(scl, exp)
                    }
                    rs = if (isneg) -rs else rs
                    val mcp = mc.precision
                    var drop = prec - mcp // prec has range [1, MAX_INT], mcp has range [0, MAX_INT];
                    // therefore, this subtract cannot overflow
                    if (mcp > 0 && drop > 0) {  // do rounding
                        while (drop > 0) {
                            scl = checkScaleNonZero(scl.toLong() - drop)
                            rs = divideAndRound(rs, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode)
                            prec = longDigitLength(rs)
                            drop = prec - mcp
                        }
                    }
                } else {
                    val coeff = CharArray(len)
                    while (len > 0) {
                        c = chars[offset]
                        // have digit
                        if ((c in '0'..'9') || Character.isDigit(c)) {
                            // First compact case, we need not to preserve the character
                            // and we can just compute the value in place.
                            if (c == '0' || Character.digit(c, 10) == 0) {
                                if (prec == 0) {
                                    coeff[idx] = c
                                    prec = 1
                                } else if (idx != 0) {
                                    coeff[idx] = c
                                    idx += 1
                                    prec += 1
                                } // else c must be a redundant leading zero
                            } else {
                                if (prec != 1 || idx != 0) {
                                    prec += 1 // prec unchanged if preceded by 0s
                                }
                                coeff[idx] = c
                                idx += 1
                            }
                            if (dot) {
                                scl += 1
                            }
                            offset += 1
                            len -= 1
                            continue
                        }
                        // have dot
                        if (c == '.') {
                            // have dot
                            if (dot) {
                                // two dots
                                throw NumberFormatException(("Character array" + " contains more than one decimal point."))
                            }
                            dot = true
                            offset += 1
                            len -= 1
                            continue
                        }
                        // exponent expected
                        if ((c != 'e') && (c != 'E')) {
                            throw NumberFormatException(("Character array" + " is missing \"e\" notation exponential mark."))
                        }
                        exp = parseExp(chars, offset, len)
                        // Next test is required for backwards compatibility
                        if (exp.toInt().toLong() != exp) {
                            // overflow
                            throw NumberFormatException("Exponent overflow.")
                        }
                        break // [saves a test]
                    }
                    // here when no characters left
                    if (prec == 0) {
                        // no digits found
                        throw NumberFormatException("No digits found.")
                    }
                    // Adjust scale if exp is not zero.
                    if (exp != 0L) { // had significant exponent
                        scl = adjustScale(scl, exp)
                    }
                    // Remove leading zeros from precision (digits count)
                    rb = BigInteger(coeff, if (isneg) -1 else 1, prec)
                    rs = compactValFor(rb)
                    val mcp = mc.precision
                    if (mcp > 0 && (prec > mcp)) {
                        if (rs == INFLATED) {
                            var drop = prec - mcp
                            while (drop > 0) {
                                scl = checkScaleNonZero(scl.toLong() - drop)
                                rb = divideAndRoundByTenPow(rb!!, drop, mc.roundingMode)
                                rs = compactValFor(rb)
                                if (rs != INFLATED) {
                                    prec = longDigitLength(rs)
                                    break
                                }
                                prec = bigDigitLength(rb)
                                drop = prec - mcp
                            }
                        }
                        if (rs != INFLATED) {
                            var drop = prec - mcp
                            while (drop > 0) {
                                scl = checkScaleNonZero(scl.toLong() - drop)
                                rs = divideAndRound(rs, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode)
                                prec = longDigitLength(rs)
                                drop = prec - mcp
                            }
                            rb = null
                        }
                    }
                }
            } catch (e: IndexOutOfBoundsException) {
                throw NumberFormatException()
            } catch (e: IllegalArgumentException) {
                throw NumberFormatException()
            }

            return BigDecimal(rb, rs, scl, prec)
        }

        internal fun valueOf(unscaledVal: Long, scale: Int, prec: Int): BigDecimal {
            if (scale == 0 && unscaledVal >= 0 && unscaledVal < zeroThroughTen.size) {
                return zeroThroughTen[unscaledVal.toInt()]
            } else if (unscaledVal == 0L) {
                return zeroValueOf(scale)
            }
            return BigDecimal(
                if (unscaledVal == INFLATED) INFLATED_BIGINT else null,
                unscaledVal, scale, prec
            )
        }

        internal fun valueOf(intVal: BigInteger, scale: Int, prec: Int): BigDecimal {
            val intCompact = compactValFor(intVal)
            if (intCompact == 0L) {
                return zeroValueOf(scale)
            } else if (scale == 0 && intCompact >= 0 && intCompact < zeroThroughTen.size) {
                return zeroThroughTen[intCompact.toInt()]
            }
            return BigDecimal(intVal, intCompact, scale, prec)
        }

        internal fun zeroValueOf(scale: Int): BigDecimal {
            return if (scale >= 0 && scale < ZERO_SCALED_BY.size)
                ZERO_SCALED_BY[scale]
            else
                BigDecimal(BigInteger.ZERO, 0, scale, 1)
        }

        /* Appease the serialization gods */
        private const val serialVersionUID = 6108874887143696463L

        /**
         * Sentinel value for {@link #intCompact} indicating the
         * significand information is only available from `intVal`.
         */
        internal const val INFLATED = Long.MIN_VALUE

        private val INFLATED_BIGINT = BigInteger.valueOf(INFLATED)

        // All 18-digit base ten strings fit into a long; not all 19-digit
        // strings will
        private const val MAX_COMPACT_DIGITS = 18

        // Cache of common small BigDecimal values.
        private val zeroThroughTen = arrayOf(
            BigDecimal(BigInteger.ZERO, 0, 0, 1),
            BigDecimal(BigInteger.ONE, 1, 0, 1),
            BigDecimal(BigInteger.valueOf(2), 2, 0, 1),
            BigDecimal(BigInteger.valueOf(3), 3, 0, 1),
            BigDecimal(BigInteger.valueOf(4), 4, 0, 1),
            BigDecimal(BigInteger.valueOf(5), 5, 0, 1),
            BigDecimal(BigInteger.valueOf(6), 6, 0, 1),
            BigDecimal(BigInteger.valueOf(7), 7, 0, 1),
            BigDecimal(BigInteger.valueOf(8), 8, 0, 1),
            BigDecimal(BigInteger.valueOf(9), 9, 0, 1),
            BigDecimal(BigInteger.TEN, 10, 0, 2)
        )

        // Cache of zero scaled by 0 - 15
        private val ZERO_SCALED_BY = arrayOf(
            zeroThroughTen[0],
            BigDecimal(BigInteger.ZERO, 0, 1, 1),
            BigDecimal(BigInteger.ZERO, 0, 2, 1),
            BigDecimal(BigInteger.ZERO, 0, 3, 1),
            BigDecimal(BigInteger.ZERO, 0, 4, 1),
            BigDecimal(BigInteger.ZERO, 0, 5, 1),
            BigDecimal(BigInteger.ZERO, 0, 6, 1),
            BigDecimal(BigInteger.ZERO, 0, 7, 1),
            BigDecimal(BigInteger.ZERO, 0, 8, 1),
            BigDecimal(BigInteger.ZERO, 0, 9, 1),
            BigDecimal(BigInteger.ZERO, 0, 10, 1),
            BigDecimal(BigInteger.ZERO, 0, 11, 1),
            BigDecimal(BigInteger.ZERO, 0, 12, 1),
            BigDecimal(BigInteger.ZERO, 0, 13, 1),
            BigDecimal(BigInteger.ZERO, 0, 14, 1),
            BigDecimal(BigInteger.ZERO, 0, 15, 1)
        )

        // Half of Long.MIN_VALUE & Long.MAX_VALUE.
        private const val HALF_LONG_MAX_VALUE = Long.MAX_VALUE / 2
        private const val HALF_LONG_MIN_VALUE = Long.MIN_VALUE / 2

        private val LONG_TEN_POWERS_TABLE = longArrayOf(
            1, // 0 / 10^0
            10, // 1 / 10^1
            100, // 2 / 10^2
            1000, // 3 / 10^3
            10000, // 4 / 10^4
            100000, // 5 / 10^5
            1000000, // 6 / 10^6
            10000000, // 7 / 10^7
            100000000, // 8 / 10^8
            1000000000, // 9 / 10^9
            10000000000L, // 10 / 10^10
            100000000000L, // 11 / 10^11
            1000000000000L, // 12 / 10^12
            10000000000000L, // 13 / 10^13
            100000000000000L, // 14 / 10^14
            1000000000000000L, // 15 / 10^15
            10000000000000000L, // 16 / 10^16
            100000000000000000L, // 17 / 10^17
            1000000000000000000L   // 18 / 10^18
        )

        private var BIG_TEN_POWERS_TABLE = arrayOf(
            BigInteger.ONE,
            BigInteger.valueOf(10),
            BigInteger.valueOf(100),
            BigInteger.valueOf(1000),
            BigInteger.valueOf(10000),
            BigInteger.valueOf(100000),
            BigInteger.valueOf(1000000),
            BigInteger.valueOf(10000000),
            BigInteger.valueOf(100000000),
            BigInteger.valueOf(1000000000),
            BigInteger.valueOf(10000000000L),
            BigInteger.valueOf(100000000000L),
            BigInteger.valueOf(1000000000000L),
            BigInteger.valueOf(10000000000000L),
            BigInteger.valueOf(100000000000000L),
            BigInteger.valueOf(1000000000000000L),
            BigInteger.valueOf(10000000000000000L),
            BigInteger.valueOf(100000000000000000L),
            BigInteger.valueOf(1000000000000000000L)
        )

        private val BIG_TEN_POWERS_TABLE_INITLEN = BIG_TEN_POWERS_TABLE.size
        private val BIG_TEN_POWERS_TABLE_MAX = 16 * BIG_TEN_POWERS_TABLE_INITLEN

        private val THRESHOLDS_TABLE = longArrayOf(
            Long.MAX_VALUE, // 0
            Long.MAX_VALUE / 10L, // 1
            Long.MAX_VALUE / 100L, // 2
            Long.MAX_VALUE / 1000L, // 3
            Long.MAX_VALUE / 10000L, // 4
            Long.MAX_VALUE / 100000L, // 5
            Long.MAX_VALUE / 1000000L, // 6
            Long.MAX_VALUE / 10000000L, // 7
            Long.MAX_VALUE / 100000000L, // 8
            Long.MAX_VALUE / 1000000000L, // 9
            Long.MAX_VALUE / 10000000000L, // 10
            Long.MAX_VALUE / 100000000000L, // 11
            Long.MAX_VALUE / 1000000000000L, // 12
            Long.MAX_VALUE / 10000000000000L, // 13
            Long.MAX_VALUE / 100000000000000L, // 14
            Long.MAX_VALUE / 1000000000000000L, // 15
            Long.MAX_VALUE / 10000000000000000L, // 16
            Long.MAX_VALUE / 100000000000000000L, // 17
            Long.MAX_VALUE / 1000000000000000000L // 18
        )

        // ----==== Constants ====----
        /**
         * The value 0, with a scale of 0.
         *
         * @since  1.5
         */
        val ZERO = zeroThroughTen[0]

        /**
         * The value 1, with a scale of 0.
         *
         * @since  1.5
         */
        val ONE = zeroThroughTen[1]

        /**
         * The value 10, with a scale of 0.
         *
         * @since  1.5
         */
        val TEN = zeroThroughTen[10]


        /**
         * Powers of 10 which can be represented exactly in `double`.
         */
        private val DOUBLE_10_POW = doubleArrayOf(
            1.0e0, 1.0e1, 1.0e2, 1.0e3, 1.0e4,
            1.0e5, 1.0e6, 1.0e7, 1.0e8, 1.0e9,
            1.0e10, 1.0e11, 1.0e12, 1.0e13, 1.0e14,
            1.0e15, 1.0e16, 1.0e17, 1.0e18, 1.0e19,
            1.0e20, 1.0e21, 1.0e22
        )

        /**
         * Powers of 10 which can be represented exactly in `float`.
         */
        private val FLOAT_10_POW = floatArrayOf(
            1.0e0f, 1.0e1f, 1.0e2f, 1.0e3f, 1.0e4f, 1.0e5f, 1.0e6f, 1.0e7f, 1.0e8f, 1.0e9f, 1.0e10f)

        private fun adjustScale(scale: Int, exp: Long): Int {
            var scl = scale
            val adjustedScale = scl - exp
            if (adjustedScale > Int.MAX_VALUE || adjustedScale < Int.MIN_VALUE) {
                throw NumberFormatException("Scale out of range.")
            }
            scl = adjustedScale.toInt()
            return scl
        }

        /*
         * parse exponent
         */
        private fun parseExp(chars: CharArray, offset: Int, length: Int): Long {
            var offset = offset
            var len = length
            var exp: Long = 0
            offset++
            var c = chars[offset]
            len -= 1
            val negexp = c == '-'
            // optional sign
            if (negexp || c == '+') {
                offset++
                c = chars[offset]
                len -= 1
            }
            if (len <= 0) {
                // no exponent digits
                throw NumberFormatException()
            }
            // skip leading zeros in the exponent
            while (len > 10 && (c == '0' || Character.digit(c, 10) == 0)) {
                offset += 1
                c = chars[offset]
                len -= 1
            }
            if (len > 10) {
                // too many nonzero exponent digits
                throw NumberFormatException()
            }
            // c now holds first digit of exponent
            while (true) {
                val v: Int
                if (c in '0'..'9') {
                    v = c - '0'
                } else {
                    v = Character.digit(c, 10)
                    if (v < 0) {
                        // not a digit
                        throw NumberFormatException()
                    }
                }
                exp = exp * 10 + v
                if (len == 1)
                    break // that was final character
                offset += 1
                c = chars[offset]
                len -= 1
            }
            if (negexp)
            // apply sign
                exp = -exp
            return exp
        } // private fun parseExp(chars: CharArray, offset: Int, length: Int): Long

        /**
         * Return 10 to the power n, as a `BigInteger`.
         *
         * @param  n the power of ten to be returned (>=0)
         * @return a `BigInteger` with the value (10<sup>n</sup>)
         */
        private fun bigTenToThe(n: Int): BigInteger {
            if (n < 0) {
                return BigInteger.ZERO
            }
            if (n < BIG_TEN_POWERS_TABLE_MAX) {
                val pows = BIG_TEN_POWERS_TABLE
                return if (n < pows.size)
                    pows[n]
                else
                    expandBigIntegerTenPowers(n)
            }

            return BigInteger.TEN.pow(n)
        }

        /**
         * Expand the BIG_TEN_POWERS_TABLE array to contain at least 10**n.
         *
         * @param n the power of ten to be returned (>=0)
         * @return a `BigDecimal` with the value (10<sup>n</sup>) and
         * in the meantime, the BIG_TEN_POWERS_TABLE array gets
         * expanded to the size greater than n.
         */
        internal fun _expandBigIntegerTenPowers(n: Int): BigInteger {
            var pows = BIG_TEN_POWERS_TABLE
            val curLen = pows.size
            // The following comparison and the above synchronized statement is
            // to prevent multiple threads from expanding the same array.
            if (curLen <= n) {
                var newLen = curLen shl 1
                while (newLen <= n) {
                    newLen = newLen shl 1
                }
                pows = Array(newLen) {
                    if (it < curLen) pows[it] else BigInteger.TEN
                }
                for (i in curLen until newLen) {
                    // pows[i] is BigInteger.TEN now
                    pows[i] *= pows[i - 1]
                }
                // Based on the following facts:
                // 1. pows is a private local varible;
                // 2. the following store is a volatile store.
                // the newly created array elements can be safely published.
                BIG_TEN_POWERS_TABLE = pows
            }
            return pows[n]
        }

        /**
         * Compute val * 10 ^ n; return this product if it is
         * representable as a long, INFLATED otherwise.
         */
        private fun longMultiplyPowerTen(longVal: Long, n: Int): Long {
            if (longVal == 0L || n <= 0) {
                return longVal
            }
            val tab = LONG_TEN_POWERS_TABLE
            val bounds = THRESHOLDS_TABLE
            if (n < tab.size && n < bounds.size) {
                val tenpower = tab[n]
                if (longVal == 1L)
                    return tenpower
                if (longVal.absoluteValue <= bounds[n])
                    return longVal * tenpower
            }
            return INFLATED
        }

        /**
         * Match the scales of two `BigDecimal`s to align their
         * least significant digits.
         *
         *
         * If the scales of bigDecimals[0] and bigDecimals[1] differ, rescale
         * (non-destructively) the lower-scaled `BigDecimal` so
         * they match.  That is, the lower-scaled reference will be
         * replaced by a reference to a new object with the same scale as
         * the other `BigDecimal`.
         *
         * @param  bigDecimals array of two elements referring to the two
         * `BigDecimal`s to be aligned.
         */
        private fun matchScale(bigDecimals: Array<BigDecimal>) {
            if (bigDecimals[0].scale == bigDecimals[1].scale) {
                return
            } else if (bigDecimals[0].scale < bigDecimals[1].scale) {
                bigDecimals[0] = bigDecimals[0].setScale(bigDecimals[1].scale, RoundingMode.UNNECESSARY)
            } else if (bigDecimals[1].scale < bigDecimals[0].scale) {
                bigDecimals[1] = bigDecimals[1].setScale(bigDecimals[0].scale, RoundingMode.UNNECESSARY)
            }
        }

        /**
         * Returns the length of the absolute value of a `long`, in decimal
         * digits.
         *
         * @param x the `long`
         * @return the length of the unscaled value, in deciaml digits.
         */
        fun longDigitLength(x: Long): Int {
            var x = x
            /*
             * As described in "Bit Twiddling Hacks" by Sean Anderson,
             * (http://graphics.stanford.edu/~seander/bithacks.html)
             * integer log 10 of x is within 1 of (1233/4096)* (1 +
             * integer log 2 of x). The fraction 1233/4096 approximates
             * log10(2). So we first do a version of log2 (a variant of
             * Long class with pre-checks and opposite directionality) and
             * then scale and check against powers table. This is a little
             * simpler in present context than the version in Hacker's
             * Delight sec 11-4. Adding one to bit length allows comparing
             * downward from the LONG_TEN_POWERS_TABLE that we need
             * anyway.
             */
            if (x == BigDecimal.INFLATED) {
                throw AssertionError()
            }
            if (x < 0) {
                x = -x
            }
            if (x < 10) {
                // must screen for 0, might as well 10
                return 1
            }
            val r = ((64 - Integers.numberOfLeadingZeros(x) + 1) * 1233).ushr(12)
            val tab = LONG_TEN_POWERS_TABLE
            // if r >= length, must have max possible digits for long
            return if (r >= tab.size || x < tab[r]) r else r + 1
        }

        /**
         * Returns the length of the absolute value of a BigInteger, in
         * decimal digits.
         *
         * @param b the BigInteger
         * @return the length of the unscaled value, in decimal digits
         */
        private fun bigDigitLength(b: BigInteger): Int {
            /*
             * Same idea as the long version, but we need a better
             * approximation of log10(2). Using 646456993/2^31
             * is accurate up to max possible reported bitLength.
             */
            if (b.signum == 0) {
                return 1
            }
            val r = ((b.bitLength().toLong() + 1) * 646456993).ushr(31).toInt()
            return if (b.compareMagnitude(bigTenToThe(r)) < 0) r else r + 1
        }

        /**
         * Returns the compact value for given `BigInteger`, or
         * INFLATED if too big. Relies on internal representation of
         * `BigInteger`.
         */
        private fun compactValFor(b: BigInteger): Long {
            val m = b.mag
            val len = m.size
            if (len == 0) {
                return 0
            }
            val d = m[0]
            if (len > 2 || len == 2 && d < 0)
                return INFLATED

            val u = if (len == 2)
                (m[1].toLong() and BigInteger.LONG_MASK) + (d.toLong() shl 32)
            else
                d.toLong() and BigInteger.LONG_MASK
            return if (b.signum < 0) -u else u
        }

        private fun longCompareMagnitude(a: Long, b: Long): Int {
            val x = a.absoluteValue
            val y = b.absoluteValue
            return if (x < y) -1 else if (x == y) 0 else 1
        }

        private fun saturateLong(s: Long): Int {
            val i = s.toInt()
            return if (s == i.toLong()) i else if (s < 0) Int.MIN_VALUE else Int.MAX_VALUE
        }

        /* the same as checkScale where value!=0 */
        private fun checkScaleNonZero(longVal: Long): Int {
            val asInt = longVal.toInt()
            if (asInt.toLong() != longVal) {
                throw ArithmeticException(if (asInt > 0) "Underflow" else "Overflow")
            }
            return asInt
        }

        private fun checkScale(intCompact: Long, longVal: Long): Int {
            var asInt = longVal.toInt()
            if (asInt.toLong() != longVal) {
                asInt = if (longVal > Int.MAX_VALUE) Int.MAX_VALUE else Int.MIN_VALUE
                if (intCompact != 0L) {
                    throw ArithmeticException(if (asInt > 0) "Underflow" else "Overflow")
                }
            }
            return asInt
        }

        private fun checkScale(intVal: BigInteger, longVal: Long): Int {
            var asInt = longVal.toInt()
            if (asInt.toLong() != longVal) {
                asInt = if (longVal > Int.MAX_VALUE) Int.MAX_VALUE else Int.MIN_VALUE
                if (intVal.signum() != 0) {
                    throw ArithmeticException(if (asInt > 0) "Underflow" else "Overflow")
                }
            }
            return asInt
        }

        /**
         * Shared logic of need increment computation.
         */
        private fun commonNeedIncrement(
            roundingMode: RoundingMode, qsign: Int,
            cmpFracHalf: Int, oddQuot: Boolean
        ): Boolean {
            when (roundingMode) {
                RoundingMode.UNNECESSARY -> throw ArithmeticException("Rounding necessary")

                RoundingMode.UP // Away from zero
                -> return true

                RoundingMode.DOWN // Towards zero
                -> return false

                RoundingMode.CEILING // Towards +infinity
                -> return qsign > 0

                RoundingMode.FLOOR // Towards -infinity
                -> return qsign < 0

                else // Some kind of half-way rounding
                -> {
                    if (roundingMode < RoundingMode.HALF_UP || roundingMode > RoundingMode.HALF_EVEN) {
                        throw AssertionError("Unexpected rounding mode: $roundingMode")
                    }

                    if (cmpFracHalf < 0)
                    // We're closer to higher digit
                        return false
                    else if (cmpFracHalf > 0)
                    // We're closer to lower digit
                        return true
                    else { // half-way
                        if (cmpFracHalf != 0) {
                            throw AssertionError()
                        }

                        return when (roundingMode) {
                            RoundingMode.HALF_DOWN -> false
                            RoundingMode.HALF_UP -> true
                            RoundingMode.HALF_EVEN -> oddQuot
                            else -> throw AssertionError("Unexpected rounding mode$roundingMode")
                        }
                    }
                }
            }
        }

        /**
         * Tests if quotient has to be incremented according the roundingMode
         */
        private fun needIncrement(
            ldivisor: Long, roundingMode: RoundingMode,
            qsign: Int, q: Long, r: Long
        ): Boolean {
            if (r == 0L) {
                throw AssertionError()
            }

            val cmpFracHalf = if (r <= HALF_LONG_MIN_VALUE || r > HALF_LONG_MAX_VALUE) {
                1 // 2 * r can't fit into long
            } else {
                longCompareMagnitude(2 * r, ldivisor)
            }

            return commonNeedIncrement(roundingMode, qsign, cmpFracHalf, q and 1L != 0L)
        }

        /**
         * Tests if quotient has to be incremented according the roundingMode
         */
        private fun needIncrement(
            ldivisor: Long, roundingMode: RoundingMode,
            qsign: Int, mq: MutableBigInteger, r: Long
        ): Boolean {
            if (r == 0L) {
                throw AssertionError("r is zero")
            }

            val cmpFracHalf: Int = if (r <= HALF_LONG_MIN_VALUE || r > HALF_LONG_MAX_VALUE) {
                1 // 2 * r can't fit into long
            } else {
                longCompareMagnitude(2 * r, ldivisor)
            }

            return commonNeedIncrement(roundingMode, qsign, cmpFracHalf, mq.isOdd())
        }

        /*
         * returns INFLATED if oveflow
         */
        private fun add(xs: Long, ys: Long): Long {
            val sum = xs + ys
            // See "Hacker's Delight" section 2-12 for explanation of
            // the overflow test.
            return if (sum xor xs and (sum xor ys) >= 0L) { // not overflowed
                sum
            } else INFLATED
        }

        private fun add(xs: Long, ys: Long, scale: Int): BigDecimal {
            val sum = add(xs, ys)
            return if (sum != INFLATED) BigDecimal.valueOf(sum, scale) else BigDecimal(
                BigInteger.valueOf(xs).add(ys),
                scale
            )
        }

        private fun add(xs: Long, scale1: Int, ys: Long, scale2: Int): BigDecimal {
            val sdiff = scale1.toLong() - scale2
            if (sdiff == 0L) {
                return add(xs, ys, scale1)
            } else if (sdiff < 0) {
                val raise = checkScale(xs, -sdiff)
                val scaledX = longMultiplyPowerTen(xs, raise)
                if (scaledX != INFLATED) {
                    return add(scaledX, ys, scale2)
                } else {
                    val bigsum = bigMultiplyPowerTen(xs, raise).add(ys)
                    return if (xs xor ys >= 0)
                    // same sign test
                        BigDecimal(bigsum, INFLATED, scale2, 0)
                    else
                        valueOf(bigsum, scale2, 0)
                }
            } else {
                val raise = checkScale(ys, sdiff)
                val scaledY = longMultiplyPowerTen(ys, raise)
                if (scaledY != INFLATED) {
                    return add(xs, scaledY, scale1)
                } else {
                    val bigsum = bigMultiplyPowerTen(ys, raise).add(xs)
                    return if (xs xor ys >= 0)
                        BigDecimal(bigsum, INFLATED, scale1, 0)
                    else
                        valueOf(bigsum, scale1, 0)
                }
            }
        }

        private fun add(xs: Long, scale1: Int, snd: BigInteger, scale2: Int): BigDecimal {
            var snd = snd
            var rscale = scale1
            val sdiff = rscale.toLong() - scale2
            val sameSigns = Integers.signum(xs) == snd.signum
            val sum: BigInteger
            if (sdiff < 0) {
                val raise = checkScale(xs, -sdiff)
                rscale = scale2
                val scaledX = longMultiplyPowerTen(xs, raise)
                if (scaledX == INFLATED) {
                    sum = snd + bigMultiplyPowerTen(xs, raise)
                } else {
                    sum = snd.add(scaledX)
                }
            } else { //if (sdiff > 0) {
                val raise = checkScale(snd, sdiff)
                snd = bigMultiplyPowerTen(snd, raise)
                sum = snd.add(xs)
            }
            return if (sameSigns)
                BigDecimal(sum, INFLATED, rscale, 0)
            else
                valueOf(sum, rscale, 0)
        }

        private fun add(fst: BigInteger, scale1: Int, snd: BigInteger, scale2: Int): BigDecimal {
            var fst = fst
            var snd = snd
            var rscale = scale1
            val sdiff = rscale.toLong() - scale2
            if (sdiff != 0L) {
                if (sdiff < 0) {
                    val raise = checkScale(fst, -sdiff)
                    rscale = scale2
                    fst = bigMultiplyPowerTen(fst, raise)
                } else {
                    val raise = checkScale(snd, sdiff)
                    snd = bigMultiplyPowerTen(snd, raise)
                }
            }
            val sum = fst + snd
            return if (fst.signum == snd.signum)
                BigDecimal(sum, INFLATED, rscale, 0)
            else
                valueOf(sum, rscale, 0)
        }

        private fun bigMultiplyPowerTen(value: Long, n: Int): BigInteger {
            return if (n <= 0) BigInteger.valueOf(value) else bigTenToThe(n).multiply(value)
        }

        private fun bigMultiplyPowerTen(value: BigInteger, n: Int): BigInteger {
            if (n <= 0) {
                return value
            }
            return if (n < LONG_TEN_POWERS_TABLE.size) {
                value.multiply(LONG_TEN_POWERS_TABLE[n])
            } else value * bigTenToThe(n)
        }

        /*
         * calculate divideAndRound for ldividend*10^raise / divisor
         * when abs(dividend)==abs(divisor);
         */
        private fun roundedTenPower(qsign: Int, raise: Int, scale: Int, preferredScale: Int): BigDecimal {
            if (scale > preferredScale) {
                val diff = scale - preferredScale
                return if (diff < raise) {
                    scaledTenPow(raise - diff, qsign, preferredScale)
                } else {
                    valueOf(qsign.toLong(), scale - raise)
                }
            } else {
                return scaledTenPow(raise, qsign, scale)
            }
        }

        fun scaledTenPow(n: Int, sign: Int, scale: Int): BigDecimal {
            return if (n < LONG_TEN_POWERS_TABLE.size)
                valueOf(sign * LONG_TEN_POWERS_TABLE[n], scale)
            else {
                var unscaledVal = bigTenToThe(n)
                if (sign == -1) {
                    unscaledVal = -unscaledVal
                }
                BigDecimal(unscaledVal, INFLATED, scale, n + 1)
            }
        }

        private fun divWord(n: Long, dLong: Long): Long {
            var r: Long
            var q: Long
            if (dLong == 1L) {
                q = n.toInt().toLong()
                return q and BigInteger.LONG_MASK
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
            return r shl 32 or (q and BigInteger.LONG_MASK)
        }

        private fun make64(hi: Long, lo: Long): Long {
            return hi shl 32 or lo
        }

        private fun mulsub(u1: Long, u0: Long, v1: Long, v0: Long, q0: Long): Long {
            val tmp = u0 - q0 * v0
            return make64(u1 + tmp.ushr(32) - q0 * v1, tmp and BigInteger.LONG_MASK)
        }

        private fun unsignedLongCompare(one: Long, two: Long): Boolean {
            return one + Long.MIN_VALUE > two + Long.MIN_VALUE
        }

        private fun unsignedLongCompareEq(one: Long, two: Long): Boolean {
            return one + Long.MIN_VALUE >= two + Long.MIN_VALUE
        }

        // Compare Normalize dividend & divisor so that both fall into [0.1, 0.999...]
        private fun compareMagnitudeNormalized(xs: Long, xscale: Int, ys: Long, yscale: Int): Int {
            var xs = xs
            var ys = ys
            // assert xs!=0 && ys!=0
            val sdiff = xscale - yscale
            if (sdiff != 0) {
                if (sdiff < 0) {
                    xs = longMultiplyPowerTen(xs, -sdiff)
                } else { // sdiff > 0
                    ys = longMultiplyPowerTen(ys, sdiff)
                }
            }
            return if (xs != INFLATED)
                if (ys != INFLATED) longCompareMagnitude(xs, ys) else -1
            else
                1
        }

        // Compare Normalize dividend & divisor so that both fall into [0.1, 0.999...]
        private fun compareMagnitudeNormalized(xs: Long, xscale: Int, ys: BigInteger, yscale: Int): Int {
            // assert "ys can't be represented as long"
            if (xs == 0L) {
                return -1
            }
            val sdiff = xscale - yscale
            if (sdiff < 0) {
                if (longMultiplyPowerTen(xs, -sdiff) == INFLATED) {
                    return bigMultiplyPowerTen(xs, -sdiff).compareMagnitude(ys)
                }
            }
            return -1
        }

        // Compare Normalize dividend & divisor so that both fall into [0.1, 0.999...]
        private fun compareMagnitudeNormalized(xs: BigInteger, xscale: Int, ys: BigInteger, yscale: Int): Int {
            val sdiff = xscale - yscale
            return if (sdiff < 0) {
                bigMultiplyPowerTen(xs, -sdiff).compareMagnitude(ys)
            } else { // sdiff >= 0
                xs.compareMagnitude(bigMultiplyPowerTen(ys, sdiff))
            }
        }

        private fun multiply(x: Long, y: Long): Long {
            val product = x * y
            val ax = x.absoluteValue
            val ay = y.absoluteValue
            return if ((ax or ay).ushr(31) == 0L || y == 0L || product / y == x) {
                product
            } else INFLATED
        }

        private fun multiply(x: Long, y: Long, scale: Int): BigDecimal {
            val product = multiply(x, y)
            return if (product != INFLATED) {
                valueOf(product, scale)
            } else BigDecimal(BigInteger.valueOf(x).multiply(y), INFLATED, scale, 0)
        }

        private fun multiply(x: Long, y: BigInteger, scale: Int): BigDecimal {
            return if (x == 0L) {
                zeroValueOf(scale)
            } else BigDecimal(y.multiply(x), INFLATED, scale, 0)
        }

        private fun multiply(x: BigInteger, y: BigInteger, scale: Int): BigDecimal {
            return BigDecimal(x * y, INFLATED, scale, 0)
        }

        /**
         * Remove insignificant trailing zeros from this
         * `BigInteger` value until the preferred scale is reached or no
         * more zeros can be removed.  If the preferred scale is less than
         * Integer.MIN_VALUE, all the trailing zeros will be removed.
         *
         * @return new `BigDecimal` with a scale possibly reduced
         * to be closed to the preferred scale.
         */
        private fun createAndStripZerosToMatchScale(intVal: BigInteger, scale: Int, preferredScale: Long): BigDecimal {
            var intVal = intVal
            var scale = scale
            var qr: Array<BigInteger> // quotient-remainder pair
            while (intVal.compareMagnitude(BigInteger.TEN) >= 0 && scale > preferredScale) {
                if (intVal.testBit(0))
                    break // odd number cannot end in 0
                qr = intVal.divideAndRemainder(BigInteger.TEN)
                if (qr[1].signum() != 0)
                    break // non-0 remainder
                intVal = qr[0]
                scale = checkScale(intVal, scale.toLong() - 1) // could Overflow
            }
            return valueOf(intVal, scale, 0)
        }

        /**
         * Remove insignificant trailing zeros from this
         * `long` value until the preferred scale is reached or no
         * more zeros can be removed.  If the preferred scale is less than
         * Integer.MIN_VALUE, all the trailing zeros will be removed.
         *
         * @return new `BigDecimal` with a scale possibly reduced
         * to be closed to the preferred scale.
         */
        private fun createAndStripZerosToMatchScale(compactVal: Long, scale: Int, preferredScale: Long): BigDecimal {
            var compactVal = compactVal
            var scale = scale
            while (compactVal.absoluteValue >= 10L && scale > preferredScale) {
                if (compactVal and 1L != 0L)
                    break // odd number cannot end in 0
                val r = compactVal % 10L
                if (r != 0L)
                    break // non-0 remainder
                compactVal /= 10
                scale = checkScale(compactVal, scale.toLong() - 1) // could Overflow
            }
            return valueOf(compactVal, scale)
        }

        private fun stripZerosToMatchScale(
            intVal: BigInteger?,
            intCompact: Long,
            scale: Int,
            preferredScale: Int
        ): BigDecimal {
            return if (intCompact != INFLATED) {
                createAndStripZerosToMatchScale(intCompact, scale, preferredScale.toLong())
            } else {
                createAndStripZerosToMatchScale(
                    intVal ?: INFLATED_BIGINT,
                    scale, preferredScale.toLong()
                )
            }
        }

        private val LONGLONG_TEN_POWERS_TABLE = arrayOf(
            longArrayOf(0L, -0x7538dcfb76180000L), //10^19
            longArrayOf(0x5L, 0x6bc75e2d63100000L), //10^20
            longArrayOf(0x36L, 0x35c9adc5dea00000L), //10^21
            longArrayOf(0x21eL, 0x19e0c9bab2400000L), //10^22
            longArrayOf(0x152dL, 0x02c7e14af6800000L), //10^23
            longArrayOf(0xd3c2L, 0x1bcecceda1000000L), //10^24
            longArrayOf(0x84595L, 0x161401484a000000L), //10^25
            longArrayOf(0x52b7d2L, -0x2337f32d1c000000L), //10^26
            longArrayOf(0x33b2e3cL, -0x602f7fc318000000L), //10^27
            longArrayOf(0x204fce5eL, 0x3e25026110000000L), //10^28
            longArrayOf(0x1431e0faeL, 0x6d7217caa0000000L), //10^29
            longArrayOf(0xc9f2c9cd0L, 0x4674edea40000000L), //10^30
            longArrayOf(0x7e37be2022L, -0x3f6eb4d980000000L), //10^31
            longArrayOf(0x4ee2d6d415bL, -0x7a53107f00000000L), //10^32
            longArrayOf(0x314dc6448d93L, 0x38c15b0a00000000L), //10^33
            longArrayOf(0x1ed09bead87c0L, 0x378d8e6400000000L), //10^34
            longArrayOf(0x13426172c74d82L, 0x2b878fe800000000L), //10^35
            longArrayOf(0xc097ce7bc90715L, -0x4cb460f000000000L), //10^36
            longArrayOf(0x785ee10d5da46d9L, 0x00f436a000000000L), //10^37
            longArrayOf(0x4b3b4ca85a86c47aL, 0x098a224000000000L)
        )//10^38

        private const val DIV_NUM_BASE = 1L shl 32 // Number base (32 bits).

        /*
         * returns precision of 128-bit value
         */
        private fun precision(hi: Long, lo: Long): Int {
            if (hi == 0L) {
                if (lo >= 0) {
                    return longDigitLength(lo)
                }
                return if (unsignedLongCompareEq(lo, LONGLONG_TEN_POWERS_TABLE[0][1])) 20 else 19
                // 0x8AC7230489E80000L  = unsigned 2^19
            }
            val r = ((128 - Integers.numberOfLeadingZeros(hi) + 1) * 1233).ushr(12)
            val idx = r - 19
            return if (idx >= LONGLONG_TEN_POWERS_TABLE.size || longLongCompareMagnitude(
                    hi, lo,
                    LONGLONG_TEN_POWERS_TABLE[idx][0], LONGLONG_TEN_POWERS_TABLE[idx][1]
                )
            ) r else r + 1
        }

        /*
         * returns true if 128 bit number <hi0,lo0> is less then <hi1,lo1>
         * hi0 & hi1 should be non-negative
         */
        private fun longLongCompareMagnitude(hi0: Long, lo0: Long, hi1: Long, lo1: Long): Boolean {
            return if (hi0 != hi1) {
                hi0 < hi1
            } else lo0 + Long.MIN_VALUE < lo1 + Long.MIN_VALUE
        }

        private fun compareInCompareMagnitude(xs: Long, ys: Long, sdiff: Long): Pair<Boolean, Long> {
            if (sdiff > Int.MAX_VALUE) {
                return Pair(false, xs)
            }
            if (xs == INFLATED) {
                return if (ys == INFLATED) Pair(true, xs) else Pair(false, xs)
            }

            return if (xs == INFLATED && ys == INFLATED) Pair(true, xs) else Pair(false, xs)
        }

        // ----==== For division ====----
        private fun divide(
            dividend: Long,
            dividendScale: Int,
            divisor: Long,
            divisorScale: Int,
            scale: Int,
            roundingMode: RoundingMode
        ): BigDecimal {
            if (checkScale(dividend, scale.toLong() + divisorScale) > dividendScale) {
                val newScale = scale + divisorScale
                val raise = newScale - dividendScale
                if (raise < LONG_TEN_POWERS_TABLE.size) {
                    val xs = longMultiplyPowerTen(dividend, raise)
                    if (xs != INFLATED) {
                        return divideAndRound(xs, divisor, scale, roundingMode, scale)
                    }
                    val q = multiplyDivideAndRound(
                        LONG_TEN_POWERS_TABLE[raise],
                        dividend,
                        divisor,
                        scale,
                        roundingMode,
                        scale
                    )
                    if (q != null) {
                        return q
                    }
                }
                val scaledDividend = bigMultiplyPowerTen(dividend, raise)
                return divideAndRound(scaledDividend, divisor, scale, roundingMode, scale)
            } else {
                val newScale = checkScale(divisor, dividendScale.toLong() - scale)
                val raise = newScale - divisorScale
                if (raise < LONG_TEN_POWERS_TABLE.size) {
                    val ys = longMultiplyPowerTen(divisor, raise)
                    if (ys != INFLATED) {
                        return divideAndRound(dividend, ys, scale, roundingMode, scale)
                    }
                }
                val scaledDivisor = bigMultiplyPowerTen(divisor, raise)
                return divideAndRound(BigInteger.valueOf(dividend), scaledDivisor.toLong(),
                        scale, roundingMode, scale)
            }
        }

        private fun divide(
            dividend: BigInteger,
            dividendScale: Int,
            divisor: Long,
            divisorScale: Int,
            scale: Int,
            roundingMode: RoundingMode
        ): BigDecimal {
            if (checkScale(dividend, scale.toLong() + divisorScale) > dividendScale) {
                val newScale = scale + divisorScale
                val raise = newScale - dividendScale
                val scaledDividend = bigMultiplyPowerTen(dividend, raise)
                return divideAndRound(scaledDividend, divisor, scale, roundingMode, scale)
            } else {
                val newScale = checkScale(divisor, dividendScale.toLong() - scale)
                val raise = newScale - divisorScale
                if (raise < LONG_TEN_POWERS_TABLE.size) {
                    val ys = longMultiplyPowerTen(divisor, raise)
                    if (ys != INFLATED) {
                        return divideAndRound(dividend, ys, scale, roundingMode, scale)
                    }
                }
                val scaledDivisor = bigMultiplyPowerTen(divisor, raise)
                return divideAndRound(dividend, scaledDivisor.toLong(), scale, roundingMode, scale)
            }
        }

        private fun divide(
            dividend: Long,
            dividendScale: Int,
            divisor: BigInteger,
            divisorScale: Int,
            scale: Int,
            roundingMode: RoundingMode
        ): BigDecimal {
            return if (checkScale(dividend, scale.toLong() + divisorScale) > dividendScale) {
                val newScale = scale + divisorScale
                val raise = newScale - dividendScale
                val scaledDividend = bigMultiplyPowerTen(dividend, raise)
                divideAndRound(scaledDividend, divisor.toLong(), scale, roundingMode, scale)
            } else {
                val newScale = checkScale(divisor, dividendScale.toLong() - scale)
                val raise = newScale - divisorScale
                val scaledDivisor = bigMultiplyPowerTen(divisor, raise)
                divideAndRound(BigInteger.valueOf(dividend), scaledDivisor.toLong(), scale, roundingMode, scale)
            }
        }

        private fun divide(
            dividend: BigInteger,
            dividendScale: Int,
            divisor: BigInteger,
            divisorScale: Int,
            scale: Int,
            roundingMode: RoundingMode
        ): BigDecimal {
            return if (checkScale(dividend, scale.toLong() + divisorScale) > dividendScale) {
                val newScale = scale + divisorScale
                val raise = newScale - dividendScale
                val scaledDividend = bigMultiplyPowerTen(dividend, raise)
                divideAndRound(scaledDividend, divisor.toLong(), scale, roundingMode, scale)
            } else {
                val newScale = checkScale(divisor, dividendScale.toLong() - scale)
                val raise = newScale - divisorScale
                val scaledDivisor = bigMultiplyPowerTen(divisor, raise)
                divideAndRound(dividend, scaledDivisor.toLong(), scale, roundingMode, scale)
            }
        }

        /**
         * Internally used for division operation for division `long` by
         * `long`.
         * The returned `BigDecimal` object is the quotient whose scale is set
         * to the passed in scale. If the remainder is not zero, it will be rounded
         * based on the passed in roundingMode. Also, if the remainder is zero and
         * the last parameter, i.e. preferredScale is NOT equal to scale, the
         * trailing zeros of the result is stripped to match the preferredScale.
         */
        private fun divideAndRound(
            ldividend: Long, ldivisor: Long, scale: Int, roundingMode: RoundingMode,
            preferredScale: Int
        ): BigDecimal {
            val q = ldividend / ldivisor // store quotient in long
            if (roundingMode == RoundingMode.DOWN && scale == preferredScale)
                return valueOf(q, scale)
            val r = ldividend % ldivisor // store remainder in long
            val qsign = if (ldividend < 0 == ldivisor < 0) 1 else -1 // quotient sign
            return if (r != 0L) {
                val increment = needIncrement(ldivisor, roundingMode, qsign, q, r)
                valueOf(if (increment) q + qsign else q, scale)
            } else {
                if (preferredScale != scale)
                    createAndStripZerosToMatchScale(q, scale, preferredScale.toLong())
                else
                    valueOf(q, scale)
            }
        }

        /**
         * Divides `long` by `long` and do rounding based on the
         * passed in roundingMode.
         */
        private fun divideAndRound(ldividend: Long, ldivisor: Long, roundingMode: RoundingMode): Long {
            val q = ldividend / ldivisor // store quotient in long
            if (roundingMode == RoundingMode.DOWN) {
                return q
            }
            val r = ldividend % ldivisor // store remainder in long
            val qsign = if (ldividend < 0 == ldivisor < 0) 1 else -1 // quotient sign
            return if (r != 0L) {
                val increment = needIncrement(ldivisor, roundingMode, qsign, q, r)
                if (increment) q + qsign else q
            } else {
                q
            }
        }

        /**
         * Divides `BigInteger` value by `long` value and
         * do rounding based on the passed in roundingMode.
         */
        private fun divideAndRound(bdividend: BigInteger, ldivisor: Long, roundingMode: RoundingMode): BigInteger {
            // Descend into mutables for faster remainder checks
            val mdividend = MutableBigInteger(bdividend.mag)
            // store quotient
            val mq = MutableBigInteger()
            // store quotient & remainder in long
            val r = mdividend.divide(ldivisor, mq)
            // record remainder is zero or not
            val isRemainderZero = r == 0L
            // quotient sign
            val qsign = if (ldivisor < 0) -bdividend.signum else bdividend.signum
            if (!isRemainderZero) {
                if (needIncrement(ldivisor, roundingMode, qsign, mq, r)) {
                    mq += MutableBigInteger.ONE
                }
            }
            return mq.toBigInteger(qsign)
        }

        /**
         * Internally used for division operation for division `BigInteger`
         * by `long`.
         * The returned `BigDecimal` object is the quotient whose scale is set
         * to the passed in scale. If the remainder is not zero, it will be rounded
         * based on the passed in roundingMode. Also, if the remainder is zero and
         * the last parameter, i.e. preferredScale is NOT equal to scale, the
         * trailing zeros of the result is stripped to match the preferredScale.
         */
        private fun divideAndRound(
            bdividend: BigInteger,
            ldivisor: Long, scale: Int, roundingMode: RoundingMode, preferredScale: Int
        ): BigDecimal {
            // Descend into mutables for faster remainder checks
            val mdividend = MutableBigInteger(bdividend.mag)
            // store quotient
            val mq = MutableBigInteger()
            // store quotient & remainder in long
            val r = mdividend.divide(ldivisor, mq)
            // record remainder is zero or not
            val isRemainderZero = r == 0L
            // quotient sign
            val qsign = if (ldivisor < 0) -bdividend.signum else bdividend.signum
            if (!isRemainderZero) {
                if (needIncrement(ldivisor, roundingMode, qsign, mq, r)) {
                    mq += MutableBigInteger.ONE
                }
                return mq.toBigDecimal(qsign, scale)
            } else {
                if (preferredScale != scale) {
                    val compactVal = mq.toCompactValue(qsign)
                    if (compactVal != INFLATED) {
                        return createAndStripZerosToMatchScale(compactVal, scale, preferredScale.toLong())
                    }
                    val intVal = mq.toBigInteger(qsign)
                    return createAndStripZerosToMatchScale(intVal, scale, preferredScale.toLong())
                } else {
                    return mq.toBigDecimal(qsign, scale)
                }
            }
        }

        /**
         * Returns a `BigDecimal` whose value is `(xs /
         * ys)`, with rounding according to the context settings.
         */
        private fun divide(
            xs: Long,
            xscale: Int,
            ys: Long,
            yscale: Int,
            preferredScale: Long,
            mc: MathContext
        ): BigDecimal {
            var yscale = yscale
            val mcp = mc.precision
            if (yscale in xscale..17 && mcp < 18) {
                return divideSmallFastPath(xs, xscale, ys, yscale, preferredScale, mc)
            }
            if (compareMagnitudeNormalized(xs, xscale, ys, yscale) > 0) {// satisfy constraint (b)
                yscale -= 1 // [that is, divisor *= 10]
            }
            val roundingMode = mc.roundingMode
            // In order to find out whether the divide generates the exact result,
            // we avoid calling the above divide method. 'quotient' holds the
            // return BigDecimal object whose scale will be set to 'scl'.
            val scl = checkScaleNonZero(preferredScale + yscale - xscale + mcp)
            val quotient: BigDecimal
            if (checkScaleNonZero(mcp.toLong() + yscale - xscale) > 0) {
                val raise = checkScaleNonZero(mcp.toLong() + yscale - xscale)
                val scaledXs: Long = longMultiplyPowerTen(xs, raise)
                quotient = if (scaledXs == INFLATED) {
                    val rb = bigMultiplyPowerTen(xs, raise)
                    divideAndRound(rb, ys, scl, roundingMode, checkScaleNonZero(preferredScale))
                } else {
                    divideAndRound(scaledXs, ys, scl, roundingMode, checkScaleNonZero(preferredScale))
                }
            } else {
                val newScale = checkScaleNonZero(xscale.toLong() - mcp)
                // assert newScale >= yscale
                if (newScale == yscale) { // easy case
                    quotient = divideAndRound(xs, ys, scl, roundingMode, checkScaleNonZero(preferredScale))
                } else {
                    val raise = checkScaleNonZero(newScale.toLong() - yscale)
                    val scaledYs: Long = longMultiplyPowerTen(ys, raise)
                    quotient = if (scaledYs == INFLATED) {
                        val rb = bigMultiplyPowerTen(ys, raise)
                        divideAndRound(
                            BigInteger.valueOf(xs),
                            rb.toLong(), scl, roundingMode, checkScaleNonZero(preferredScale)
                        )
                    } else {
                        divideAndRound(xs, scaledYs, scl,
                            roundingMode, checkScaleNonZero(preferredScale))
                    }
                }
            }
            // doRound, here, only affects 1000000000 case.
            return doRound(quotient, mc)
        }

        /**
         * Returns a `BigDecimal` whose value is `(xs /
         * ys)`, with rounding according to the context settings.
         */
        private fun divide(
            xs: BigInteger,
            xscale: Int,
            ys: Long,
            yscale: Int,
            preferredScale: Long,
            mc: MathContext
        ): BigDecimal {
            var yscale = yscale
            // Normalize dividend & divisor so that both fall into [0.1, 0.999...]
            if (-compareMagnitudeNormalized(ys, yscale, xs, xscale) > 0) {// satisfy constraint (b)
                yscale -= 1 // [that is, divisor *= 10]
            }
            val mcp = mc.precision
            val roundingMode = mc.roundingMode

            // In order to find out whether the divide generates the exact result,
            // we avoid calling the above divide method. 'quotient' holds the
            // return BigDecimal object whose scale will be set to 'scl'.
            val quotient: BigDecimal
            val scl = checkScaleNonZero(preferredScale + yscale - xscale + mcp)
            if (checkScaleNonZero(mcp.toLong() + yscale - xscale) > 0) {
                val raise = checkScaleNonZero(mcp.toLong() + yscale - xscale)
                val rb = bigMultiplyPowerTen(xs, raise)
                quotient = divideAndRound(rb, ys, scl, roundingMode, checkScaleNonZero(preferredScale))
            } else {
                val newScale = checkScaleNonZero(xscale.toLong() - mcp)
                // assert newScale >= yscale
                if (newScale == yscale) { // easy case
                    quotient = divideAndRound(xs, ys, scl, roundingMode, checkScaleNonZero(preferredScale))
                } else {
                    val raise = checkScaleNonZero(newScale.toLong() - yscale)
                    val scaledYs: Long = longMultiplyPowerTen(ys, raise)
                    quotient = if (scaledYs == INFLATED) {
                        val rb = bigMultiplyPowerTen(ys, raise)
                        divideAndRound(xs, rb.toLong(), scl, roundingMode, checkScaleNonZero(preferredScale))
                    } else {
                        divideAndRound(xs, scaledYs, scl, roundingMode, checkScaleNonZero(preferredScale))
                    }
                }
            }
            // doRound, here, only affects 1000000000 case.
            return doRound(quotient, mc)
        }

        /**
         * Returns a `BigDecimal` whose value is `(xs /
         * ys)`, with rounding according to the context settings.
         */
        private fun divide(
            xs: Long,
            xscale: Int,
            ys: BigInteger,
            yscale: Int,
            preferredScale: Long,
            mc: MathContext
        ): BigDecimal {
            var yscale = yscale
            // Normalize dividend & divisor so that both fall into [0.1, 0.999...]
            if (compareMagnitudeNormalized(xs, xscale, ys, yscale) > 0) {// satisfy constraint (b)
                yscale -= 1 // [that is, divisor *= 10]
            }
            val mcp = mc.precision
            val roundingMode = mc.roundingMode

            // In order to find out whether the divide generates the exact result,
            // we avoid calling the above divide method. 'quotient' holds the
            // return BigDecimal object whose scale will be set to 'scl'.
            val quotient: BigDecimal
            val scl = checkScaleNonZero(preferredScale + yscale - xscale + mcp)
            quotient = if (checkScaleNonZero(mcp.toLong() + yscale - xscale) > 0) {
                val raise = checkScaleNonZero(mcp.toLong() + yscale - xscale)
                val rb = bigMultiplyPowerTen(xs, raise)
                divideAndRound(rb, ys.toLong(), scl,
                        roundingMode, checkScaleNonZero(preferredScale))
            } else {
                val newScale = checkScaleNonZero(xscale.toLong() - mcp)
                val raise = checkScaleNonZero(newScale.toLong() - yscale)
                val rb = bigMultiplyPowerTen(ys, raise)
                divideAndRound(BigInteger.valueOf(xs), rb.toLong(),
                        scl, roundingMode, checkScaleNonZero(preferredScale))
            }
            // doRound, here, only affects 1000000000 case.
            return doRound(quotient, mc)
        }

        /**
         * Returns a `BigDecimal` whose value is `(xs /
         * ys)`, with rounding according to the context settings.
         */
        private fun divide(
            xs: BigInteger,
            xscale: Int,
            ys: BigInteger,
            yscale: Int,
            preferredScale: Long,
            mc: MathContext
        ): BigDecimal {
            var yscale = yscale
            // Normalize dividend & divisor so that both fall into [0.1, 0.999...]
            if (compareMagnitudeNormalized(xs, xscale, ys, yscale) > 0) {// satisfy constraint (b)
                yscale -= 1 // [that is, divisor *= 10]
            }
            val mcp = mc.precision
            val roundingMode = mc.roundingMode

            // In order to find out whether the divide generates the exact result,
            // we avoid calling the above divide method. 'quotient' holds the
            // return BigDecimal object whose scale will be set to 'scl'.
            val quotient: BigDecimal
            val scl = checkScaleNonZero(preferredScale + yscale - xscale + mcp)
            if (checkScaleNonZero(mcp.toLong() + yscale - xscale) > 0) {
                val raise = checkScaleNonZero(mcp.toLong() + yscale - xscale)
                val rb = bigMultiplyPowerTen(xs, raise)
                quotient = divideAndRound(rb, ys.toLong(), scl, roundingMode, checkScaleNonZero(preferredScale))
            } else {
                val newScale = checkScaleNonZero(xscale.toLong() - mcp)
                val raise = checkScaleNonZero(newScale.toLong() - yscale)
                val rb = bigMultiplyPowerTen(ys, raise)
                quotient = divideAndRound(xs, rb.toLong(), scl, roundingMode, checkScaleNonZero(preferredScale))
            }
            // doRound, here, only affects 1000000000 case.
            return doRound(quotient, mc)
        }

        /*
         * performs divideAndRound for (dividend0*dividend1, divisor)
         * returns null if quotient can't fit into long value;
         */
        private fun multiplyDivideAndRound(
            dividend0: Long, dividend1: Long, divisor: Long, scale: Int, roundingMode: RoundingMode,
            preferredScale: Int
        ): BigDecimal? {
            var dividend0 = dividend0
            var dividend1 = dividend1
            var divisor = divisor
            val qsign = Integers.signum(dividend0) * Integers.signum(dividend1) * Integers.signum(divisor)
            dividend0 = dividend0.absoluteValue
            dividend1 = dividend1.absoluteValue
            divisor = divisor.absoluteValue
            // multiply dividend0 * dividend1
            val d0_hi = dividend0.ushr(32)
            val d0_lo = dividend0 and BigInteger.LONG_MASK
            val d1_hi = dividend1.ushr(32)
            val d1_lo = dividend1 and BigInteger.LONG_MASK
            var product = d0_lo * d1_lo
            val d0 = product and BigInteger.LONG_MASK
            var d1 = product.ushr(32)
            product = d0_hi * d1_lo + d1
            d1 = product and BigInteger.LONG_MASK
            var d2 = product.ushr(32)
            product = d0_lo * d1_hi + d1
            d1 = product and BigInteger.LONG_MASK
            d2 += product.ushr(32)
            var d3 = d2.ushr(32)
            d2 = d2 and BigInteger.LONG_MASK
            product = d0_hi * d1_hi + d2
            d2 = product and BigInteger.LONG_MASK
            d3 = product.ushr(32) + d3 and BigInteger.LONG_MASK
            val dividendHi = make64(d3, d2)
            val dividendLo = make64(d1, d0)
            // divide
            return divideAndRound128(dividendHi, dividendLo, divisor, qsign, scale, roundingMode, preferredScale)
        }

        /*
         * divideAndRound 128-bit value by long divisor.
         * returns null if quotient can't fit into long value;
         * Specialized version of Knuth's division
         */
        private fun divideAndRound128(
            dividendHi: Long, dividendLo: Long, divisor: Long, sign: Int,
            scale: Int, roundingMode: RoundingMode, preferredScale: Int
        ): BigDecimal? {
            var divisor = divisor
            if (dividendHi >= divisor) {
                return null
            }

            val shift = Integers.numberOfLeadingZeros(divisor)
            divisor = divisor shl shift

            val v1 = divisor.ushr(32)
            val v0 = divisor and BigInteger.LONG_MASK

            var tmp = dividendLo shl shift
            var u1 = tmp.ushr(32)
            val u0 = tmp and BigInteger.LONG_MASK

            tmp = dividendHi.shl(shift) or dividendLo.ushr(64 - shift)
            val u2 = tmp and BigInteger.LONG_MASK
            var q1: Long
            var r_tmp: Long = if (v1 == 1L) {
                q1 = tmp
                0L
            } else if (tmp >= 0) {
                q1 = tmp / v1
                tmp - q1 * v1
            } else {
                val rq = divRemNegativeLong(tmp, v1)
                q1 = rq[1]
                rq[0]
            }

            while (q1 >= DIV_NUM_BASE || unsignedLongCompare(q1 * v0, make64(r_tmp, u1))) {
                q1 -= 1
                r_tmp += v1
                if (r_tmp >= DIV_NUM_BASE)
                    break
            }

            tmp = mulsub(u2, u1, v1, v0, q1)
            u1 = tmp and BigInteger.LONG_MASK
            var q0: Long
            r_tmp = if (v1 == 1L) {
                q0 = tmp
                0L
            } else if (tmp >= 0) {
                q0 = tmp / v1
                tmp - q0 * v1
            } else {
                val rq = divRemNegativeLong(tmp, v1)
                q0 = rq[1]
                rq[0]
            }

            while (q0 >= DIV_NUM_BASE || unsignedLongCompare(q0 * v0, make64(r_tmp, u0))) {
                q0 -= 1
                r_tmp += v1
                if (r_tmp >= DIV_NUM_BASE)
                    break
            }

            if (q1.toInt() < 0) {
                // result (which is positive and unsigned here)
                // can't fit into long due to sign bit is used for value
                val mq = MutableBigInteger(intArrayOf(q1.toInt(), q0.toInt()))
                if (roundingMode == RoundingMode.DOWN && scale == preferredScale) {
                    return mq.toBigDecimal(sign, scale)
                }
                val r = mulsub(u1, u0, v1, v0, q0).ushr(shift)
                return if (r != 0L) {
                    if (needIncrement(divisor.ushr(shift), roundingMode, sign, mq, r)) {
                        mq += MutableBigInteger.ONE
                    }
                    mq.toBigDecimal(sign, scale)
                } else {
                    if (preferredScale != scale) {
                        val intVal = mq.toBigInteger(sign)
                        createAndStripZerosToMatchScale(intVal, scale, preferredScale.toLong())
                    } else {
                        mq.toBigDecimal(sign, scale)
                    }
                }
            }

            var q = make64(q1, q0)
            q *= sign.toLong()

            if (roundingMode == RoundingMode.DOWN && scale == preferredScale) {
                return valueOf(q, scale)
            }
            val r = mulsub(u1, u0, v1, v0, q0).ushr(shift)
            return if (r != 0L) {
                val increment = needIncrement(divisor.ushr(shift), roundingMode, sign, q, r)
                valueOf(if (increment) q + sign else q, scale)
            } else {
                if (preferredScale != scale) {
                    createAndStripZerosToMatchScale(q, scale, preferredScale.toLong())
                } else {
                    valueOf(q, scale)
                }
            }
        } // private fun divideAndRound128

        /**
         * Calculate the quotient and remainder of dividing a negative long by
         * another long.
         *
         * @param n the numerator; must be negative
         * @param d the denominator; must not be unity
         * @return a two-element {@long} array with the remainder and quotient in
         * the initial and final elements, respectively
         */
        private fun divRemNegativeLong(n: Long, d: Long): LongArray {
            if(n >= 0) {
                throw AssertionError("Non-negative numerator $n")
            }
            if(d == 1L) {
                throw AssertionError("Unity denominator")
            }

            // Approximate the quotient and remainder
            var q = n.ushr(1) / d.ushr(1)
            var r = n - q * d

            // Correct the approximation
            while (r < 0) {
                r += d
                q -= 1
            }
            while (r >= d) {
                r -= d
                q += 1
            }

            // n - q*d == r && 0 <= r < d, hence we're done.
            return longArrayOf(r, q)
        }

        /**
         * Returns a `BigDecimal` rounded according to the MathContext
         * settings;
         * If rounding is needed a new `BigDecimal` is created and returned.
         *
         * @param bigDecimal the value to be rounded
         * @param mc the context to use.
         * @return a `BigDecimal` rounded according to the MathContext
         * settings.  May return `value`, if no rounding needed.
         * @throws ArithmeticException if the rounding mode is
         * `RoundingMode.UNNECESSARY` and the
         * result is inexact.
         */
        private fun doRound(bigDecimal: BigDecimal, mc: MathContext): BigDecimal {
            val mcp = mc.precision
            var wasDivided = false
            if (mcp > 0) {
                var intVal = bigDecimal.intVal
                var compactVal = bigDecimal.intCompact
                var scale = bigDecimal.scale
                var prec = bigDecimal.precision()
                val mode = mc.roundingMode
                var drop: Int
                if (compactVal == INFLATED) {
                    drop = prec - mcp
                    while (drop > 0) {
                        scale = checkScaleNonZero(scale.toLong() - drop)
                        intVal = divideAndRoundByTenPow(intVal!!, drop, mode)
                        wasDivided = true
                        compactVal = compactValFor(intVal)
                        if (compactVal != INFLATED) {
                            prec = longDigitLength(compactVal)
                            break
                        }
                        prec = bigDigitLength(intVal)
                        drop = prec - mcp
                    }
                }
                if (compactVal != INFLATED) {
                    drop = prec - mcp  // drop can't be more than 18
                    while (drop > 0) {
                        scale = checkScaleNonZero(scale.toLong() - drop)
                        compactVal = divideAndRound(compactVal, LONG_TEN_POWERS_TABLE[drop],
                                mc.roundingMode)
                        wasDivided = true
                        prec = longDigitLength(compactVal)
                        drop = prec - mcp
                        intVal = null
                    }
                }
                return if (wasDivided) BigDecimal(intVal, compactVal, scale, prec) else bigDecimal
            }
            return bigDecimal
        }

        /*
         * Returns a `BigDecimal` created from `long` value with
         * given scale rounded according to the MathContext settings
         */
        private fun doRound(compactVal: Long, scale: Int, mc: MathContext): BigDecimal {
            var compactVal = compactVal
            var scale = scale
            val mcp = mc.precision
            if (mcp in 1..18) {
                var prec = longDigitLength(compactVal)
                var drop = prec - mcp  // drop can't be more than 18
                while (drop > 0) {
                    scale = checkScaleNonZero(scale.toLong() - drop)
                    compactVal = divideAndRound(compactVal, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode)
                    prec = longDigitLength(compactVal)
                    drop = prec - mcp
                }
                return valueOf(compactVal, scale, prec)
            }
            return valueOf(compactVal, scale)
        }

        /*
         * Returns a `BigDecimal` created from `BigInteger` value with
         * given scale rounded according to the MathContext settings
         */
        private fun doRound(intVal: BigInteger, scale: Int, mc: MathContext): BigDecimal {
            var intVal = intVal
            var scale = scale
            val mcp = mc.precision
            var prec = 0
            if (mcp > 0) {
                var compactVal = compactValFor(intVal)
                val mode = mc.roundingMode
                var drop: Int
                if (compactVal == INFLATED) {
                    prec = bigDigitLength(intVal)
                    drop = prec - mcp
                    while (drop > 0) {
                        scale = checkScaleNonZero(scale.toLong() - drop)
                        intVal = divideAndRoundByTenPow(intVal, drop, mode)
                        compactVal = compactValFor(intVal)
                        if (compactVal != INFLATED) {
                            break
                        }
                        prec = bigDigitLength(intVal)
                        drop = prec - mcp
                    }
                }
                if (compactVal != INFLATED) {
                    prec = longDigitLength(compactVal)
                    drop = prec - mcp     // drop can't be more than 18
                    while (drop > 0) {
                        scale = checkScaleNonZero(scale.toLong() - drop)
                        compactVal = divideAndRound(compactVal, LONG_TEN_POWERS_TABLE[drop],
                                mc.roundingMode)
                        prec = longDigitLength(compactVal)
                        drop = prec - mcp
                    }
                    return valueOf(compactVal, scale, prec)
                }
            }
            return BigDecimal(intVal, INFLATED, scale, prec)
        }

        /*
         * Divides `BigInteger` value by ten power.
         */
        private fun divideAndRoundByTenPow(intVal: BigInteger, tenPow: Int, roundingMode: RoundingMode): BigInteger {
            return if (tenPow < LONG_TEN_POWERS_TABLE.size)
                divideAndRound(intVal, LONG_TEN_POWERS_TABLE[tenPow], roundingMode)
            else
                divideAndRound(intVal, bigTenToThe(tenPow).toLong(), roundingMode)
        }

        /**
         * Returns a `BigDecimal` whose value is `(xs /
         * ys)`, with rounding according to the context settings.
         *
         * Fast path - used only when (xscale <= yscale && yscale < 18
         * && mc.presision<18) {
         */
        private fun divideSmallFastPath(
            xs: Long, xscale: Int,
            ys: Long, yscale: Int,
            preferredScale: Long, mc: MathContext
        ): BigDecimal {
            var yscale = yscale
            val mcp = mc.precision
            val roundingMode = mc.roundingMode

            if (!(yscale in xscale..17 && mcp < 18)) {
                throw AssertionError()
            }
            val xraise = yscale - xscale // xraise >=0
            val scaledX = if (xraise == 0)
                xs
            else
                longMultiplyPowerTen(xs, xraise) // can't overflow here!
            var quotient: BigDecimal?

            val cmp = longCompareMagnitude(scaledX, ys)
            if (cmp > 0) { // satisfy constraint (b)
                yscale -= 1 // [that is, divisor *= 10]
                val scl = checkScaleNonZero(preferredScale + yscale - xscale + mcp)
                if (checkScaleNonZero(mcp.toLong() + yscale - xscale) > 0) {
                    // assert newScale >= xscale
                    val raise = checkScaleNonZero(mcp.toLong() + yscale - xscale)
                    val scaledXs: Long = longMultiplyPowerTen(xs, raise)
                    if (scaledXs == INFLATED) {
                        quotient = null
                        if (mcp - 1 >= 0 && mcp - 1 < LONG_TEN_POWERS_TABLE.size) {
                            quotient = multiplyDivideAndRound(
                                LONG_TEN_POWERS_TABLE[mcp - 1],
                                scaledX,
                                ys,
                                scl,
                                roundingMode,
                                checkScaleNonZero(preferredScale)
                            )
                        }
                        if (quotient == null) {
                            val rb = bigMultiplyPowerTen(scaledX, mcp - 1)
                            quotient = divideAndRound(
                                rb, ys,
                                scl, roundingMode, checkScaleNonZero(preferredScale)
                            )
                        }
                    } else {
                        quotient = divideAndRound(scaledXs, ys, scl, roundingMode,
                                checkScaleNonZero(preferredScale))
                    }
                } else {
                    val newScale = checkScaleNonZero(xscale.toLong() - mcp)
                    // assert newScale >= yscale
                    if (newScale == yscale) { // easy case
                        quotient = divideAndRound(xs, ys, scl, roundingMode, checkScaleNonZero(preferredScale))
                    } else {
                        val raise = checkScaleNonZero(newScale.toLong() - yscale)
                        val scaledYs: Long = longMultiplyPowerTen(ys, raise)
                        quotient = if (scaledYs == INFLATED) {
                            val rb = bigMultiplyPowerTen(ys, raise)
                            divideAndRound(
                                BigInteger.valueOf(xs),
                                rb.toLong(), scl, roundingMode, checkScaleNonZero(preferredScale)
                            )
                        } else {
                            divideAndRound(xs, scaledYs, scl, roundingMode, checkScaleNonZero(preferredScale))
                        }
                    }
                }
            } else {
                // abs(scaledX) <= abs(ys)
                // result is "scaledX * 10^msp / ys"
                val scl = checkScaleNonZero(preferredScale + yscale - xscale + mcp)
                if (cmp == 0) {
                    // abs(scaleX)== abs(ys) => result will be scaled 10^mcp + correct sign
                    quotient = roundedTenPower(
                        if (scaledX < 0 == ys < 0) 1 else -1,
                        mcp,
                        scl,
                        checkScaleNonZero(preferredScale)
                    )
                } else {
                    // abs(scaledX) < abs(ys)
                    val scaledXs: Long = longMultiplyPowerTen(scaledX, mcp)
                    if (scaledXs == INFLATED) {
                        quotient = null
                        if (mcp < LONG_TEN_POWERS_TABLE.size) {
                            quotient = multiplyDivideAndRound(
                                LONG_TEN_POWERS_TABLE[mcp],
                                scaledX,
                                ys,
                                scl,
                                roundingMode,
                                checkScaleNonZero(preferredScale)
                            )
                        }
                        if (quotient == null) {
                            val rb = bigMultiplyPowerTen(scaledX, mcp)
                            quotient = divideAndRound(
                                rb, ys,
                                scl, roundingMode, checkScaleNonZero(preferredScale)
                            )
                        }
                    } else {
                        quotient = divideAndRound(scaledXs, ys, scl, roundingMode,
                                checkScaleNonZero(preferredScale))
                    }
                }
            }
            // doRound, here, only affects 1000000000 case.
            return doRound(quotient, mc)
        }

        // Private class to build a string representation for BigDecimal object.
        // "StringBuilderHelper" is constructed as a thread local variable so it is
        // thread safe. The StringBuilder field acts as a buffer to hold the temporary
        // representation of BigDecimal. The cmpCharArray holds all the characters for
        // the compact representation of BigDecimal (except for '-' sign' if it is
        // negative) if its intCompact field is not INFLATED. It is shared by all
        // calls to toString() and its variants in that particular thread.
        internal class StringBuilderHelper {
            internal var sb = StringBuilder()    // Placeholder for BigDecimal string
            internal val cmpCharArray = CharArray(19) // character array to place the intCompact

            // Accessors.
            internal fun getStringBuilder(): StringBuilder {
                sb = StringBuilder()
                return sb
            }

            /**
             * Places characters representing the intCompact in `long` into
             * cmpCharArray and returns the offset to the array where the
             * representation starts.
             *
             * @param intCompact the number to put into the cmpCharArray.
             * @return offset to the array where the representation starts.
             * Note: intCompact must be greater or equal to zero.
             */
            internal fun putIntCompact(intCompact: Long): Int {
                var intCompact = intCompact
                if (intCompact < 0) {
                    throw AssertionError()
                }
                var q: Long
                var r: Int
                // since we start from the least significant digit, charPos points to
                // the last character in cmpCharArray.
                var charPos = cmpCharArray.size

                // Get 2 digits/iteration using longs until quotient fits into an int
                while (intCompact > Int.MAX_VALUE) {
                    q = intCompact / 100
                    r = (intCompact - q * 100).toInt()
                    intCompact = q
                    cmpCharArray[--charPos] = DIGIT_ONES[r]
                    cmpCharArray[--charPos] = DIGIT_TENS[r]
                }

                // Get 2 digits/iteration using ints when i2 >= 100
                var q2: Int
                var i2 = intCompact.toInt()
                while (i2 >= 100) {
                    q2 = i2 / 100
                    r = i2 - q2 * 100
                    i2 = q2
                    cmpCharArray[--charPos] = DIGIT_ONES[r]
                    cmpCharArray[--charPos] = DIGIT_TENS[r]
                }

                cmpCharArray[--charPos] = DIGIT_ONES[i2]
                if (i2 >= 10) {
                    cmpCharArray[--charPos] = DIGIT_TENS[i2]
                }
                return charPos
            } // fun putIntCompact(intCompact: Long)

            companion object {
                internal val DIGIT_TENS = charArrayOf(
                    '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
                    '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
                    '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
                    '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
                    '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
                    '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
                    '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
                    '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
                    '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
                    '9', '9', '9', '9', '9', '9', '9', '9', '9', '9'
                )

                internal val DIGIT_ONES = charArrayOf(
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
                )
            }
        }

        private object LongOverflow {
            /** BigInteger equal to Long.MIN_VALUE.  */
            private val LONGMIN = BigInteger.valueOf(Long.MIN_VALUE)

            /** BigInteger equal to Long.MAX_VALUE.  */
            private val LONGMAX = BigInteger.valueOf(Long.MAX_VALUE)

            internal fun check(num: BigDecimal) {
                val intVal = num.inflated()
                if (intVal < LONGMIN || intVal > LONGMAX) {
                    throw ArithmeticException("Overflow")
                }
            }
        }
    } // companion object

    override fun toByte(): Byte {
        return if (this.intCompact != INFLATED && this.scale == 0)
            this.intCompact.toByte()
        else
            toBigInteger().toByte()
    }

    override fun toChar(): Char {
        return if (this.intCompact != INFLATED && this.scale == 0)
            this.intCompact.toChar()
        else
            toBigInteger().toChar()
    }

    /**
     * Converts this `BigDecimal` to a `double`.
     * This conversion is similar to the
     * <i>narrowing primitive conversion</i> from `double` to
     * `float` as defined in
     * <cite>The Java&trade; Language Specification</cite>:
     * if this `BigDecimal` has too great a
     * magnitude represent as a `double`, it will be
     * converted to {@link Double#NEGATIVE_INFINITY} or {@link
     * Double#POSITIVE_INFINITY} as appropriate.  Note that even when
     * the return value is finite, this conversion can lose
     * information about the precision of the `BigDecimal`
     * value.
     *
     * @return this `BigDecimal` converted to a `double`.
     * @jls 5.1.3 Narrowing Primitive Conversion
     */
    override fun toDouble(): Double {
        if (this.intCompact != INFLATED) {
            if (this.scale == 0) {
                return this.intCompact.toDouble()
            } else {
                /*
                 * If both intCompact and the scale can be exactly
                 * represented as double values, perform a single
                 * double multiply or divide to compute the (properly
                 * rounded) result.
                 */
                if (this.intCompact.absoluteValue < 1L.shl(52)) {
                    // Don't have too guard against
                    // Math.abs(MIN_VALUE) because of outer check
                    // against INFLATED.
                    if (this.scale > 0 && this.scale < DOUBLE_10_POW.size) {
                        return intCompact.toDouble() / DOUBLE_10_POW[scale]
                    } else if (scale < 0 && scale > -DOUBLE_10_POW.size) {
                        return intCompact.toDouble() * DOUBLE_10_POW[-scale]
                    }
                }
            }
        }
        // Somewhat inefficient, but guaranteed to work.
        return this.toString().toDouble()
    }

    /**
     * Converts this `BigDecimal` to a `float`.
     * This conversion is similar to the
     * <i>narrowing primitive conversion</i> from `double` to
     * `float` as defined in
     * <cite>The Java&trade; Language Specification</cite>:
     * if this `BigDecimal` has too great a
     * magnitude to represent as a `float`, it will be
     * converted to {@link Float#NEGATIVE_INFINITY} or {@link
     * Float#POSITIVE_INFINITY} as appropriate.  Note that even when
     * the return value is finite, this conversion can lose
     * information about the precision of the `BigDecimal`
     * value.
     *
     * @return this `BigDecimal` converted to a `float`.
     * @jls 5.1.3 Narrowing Primitive Conversion
     */
    override fun toFloat(): Float {
        if (intCompact != INFLATED) {
            if (scale == 0) {
                return intCompact.toFloat()
            } else {
                /*
                 * If both intCompact and the scale can be exactly
                 * represented as float values, perform a single float
                 * multiply or divide to compute the (properly
                 * rounded) result.
                 */
                if (intCompact.absoluteValue < 1L.shl(22)) {
                    // Don't have too guard against
                    // Math.abs(MIN_VALUE) because of outer check
                    // against INFLATED.
                    if (scale > 0 && scale < FLOAT_10_POW.size) {
                        return intCompact.toFloat() / FLOAT_10_POW[scale]
                    } else if (scale < 0 && scale > -FLOAT_10_POW.size) {
                        return intCompact.toFloat() * FLOAT_10_POW[-scale]
                    }
                }
            }
        }
        // Somewhat inefficient, but guaranteed to work.
        return this.toString().toFloat()
    }

    /**
     * Converts this `BigDecimal` to an `int`.
     * This conversion is analogous to the
     * *narrowing primitive conversion* from `double` to
     * `short` as defined in
     * <cite>The Java Language Specification</cite>:
     * any fractional part of this
     * `BigDecimal` will be discarded, and if the resulting
     * "`BigInteger`" is too big to fit in an
     * `int`, only the low-order 32 bits are returned.
     * Note that this conversion can lose information about the
     * overall magnitude and precision of this `BigDecimal`
     * value as well as return a result with the opposite sign.
     *
     * @return this `BigDecimal` converted to an `int`.
     * @jls 5.1.3 Narrowing Primitive Conversion
     */
    override fun toInt(): Int {
        return if (this.intCompact != INFLATED && this.scale == 0)
            this.intCompact.toInt()
        else
            toBigInteger().toInt()
    }

    /**
     * Converts this `BigDecimal` to a `long`.
     * This conversion is analogous to the
     * *narrowing primitive conversion* from `double` to
     * `short` as defined in
     * <cite>The Java Language Specification</cite>:
     * any fractional part of this
     * `BigDecimal` will be discarded, and if the resulting
     * "`BigInteger`" is too big to fit in a
     * `long`, only the low-order 64 bits are returned.
     * Note that this conversion can lose information about the
     * overall magnitude and precision of this `BigDecimal` value as well
     * as return a result with the opposite sign.
     *
     * @return this `BigDecimal` converted to a `long`.
     * @jls 5.1.3 Narrowing Primitive Conversion
     */
    override fun toLong(): Long {
        return if (this.intCompact != INFLATED && this.scale == 0)
            this.intCompact
        else
            toBigInteger().toLong()
    }

    /**
     * Converts this `BigDecimal` to a `long`, checking
     * for lost information.  If this `BigDecimal` has a
     * nonzero fractional part or is out of the possible range for a
     * `long` result then an `ArithmeticException` is
     * thrown.
     *
     * @return this `BigDecimal` converted to a `long`.
     * @throws ArithmeticException if `this` has a nonzero
     * fractional part, or will not fit in a `long`.
     * @since  1.5
     */
    fun longValueExact(): Long {
        if (intCompact != INFLATED && scale == 0) {
            return intCompact
        }
        // If more than 19 digits in integer part it cannot possibly fit
        if (precision() - scale > 19) {
            // [OK for negative scale too]
            throw ArithmeticException("Overflow")
        }
        // Fastpath zero and < 1.0 numbers (the latter can be very slow
        // to round if very small)
        if (this.signum() == 0) {
            return 0
        }
        if (this.precision() - this.scale <= 0) {
            throw ArithmeticException("Rounding necessary")
        }
        // round to an integer, with Exception if decimal part non-0
        val num = this.setScale(0, RoundingMode.UNNECESSARY)
        if (num.precision() >= 19) {
            // need to check carefully
            LongOverflow.check(num)
        }
        return num.inflated().toLong()
    }

    override fun toShort(): Short {
        return if (this.intCompact != INFLATED && this.scale == 0)
            this.intCompact.toShort()
        else
            toBigInteger().toShort()
    }

    /**
     * Converts this `BigDecimal` to a `BigInteger`.
     * This conversion is analogous to the
     * *narrowing primitive conversion* from `double` to
     * `long` as defined in
     * <cite>The Java Language Specification</cite>:
     * any fractional part of this
     * `BigDecimal` will be discarded.  Note that this
     * conversion can lose information about the precision of the
     * `BigDecimal` value.
     *
     *
     * To have an exception thrown if the conversion is inexact (in
     * other words if a nonzero fractional part is discarded), use the
     * [.toBigIntegerExact] method.
     *
     * @return this `BigDecimal` converted to a `BigInteger`.
     * @jls 5.1.3 Narrowing Primitive Conversion
     */
    fun toBigInteger(): BigInteger {
        // force to an integer, quietly
        return this.setScale(0, RoundingMode.DOWN).inflated()
    }

    /**
     * Converts this `BigDecimal` to a `BigInteger`,
     * checking for lost information.  An exception is thrown if this
     * `BigDecimal` has a nonzero fractional part.
     *
     * @return this `BigDecimal` converted to a `BigInteger`.
     * @throws ArithmeticException if `this` has a nonzero
     * fractional part.
     * @since  Java 1.5
     */
    fun toBigIntegerExact(): BigInteger {
        // round to an integer, with Exception if decimal part non-0
        return this.setScale(0, RoundingMode.UNNECESSARY).inflated()
    }

    // ----==== Arithmetic Operations ====----
    /**
     * Returns a `BigDecimal` whose value is `(this +
     * augend)`, and whose scale is `max(this.scale(),
     * augend.scale())`.
     *
     * Rename from `add` to `plus` to accord with `Int.plus` in Kotlin
     *
     * @param  augend value to be added to this `BigDecimal`.
     * @return `this + augend`
     */
    operator fun plus(augend: BigDecimal): BigDecimal {
        return if (this.intCompact != INFLATED) {
            if (augend.intCompact != INFLATED) {
                add(this.intCompact, this.scale, augend.intCompact, augend.scale)
            } else {
                add(this.intCompact, this.scale, augend.intVal!!, augend.scale)
            }
        } else {
            if (augend.intCompact != INFLATED) {
                add(augend.intCompact, augend.scale, this.intVal!!, this.scale)
            } else {
                add(this.intVal!!, this.scale, augend.intVal!!, augend.scale)
            }
        }
    }

    /**
     * Returns a `BigDecimal` whose value is `(this -
     * subtrahend)`, and whose scale is `max(this.scale(),
     * subtrahend.scale())`.
     *
     * Rename from `subtract` to `minus` to accord with `Int.minus` in Kotlin
     *
     * @param  subtrahend value to be subtracted from this `BigDecimal`.
     * @return `this - subtrahend`
     */
    operator fun minus(subtrahend: BigDecimal): BigDecimal {
        return if (this.intCompact != INFLATED) {
            if (subtrahend.intCompact != INFLATED) {
                add(this.intCompact, this.scale, -subtrahend.intCompact, subtrahend.scale)
            } else {
                add(this.intCompact, this.scale, subtrahend.intVal!!.unaryMinus(), subtrahend.scale)
            }
        } else {
            if (subtrahend.intCompact != INFLATED) {
                // Pair of subtrahend values given before pair of
                // values from this BigDecimal to avoid need for
                // method overloading on the specialized add method
                add(-subtrahend.intCompact, subtrahend.scale, this.intVal!!, this.scale)
            } else {
                add(this.intVal!!, this.scale, subtrahend.intVal!!.unaryMinus(), subtrahend.scale)
            }
        }
    }

    /**
     * Returns a `BigDecimal` whose value is <tt>(this
     * multiplicand)</tt>, and whose scale is `(this.scale() +
     * multiplicand.scale())`.
     *
     * Rename from `multiply` to `times` to accord with `Int.times` in Kotlin
     *
     * @param  multiplicand value to be multiplied by this `BigDecimal`.
     * @return `this * multiplicand`
     */
    operator fun times(multiplicand: BigDecimal): BigDecimal {
        val productScale = checkScale(this.scale.toLong() + multiplicand.scale)
        return if (this.intCompact != INFLATED) {
            if (multiplicand.intCompact != INFLATED) {
                multiply(this.intCompact, multiplicand.intCompact, productScale)
            } else {
                multiply(this.intCompact, multiplicand.intVal!!, productScale)
            }
        } else {
            if (multiplicand.intCompact != INFLATED) {
                multiply(multiplicand.intCompact, this.intVal!!, productScale)
            } else {
                multiply(this.intVal!!, multiplicand.intVal!!, productScale)
            }
        }
    }

    /**
     * Returns a `BigDecimal` whose value is the absolute value
     * of this `BigDecimal`, and whose scale is
     * `this.scale()`.
     *
     * @return `abs(this)`
     */
    fun abs(): BigDecimal {
        return if (signum() < 0) -this else this
    }

    /**
     * Returns a `BigDecimal` whose value is `(-this)`,
     * and whose scale is `this.scale()`.
     *
     * Rename from `negate` to `unaryMinus` to accord with the `unaryMinus` operator in Kotlin
     *
     * @return `-this`.
     */
    operator fun unaryMinus(): BigDecimal {
        return if (this.intCompact == INFLATED) {
            BigDecimal(this.intVal!!.unaryMinus(), INFLATED, this.scale, this.precision)
        } else {
            valueOf(-this.intCompact, this.scale, this.precision)
        }
    }

    /**
     * Returns the signum function of this `BigDecimal`.
     *
     * @return -1, 0, or 1 as the value of this `BigDecimal`
     * is negative, zero, or positive.
     */
    fun signum(): Int {
        return if (this.intCompact != INFLATED)
            (if (this.intCompact > 0) 1 else if (this.intCompact < 0) -1 else 1)
        else
            this.intVal!!.signum()
    }

    /**
     * Returns the *scale* of this `BigDecimal`.  If zero
     * or positive, the scale is the number of digits to the right of
     * the decimal point.  If negative, the unscaled value of the
     * number is multiplied by ten to the power of the negation of the
     * scale.  For example, a scale of `-3` means the unscaled
     * value is multiplied by 1000.
     *
     * @return the scale of this `BigDecimal`.
     */
    fun scale(): Int {
        return this.scale
    }

    /**
     * Returns the *precision* of this `BigDecimal`.  (The
     * precision is the number of digits in the unscaled value.)
     *
     *
     * The precision of a zero value is 1.
     *
     * @return the precision of this `BigDecimal`.
     * @since  1.5
     */
    fun precision(): Int {
        var result = this.precision
        if (result == 0) {
            val s = this.intCompact
            result = if (s != INFLATED)
                longDigitLength(s)
            else
                bigDigitLength(this.intVal!!)
            this.precision = result
        }
        return result
    }

    /**
     * Returns a `BigInteger` whose value is the *unscaled
     * value* of this `BigDecimal`.  (Computes <tt>(this *
     * 10<sup>this.scale()</sup>)</tt>.)
     *
     * @return the unscaled value of this `BigDecimal`.
     * @since  1.2
     */
    fun unscaledValue(): BigInteger {
        return this.inflated()
    }

    /**
     * Returns a `BigDecimal` whose value is `(this /
     * divisor)`, and whose scale is as specified.  If rounding must
     * be performed to generate a result with the specified scale, the
     * specified rounding mode is applied.
     *
     * @param  divisor value by which this `BigDecimal` is to be divided.
     * @param  scale scale of the `BigDecimal` quotient to be returned.
     * @param  roundingMode rounding mode to apply.
     * @return `this / divisor`
     * @throws ArithmeticException if `divisor` is zero,
     * `roundingMode==RoundingMode.UNNECESSARY` and
     * the specified scale is insufficient to represent the result
     * of the division exactly.
     * @since Java 1.5
     */
    fun divide(divisor: BigDecimal, scale: Int, roundingMode: RoundingMode): BigDecimal {
        if (roundingMode < RoundingMode.UP || roundingMode > RoundingMode.UNNECESSARY) {
            throw IllegalArgumentException ("Invalid rounding mode")
        }
        return if (this.intCompact != INFLATED) {
            if ((divisor.intCompact != INFLATED)) {
                divide(this.intCompact, this.scale, divisor.intCompact, divisor.scale, scale, roundingMode)
            } else {
                divide(this.intCompact, this.scale, divisor.intVal!!, divisor.scale, scale, roundingMode)
            }
        } else {
            if ((divisor.intCompact != INFLATED)) {
                divide(this.intVal!!, this.scale, divisor.intCompact, divisor.scale, scale, roundingMode)
            } else {
                divide(this.intVal!!, this.scale, divisor.intVal!!, divisor.scale, scale, roundingMode)
            }
        }
    }

    /**
     * Returns a `BigDecimal` whose value is `(this /
     * divisor)`, and whose preferred scale is `(this.scale() -
     * divisor.scale())`; if the exact quotient cannot be
     * represented (because it has a non-terminating decimal
     * expansion) an `ArithmeticException` is thrown.
     *
     * Rename from `divide` to `div` to accord with `Int.div` in Kotlin
     *
     * @param  divisor value by which this `BigDecimal` is to be divided.
     * @throws ArithmeticException if the exact quotient does not have a
     * terminating decimal expansion
     * @return `this / divisor`
     * @since Java 1.5
     * @author Joseph D. Darcy
     */
    operator fun div(divisor: BigDecimal): BigDecimal {
        /*
         * Handle zero cases first.
         */
        if (divisor.signum() == 0) {   // x/0
            if (this.signum() == 0) {
                // 0/0
                throw ArithmeticException("Division undefined")  // NaN
            }
            throw ArithmeticException("Division by zero")
        }

        // Calculate preferred scale
        val preferredScale = saturateLong(this.scale.toLong() - divisor.scale)

        if (this.signum() == 0) {
            // 0/y
            return zeroValueOf(preferredScale)
        } else {
            /*
             * If the quotient this/divisor has a terminating decimal
             * expansion, the expansion can have no more than
             * (a.precision() + ceil(10*b.precision)/3) digits.
             * Therefore, create a MathContext object with this
             * precision and do a divide with the UNNECESSARY rounding
             * mode.
             */
            val mc = MathContext(
                minOf(
                    this.precision() + kotlin.math.ceil(10.0 * divisor.precision() / 3.0).toLong(),
                    Int.MAX_VALUE.toLong()
                ).toInt(),
                RoundingMode.UNNECESSARY
            )
            val quotient: BigDecimal
            try {
                quotient = this.divide(divisor, mc)
            } catch (e: ArithmeticException) {
                throw ArithmeticException("Non-terminating decimal expansion; " + "no exact representable decimal result.")
            }

            val quotientScale = quotient.scale()

            // divide(BigDecimal, mc) tries to adjust the quotient to
            // the desired one by removing trailing zeros; since the
            // exact divide method does not have an explicit digit
            // limit, we can add zeros too.
            return if (preferredScale > quotientScale) quotient.setScale(
                preferredScale,
                RoundingMode.UNNECESSARY
            ) else quotient
        }
    } // operator fun div(divisor: BigDecimal): BigDecimal

    /**
     * Returns a `BigDecimal` whose value is `(this /
     * divisor)`, with rounding according to the context settings.
     *
     * @param  divisor value by which this `BigDecimal` is to be divided.
     * @param  mc the context to use.
     * @return `this / divisor`, rounded as necessary.
     * @throws ArithmeticException if the result is inexact but the
     * rounding mode is `UNNECESSARY` or
     * `mc.precision == 0` and the quotient has a
     * non-terminating decimal expansion.
     * @since  Java 1.5
     */
    fun divide(divisor: BigDecimal, mc: MathContext): BigDecimal {
        val mcp = mc.precision
        if (mcp == 0) {
            return div(divisor)
        }
        val dividend = this
        val preferredScale = dividend.scale.toLong() - divisor.scale
        // Now calculate the answer.  We use the existing
        // divide-and-round method, but as this rounds to scale we have
        // to normalize the values here to achieve the desired result.
        // For x/y we first handle y=0 and x=0, and then normalize x and
        // y to give x' and y' with the following constraints:
        //   (a) 0.1 <= x' < 1
        //   (b)  x' <= y' < 10*x'
        // Dividing x'/y' with the required scale set to mc.precision then
        // will give a result in the range 0.1 to 1 rounded to exactly
        // the right number of digits (except in the case of a result of
        // 1.000... which can arise when x=y, or when rounding overflows
        // The 1.000... case will reduce properly to 1.
        if (divisor.signum() == 0) {      // x/0
            if (dividend.signum() == 0) {
                // 0/0
                throw ArithmeticException("Division undefined")  // NaN
            }
            throw ArithmeticException("Division by zero")
        }
        if (dividend.signum() == 0) {
            // 0/y
            return zeroValueOf(saturateLong(preferredScale))
        }
        val xscale = dividend.precision()
        val yscale = divisor.precision()
        return if (dividend.intCompact != INFLATED) {
            if (divisor.intCompact != INFLATED) {
                divide(dividend.intCompact, xscale, divisor.intCompact, yscale, preferredScale, mc)
            } else {
                divide(dividend.intCompact, xscale, divisor.intVal!!, yscale, preferredScale, mc)
            }
        } else {
            if (divisor.intCompact != INFLATED) {
                divide(dividend.intVal!!, xscale, divisor.intCompact, yscale, preferredScale, mc)
            } else {
                divide(dividend.intVal!!, xscale, divisor.intVal!!, yscale, preferredScale, mc)
            }
        }
    } // fun divide(divisor: BigDecimal, mc: MathContext): BigDecimal

    /**
     * Returns a `BigDecimal` whose value is `(this % divisor)`.
     *
     *
     * The remainder is given by
     * `this.subtract(this.divideToIntegralValue(divisor).multiply(divisor))`.
     * Note that this is *not* the modulo operation (the result can be
     * negative).
     *
     * Rename from `remainder` to `rem` to accord with `Int.rem` in Kotlin
     *
     * @param  divisor value by which this `BigDecimal` is to be divided.
     * @return `this % divisor`.
     * @throws ArithmeticException if `divisor==0`
     * @since  1.5
     */
    operator fun rem(divisor: BigDecimal): BigDecimal {
        val divrem = this.divideAndRemainder(divisor)
        return divrem[1]
    }

    /**
     * Returns a two-element `BigDecimal` array containing the
     * result of `divideToIntegralValue` followed by the result of
     * `remainder` on the two operands.
     *
     *
     * Note that if both the integer quotient and remainder are
     * needed, this method is faster than using the
     * `divideToIntegralValue` and `remainder` methods
     * separately because the division need only be carried out once.
     *
     * @param  divisor value by which this `BigDecimal` is to be divided,
     * and the remainder computed.
     * @return a two element `BigDecimal` array: the quotient
     * (the result of `divideToIntegralValue`) is the initial element
     * and the remainder is the final element.
     * @throws ArithmeticException if `divisor==0`
     * @see .divideToIntegralValue
     * @see .remainder
     * @since  Java 1.5
     */
    fun divideAndRemainder(divisor: BigDecimal): Array<BigDecimal> {
        // we use the identity  x = i * y + r to determine r
        val q = this.divideToIntegralValue(divisor)
        return arrayOf(q, this.minus(q * divisor))
    }

    /**
     * Returns a `BigDecimal` whose value is the integer part
     * of the quotient `(this / divisor)` rounded down.  The
     * preferred scale of the result is `(this.scale() -
     * divisor.scale())`.
     *
     * @param  divisor value by which this `BigDecimal` is to be divided.
     * @return The integer part of `this / divisor`.
     * @throws ArithmeticException if `divisor==0`
     * @since  Java 1.5
     */
    fun divideToIntegralValue(divisor: BigDecimal): BigDecimal {
        // Calculate preferred scale
        val preferredScale = saturateLong(this.scale.toLong() - divisor.scale)
        if (this.compareMagnitude(divisor) < 0) {
            // much faster when this << divisor
            return zeroValueOf(preferredScale)
        }

        if (this.signum() == 0 && divisor.signum() != 0) {
            return this.setScale(preferredScale, RoundingMode.UNNECESSARY)
        }
        // Perform a divide with enough digits to round to a correct
        // integer value; then remove any fractional digits

        val maxDigits = minOf(
            this.precision() +
                    kotlin.math.ceil(10.0 * divisor.precision() / 3.0).toLong() +
                    (this.scale().toLong() - divisor.scale()).absoluteValue + 2,
            Int.MAX_VALUE.toLong()
        ).toInt()
        var quotient = this.divide(
            divisor, MathContext(
                maxDigits,
                RoundingMode.DOWN
            )
        )
        if (quotient.scale > 0) {
            quotient = quotient.setScale(0, RoundingMode.DOWN)
            quotient = stripZerosToMatchScale(quotient.intVal, quotient.intCompact, quotient.scale, preferredScale)
        }

        if (quotient.scale < preferredScale) {
            // pad with zeros if necessary
            quotient = quotient.setScale(preferredScale, RoundingMode.UNNECESSARY)
        }

        return quotient
    }

    /**
     * Returns a `BigDecimal` whose scale is the specified
     * value, and whose unscaled value is determined by multiplying or
     * dividing this `BigDecimal`'s unscaled value by the
     * appropriate power of ten to maintain its overall value.  If the
     * scale is reduced by the operation, the unscaled value must be
     * divided (rather than multiplied), and the value may be changed;
     * in this case, the specified rounding mode is applied to the
     * division.
     *
     * @apiNote Since BigDecimal objects are immutable, calls of
     * this method do *not* result in the original object being
     * modified, contrary to the usual convention of having methods
     * named `set*X*` mutate field *`X`*.
     * Instead, `setScale` returns an object with the proper
     * scale; the returned object may or may not be newly allocated.
     *
     * @param  newScale scale of the `BigDecimal` value to be returned.
     * @param  roundingMode The rounding mode to apply.
     * @return a `BigDecimal` whose scale is the specified value,
     * and whose unscaled value is determined by multiplying or
     * dividing this `BigDecimal`'s unscaled value by the
     * appropriate power of ten to maintain its overall value.
     * @throws ArithmeticException if `roundingMode==ROUND_UNNECESSARY`
     * and the specified scaling operation would require
     * rounding.
     * @throws IllegalArgumentException if `roundingMode` does not
     * represent a valid rounding mode.
     * @see RoundingMode
     */
    fun setScale(newScale: Int, roundingMode: RoundingMode): BigDecimal {
        val oldScale = this.scale
        if (newScale == oldScale) {
            // easy case
            return this
        }
        if (this.signum() == 0) {
            // zero can have any scale
            return zeroValueOf(newScale)
        }
        if (this.intCompact != INFLATED) {
            var rs = this.intCompact
            if (newScale > oldScale) {
                val raise = checkScale(newScale.toLong() - oldScale)
                rs = longMultiplyPowerTen(rs, raise)
                if (rs != INFLATED) {
                    return valueOf(rs, newScale)
                }
                val rb = bigMultiplyPowerTen(raise)
                return BigDecimal(rb, INFLATED, newScale, if (precision > 0) precision + raise else 0)
            } else {
                // newScale < oldScale -- drop some digits
                // Can't predict the precision due to the effect of rounding.
                val drop = checkScale(oldScale.toLong() - newScale)
                return if (drop < LONG_TEN_POWERS_TABLE.size) {
                    divideAndRound(rs, LONG_TEN_POWERS_TABLE[drop], newScale, roundingMode, newScale)
                } else {
                    divideAndRound(this.inflated(), bigTenToThe(drop).toLong(),
                            newScale, roundingMode, newScale)
                }
            }
        } else {
            if (newScale > oldScale) {
                val raise = checkScale(newScale.toLong() - oldScale)
                val rb = bigMultiplyPowerTen(this.intVal!!, raise)
                return BigDecimal(rb, INFLATED, newScale, if (precision > 0) precision + raise else 0)
            } else {
                // newScale < oldScale -- drop some digits
                // Can't predict the precision due to the effect of rounding.
                val drop = checkScale(oldScale.toLong() - newScale)
                return if (drop < LONG_TEN_POWERS_TABLE.size)
                    divideAndRound(
                        this.intVal!!, LONG_TEN_POWERS_TABLE[drop], newScale, roundingMode,
                        newScale
                    )
                else
                    divideAndRound(this.intVal!!, bigTenToThe(drop).toLong(),
                            newScale, roundingMode, newScale)
            }
        }
    }

    /**
     * Returns a `BigDecimal` whose scale is the specified
     * value, and whose value is numerically equal to this
     * `BigDecimal`'s.  Throws an `ArithmeticException`
     * if this is not possible.
     *
     *
     * This call is typically used to increase the scale, in which
     * case it is guaranteed that there exists a `BigDecimal`
     * of the specified scale and the correct value.  The call can
     * also be used to reduce the scale if the caller knows that the
     * `BigDecimal` has sufficiently many zeros at the end of
     * its fractional part (i.e., factors of ten in its integer value)
     * to allow for the rescaling without changing its value.
     *
     *
     * This method returns the same result as the two-argument
     * versions of `setScale`, but saves the caller the trouble
     * of specifying a rounding mode in cases where it is irrelevant.
     *
     * @apiNote Since `BigDecimal` objects are immutable,
     * calls of this method do *not* result in the original
     * object being modified, contrary to the usual convention of
     * having methods named `set*X*` mutate field
     * *`X`*.  Instead, `setScale` returns an
     * object with the proper scale; the returned object may or may
     * not be newly allocated.
     *
     * @param  newScale scale of the `BigDecimal` value to be returned.
     * @return a `BigDecimal` whose scale is the specified value, and
     * whose unscaled value is determined by multiplying or dividing
     * this `BigDecimal`'s unscaled value by the appropriate
     * power of ten to maintain its overall value.
     * @throws ArithmeticException if the specified scaling operation would
     * require rounding.
     * @see .setScale
     */
    fun setScale(newScale: Int): BigDecimal {
        return setScale(newScale, RoundingMode.UNNECESSARY)
    }

    /**
     * Returns a `BigDecimal` which is numerically equal to
     * this one but with any trailing zeros removed from the
     * representation.  For example, stripping the trailing zeros from
     * the `BigDecimal` value `600.0`, which has
     * [`BigInteger`, `scale`] components equals to
     * [6000, 1], yields `6E2` with [`BigInteger`,
     * `scale`] components equals to [6, -2].  If
     * this BigDecimal is numerically equal to zero, then
     * `BigDecimal.ZERO` is returned.
     *
     * @return a numerically equal `BigDecimal` with any
     * trailing zeros removed.
     * @since 1.5
     */
    fun stripTrailingZeros(): BigDecimal {
        return if (this.intCompact == 0L ||
                this.intVal != null && this.intVal.signum() == 0) {
            BigDecimal.ZERO
        } else if (this.intCompact != INFLATED) {
            createAndStripZerosToMatchScale(this.intCompact, this.scale, Long.MIN_VALUE)
        } else {
            createAndStripZerosToMatchScale(this.intVal!!, scale, Long.MIN_VALUE)
        }
    }

    // ----==== Decimal Point Motion Operations ====----

    /**
     * Returns a `BigDecimal` which is equivalent to this one
     * with the decimal point moved `n` places to the left.  If
     * `n` is non-negative, the call merely adds `n` to
     * the scale.  If `n` is negative, the call is equivalent
     * to `movePointRight(-n)`.  The `BigDecimal`
     * returned by this call has value <tt>(this
     * 10<sup>-n</sup>)</tt> and scale `max(this.scale()+n,
     * 0)`.
     *
     * @param  n number of places to move the decimal point to the left.
     * @return a `BigDecimal` which is equivalent to this one with the
     * decimal point moved `n` places to the left.
     * @throws ArithmeticException if scale overflows.
     */
    fun movePointLeft(n: Int): BigDecimal {
        // Cannot use movePointRight(-n) in case of n==Integer.MIN_VALUE
        val newScale = checkScale(scale.toLong() + n)
        val num = BigDecimal(intVal, intCompact, newScale, 0)
        return if (num.scale < 0) num.setScale(0, RoundingMode.UNNECESSARY) else num
    }

    /**
     * Returns a `BigDecimal` which is equivalent to this one
     * with the decimal point moved `n` places to the right.
     * If `n` is non-negative, the call merely subtracts
     * `n` from the scale.  If `n` is negative, the call
     * is equivalent to `movePointLeft(-n)`.  The
     * `BigDecimal` returned by this call has value <tt>(this
     *  10<sup>n</sup>)</tt> and scale `max(this.scale()-n,
     * 0)`.
     *
     * @param  n number of places to move the decimal point to the right.
     * @return a `BigDecimal` which is equivalent to this one
     * with the decimal point moved `n` places to the right.
     * @throws ArithmeticException if scale overflows.
     */
    fun movePointRight(n: Int): BigDecimal {
        // Cannot use movePointLeft(-n) in case of n==Integer.MIN_VALUE
        val newScale = checkScale(scale.toLong() - n)
        val num = BigDecimal(intVal, intCompact, newScale, 0)
        return if (num.scale < 0) num.setScale(0, RoundingMode.UNNECESSARY) else num
    }

    /**
     * Returns a BigDecimal whose numerical value is equal to
     * (`this` * 10<sup>n</sup>).  The scale of
     * the result is `(this.scale() - n)`.
     *
     * @param n the exponent power of ten to scale by
     * @return a BigDecimal whose numerical value is equal to
     * (`this` * 10<sup>n</sup>)
     * @throws ArithmeticException if the scale would be
     * outside the range of a 32-bit integer.
     *
     * @since 1.5
     */
    fun scaleByPowerOfTen(n: Int): BigDecimal {
        return BigDecimal(
            intVal, intCompact,
            checkScale(scale.toLong() - n), precision
        )
    }

    /**
     * Compares this `BigDecimal` with the specified
     * `BigDecimal`.  Two `BigDecimal` objects that are
     * equal in value but have a different scale (like 2.0 and 2.00)
     * are considered equal by this method.  This method is provided
     * in preference to individual methods for each of the six boolean
     * comparison operators (&lt;, ==,
     * &gt;, &gt;=, !=, &lt;=).  The
     * suggested idiom for performing these comparisons is:
     * `(x.compareTo(y)` &lt;*op*&gt; `0)`, where
     * &lt;*op*&gt; is one of the six comparison operators.
     *
     * @param  other `BigDecimal` to which this `BigDecimal` is
     * to be compared.
     * @return -1, 0, or 1 as this `BigDecimal` is numerically
     * less than, equal to, or greater than `other`.
     */
    override operator fun compareTo(other: BigDecimal): Int {
        // Quick path for equal scale and non-inflated case.
        if (this.scale == other.scale) {
            val xs = this.intCompact
            val ys = other.intCompact
            if (xs != INFLATED && ys != INFLATED)
                return if (xs != ys) (if (xs > ys) 1 else -1) else 0
        }
        val xsign = this.signum()
        val ysign = other.signum()
        if (xsign != ysign) {
            return if (xsign > ysign) 1 else -1
        }
        if (xsign == 0) {
            return 0
        }
        val cmp = compareMagnitude(other)
        return if (xsign > 0) cmp else -cmp
    }

    /**
     * Version of compareTo that ignores sign.
     */
    private fun compareMagnitude(other: BigDecimal): Int {
        // Match scales, avoid unnecessary inflation
        var ys = other.intCompact
        var xs = this.intCompact
        if (xs == 0L)
            return if (ys == 0L) 0 else -1
        if (ys == 0L)
            return 1

        val sdiff = this.scale.toLong() - other.scale
        if (sdiff != 0L) {
            // Avoid matching scales if the (adjusted) exponents differ
            val xae = this.precision().toLong() - this.scale   // [-1]
            val yae = other.precision().toLong() - other.scale     // [-1]
            if (xae < yae)
                return -1
            if (xae > yae) {
                return 1
            }
            val rb: BigInteger?
            if (sdiff < 0) {
                // The cases sdiff <= Integer.MIN_VALUE intentionally fall through.
                val result = compareInCompareMagnitude(xs, ys, sdiff)
                xs = result.second
                if (result.first) {
                    rb = bigMultiplyPowerTen((-sdiff).toInt())
                    return rb.compareMagnitude(other.intVal!!)
                }
            } else { // sdiff > 0
                // The cases sdiff > Integer.MAX_VALUE intentionally fall through.
                val result = compareInCompareMagnitude(ys, xs, sdiff)
                ys = result.second
                if (result.first) {
                    rb = other.bigMultiplyPowerTen(sdiff.toInt())
                    return this.intVal!!.compareMagnitude(rb)
                }
            }
        }
        return if (xs != INFLATED)
            if (ys != INFLATED) longCompareMagnitude(xs, ys) else -1
        else if (ys != INFLATED)
            1
        else
            this.intVal!!.compareMagnitude(other.intVal!!)
    } // private fun compareMagnitude(other: BigDecimal): Int

    /**
     * Returns the string representation of this `BigDecimal`,
     * using scientific notation if an exponent is needed.
     *
     *
     * A standard canonical string form of the `BigDecimal`
     * is created as though by the following steps: first, the
     * absolute value of the unscaled value of the `BigDecimal`
     * is converted to a string in base ten using the characters
     * `'0'` through `'9'` with no leading zeros (except
     * if its value is zero, in which case a single `'0'`
     * character is used).
     *
     *
     * Next, an *adjusted exponent* is calculated; this is the
     * negated scale, plus the number of characters in the converted
     * unscaled value, less one.  That is,
     * `-scale+(ulength-1)`, where `ulength` is the
     * length of the absolute value of the unscaled value in decimal
     * digits (its *precision*).
     *
     *
     * If the scale is greater than or equal to zero and the
     * adjusted exponent is greater than or equal to `-6`, the
     * number will be converted to a character form without using
     * exponential notation.  In this case, if the scale is zero then
     * no decimal point is added and if the scale is positive a
     * decimal point will be inserted with the scale specifying the
     * number of characters to the right of the decimal point.
     * `'0'` characters are added to the left of the converted
     * unscaled value as necessary.  If no character precedes the
     * decimal point after this insertion then a conventional
     * `'0'` character is prefixed.
     *
     *
     * Otherwise (that is, if the scale is negative, or the
     * adjusted exponent is less than `-6`), the number will be
     * converted to a character form using exponential notation.  In
     * this case, if the converted `BigInteger` has more than
     * one digit a decimal point is inserted after the first digit.
     * An exponent in character form is then suffixed to the converted
     * unscaled value (perhaps with inserted decimal point); this
     * comprises the letter `'E'` followed immediately by the
     * adjusted exponent converted to a character form.  The latter is
     * in base ten, using the characters `'0'` through
     * `'9'` with no leading zeros, and is always prefixed by a
     * sign character `'-'` (`'&#92;u002D'`) if the
     * adjusted exponent is negative, `'+'`
     * (`'&#92;u002B'`) otherwise).
     *
     *
     * Finally, the entire string is prefixed by a minus sign
     * character `'-'` (`'&#92;u002D'`) if the unscaled
     * value is less than zero.  No sign character is prefixed if the
     * unscaled value is zero or positive.
     *
     *
     * **Examples:**
     *
     * For each representation [*unscaled value*, *scale*]
     * on the left, the resulting string is shown on the right.
     * <pre>
     * [123,0]      "123"
     * [-123,0]     "-123"
     * [123,-1]     "1.23E+3"
     * [123,-3]     "1.23E+5"
     * [123,1]      "12.3"
     * [123,5]      "0.00123"
     * [123,10]     "1.23E-8"
     * [-123,12]    "-1.23E-10"
    </pre> *
     *
     * **Notes:**
     *
     *
     *  1. There is a one-to-one mapping between the distinguishable
     * `BigDecimal` values and the result of this conversion.
     * That is, every distinguishable `BigDecimal` value
     * (unscaled value and scale) has a unique string representation
     * as a result of using `toString`.  If that string
     * representation is converted back to a `BigDecimal` using
     * the [.BigDecimal] constructor, then the original
     * value will be recovered.
     *
     *  1. The string produced for a given number is always the same;
     * it is not affected by locale.  This means that it can be used
     * as a canonical string representation for exchanging decimal
     * data, or as a key for a Hashtable, etc.  Locale-sensitive
     * number formatting and parsing is handled by the [ ] class and its subclasses.
     *
     *  1. The [.toEngineeringString] method may be used for
     * presenting numbers with exponents in engineering notation, and the
     * [setScale][.setScale] method may be used for
     * rounding a `BigDecimal` so it has a known number of digits after
     * the decimal point.
     *
     *  1. The digit-to-character mapping provided by
     * `Character.forDigit` is used.
     *
     *
     *
     * @return string representation of this `BigDecimal`.
     * @see Character.forDigit
     *
     * @see .BigDecimal
     */
    override fun toString(): String {
        var sc = stringCache
        if (sc != null) {
            return sc
        }

        sc = layoutChars(true)
        stringCache = sc
        return sc
    }

    /**
     * Lay out this `BigDecimal` into a {@code char[]} array.
     * The Java 1.2 equivalent to this was called `getValueString`.
     *
     * @param  sci `true` for Scientific exponential notation;
     *          `false` for Engineering
     * @return string with canonical string representation of this
     *         `BigDecimal`
     */
    private fun layoutChars(sci: Boolean): String {
        if (this.scale == 0) {                    // zero scale is trivial
            return if (intCompact != INFLATED) intCompact.toString() else this.intVal.toString()
        }
        if (scale == 2  &&
            intCompact >= 0 && intCompact < Int.MAX_VALUE) {
            // currency fast path
            val lowInt = this.intCompact.toInt() % 100
            val highInt = this.intCompact.toInt() / 100
            return (highInt.toString() + '.' +
                    StringBuilderHelper.DIGIT_TENS[lowInt] +
                    StringBuilderHelper.DIGIT_ONES[lowInt])
        }

        val sbHelper: StringBuilderHelper = getStringBuilderHelper()
        val coeff: CharArray
        val offset: Int  // offset is the starting index for coeff array
        // Get the significand as an absolute value
        if (intCompact != INFLATED) {
            offset = sbHelper.putIntCompact(intCompact.absoluteValue)
            coeff  = sbHelper.cmpCharArray
        } else {
            offset = 0
            val str = this.intVal!!.abs().toString()
            coeff = CharArray(str.length)
            for (i in 0 until str.length) {
                coeff[i] = str[i]
            }
        }

        // Construct a buffer, with sufficient capacity for all cases.
        // If E-notation is needed, length will be: +1 if negative, +1
        // if '.' needed, +2 for "E+", + up to 10 for adjusted exponent.
        // Otherwise it could have +1 if negative, plus leading "0.00000"
        val buf = sbHelper.getStringBuilder()
        if (signum() < 0) {           // prefix '-' if negative
            buf.append('-')
        }
        val coeffLen = coeff.size - offset
        var adjusted = -scale.toLong() + (coeffLen -1)
        if ((scale >= 0) && (adjusted >= -6)) { // plain number
            var pad = scale - coeffLen          // count of padding zeros
            if (pad >= 0) {                     // 0.xxx form
                buf.append('0')
                buf.append('.')
                while (pad > 0) {
                    buf.append('0')
                    pad -= 1
                }
                buf.append(coeff, offset, coeffLen)
            } else {                         // xx.xx form
                buf.append(coeff, offset, -pad)
                buf.append('.')
                buf.append(coeff, -pad + offset, scale)
            }
        } else { // E-notation is needed
            if (sci) {                       // Scientific notation
                buf.append(coeff[offset]);   // first character
                if (coeffLen > 1) {          // more to come
                    buf.append('.');
                    buf.append(coeff, offset + 1, coeffLen - 1);
                }
            } else {                         // Engineering notation
                var sig = (adjusted % 3).toInt()
                if (sig < 0) {
                    sig += 3                 // [adjusted was negative]
                }
                adjusted -= sig              // now a multiple of 3
                sig += 1
                if (signum() == 0) {
                    when (sig) {
                        1 ->
                            buf.append('0') // exponent is a multiple of three
                        2 -> {
                            buf.append("0.00")
                            adjusted += 3
                        }
                        3 -> {
                            buf.append("0.0")
                            adjusted += 3
                        }
                        else ->
                            throw AssertionError("Unexpected sig value $sig")
                    }
                } else if (sig >= coeffLen) {   // significand all in integer
                    buf.append(coeff, offset, coeffLen)
                    // may need some zeros, too
                    for (i in sig - coeffLen downTo 1) {
                        buf.append('0')
                    }
                } else {                     // xx.xxE form
                    buf.append(coeff, offset, sig)
                    buf.append('.')
                    buf.append(coeff, offset + sig, coeffLen - sig)
                }
            }
            if (adjusted != 0L) {            // [!sci could have made 0]
                buf.append('E')
                if (adjusted > 0) {          // force sign for positive
                    buf.append('+')
                }
                buf.append(adjusted)
            }
        }
        return buf.toString()
    }

    /**
     * Compares this `BigDecimal` with the specified
     * `Object` for equality.  Unlike [ ][.compareTo], this method considers two
     * `BigDecimal` objects equal only if they are equal in
     * value and scale (thus 2.0 is not equal to 2.00 when compared by
     * this method).
     *
     * @param  other `Object` to which this `BigDecimal` is
     * to be compared.
     * @return `true` if and only if the specified `Object` is a
     * `BigDecimal` whose value and scale are equal to this
     * `BigDecimal`'s.
     * @see .compareTo
     * @see .hashCode
     */
    override fun equals(other: Any?): Boolean {
        if (other !is BigDecimal) {
            return false
        }
        if (other === this) {
            return true
        }
        if (scale != other.scale) {
            return false
        }
        val s = this.intCompact
        var xs = other.intCompact
        if (s != INFLATED) {
            if (xs == INFLATED) {
                xs = compactValFor(other.intVal!!)
            }
            return xs == s
        } else if (xs != INFLATED) {
            return xs == compactValFor(this.intVal!!)
        }
        return this.inflated() == other.inflated()
    }

    /**
     * Returns the hash code for this `BigDecimal`.  Note that
     * two `BigDecimal` objects that are numerically equal but
     * differ in scale (like 2.0 and 2.00) will generally *not*
     * have the same hash code.
     *
     * @return hash code for this `BigDecimal`.
     * @see .equals
     */
    override fun hashCode(): Int {
        return if (this.intCompact != INFLATED) {
            val val2 = if (intCompact < 0) -intCompact else intCompact
            val temp = (val2.ushr(32).toInt() * 31 + (val2 and BigInteger.LONG_MASK)).toInt()
            31 * (if (intCompact < 0) -temp else temp) + scale
        } else {
            31 * intVal.hashCode() + scale
        }
    }

    /**
     * Returns the minimum of this `BigDecimal` and
     * `other`.
     *
     * @param  other value with which the minimum is to be computed.
     * @return the `BigDecimal` whose value is the lesser of this
     * `BigDecimal` and `other`.  If they are equal,
     * as defined by the [compareTo][.compareTo]
     * method, `this` is returned.
     * @see .compareTo
     */
    fun min(other: BigDecimal): BigDecimal {
        return if (compareTo(other) <= 0) this else other
    }

    /**
     * Returns the maximum of this `BigDecimal` and `other`.
     *
     * @param  other value with which the maximum is to be computed.
     * @return the `BigDecimal` whose value is the greater of this
     * `BigDecimal` and `other`.  If they are equal,
     * as defined by the [compareTo][.compareTo]
     * method, `this` is returned.
     * @see .compareTo
     */
    fun max(other: BigDecimal): BigDecimal {
        return if (compareTo(other) >= 0) this else other
    }

    /**
     * Returns a string representation of this `BigDecimal`
     * without an exponent field.  For values with a positive scale,
     * the number of digits to the right of the decimal point is used
     * to indicate scale.  For values with a zero or negative scale,
     * the resulting string is generated as if the value were
     * converted to a numerically equal value with zero scale and as
     * if all the trailing zeros of the zero scale value were present
     * in the result.
     *
     * The entire string is prefixed by a minus sign character '-'
     * (<tt>'&#92;u002D'</tt>) if the unscaled value is less than
     * zero. No sign character is prefixed if the unscaled value is
     * zero or positive.
     *
     * Note that if the result of this method is passed to the
     * [string constructor][.BigDecimal], only the
     * numerical value of this `BigDecimal` will necessarily be
     * recovered; the representation of the new `BigDecimal`
     * may have a different scale.  In particular, if this
     * `BigDecimal` has a negative scale, the string resulting
     * from this method will have a scale of zero when processed by
     * the string constructor.
     *
     * (This method behaves analogously to the `toString`
     * method in 1.4 and earlier releases.)
     *
     * @return a string representation of this `BigDecimal`
     * without an exponent field.
     * @since 1.5
     * @see .toString
     * @see .toEngineeringString
     */
    fun toPlainString(): String {
        if (this.scale == 0) {
            return if (this.intCompact != INFLATED) {
                intCompact.toString()
            } else {
                intVal.toString()
            }
        }
        if (this.scale < 0) { // No decimal point
            if (signum() == 0) {
                return "0"
            }
            val tailingZeros = checkScaleNonZero(-(this.scale.toLong()))
            val buf: StringBuilder
            if (this.intCompact != INFLATED) {
                buf = StringBuilder(20 + tailingZeros)
                buf.append(this.intCompact)
            } else {
                val str = intVal.toString()
                buf = StringBuilder(str.length + tailingZeros)
                buf.append(str)
            }
            for (i in 0 until tailingZeros)
                buf.append('0')
            return buf.toString()
        }
        val str = if (this.intCompact != INFLATED) {
            intCompact.absoluteValue.toString()
        } else {
            intVal!!.abs().toString()
        }
        return getValueString(signum(), str, scale)
    }

    /* Returns a digit.digit string */
    private fun getValueString(signum: Int, intString: String, scale: Int): String {
        /* Insert decimal point */
        val insertionPoint = intString.length - scale
        if (insertionPoint == 0) {  /* Point goes right before intVal */
            return (if (signum < 0) "-0." else "0.") + intString
        } else if (insertionPoint > 0) { /* Point goes inside intVal */
            val buf = intString.substring(0, insertionPoint) + "." + intString.substring(insertionPoint)
            return if (signum < 0) "-" + buf else buf
        } else { /* We must insert zeros between point and intVal */
            val buf = StringBuilder(3 - insertionPoint + intString.length)
            buf.append(if (signum < 0) "-0." else "0.")
            for (i in 0 until -insertionPoint) {
                buf.append('0')
            }
            buf.append(intString)
            return buf.toString()
        }
    }

    /**
     * Compute this * 10 ^ n.
     * Needed mainly to allow special casing to trap zero value
     */
    private fun bigMultiplyPowerTen(n: Int): BigInteger {
        if (n <= 0) {
            return this.inflated()
        }
        return if (this.intCompact != INFLATED)
            bigTenToThe(n).multiply(intCompact)
        else
            intVal!! * bigTenToThe(n)
    }

    /**
     * Returns appropriate BigInteger from intVal field if intVal is
     * null, i.e. the compact representation is in use.
     */
    private fun inflated(): BigInteger {
        return intVal ?: BigInteger.valueOf(intCompact)
    }

    /**
     * Check a scale for Underflow or Overflow.  If this BigDecimal is
     * nonzero, throw an exception if the scale is outof range. If this
     * is zero, saturate the scale to the extreme value of the right
     * sign if the scale is out of range.
     *
     * @param longVal The new scale.
     * @throws ArithmeticException (overflow or underflow) if the new
     * scale is out of range.
     * @return validated scale as an int.
     */
    private fun checkScale(longVal: Long): Int {
        var asInt = longVal.toInt()
        if (asInt.toLong() != longVal) {
            asInt = if (longVal > Int.MAX_VALUE) Int.MAX_VALUE else Int.MIN_VALUE
            if (this.intCompact != 0L) {
                val b = this.intVal
                if (b == null || b.signum() != 0) {
                    throw ArithmeticException(if (asInt > 0) "Underflow" else "Overflow")
                }
            }
        }
        return asInt
    }
}

internal expect fun expandBigIntegerTenPowers(n: Int): BigInteger
internal expect fun getStringBuilderHelper(): BigDecimal.Companion.StringBuilderHelper
