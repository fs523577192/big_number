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
package org.firas.util

/**
 *
 * @author Wu Yuping
 */
class Integers private constructor() {

    companion object {
        /**
         * Returns the signum function of the specified `int` value.  (The
         * return value is -1 if the specified value is negative; 0 if the
         * specified value is zero; and 1 if the specified value is positive.)
         *
         * @param i the value whose signum is to be computed
         * @return the signum function of the specified `int` value.
         * @since Java 1.5
         */
        fun signum(i: Int): Int {
            // HD, Section 2-7
            return i.shr(31) or (-i).ushr(31)
        }

        /**
         * Returns the signum function of the specified `long` value.  (The
         * return value is -1 if the specified value is negative; 0 if the
         * specified value is zero; and 1 if the specified value is positive.)
         *
         * @param i the value whose signum is to be computed
         * @return the signum function of the specified `long` value.
         * @since Java 1.5
         */
        fun signum(i: Long): Int {
            // HD, Section 2-7
            return (i.shr(63) or (-i).ushr(63)).toInt()
        }

        /**
         * Returns the number of one-bits in the two's complement binary
         * representation of the specified `int` value.  This function is
         * sometimes referred to as the *population count*.
         *
         * @param i the value whose bits are to be counted
         * @return the number of one-bits in the two's complement binary
         * representation of the specified `int` value.
         * @since 1.5
         */
        fun bitCount(i: Int): Int {
            // HD, Figure 5-2
            var n = i - (i.ushr(1) and 0x55555555)
            n = (n and 0x33333333) + (n.ushr(2) and 0x33333333)
            n = n + n.ushr(4) and 0x0f0f0f0f
            n += n.ushr(8)
            n += n.ushr(16)
            return n and 0x3f
        }

        /**
         * Returns an `int` value with at most a single one-bit, in the
         * position of the highest-order ("leftmost") one-bit in the specified
         * `int` value.  Returns zero if the specified value has no
         * one-bits in its two's complement binary representation, that is, if it
         * is equal to zero.
         *
         * @param i the value whose highest one bit is to be computed
         * @return an `int` value with a single one-bit, in the position
         * of the highest-order one-bit in the specified value, or zero if
         * the specified value is itself equal to zero.
         * @since Java 1.5
         */
        fun highestOneBit(i: Int): Int {
            var n = i
            // HD, Figure 3-1
            n = n or (n shr 1)
            n = n or (n shr 2)
            n = n or (n shr 4)
            n = n or (n shr 8)
            n = n or (n shr 16)
            return n - n.ushr(1)
        }

        /**
         * Returns an `int` value with at most a single one-bit, in the
         * position of the lowest-order ("rightmost") one-bit in the specified
         * `int` value.  Returns zero if the specified value has no
         * one-bits in its two's complement binary representation, that is, if it
         * is equal to zero.
         *
         * @param i the value whose lowest one bit is to be computed
         * @return an `int` value with a single one-bit, in the position
         * of the lowest-order one-bit in the specified value, or zero if
         * the specified value is itself equal to zero.
         * @since Java 1.5
         */
        fun lowestOneBit(i: Int): Int {
            // HD, Section 2-1
            return i and -i
        }

        /**
         * Returns the number of zero bits preceding the highest-order
         * ("leftmost") one-bit in the two's complement binary representation
         * of the specified `int` value.  Returns 32 if the
         * specified value has no one-bits in its two's complement representation,
         * in other words if it is equal to zero.
         *
         *
         * Note that this method is closely related to the logarithm base 2.
         * For all positive `int` values x:
         *
         *  * floor(log<sub>2</sub>(x)) = `31 - numberOfLeadingZeros(x)`
         *  * ceil(log<sub>2</sub>(x)) = `32 - numberOfLeadingZeros(x - 1)`
         *
         *
         * @param i the value whose number of leading zeros is to be computed
         * @return the number of zero bits preceding the highest-order
         * ("leftmost") one-bit in the two's complement binary representation
         * of the specified `int` value, or 32 if the value
         * is equal to zero.
         * @since Java 1.5
         */
        fun numberOfLeadingZeros(i: Int): Int {
            var i = i
            // HD, Figure 5-6
            if (i == 0) {
                return 32
            }
            var n = 1
            if (i.ushr(16) == 0) {
                n += 16
                i = i shl 16
            }
            if (i.ushr(24) == 0) {
                n += 8
                i = i shl 8
            }
            if (i.ushr(28) == 0) {
                n += 4
                i = i shl 4
            }
            if (i.ushr(30) == 0) {
                n += 2
                i = i shl 2
            }
            n -= i.ushr(31)
            return n
        }

        /**
         * Returns the number of zero bits preceding the highest-order
         * ("leftmost") one-bit in the two's complement binary representation
         * of the specified `long` value.  Returns 64 if the
         * specified value has no one-bits in its two's complement representation,
         * in other words if it is equal to zero.
         *
         *
         * Note that this method is closely related to the logarithm base 2.
         * For all positive `long` values x:
         *
         *  * floor(log<sub>2</sub>(x)) = `63 - numberOfLeadingZeros(x)`
         *  * ceil(log<sub>2</sub>(x)) = `64 - numberOfLeadingZeros(x - 1)`
         *
         *
         * @param i the value whose number of leading zeros is to be computed
         * @return the number of zero bits preceding the highest-order
         * ("leftmost") one-bit in the two's complement binary representation
         * of the specified `long` value, or 64 if the value
         * is equal to zero.
         * @since 1.5
         */
        fun numberOfLeadingZeros(i: Long): Int {
            // HD, Figure 5-6
            if (i == 0L)
                return 64
            var n = 1
            var x = i.ushr(32).toInt()
            if (x == 0) {
                n += 32
                x = i.toInt()
            }
            if (x.ushr(16) == 0) {
                n += 16
                x = x shl 16
            }
            if (x.ushr(24) == 0) {
                n += 8
                x = x shl 8
            }
            if (x.ushr(28) == 0) {
                n += 4
                x = x shl 4
            }
            if (x.ushr(30) == 0) {
                n += 2
                x = x shl 2
            }
            n -= x.ushr(31)
            return n
        }

        /**
         * Returns the number of zero bits following the lowest-order ("rightmost")
         * one-bit in the two's complement binary representation of the specified
         * `long` value.  Returns 64 if the specified value has no
         * one-bits in its two's complement representation, in other words if it is
         * equal to zero.
         *
         * @param i the value whose number of trailing zeros is to be computed
         * @return the number of zero bits following the lowest-order ("rightmost")
         * one-bit in the two's complement binary representation of the
         * specified `long` value, or 64 if the value is equal
         * to zero.
         * @since 1.5
         */
        fun numberOfTrailingZeros(i: Long): Int {
            // HD, Figure 5-14
            if (i == 0L) return 64
            var n = 63
            var x: Int
            var y = i.toInt()
            if (y != 0) {
                n -= 32
                x = y
            } else x = i.ushr(32).toInt()
            y = x shl 16
            if (y != 0) {
                n -= 16
                x = y
            }
            y = x shl 8
            if (y != 0) {
                n -= 8
                x = y
            }
            y = x shl 4
            if (y != 0) {
                n -= 4
                x = y
            }
            y = x shl 2
            if (y != 0) {
                n -= 2
                x = y
            }
            return n - (x shl 1).ushr(31)
        }

        /**
         * Returns the number of zero bits following the lowest-order ("rightmost")
         * one-bit in the two's complement binary representation of the specified
         * `Int` value.  Returns 32 if the specified value has no
         * one-bits in its two's complement representation, in other words if it is
         * equal to zero.
         *
         * @param i the value whose number of trailing zeros is to be computed
         * @return the number of zero bits following the lowest-order ("rightmost")
         *     one-bit in the two's complement binary representation of the
         *     specified `Int` value, or 32 if the value is equal
         *     to zero.
         * @since Java 1.5
         */
        fun numberOfTrailingZeros(i: Int): Int {
            var i = i

            // HD, Figure 5-14
            if (i == 0) return 32

            var n = 31
            var y: Int = i shl 16
            if (y != 0) {
                n -= 16
                i = y
            }

            y = i shl 8
            if (y != 0) {
                n -= 8
                i = y
            }

            y = i shl 4
            if (y != 0) {
                n -= 4
                i = y
            }

            y = i shl 2
            if (y != 0) {
                n -= 2
                i = y
            }
            return n - (i shl 1).ushr(31)
        }
    }
}