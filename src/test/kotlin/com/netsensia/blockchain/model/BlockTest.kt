import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class BlockTest {

    @Test
    fun `mining should produce a hash with difficulty number of leading zeroes`() {
        val difficulty = 4
        val unminedBlock = UnminedBlock(0, System.currentTimeMillis(), listOf(Transaction("Alice", "Bob", 50.0)), "0")
        val minedBlock = mine(unminedBlock, difficulty)
        assertEquals("0".repeat(difficulty), minedBlock.hash.substring(0, difficulty))
    }

    @Test
    fun `changing transaction should change hash`() {
        val difficulty = 4
        val unminedBlock1 = UnminedBlock(0, System.currentTimeMillis(), listOf(Transaction("Alice", "Bob", 50.0)), "0")
        val unminedBlock2 = UnminedBlock(0, System.currentTimeMillis(), listOf(Transaction("Anna", "Bob", 50.0)), "0")

        val minedBlock1 = mine(unminedBlock1, difficulty)
        val minedBlock2 = mine(unminedBlock2, difficulty)

        assertNotEquals(minedBlock1.hash, minedBlock2.hash)
    }
}