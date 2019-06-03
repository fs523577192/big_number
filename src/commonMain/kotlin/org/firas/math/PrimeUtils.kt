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
package org.firas.math

import kotlin.js.JsName
import kotlin.random.Random
import kotlin.math.absoluteValue

/**
 *
 * @author Wu Yuping
 */
internal class PrimeUtils private constructor() {

    companion object {
        /**
         * Bit lengths larger than this constant can cause overflow in searchLen
         * calculation and in BitSieve.singleSearch method.
         */
        private const val PRIME_SEARCH_BIT_LENGTH_LIMIT = 500000000

        // Minimum size in bits that the requested prime number has
        // before we use the large prime number generating algorithms.
        // The cutoff of 95 was chosen empirically for best performance.
        private const val SMALL_PRIME_THRESHOLD = 95

        // Certainty required to meet the spec of probablePrime
        private const val DEFAULT_PRIME_CERTAINTY = 100

        private val SMALL_PRIME_PRODUCT = BigInteger.valueOf(3L * 5 * 7 * 11 * 13 * 17 * 19 * 23 * 29 * 31 * 37 * 41)

        /**
         * Returns `true` if this BigInteger is probably prime,
         * `false` if it's definitely composite.
         *
         * This method assumes bitLength > 2.
         *
         * @param  certainty a measure of the uncertainty that the caller is
         * willing to tolerate: if the call returns `true`
         * the probability that this BigInteger is prime exceeds
         * `(1 - 1/2<sup>certainty</sup>)`.  The execution time of
         * this method is proportional to the value of this parameter.
         * @return `true` if this BigInteger is probably prime,
         * `false` if it's definitely composite.
         */
        @JsName("primeToCertainty")
        internal fun primeToCertainty(value: BigInteger, certainty: Int, random: Random): Boolean {
            var rounds: Int
            val n = (minOf(certainty, Int.MAX_VALUE - 1) + 1) / 2

            // The relationship between the certainty and the number of rounds
            // we perform is given in the draft standard ANSI X9.80, "PRIME
            // NUMBER GENERATION, PRIMALITY TESTING, AND PRIMALITY CERTIFICATES".
            val sizeInBits = value.bitLength()
            if (sizeInBits < 100) {
                rounds = 50
                rounds = if (n < rounds) n else rounds
                return passesMillerRabin(value, rounds, random)
            }

            rounds = when {
                sizeInBits < 256 -> 27
                sizeInBits < 512 -> 15
                sizeInBits < 768 -> 8
                sizeInBits < 1024 -> 4
                else -> 2
            }
            rounds = if (n < rounds) n else rounds

            return passesMillerRabin(value, rounds, random) && passesLucasLehmer(value)
        }

        /**
         * Returns true iff this BigInteger is a Lucas-Lehmer probable prime.
         *
         * The following assumptions are made:
         * This BigInteger is a positive, odd number.
         */
        private fun passesLucasLehmer(value: BigInteger): Boolean {
            val thisPlusOne = value + BigInteger.ONE

            // Step 1
            var d = 5
            while (jacobiSymbol(d, value) != -1) {
                // 5, -7, 9, -11, ...
                d = if (d < 0) d.absoluteValue+2 else -(d+2)
            }

            // Step 2
            val u = lucasLehmerSequence(d, thisPlusOne, value)

            // Step 3
            return u.rem(value) == BigInteger.ZERO
        }

        /**
         * Computes Jacobi(p,n).
         * Assumes n positive, odd, n>=3.
         */
        private fun jacobiSymbol(p: Int, n: BigInteger): Int {
            var p = p
            if (p == 0) {
                return 0
            }
            // Algorithm and comments adapted from Colin Plumb's C library.
            var j = 1
            var u = n.mag[n.mag.size - 1]

            // Make p positive
            if (p < 0) {
                p = -p
                val n8 = u and 7
                if (n8 == 3 || n8 == 7) {
                    j = -j // 3 (011) or 7 (111) mod 8
                }
            }

            // Get rid of factors of 2 in p
            while (p and 3 == 0) {
                p = p shr 2
            }
            if (p and 1 == 0) {
                p = p shr 1
                if (u xor (u shr 1) and 2 != 0) {
                    j = -j // 3 (011) or 5 (101) mod 8
                }
            }
            if (p == 1) {
                return j
            }
            // Then, apply quadratic reciprocity
            if (p and u and 2 != 0) {
                // p = u = 3 (mod 4)?
                j = -j
            }
            // And reduce u mod p
            u = n.rem(BigInteger.valueOf(p.toLong())).toInt()

            // Now compute Jacobi(u,p), u < p
            while (u != 0) {
                while (u and 3 == 0) {
                    u = u shr 2
                }
                if (u and 1 == 0) {
                    u = u shr 1
                    if (p xor (p shr 1) and 2 != 0) {
                        j = -j     // 3 (011) or 5 (101) mod 8
                    }
                }
                if (u == 1) {
                    return j
                }
                // Now both u and p are odd, so use quadratic reciprocity
                if (!(u < p)) {
                    throw AssertionError()
                }
                val t = u
                u = p
                p = t
                if (u and p and 2 != 0) {
                    // u = p = 3 (mod 4)?
                    j = -j
                }
                // Now u >= p, so it can be reduced
                u %= p
            }
            return 0
        } // private fun jacobiSymbol(p: Int, n: BigInteger): Int

        private fun lucasLehmerSequence(z: Int, k: BigInteger, n: BigInteger): BigInteger {
            val d = BigInteger.valueOf(z.toLong())
            var u = BigInteger.ONE
            var v = BigInteger.ONE

            for (i in k.bitLength() - 2 downTo 0) {
                var u2 = u.times(v).rem(n)

                var v2 = (AlgorithmUtils.square(v) + d * AlgorithmUtils.square(u)).rem(n)
                if (v2.testBit(0)) {
                    v2 -= n
                }
                v2 = v2.shr(1)

                u = u2
                v = v2
                if (k.testBit(i)) {
                    u2 = u.plus(v).rem(n)
                    if (u2.testBit(0)) {
                        u2 -= n
                    }
                    u2 = u2.shr(1)
                    v2 = v.plus(d.times(u)).rem(n)
                    if (v2.testBit(0)) {
                        v2 -= n
                    }
                    v2 = v2.shr(1)

                    u = u2
                    v = v2
                }
            }
            return u
        } // private fun lucasLehmerSequence(z: Int, k: BigInteger, n: BigInteger): BigInteger

        /**
         * Returns true iff this BigInteger passes the specified number of
         * Miller-Rabin tests. This test is taken from the DSA spec (NIST FIPS
         * 186-2).
         *
         * The following assumptions are made:
         * This BigInteger is a positive, odd number greater than 2.
         * iterations<=50.
         */
        private fun passesMillerRabin(value: BigInteger, iterations: Int, rnd: Random?): Boolean {
            var rnd = rnd
            // Find a and m such that m is odd and this == 1 + 2**a * m
            val valueMinusOne = value - BigInteger.ONE
            var m = valueMinusOne
            val a = m.getLowestSetBit()
            m = m.shr(a)

            // Do the tests
            if (rnd == null) {
                rnd = Random.Default
            }
            for (i in 0 until iterations) {
                // Generate a uniform random on (1, value)
                var b: BigInteger
                do {
                    b = BigInteger.fromRandom(value.bitLength(), rnd)
                } while (b <= BigInteger.ONE || b >= value)

                var j = 0
                var z = b.modPow(m, value)
                while (!(j == 0 && z == BigInteger.ONE || z == valueMinusOne)) {
                    if (j > 0 && z == BigInteger.ONE || ++j == a) {
                        return false
                    }
                    z = z.modPow(BigInteger.TWO, value)
                }
            }
            return true
        } // private fun passesMillerRabin(value: BigInteger, iterations: Int, rnd: Random?): Boolean

        /**
         * Find a random number of the specified bitLength that is probably prime.
         * This method is used for smaller primes, its performance degrades on
         * larger bitlengths.
         *
         * This method assumes bitLength > 1.
         */
        private fun smallPrime(bitLength: Int, certainty: Int, rnd: Random): BigInteger {
            val magLen = (bitLength + 31).ushr(5)
            val temp = IntArray(magLen)
            val highBit = 1 shl (bitLength + 31 and 0x1f)  // High bit of high int
            val highMask = (highBit shl 1) - 1  // Bits to keep in high int

            while (true) {
                // Construct a candidate
                for (i in 0 until magLen) {
                    temp[i] = rnd.nextInt()
                }
                temp[0] = temp[0] and highMask or highBit  // Ensure exact length
                if (bitLength > 2) {
                    temp[magLen - 1] = temp[magLen - 1] or 1  // Make odd if bitlen > 2
                }
                val p = BigInteger(temp, 1)

                // Do cheap "pre-test" if applicable
                if (bitLength > 6) {
                    val r = p.rem(SMALL_PRIME_PRODUCT).toLong()
                    if (r % 3 == 0L || r % 5 == 0L || r % 7 == 0L || r % 11 == 0L ||
                            r % 13 == 0L || r % 17 == 0L || r % 19 == 0L || r % 23 == 0L ||
                            r % 29 == 0L || r % 31 == 0L || r % 37 == 0L || r % 41 == 0L)
                        continue // Candidate is composite; try another
                }

                // All candidates of bitLength 2 and 3 are prime by this point
                if (bitLength < 4) {
                    return p
                }
                // Do expensive test if we survive pre-test (or it's inapplicable)
                if (primeToCertainty(p, certainty, rnd)) {
                    return p
                }
            }
        } // private fun smallPrime(bitLength: Int, certainty: Int, rnd: Random): BigInteger

        /**
         * Find a random number of the specified bitLength that is probably prime.
         * This method is more appropriate for larger bitlengths since it uses
         * a sieve to eliminate most composites before using a more expensive
         * test.
         */
        private fun largePrime(bitLength: Int, certainty: Int, rnd: Random): BigInteger {
            var p: BigInteger
            p = BigInteger.fromRandom(bitLength, rnd).setBit(bitLength - 1)
            p.mag[p.mag.size - 1] = p.mag[p.mag.size - 1] and -0x2

            // Use a sieve length likely to contain the next prime number
            val searchLen = getPrimeSearchLen(bitLength)
            var searchSieve = BitSieve(p, searchLen)
            var candidate = searchSieve.retrieve(p, certainty, rnd)

            while (candidate == null || candidate.bitLength() != bitLength) {
                p = p.plus(BigInteger.valueOf(2L * searchLen))
                if (p.bitLength() != bitLength) {
                    p = BigInteger.fromRandom(bitLength, rnd).setBit(bitLength - 1)
                }
                p.mag[p.mag.size - 1] = p.mag[p.mag.size - 1] and -0x2
                searchSieve = BitSieve(p, searchLen)
                candidate = searchSieve.retrieve(p, certainty, rnd)
            }
            return candidate
        } // private fun largePrime(bitLength: Int, certainty: Int, rnd: Random): BigInteger

        private fun getPrimeSearchLen(bitLength: Int): Int {
            if (bitLength > PRIME_SEARCH_BIT_LENGTH_LIMIT + 1) {
                throw ArithmeticException("Prime search implementation restriction on bitLength")
            }
            return bitLength / 20 * 64
        }
    }
}