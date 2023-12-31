package com.netsensia.blockchain.model

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest
class BlockTest {

    @Test
    fun `mining should produce a hash with difficulty number of leading zeroes`() {
        val difficulty = 4
        val unminedBlock = Block.Unmined(0, System.currentTimeMillis(), listOf(Transaction(UUID.randomUUID(), "Alice", "Bob", 50.0)), "0")
        val minedBlock = unminedBlock.mine(difficulty)
        assertEquals("0".repeat(difficulty), minedBlock.hash.substring(0, difficulty))
    }

    @Test
    fun `changing transaction should change hash`() {
        val difficulty = 4
        val unminedBlock1 = Block.Unmined(0, System.currentTimeMillis(), listOf(Transaction(UUID.randomUUID(), "Alice", "Bob", 50.0)), "0")
        val unminedBlock2 = Block.Unmined(0, System.currentTimeMillis(), listOf(Transaction(UUID.randomUUID(), "Anna", "Bob", 50.0)), "0")

        val minedBlock1 = unminedBlock1.mine(difficulty)
        val minedBlock2 = unminedBlock2.mine(difficulty)

        assertNotEquals(minedBlock1.hash, minedBlock2.hash)
    }
}