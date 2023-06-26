package com.netsensia.blockchain.service

import com.netsensia.blockchain.model.Block
import com.netsensia.blockchain.model.Transaction
import jakarta.inject.Singleton

interface BlockService {
    fun mineBlock(previousBlock: Block.Mined, transactions: List<Transaction>, difficulty: Int): Block.Mined
}

@Singleton
class DefaultBlockService : BlockService {
    override fun mineBlock(previousBlock: Block.Mined, transactions: List<Transaction>, difficulty: Int): Block.Mined {
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