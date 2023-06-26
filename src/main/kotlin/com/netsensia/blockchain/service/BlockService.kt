package com.netsensia.blockchain.service

import Transaction
import jakarta.inject.Singleton

@Singleton
class BlockService {
    fun mineBlock(previousBlock: Block.Mined, transactions: List<Transaction>, difficulty: Int): Block.Mined {
        val unminedBlock = Block.Unmined(
            index = previousBlock.index + 1,
            timestamp = System.currentTimeMillis(),
            transactions = transactions,
            previousHash = previousBlock.hash
        )

        val minedBlock = unminedBlock.mine(difficulty)
        return minedBlock
    }
}