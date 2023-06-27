package com.netsensia.blockchain.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import assertk.assertions.isFalse
import com.netsensia.blockchain.service.BlockchainService
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest
class BlockchainTest {

    @Inject
    lateinit var blockchainService: BlockchainService

    @Test
    fun `blockchain should start with genesis block`() {
        val blockchain = blockchainService.genesis()
        val blocks = blockchain.blocks
        assertThat(blocks.size).isEqualTo(1)
        assertThat(blocks[0].index).isEqualTo(0)
    }

    @Test
    fun `adding a block should increase the size of blockchain`() {
        val blockchain = blockchainService.genesis()
        val newChain = blockchain.mineAndAddBlock(listOf(Transaction(UUID.randomUUID(), "Alice", "Bob", 50.0)), 4)
        assertThat(blockchain.blocks.size).isEqualTo(1)
        assertThat(newChain.blocks.size).isEqualTo(2)
    }

    @Test
    fun `newly added block should contain given transactions`() {
        val blockchain = blockchainService.genesis()
        val transactions = listOf(Transaction(UUID.randomUUID(), "Alice", "Bob", 50.0))
        val newChain = blockchain.mineAndAddBlock(transactions, 4)
        assertThat(newChain.getLastBlock().transactions).isEqualTo(transactions)
    }

    @Test
    fun `blockchain should be valid after adding blocks`() {
        val blockchain = blockchainService.genesis()
        blockchain.mineAndAddBlock(listOf(Transaction(UUID.randomUUID(), "Alice", "Bob", 50.0)), 4)
        assertThat(blockchain.validate()).isTrue()
    }

    @Test
    fun `blockchain should be invalid if a block is tampered`() {
        val blockchain = blockchainService.genesis()

        val validChain = blockchain
            .mineAndAddBlock(listOf(Transaction(UUID.randomUUID(), "Alice", "Bob", 50.0)), 4)
            .mineAndAddBlock(listOf(Transaction(UUID.randomUUID(), "Alice", "Barry", 10.0)), 4)
            .mineAndAddBlock(listOf(Transaction(UUID.randomUUID(), "Anna", "Alice", 10.2)), 4)

        assertThat(validChain.validate()).isTrue()

        // Get a copy of the current blockchain state
        val validBlocks = validChain.blocks

        val tamperedChain = Blockchain(
            validBlocks.toMutableList().apply {
                this[1] = Block.Unmined(
                    1,
                    validBlocks[1].timestamp,
                    listOf(Transaction(UUID.randomUUID(), "Alice", "Barry", 11.0)),
                    validBlocks[1].previousHash
                ).mine(4)
            }
        )

        assertThat(tamperedChain.validate()).isFalse()
    }

    @Test
    fun `should calculate balances`() {
        val blockchain = blockchainService.genesis()
            .mineAndAddBlock(listOf(Transaction(UUID.randomUUID(), "Alice", "Chrismo", 50.0)), 4)
            .mineAndAddBlock(listOf(
                Transaction(UUID.randomUUID(), "Alice", "Bob", 10.0),
                Transaction(UUID.randomUUID(), "Chrismo", "Alice", 10.2)),
                4
            )

        assertThat(blockchain.blocks.size).isEqualTo(3)

        assertThat(blockchain.getBalance("Alice")).isEqualTo(10000 - 50.0 - 10.0 + 10.2)
        assertThat(blockchain.getBalance("Chrismo")).isEqualTo(10000 + 50.0 - 10.2)
        assertThat(blockchain.getBalance("Bob")).isEqualTo(10000 + 10.0)
    }

    @Test
    fun `should filter out transactions where balance is too low`() {
        val blockchain = blockchainService.genesis()
            .mineAndAddBlock(listOf(Transaction(UUID.randomUUID(), "Alice", "Chrismo", 50.0)), 4)
            .mineAndAddBlock(listOf(
                Transaction(UUID.randomUUID(), "Alice", "Bob", 10.0),
                Transaction(UUID.randomUUID(), "Chrismo", "Alice", 10.2)),
                4
            )
            .mineAndAddBlock(listOf(
                Transaction(UUID.randomUUID(), "Alice", "Chrismo", 100000.0),
                Transaction(UUID.randomUUID(), "Chrismo", "Alice", 10.0),
                Transaction(UUID.randomUUID(), "Chrismo", "Bob", 20.0),
            ), 4)

        assertThat(blockchain.blocks.size).isEqualTo(4)

        assertThat(blockchain.getBalance("Alice")).isEqualTo(10000 - 50.0 - 10.0 + 10.2 + 10.0)
        assertThat(blockchain.getBalance("Chrismo")).isEqualTo(10000 + 50.0 - 10.2 - 10.0 - 20.0)
        assertThat(blockchain.getBalance("Bob")).isEqualTo(10000 + 10.0 + 20.0)
    }
}
