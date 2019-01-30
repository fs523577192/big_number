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
package org.firas.math

/**
 * A class used to represent multiprecision integers that makes efficient
 * use of allocated space by allowing a number to occupy only part of
 * an array so that the arrays do not have to be reallocated as often.
 * When performing an operation with many iterations the array used to
 * hold a number is only increased when necessary and does not have to
 * be the same size as the number it represents. A mutable number allows
 * calculations to occur on the same number without having to create
 * a new number for every step of the calculation as occurs with
 * BigIntegers.
 *
 * Note that SignedMutableBigIntegers only support signed addition and
 * subtraction. All other operations occur as with MutableBigIntegers.
 *
 * @see     BigInteger
 * @author  Michael McCloskey
 * @author  Wu Yuping
 * @since   Java 1.3
 */
internal class SignedMutableBigInteger: MutableBigInteger {

    /**
     * The sign of this MutableBigInteger.
     */
    internal var sign = 1

    /**
     * The default constructor. An empty MutableBigInteger is created with
     * a one word capacity.
     */
    internal constructor(): super()

    /**
     * Construct a new MutableBigInteger with a magnitude specified by
     * the int val.
     */
    internal constructor(value: Int): super(value)

    /**
     * Construct a new MutableBigInteger with a magnitude equal to the
     * specified MutableBigInteger.
     */
    internal constructor(value: MutableBigInteger): super(value)

    // --== Arithmetic Operations ==--
    /**
     * Signed addition built upon unsigned add and subtract.
     */
    fun signedAdd(addend: SignedMutableBigInteger) {
        if (this.sign == addend.sign) {
            add(addend)
        } else {
            this.sign = this.sign * subtract(addend)
        }
    }

    /**
     * Signed addition built upon unsigned add and subtract.
     */
    fun signedAdd(addend: MutableBigInteger) {
        if (this.sign == 1) {
            add(addend)
        } else {
            this.sign = this.sign * subtract(addend)
        }
    }

    /**
     * Signed subtraction built upon unsigned add and subtract.
     */
    fun signedSubtract(addend: SignedMutableBigInteger) {
        if (this.sign == addend.sign) {
            this.sign = this.sign * subtract(addend)
        } else {
            add(addend)
        }
    }

    /**
     * Signed subtraction built upon unsigned add and subtract.
     */
    fun signedSubtract(addend: MutableBigInteger) {
        if (this.sign == 1) {
            this.sign = this.sign * subtract(addend)
        } else {
            add(addend)
        }
        if (this.intLen == 0) {
            this.sign = 1
        }
    }
}