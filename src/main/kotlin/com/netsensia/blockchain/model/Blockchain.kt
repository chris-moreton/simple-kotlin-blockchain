import com.netsensia.blockchain.service.BlockService
import jakarta.inject.Inject

class Blockchain(val blocks: List<Block.Mined>) {

    val blockService = BlockService()

    fun addBlock(transactions: List<Transaction>, difficulty: Int): Blockchain {
        val minedBlock = blockService.mineBlock(blocks.last(), transactions, difficulty)
        return Blockchain(blocks + minedBlock)
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

