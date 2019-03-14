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
         * Searches the specified array of longs for the specified value using the
         * binary search algorithm.  The array must be sorted (as
         * by the [.sort] method) prior to making this call.  If it
         * is not sorted, the results are undefined.  If the array contains
         * multiple elements with the specified value, there is no guarantee which
         * one will be found.
         *
         * @param a the array to be searched
         * @param key the value to be searched for
         * @return index of the search key, if it is contained in the array;
         * otherwise, `(-(*insertion point*) - 1)`.  The
         * *insertion point* is defined as the point at which the
         * key would be inserted into the array: the index of the first
         * element greater than the key, or `a.length` if all
         * elements in the array are less than the specified key.  Note
         * that this guarantees that the return value will be &gt;= 0 if
         * and only if the key is found.
         */
        fun binarySearch(a: LongArray, key: Long): Int {
            return binarySearch0(a, 0, a.size, key)
        }

        /**
         * Searches a range of
         * the specified array of longs for the specified value using the
         * binary search algorithm.
         * The range must be sorted (as
         * by the [.sort] method)
         * prior to making this call.  If it
         * is not sorted, the results are undefined.  If the range contains
         * multiple elements with the specified value, there is no guarantee which
         * one will be found.
         *
         * @param a the array to be searched
         * @param fromIndex the index of the first element (inclusive) to be
         * searched
         * @param toIndex the index of the last element (exclusive) to be searched
         * @param key the value to be searched for
         * @return index of the search key, if it is contained in the array
         * within the specified range;
         * otherwise, `(-(*insertion point*) - 1)`.  The
         * *insertion point* is defined as the point at which the
         * key would be inserted into the array: the index of the first
         * element in the range greater than the key,
         * or `toIndex` if all
         * elements in the range are less than the specified key.  Note
         * that this guarantees that the return value will be &gt;= 0 if
         * and only if the key is found.
         * @throws IllegalArgumentException
         * if `fromIndex > toIndex`
         * @throws ArrayIndexOutOfBoundsException
         * if `fromIndex < 0 or toIndex > a.length`
         * @since Java 1.6
         */
        fun binarySearch(
            a: LongArray, fromIndex: Int, toIndex: Int,
            key: Long
        ): Int {
            rangeCheck(a.size, fromIndex, toIndex)
            return binarySearch0(a, fromIndex, toIndex, key)
        }

        // Like public version, but without range checks.
        private fun binarySearch0(
            a: LongArray, fromIndex: Int, toIndex: Int,
            key: Long
        ): Int {
            var low = fromIndex
            var high = toIndex - 1

            while (low <= high) {
                val mid = (low + high).ushr(1)
                val midVal = a[mid]

                if (midVal < key)
                    low = mid + 1
                else if (midVal > key)
                    high = mid - 1
                else
                    return mid // key found
            }
            return -(low + 1)  // key not found.
        }

        /**
         * Returns `true` if the two specified arrays of longs are
         * *equal* to one another.  Two arrays are considered equal if both
         * arrays contain the same number of elements, and all corresponding pairs
         * of elements in the two arrays are equal.  In other words, two arrays
         * are equal if they contain the same elements in the same order.  Also,
         * two array references are considered equal if both are `null`.
         *
         * @param a one array to be tested for equality
         * @param a2 the other array to be tested for equality
         * @return `true` if the two arrays are equal
         */
        fun equals(a: LongArray?, a2: LongArray?): Boolean {
            if (a === a2) {
                return true
            }
            if (a == null || a2 == null) {
                return false
            }

            val length = a.size
            if (a2.size != length) {
                return false
            }
            for (i in 0 until length) {
                if (a[i] != a2[i]) {
                    return false
                }
            }
            return true
        }

        /**
         * Returns `true` if the two specified arrays of Objects are
         * *equal* to one another.  The two arrays are considered equal if
         * both arrays contain the same number of elements, and all corresponding
         * pairs of elements in the two arrays are equal.  Two objects `e1`
         * and `e2` are considered *equal* if
         * `Objects.equals(e1, e2)`.
         * In other words, the two arrays are equal if
         * they contain the same elements in the same order.  Also, two array
         * references are considered equal if both are `null`.
         *
         * @param a one array to be tested for equality
         * @param a2 the other array to be tested for equality
         * @return `true` if the two arrays are equal
         */
        fun equals(a: Array<Any?>?, a2: Array<Any?>?): Boolean {
            if (a === a2) {
                return true
            }
            if (a == null || a2 == null) {
                return false
            }

            val length = a.size
            if (a2.size != length) {
                return false
            }
            for (i in 0 until length) {
                if (a[i] != a2[i]) {
                    return false
                }
            }
            return true
        }

        /**
         * Returns a hash code based on the contents of the specified array.
         * For any two `long` arrays `a` and `b`
         * such that `Arrays.equals(a, b)`, it is also the case that
         * `Arrays.hashCode(a) == Arrays.hashCode(b)`.
         *
         *
         * The value returned by this method is the same value that would be
         * obtained by invoking the [hashCode][List.hashCode]
         * method on a [List] containing a sequence of [Long]
         * instances representing the elements of `a` in the same order.
         * If `a` is `null`, this method returns 0.
         *
         * @param a the array whose hash value to compute
         * @return a content-based hash code for `a`
         * @since Java 1.5
         */
        fun hashCode(a: LongArray?): Int {
            if (a == null) {
                return 0
            }
            var result = 1
            for (element in a) {
                val elementHash = (element xor element.ushr(32)).toInt()
                result = 31 * result + elementHash
            }
            return result
        }

        /**
         * Returns a hash code based on the contents of the specified array.  If
         * the array contains other arrays as elements, the hash code is based on
         * their identities rather than their contents.  It is therefore
         * acceptable to invoke this method on an array that contains itself as an
         * element,  either directly or indirectly through one or more levels of
         * arrays.
         *
         *
         * For any two arrays `a` and `b` such that
         * `Arrays.equals(a, b)`, it is also the case that
         * `Arrays.hashCode(a) == Arrays.hashCode(b)`.
         *
         *
         * The value returned by this method is equal to the value that would
         * be returned by `Arrays.asList(a).hashCode()`, unless `a`
         * is `null`, in which case `0` is returned.
         *
         * @param a the array whose content-based hash code to compute
         * @return a content-based hash code for `a`
         * @see .deepHashCode
         * @since Java 1.5
         */
        fun hashCode(a: Array<Any?>?): Int {
            if (a == null) {
                return 0
            }
            var result = 1
            for (element in a) {
                result = 31 * result + (element?.hashCode() ?: 0)
            }
            return result
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