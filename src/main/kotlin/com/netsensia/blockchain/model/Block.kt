package com.netsensia.blockchain.model
import com.netsensia.blockchain.SimpleKotlinBlockchainCommand.Companion.output
import com.netsensia.blockchain.simulate.DefaultSimulator.Companion.DIFFICULTY
import com.netsensia.blockchain.simulate.Network
import java.security.MessageDigest
import kotlin.random.Random

sealed class Block {
    abstract val index: Int
    abstract val timestamp: Long
    abstract val transactions: List<Transaction>
    abstract val previousHash: String

    data class Unmined(
        override val index: Int,
        override val timestamp: Long,
        override val transactions: List<Transaction>,
        override val previousHash: String,
    ) : Block() {

        fun mine(difficulty: Int = DIFFICULTY): Mined {
            val target = "0".repeat(difficulty)
            var nonce = Random.nextInt()
            var hash = calculateHash(this, nonce)
            while (hash.substring(0, difficulty) != target) {
                nonce = Random.nextInt()
                hash = calculateHash(this, nonce)
            }
            return Mined(index, timestamp, transactions, previousHash, difficulty, nonce, hash)
        }

        fun toMined(difficulty: Int, nonce: Int): Mined {
            val hash = calculateHash(this, nonce)
            return Mined(
                index = index,
                timestamp = timestamp,
                transactions = transactions,
                previousHash = previousHash,
                difficulty = difficulty,
                nonce = nonce,
                hash = hash
            )
        }

    }

    data class Mined(
        override val index: Int,
        override val timestamp: Long,
        override val transactions: List<Transaction>,
        override val previousHash: String,
        val difficulty: Int,
        val nonce: Int,
        val hash: String
    ) : Block()

    companion object {
        fun calculateHash(block: Block, nonce: Int): String {
            return "${block.index}${block.timestamp}${block.transactions.hashCode()}${block.previousHash}${nonce}".hash()
        }
    }
}

fun String.hash(): String {
    return MessageDigest.getInstance("SHA-256")
        .digest(this.toByteArray())
        .joinToString("") { "%02x".format(it) }
}

