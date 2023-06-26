package com.netsensia.blockchain.model

import Transaction
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.netsensia.blockchain.service.BlockchainService
import org.junit.jupiter.api.Test

class BlockchainServiceTest {

    val blockchainService = BlockchainService()

    @Test
    fun `should save blockchain to a file`() {
        val blockchain = blockchainService.genesis()
            .addBlock(listOf(Transaction("Alice", "Bob", 50.0)), 4)
            .addBlock(listOf(Transaction("Alice", "Barry", 10.0)), 4)
            .addBlock(listOf(Transaction("Anna", "Alice", 10.2)), 4)

        assertThat(blockchain.blocks.size).isEqualTo(4)

        blockchainService.save("blockchain.txt", blockchain)

        val newChain = blockchainService.genesis()
            .addBlock(listOf(Transaction("Steve", "Bob", 5.0)), 4)
            .addBlock(listOf(Transaction("Alice", "Barry", 16.0)), 3)
            .addBlock(listOf(Transaction("Anna", "Jemma", 11.1)), 5)

        assertThat(newChain.blocks.size).isEqualTo(4)

        blockchainService.save("blockchain.txt", newChain)

        val loadedChain = blockchainService.load("blockchain.txt")
        assertThat(loadedChain.blocks.size).isEqualTo(4)
        assertThat(loadedChain.validate()).isTrue()
        assertThat(loadedChain.equals(newChain)).isTrue()
    }


}