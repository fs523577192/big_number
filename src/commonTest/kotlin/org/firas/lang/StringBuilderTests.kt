package org.firas.lang

import kotlin.random.Random

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 * @author Wu Yuping
 */
class StringBuilderTests {

    @Test
    fun testSetLength() {
        val random = Random.Default
        for (i in 1..1000) {
            val sb = StringBuilder()
            for (j in 1..random.nextInt(1, 1000 + 1)) {
                sb.append(random.nextInt(' '.toInt(), 128).toChar())
            }
            val length = random.nextInt(0, sb.length + 10 + 1)
            sb.setLength(length)
            assertEquals(length, sb.length)
        }
    }

    @Test
    fun testInsert() {
        val random = Random.Default
        for (i in 1..1000) {
            val sb1 = StringBuilder()
            val sb2 = StringBuilder()
            val length1 = random.nextInt(1, 1000 + 1)
            for (j in 1..length1) {
                sb1.append(random.nextInt(' '.toInt(), 128).toChar())
            }
            assertEquals(length1, sb1.length)

            for (j in 1..random.nextInt(1, 1000 + 1)) {
                sb2.append(random.nextInt(' '.toInt(), 128).toChar())
            }
            sb1.insert(random.nextInt(0, sb1.length + 1), sb2)
            assertEquals(length1 + sb2.length, sb1.length)
            assertTrue(sb1.contains(sb2))
        }
    }
}