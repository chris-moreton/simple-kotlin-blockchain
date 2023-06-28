package com.netsensia.blockchain.model

import com.netsensia.blockchain.SimpleKotlinBlockchainCommand.Companion.output
import com.netsensia.blockchain.service.DefaultBlockService
import com.netsensia.blockchain.simulate.DefaultSimulator.Companion.DIFFICULTY
import com.netsensia.blockchain.simulate.Network
import kotlinx.coroutines.runBlocking

class Blockchain(val blocks: List<Block.Mined>) {

    val blockService = DefaultBlockService()

    fun mineAndAddBlock(transactions: List<Transaction>, difficulty: Int = DIFFICULTY): Blockchain {
        val validTransactions = validTransactionsOnly(transactions)
        val minedBlock = runBlocking {
            blockService.mineBlock(blocks.last(), validTransactions, difficulty)
        }
        return Blockchain(blocks + minedBlock)
    }

    fun validTransactionsOnly(
        transactions: List<Transaction>
    ): List<Transaction> {
        var effectiveBalances = HashMap<String, Double>()
        val validTransactions = transactions.filter { transaction ->
            val valid = validTransaction(transaction, effectiveBalances)
            // update effective balance
            if (valid) {
                effectiveBalances[transaction.sender] = effectiveBalances.getOrDefault(
                    transaction.sender,
                    getBalance(transaction.sender)
                ) - transaction.amount
                effectiveBalances[transaction.recipient] = effectiveBalances.getOrDefault(
                    transaction.recipient,
                    getBalance(transaction.recipient)
                ) + transaction.amount
            }
            valid
        }
        return validTransactions
    }

    fun addMinedBlock(block: Block.Mined): Blockchain {
        return Blockchain(blocks + block)
    }

    fun replaceLastBlock(block: Block.Mined): Blockchain {
        return Blockchain(blocks.dropLast(1) + block)
    }

    fun validTransaction(transaction: Transaction, effectiveBalances: HashMap<String, Double>): Boolean {
        if (transaction.sender == "Genesis") {
            return true
        }

        // reject if transaction already exists in blockchain
        blocks.forEach { block ->
            if (transaction.id in block.transactions.map { it.id }) {
                output("Transaction ${transaction.id} already exists in blockchain in block ${block.hash}", 5)
                output("Block transactions: ${block.transactions}", 5)
                return false
            }
        }

        val balance = effectiveBalances.getOrDefault(transaction.sender, getBalance(transaction.sender))
        if (balance >= transaction.amount) {
            return true
        } else {
            output("Insufficient funds [${balance}] for transaction ${transaction.id} with amount ${transaction.amount}", 5)
            return false
        }
    }

    fun getBalance(address: String): Double {
        var balance = 0.0
        for (block in blocks) {
            for (transaction in block.transactions) {
                if (transaction.sender == address) {
                    balance -= transaction.amount
                }
                if (transaction.recipient == address) {
                    balance += transaction.amount
                }
            }
        }
        return balance
    }

    fun getLastBlock(): Block.Mined = blocks.last()

    fun validate(): Boolean {
        for (i in 1 until blocks.size) {
            val current = blocks[i]
            val previous = blocks[i - 1]

            if (current.hash != Block.calculateHash(current, current.nonce)) {
                return false
            }

            if (current.previousHash != previous.hash) {
                return false
            }
        }
        return true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Blockchain

        return (blocks == other.blocks)
    }

    override fun hashCode(): Int {
        var result = blocks.hashCode()
        result = 31 * result + blockService.hashCode()
        return result
    }

}

