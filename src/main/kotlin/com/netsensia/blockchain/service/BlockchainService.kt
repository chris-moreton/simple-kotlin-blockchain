package com.netsensia.blockchain.service

import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.netsensia.blockchain.model.Block
import com.netsensia.blockchain.model.Blockchain
import com.netsensia.blockchain.model.Transaction
import jakarta.inject.Singleton
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeLines

interface BlockchainService {
    fun genesis(): Blockchain
    fun load(filename: String): Blockchain
    fun save(filename: String, chain: Blockchain)
    fun isValidNewBlock(block: Block.Mined, lastBlock: Block.Mined): Boolean
}

@Singleton
class DefaultBlockchainService : BlockchainService {

    val objectMapper = jacksonObjectMapper().apply {
        registerModule(
            KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyCollection, false)
                .configure(KotlinFeature.NullToEmptyMap, false)
                .configure(KotlinFeature.NullIsSameAsDefault, false)
                .configure(KotlinFeature.SingletonSupport, false)
                .configure(KotlinFeature.StrictNullChecks, false)
                .build()
        )
    }

    override fun genesis(): Blockchain {
        return Blockchain(listOf(createGenesisBlock()))
    }

    override fun load(filename: String): Blockchain {
        val path = Path.of(filename)
        val blocks = if (Files.exists(path)) {
            Files.readAllLines(path).map { readBlock(it) }.toMutableList()
        } else {
            mutableListOf(createGenesisBlock())
        }
        return Blockchain(blocks)
    }

    private fun readBlock(blockString: String): Block.Mined {
        return objectMapper.readValue(blockString, Block.Mined::class.java)
            ?: throw IllegalStateException("Unable to read block from file")
    }

    override fun save(filename: String, chain: Blockchain) {
        val path = Path.of(filename)
        val file = Files.deleteIfExists(path).let { Files.createFile(path) }
        val strings = chain.blocks.map {
            objectMapper.writeValueAsString(it)
        }
        file.writeLines(strings)
    }

    override fun isValidNewBlock(block: Block.Mined, lastBlock: Block.Mined): Boolean {
        return block.index == lastBlock.index + 1 &&
                block.previousHash == lastBlock.hash &&
                block.hash == Block.calculateHash(block, block.nonce)
    }

    private fun createGenesisBlock(): Block.Mined {
        val preMineTransactions = listOf(
            Transaction("Genesis", "Alice", 10000.0),
            Transaction("Genesis", "Bob", 10000.0),
            Transaction("Genesis", "Chrismo", 10000.0)
       )
        val unminedGenesisBlock = Block.Unmined(
            index = 0,
            timestamp = System.currentTimeMillis(),
            transactions = preMineTransactions,
            previousHash = "0"
        )
        return unminedGenesisBlock.mine(4)
    }

}