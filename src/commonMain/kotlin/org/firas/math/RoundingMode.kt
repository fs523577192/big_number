package org.firas.math

/**
 *
 * @author Wu Yuping
 */
expect enum class RoundingMode {

    /**
     * Rounding mode to round away from zero.  Always increments the
     * digit prior to a non-zero discarded fraction.  Note that this
     * rounding mode never decreases the magnitude of the calculated
     * value.
     *
     *<p>Example:
     *<table border>
     * <caption><b>Rounding mode UP Examples</b></caption>
     *<tr valign=top><th>Input Number</th>
     *    <th>Input rounded to one digit<br> with `UP` rounding
     *<tr align=right><td>5.5</td>  <td>6</td>
     *<tr align=right><td>2.5</td>  <td>3</td>
     *<tr align=right><td>1.6</td>  <td>2</td>
     *<tr align=right><td>1.1</td>  <td>2</td>
     *<tr align=right><td>1.0</td>  <td>1</td>
     *<tr align=right><td>-1.0</td> <td>-1</td>
     *<tr align=right><td>-1.1</td> <td>-2</td>
     *<tr align=right><td>-1.6</td> <td>-2</td>
     *<tr align=right><td>-2.5</td> <td>-3</td>
     *<tr align=right><td>-5.5</td> <td>-6</td>
     *</table>
     */
    UP,

    /**
     * Rounding mode to round towards zero.  Never increments the digit
     * prior to a discarded fraction (i.e., truncates).  Note that this
     * rounding mode never increases the magnitude of the calculated value.
     *
     *<p>Example:
     *<table border>
     * <caption><b>Rounding mode DOWN Examples</b></caption>
     *<tr valign=top><th>Input Number</th>
     *    <th>Input rounded to one digit<br> with `DOWN` rounding
     *<tr align=right><td>5.5</td>  <td>5</td>
     *<tr align=right><td>2.5</td>  <td>2</td>
     *<tr align=right><td>1.6</td>  <td>1</td>
     *<tr align=right><td>1.1</td>  <td>1</td>
     *<tr align=right><td>1.0</td>  <td>1</td>
     *<tr align=right><td>-1.0</td> <td>-1</td>
     *<tr align=right><td>-1.1</td> <td>-1</td>
     *<tr align=right><td>-1.6</td> <td>-1</td>
     *<tr align=right><td>-2.5</td> <td>-2</td>
     *<tr align=right><td>-5.5</td> <td>-5</td>
     *</table>
     */
    DOWN,

    /**
     * Rounding mode to round towards positive infinity.  If the
     * result is positive, behaves as for {@code RoundingMode.UP};
     * if negative, behaves as for {@code RoundingMode.DOWN}.  Note
     * that this rounding mode never decreases the calculated value.
     *
     *<p>Example:
     *<table border>
     * <caption><b>Rounding mode CEILING Examples</b></caption>
     *<tr valign=top><th>Input Number</th>
     *    <th>Input rounded to one digit<br> with `CEILING` rounding
     *<tr align=right><td>5.5</td>  <td>6</td>
     *<tr align=right><td>2.5</td>  <td>3</td>
     *<tr align=right><td>1.6</td>  <td>2</td>
     *<tr align=right><td>1.1</td>  <td>2</td>
     *<tr align=right><td>1.0</td>  <td>1</td>
     *<tr align=right><td>-1.0</td> <td>-1</td>
     *<tr align=right><td>-1.1</td> <td>-1</td>
     *<tr align=right><td>-1.6</td> <td>-1</td>
     *<tr align=right><td>-2.5</td> <td>-2</td>
     *<tr align=right><td>-5.5</td> <td>-5</td>
     *</table>
     */
    CEILING,

    /**
     * Rounding mode to round towards negative infinity.  If the
     * result is positive, behave as for {@code RoundingMode.DOWN};
     * if negative, behave as for {@code RoundingMode.UP}.  Note that
     * this rounding mode never increases the calculated value.
     *
     *<p>Example:
     *<table border>
     * <caption><b>Rounding mode FLOOR Examples</b></caption>
     *<tr valign=top><th>Input Number</th>
     *    <th>Input rounded to one digit<br> with `FLOOR` rounding
     *<tr align=right><td>5.5</td>  <td>5</td>
     *<tr align=right><td>2.5</td>  <td>2</td>
     *<tr align=right><td>1.6</td>  <td>1</td>
     *<tr align=right><td>1.1</td>  <td>1</td>
     *<tr align=right><td>1.0</td>  <td>1</td>
     *<tr align=right><td>-1.0</td> <td>-1</td>
     *<tr align=right><td>-1.1</td> <td>-2</td>
     *<tr align=right><td>-1.6</td> <td>-2</td>
     *<tr align=right><td>-2.5</td> <td>-3</td>
     *<tr align=right><td>-5.5</td> <td>-6</td>
     *</table>
     */
    FLOOR,

    /**
     * Rounding mode to round towards {@literal "nearest neighbor"}
     * unless both neighbors are equidistant, in which case round up.
     * Behaves as for {@code RoundingMode.UP} if the discarded
     * fraction is &ge; 0.5; otherwise, behaves as for
     * {@code RoundingMode.DOWN}.  Note that this is the rounding
     * mode commonly taught at school.
     *
     *<p>Example:
     *<table border>
     * <caption><b>Rounding mode HALF_UP Examples</b></caption>
     *<tr valign=top><th>Input Number</th>
     *    <th>Input rounded to one digit<br> with `HALF_UP` rounding
     *<tr align=right><td>5.5</td>  <td>6</td>
     *<tr align=right><td>2.5</td>  <td>3</td>
     *<tr align=right><td>1.6</td>  <td>2</td>
     *<tr align=right><td>1.1</td>  <td>1</td>
     *<tr align=right><td>1.0</td>  <td>1</td>
     *<tr align=right><td>-1.0</td> <td>-1</td>
     *<tr align=right><td>-1.1</td> <td>-1</td>
     *<tr align=right><td>-1.6</td> <td>-2</td>
     *<tr align=right><td>-2.5</td> <td>-3</td>
     *<tr align=right><td>-5.5</td> <td>-6</td>
     *</table>
     */
    HALF_UP,

    /**
     * Rounding mode to round towards {@literal "nearest neighbor"}
     * unless both neighbors are equidistant, in which case round
     * down.  Behaves as for {@code RoundingMode.UP} if the discarded
     * fraction is &gt; 0.5; otherwise, behaves as for
     * {@code RoundingMode.DOWN}.
     *
     *<p>Example:
     *<table border>
     * <caption><b>Rounding mode HALF_DOWN Examples</b></caption>
     *<tr valign=top><th>Input Number</th>
     *    <th>Input rounded to one digit<br> with `HALF_DOWN` rounding
     *<tr align=right><td>5.5</td>  <td>5</td>
     *<tr align=right><td>2.5</td>  <td>2</td>
     *<tr align=right><td>1.6</td>  <td>2</td>
     *<tr align=right><td>1.1</td>  <td>1</td>
     *<tr align=right><td>1.0</td>  <td>1</td>
     *<tr align=right><td>-1.0</td> <td>-1</td>
     *<tr align=right><td>-1.1</td> <td>-1</td>
     *<tr align=right><td>-1.6</td> <td>-2</td>
     *<tr align=right><td>-2.5</td> <td>-2</td>
     *<tr align=right><td>-5.5</td> <td>-5</td>
     *</table>
     */
    HALF_DOWN,

    /**
     * Rounding mode to round towards the {@literal "nearest neighbor"}
     * unless both neighbors are equidistant, in which case, round
     * towards the even neighbor.  Behaves as for
     * {@code RoundingMode.HALF_UP} if the digit to the left of the
     * discarded fraction is odd; behaves as for
     * {@code RoundingMode.HALF_DOWN} if it's even.  Note that this
     * is the rounding mode that statistically minimizes cumulative
     * error when applied repeatedly over a sequence of calculations.
     * It is sometimes known as {@literal "Banker's rounding,"} and is
     * chiefly used in the USA.  This rounding mode is analogous to
     * the rounding policy used for `float` and `double`
     * arithmetic in Java.
     *
     *<p>Example:
     *<table border>
     * <caption><b>Rounding mode HALF_EVEN Examples</b></caption>
     *<tr valign=top><th>Input Number</th>
     *    <th>Input rounded to one digit<br> with `HALF_EVEN` rounding
     *<tr align=right><td>5.5</td>  <td>6</td>
     *<tr align=right><td>2.5</td>  <td>2</td>
     *<tr align=right><td>1.6</td>  <td>2</td>
     *<tr align=right><td>1.1</td>  <td>1</td>
     *<tr align=right><td>1.0</td>  <td>1</td>
     *<tr align=right><td>-1.0</td> <td>-1</td>
     *<tr align=right><td>-1.1</td> <td>-1</td>
     *<tr align=right><td>-1.6</td> <td>-2</td>
     *<tr align=right><td>-2.5</td> <td>-2</td>
     *<tr align=right><td>-5.5</td> <td>-6</td>
     *</table>
     */
    HALF_EVEN,

    /**
     * Rounding mode to assert that the requested operation has an exact
     * result, hence no rounding is necessary.  If this rounding mode is
     * specified on an operation that yields an inexact result, an
     * `ArithmeticException` is thrown.
     *<p>Example:
     *<table border>
     * <caption><b>Rounding mode UNNECESSARY Examples</b></caption>
     *<tr valign=top><th>Input Number</th>
     *    <th>Input rounded to one digit<br> with `UNNECESSARY` rounding
     *<tr align=right><td>5.5</td>  <td>throw `ArithmeticException`</td>
     *<tr align=right><td>2.5</td>  <td>throw `ArithmeticException`</td>
     *<tr align=right><td>1.6</td>  <td>throw `ArithmeticException`</td>
     *<tr align=right><td>1.1</td>  <td>throw `ArithmeticException`</td>
     *<tr align=right><td>1.0</td>  <td>1</td>
     *<tr align=right><td>-1.0</td> <td>-1</td>
     *<tr align=right><td>-1.1</td> <td>throw `ArithmeticException`</td>
     *<tr align=right><td>-1.6</td> <td>throw `ArithmeticException`</td>
     *<tr align=right><td>-2.5</td> <td>throw `ArithmeticException`</td>
     *<tr align=right><td>-5.5</td> <td>throw `ArithmeticException`</td>
     *</table>
     */
    UNNECESSARY;
}