package com.netsensia.blockchain.service

import Block
import Blockchain
import Transaction
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.inject.Singleton
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeLines

@Singleton
class BlockchainService {

    val objectMapper = jacksonObjectMapper().apply {
        registerModule(KotlinModule())
    }

    fun genesis(): Blockchain {
        return Blockchain(listOf(createGenesisBlock()))
    }

    fun load(filename: String): Blockchain {
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

    fun save(filename: String, chain: Blockchain) {
        val path = Path.of(filename)
        val file = Files.deleteIfExists(path).let { Files.createFile(path) }
        val strings = chain.blocks.map {
            objectMapper.writeValueAsString(it)
        }
        file.writeLines(strings)
    }

    private fun createGenesisBlock(): Block.Mined {
        val genesisTransactions = listOf(Transaction("Genesis", "Genesis", 0.0))
        val unminedGenesisBlock = Block.Unmined(
            index = 0,
            timestamp = System.currentTimeMillis(),
            transactions = genesisTransactions,
            previousHash = "0"
        )
        return unminedGenesisBlock.mine(4)
    }
}