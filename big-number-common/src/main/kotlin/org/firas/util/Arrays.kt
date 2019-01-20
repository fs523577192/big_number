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
 * This class contains various methods for manipulating arrays (such as
 * sorting and searching). This class also contains a static factory
 * that allows arrays to be viewed as lists.
 *
 * <p>The methods in this class all throw a {@code NullPointerException},
 * if the specified array reference is null, except where noted.
 *
 * <p>The documentation for the methods contained in this class includes
 * briefs description of the <i>implementations</i>. Such descriptions should
 * be regarded as <i>implementation notes</i>, rather than parts of the
 * <i>specification</i>. Implementors should feel free to substitute other
 * algorithms, so long as the specification itself is adhered to. (For
 * example, the algorithm used by `sort(Object[])` does not have to be
 * a MergeSort, but it does have to be <i>stable</i>.)
 *
 * <p>This class is a member of the
 * <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author Josh Bloch
 * @author Neal Gafter
 * @author John Rose
 * @author Wu Yuping
 * @since  Java 1.2
 */
class Arrays private constructor() {
    companion object {
        /**
         * Assigns the specified int value to each element of the specified array
         * of ints.
         *
         * @param a the array to be filled
         * @param value the value to be stored in all elements of the array
         */
        fun fill(a: IntArray, value: Int) {
            var i = 0
            val len = a.size
            while (i < len) {
                a[i] = value
                i += 1
            }
        }

        /**
         * Assigns the specified int value to each element of the specified
         * range of the specified array of ints.  The range to be filled
         * extends from index <tt>fromIndex</tt>, inclusive, to index
         * <tt>toIndex</tt>, exclusive.  (If <tt>fromIndex==toIndex</tt>, the
         * range to be filled is empty.)
         *
         * @param a the array to be filled
         * @param fromIndex the index of the first element (inclusive) to be
         * filled with the specified value
         * @param toIndex the index of the last element (exclusive) to be
         * filled with the specified value
         * @param value the value to be stored in all elements of the array
         * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
         * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
         * <tt>toIndex &gt; a.length</tt>
         */
        fun fill(a: IntArray, fromIndex: Int, toIndex: Int, value: Int) {
            rangeCheck(a.size, fromIndex, toIndex)
            for (i in fromIndex until toIndex)
                a[i] = value
        }

        /**
         * Checks that `fromIndex` and `toIndex` are in
         * the range and throws an exception if they aren't.
         */
        private fun rangeCheck(arrayLength: Int, fromIndex: Int, toIndex: Int) {
            if (fromIndex > toIndex) {
                throw IllegalArgumentException(
                        "fromIndex($fromIndex) > toIndex($toIndex)")
            }
            if (fromIndex < 0) {
                throw IndexOutOfBoundsException("Array index out of range: $fromIndex")
            }
            if (toIndex > arrayLength) {
                throw IndexOutOfBoundsException("Array index out of range: $toIndex")
            }
        }
    }
}