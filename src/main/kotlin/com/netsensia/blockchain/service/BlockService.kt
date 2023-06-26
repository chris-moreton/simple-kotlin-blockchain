package com.netsensia.blockchain.service

import com.netsensia.blockchain.model.Block
import com.netsensia.blockchain.model.Transaction
import com.netsensia.blockchain.simulate.Network
import jakarta.inject.Singleton

interface BlockService {
    suspend fun mineBlock(previousBlock: Block.Mined, transactions: List<Transaction>, difficulty: Int = Network.DIFFICULTY): Block.Mined
}

@Singleton
class DefaultBlockService : BlockService {
    override suspend fun mineBlock(previousBlock: Block.Mined, transactions: List<Transaction>, difficulty: Int): Block.Mined {
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