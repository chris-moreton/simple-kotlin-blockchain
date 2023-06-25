import java.security.MessageDigest

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
        fun mine(difficulty: Int): Block.Mined {
            val target = "0".repeat(difficulty)
            var nonce = 0
            var hash = calculateHash(this, nonce)
            while (hash.substring(0, difficulty) != target) {
                nonce++
                hash = calculateHash(this, nonce)
            }
            return Mined(index, timestamp, transactions, previousHash, nonce, hash)
        }
    }

    data class Mined(
        override val index: Int,
        override val timestamp: Long,
        override val transactions: List<Transaction>,
        override val previousHash: String,
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

