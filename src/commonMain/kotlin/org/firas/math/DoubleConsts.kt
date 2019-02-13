/*
 * Copyright (c) 2003, 2016, Oracle and/or its affiliates. All rights reserved.
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

/**
 * This class contains additional constants documenting limits of the
 * `Double` type.
 *
 * @author Joseph D. Darcy
 * @author Wu Yuping
 */
class DoubleConsts private constructor() {

    companion object {
        /**
         * The number of logical bits in the significand of a
         * `Double` number, including the implicit bit.
         */
        const val SIGNIFICAND_WIDTH = 53

        /**
         * Maximum exponent a finite `Double` variable may have.
         * It is equal to the value returned by
         * `Math.getExponent(Double.MAX_VALUE)`.
         *
         * @since Java 1.6
         */
        const val MAX_EXPONENT = 1023

        /**
         * Minimum exponent a normalized `Double` variable may
         * have.  It is equal to the value returned by
         * `Math.getExponent(Double.MIN_NORMAL)`.
         *
         * @since Java 1.6
         */
        const val MIN_EXPONENT = -1022

        /**
         * The exponent the smallest positive `double`
         * subnormal value would have if it could be normalized..
         */
        const val MIN_SUB_EXPONENT = MIN_EXPONENT - (SIGNIFICAND_WIDTH - 1)

        /**
         * Bias used in representing a `double` exponent.
         */
        const val EXP_BIAS = 1023

        /**
         * Bit mask to isolate the sign bit of a `double`.
         */
        const val SIGN_BIT_MASK = 0x4000000000000000L shl 1 // 0x8000000000000000L

        /**
         * Bit mask to isolate the exponent field of a
         * `Double`.
         */
        const val EXP_BIT_MASK = 0x7FF0000000000000L

        /**
         * Bit mask to isolate the significand field of a
         * `Double`.
         */
        const val SIGNIF_BIT_MASK = 0x000FFFFFFFFFFFFFL
    }
}