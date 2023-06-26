package com.netsensia.blockchain.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.netsensia.blockchain.model.Block
import com.netsensia.blockchain.model.Transaction
import com.netsensia.blockchain.model.hash
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

@MicronautTest
class DefaultBlockchainServiceTest(
    private val blockchainService: BlockchainService,
    private val blockService: BlockService
) {

    @Test
    fun `should save blockchain to a file and be able to reload it`() {
        val blockchain = blockchainService.genesis()
            .mineAndAddBlock(listOf(Transaction("Alice", "Bob", 50.0)), 4)
            .mineAndAddBlock(listOf(Transaction("Alice", "Barry", 10.0)), 4)
            .mineAndAddBlock(listOf(Transaction("Anna", "Alice", 10.2)), 4)

        assertThat(blockchain.blocks.size).isEqualTo(4)

        blockchainService.save("blockchain.txt", blockchain)

        val newChain = blockchainService.genesis()
            .mineAndAddBlock(listOf(Transaction("Steve", "Bob", 5.0)), 4)
            .mineAndAddBlock(listOf(Transaction("Alice", "Barry", 16.0)), 3)
            .mineAndAddBlock(listOf(Transaction("Anna", "Jemma", 11.1)), 5)

        assertThat(newChain.blocks.size).isEqualTo(4)

        blockchainService.save("blockchain.txt", newChain)

        val loadedChain = blockchainService.load("blockchain.txt")
        assertThat(loadedChain.blocks.size).isEqualTo(4)
        assertThat(loadedChain.validate()).isTrue()
        assertThat(loadedChain.equals(newChain)).isTrue()
    }

    @Test
    fun `should validate a valid newly-received block`() {
        val blockchain = blockchainService.genesis()
            .mineAndAddBlock(listOf(Transaction("Alice", "Chrismo", 50.0)), 4)
            .mineAndAddBlock(listOf(
                Transaction("Alice", "Bob", 10.0),
                Transaction("Chrismo", "Alice", 10.2)),
                4
            )

        val transactions = listOf(
            Transaction("Alice", "Bob", 15.0),
            Transaction("Chrismo", "Alice", 12.2)
        )

        val newBlock = runBlocking {
            blockService.mineBlock(blockchain.getLastBlock(), transactions, 4)
        }

        assertThat(blockchainService.isValidNewBlock(newBlock, blockchain.getLastBlock())).isTrue()
    }

    @Test
    fun `should not validate am invalid newly-received block`() {
        val blockchain = blockchainService.genesis()
            .mineAndAddBlock(listOf(Transaction("Alice", "Chrismo", 50.0)), 4)
            .mineAndAddBlock(listOf(
                Transaction("Alice", "Bob", 10.0),
                Transaction("Chrismo", "Alice", 10.2)),
                4
            )

        val transactions = listOf(
            Transaction("Alice", "Bob", 15.0),
            Transaction("Chrismo", "Alice", 12.2)
        )

        val fakeBlock = Block.Mined(
            index = 3,
            timestamp = System.currentTimeMillis(),
            previousHash = blockchain.getLastBlock().hash,
            transactions = transactions,
            nonce = 1231239879,
            difficulty = 4,
            hash = "Fake hash".hash().replaceRange(0, 4, "0000")
        )


        assertThat(blockchainService.isValidNewBlock(fakeBlock, blockchain.getLastBlock())).isFalse()
    }

}