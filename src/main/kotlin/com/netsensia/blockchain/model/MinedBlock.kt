import java.security.MessageDigest


data class UnminedBlock(
    val index: Int,
    val timestamp: Long,
    val transactions: List<Transaction>,
    val previousHash: String,
)

data class MinedBlock(
    val index: Int,
    val timestamp: Long,
    val transactions: List<Transaction>,
    val previousHash: String,
    val nonce: Int,
    val hash: String
) {
    fun toUnminedBlock(): UnminedBlock {
        return UnminedBlock(index, timestamp, transactions, previousHash)
    }
}

fun String.hash(): String {
    return MessageDigest.getInstance("SHA-256")
        .digest(this.toByteArray())
        .joinToString("") { "%02x".format(it) }
}

fun mine(unminedBlock: UnminedBlock, difficulty: Int): MinedBlock {
    val target = "0".repeat(difficulty)
    var nonce = 0
    var hash = calculateHash(unminedBlock, nonce)
    while (hash.substring(0, difficulty) != target) {
        nonce++
        hash = calculateHash(unminedBlock, nonce)
    }
    return MinedBlock(unminedBlock.index, unminedBlock.timestamp, unminedBlock.transactions, unminedBlock.previousHash, nonce, hash)
}

fun calculateHash(unminedBlock: UnminedBlock, nonce: Int): String {
    return "${unminedBlock.index}${unminedBlock.timestamp}${unminedBlock.transactions.hashCode()}${unminedBlock.previousHash}${nonce}".hash()
}