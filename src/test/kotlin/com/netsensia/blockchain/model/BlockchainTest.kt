import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import assertk.assertions.isFalse
import com.netsensia.blockchain.service.BlockService
import com.netsensia.blockchain.service.BlockchainService
import org.junit.jupiter.api.Test

class BlockchainTest {

    val blockchainService = BlockchainService()
    val blockService = BlockService()

    @Test
    fun `blockchain should start with genesis block`() {
        val blockchain = blockchainService.genesis()
        val blocks = blockchain.blocks
        assertThat(blocks.size).isEqualTo(1)
        assertThat(blocks[0].index).isEqualTo(0)
    }

    @Test
    fun `adding a block should increase the size of blockchain`() {
        val blockchain = blockchainService.genesis()
        val newChain = blockchain.addBlock(listOf(Transaction("Alice", "Bob", 50.0)), 4)
        assertThat(blockchain.blocks.size).isEqualTo(1)
        assertThat(newChain.blocks.size).isEqualTo(2)
    }

    @Test
    fun `newly added block should contain given transactions`() {
        val blockchain = blockchainService.genesis()
        val transactions = listOf(Transaction("Alice", "Bob", 50.0))
        val newChain = blockchain.addBlock(transactions, 4)
        assertThat(newChain.getLastBlock().transactions).isEqualTo(transactions)
    }

    @Test
    fun `blockchain should be valid after adding blocks`() {
        val blockchain = blockchainService.genesis()
        blockchain.addBlock(listOf(Transaction("Alice", "Bob", 50.0)), 4)
        assertThat(blockchain.validate()).isTrue()
    }

    @Test
    fun `blockchain should be invalid if a block is tampered`() {
        val blockchain = blockchainService.genesis()

        val validChain = blockchain
            .addBlock(listOf(Transaction("Alice", "Bob", 50.0)), 4)
            .addBlock(listOf(Transaction("Alice", "Barry", 10.0)), 4)
            .addBlock(listOf(Transaction("Anna", "Alice", 10.2)), 4)

        assertThat(validChain.validate()).isTrue()

        // Get a copy of the current blockchain state
        val validBlocks = validChain.blocks

        val tamperedChain = Blockchain(
            validBlocks.toMutableList().apply {
                this[1] = Block.Unmined(
                    1,
                    validBlocks[1].timestamp,
                    listOf(Transaction("Alice", "Barry", 11.0)),
                    validBlocks[1].previousHash
                ).mine(4)
            }
        )

        assertThat(tamperedChain.validate()).isFalse()
    }

    @Test
    fun `should save blockchain to a file`() {
        val blockchain = blockchainService.genesis()
            .addBlock(listOf(Transaction("Alice", "Bob", 50.0)), 4)
            .addBlock(listOf(Transaction("Alice", "Barry", 10.0)), 4)
            .addBlock(listOf(Transaction("Anna", "Alice", 10.2)), 4)

        assertThat(blockchain.blocks.size).isEqualTo(4)

        blockchainService.save("blockchain.txt", blockchain)

        val newChain = blockchainService.genesis()
            .addBlock(listOf(Transaction("Steve", "Bob", 5.0)), 4)
            .addBlock(listOf(Transaction("Alice", "Barry", 16.0)), 3)
            .addBlock(listOf(Transaction("Anna", "Jemma", 11.1)), 5)

        assertThat(newChain.blocks.size).isEqualTo(4)

        blockchainService.save("blockchain.txt", newChain)

        val loadedChain = blockchainService.load("blockchain.txt")
        assertThat(loadedChain.blocks.size).isEqualTo(4)
        assertThat(loadedChain.validate()).isTrue()
        assertThat(loadedChain.equals(newChain)).isTrue()
    }
}
