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
package org.firas.lang

/**
 *
 * @author Wu Yuping
 */
class Character {
    companion object {
        /**
         * The minimum radix available for conversion to and from strings.
         * The constant value of this field is the smallest value permitted
         * for the radix argument in radix-conversion methods such as the
         * `digit` method, the `forDigit` method, and the
         * `toString` method of class `Integer`.
         *
         * @see Character.digit
         * @see Character.forDigit
         * @see Int.toString
         * @see String.toInt
         */
        val MIN_RADIX = 2

        /**
         * The maximum radix available for conversion to and from strings.
         * The constant value of this field is the largest value permitted
         * for the radix argument in radix-conversion methods such as the
         * `digit` method, the `forDigit` method, and the
         * `toString` method of class `Integer`.
         *
         * @see Character.digit
         * @see Character.forDigit
         * @see Int.toString
         * @see String.toInt
         */
        val MAX_RADIX = 36

        /**
         * Returns the numeric value of the character `ch` in the
         * specified radix.
         *
         *
         * If the radix is not in the range `MIN_RADIX`
         * `radix`  `MAX_RADIX` or if the
         * value of `ch` is not a valid digit in the specified
         * radix, `-1` is returned. A character is a valid digit
         * if at least one of the following is true:
         *
         *  * The method `isDigit` is `true` of the character
         * and the Unicode decimal digit value of the character (or its
         * single-character decomposition) is less than the specified radix.
         * In this case the decimal digit value is returned.
         *  * The character is one of the uppercase Latin letters
         * `'A'` through `'Z'` and its code is less than
         * `radix + 'A' - 10`.
         * In this case, `ch - 'A' + 10`
         * is returned.
         *  * The character is one of the lowercase Latin letters
         * `'a'` through `'z'` and its code is less than
         * `radix + 'a' - 10`.
         * In this case, `ch - 'a' + 10`
         * is returned.
         *  * The character is one of the fullwidth uppercase Latin letters A
         * (`'\u005CuFF21'`) through Z (`'\u005CuFF3A'`)
         * and its code is less than
         * `radix + '\u005CuFF21' - 10`.
         * In this case, `ch - '\u005CuFF21' + 10`
         * is returned.
         *  * The character is one of the fullwidth lowercase Latin letters a
         * (`'\u005CuFF41'`) through z (`'\u005CuFF5A'`)
         * and its code is less than
         * `radix + '\u005CuFF41' - 10`.
         * In this case, `ch - '\u005CuFF41' + 10`
         * is returned.
         *
         *
         *
         * **Note:** This method cannot handle [ supplementary characters](#supplementary). To support
         * all Unicode characters, including supplementary characters, use
         * the [.digit] method.
         *
         * @param   ch      the character to be converted.
         * @param   radix   the radix.
         * @return  the numeric value represented by the character in the
         * specified radix.
         * @see Character.forDigit
         * @see Character.isDigit
         * @author Wu Yuping
         */
        fun digit(ch: Char, radix: Int): Int {
            if (radix < MIN_RADIX || radix > MAX_RADIX) {
                throw IllegalArgumentException("Invalid radix: $radix")
            }
            val result = when (ch) {
                in '0' .. '9' -> {
                    ch.toInt() - '0'.toInt()
                }
                in 'A' .. 'Z' -> {
                    ch.toInt() - 'A'.toInt() + 10
                }
                in 'a' .. 'z' -> {
                    ch.toInt() - 'a'.toInt() + 10
                }
                // TODO: full-width character
                else -> throw NumberFormatException("Not a valid digit: $ch")
            }
            if (result >= radix) {
                throw NumberFormatException("Not a valid digit ($ch) with the radix $radix")
            }
            return result
        }
    }
}