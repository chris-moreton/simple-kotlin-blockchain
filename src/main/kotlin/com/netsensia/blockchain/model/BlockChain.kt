class Blockchain(val difficulty: Int = 4) {
    val blocks = mutableListOf<MinedBlock>()

    init {
        blocks.add(createGenesisBlock())
    }

    private fun createGenesisBlock(): MinedBlock {
        return mine(UnminedBlock(0, System.currentTimeMillis(), emptyList(), "0"), difficulty)
    }

    fun addBlock(transactions: List<Transaction>) {
        val lastBlock = blocks.last()
        val newBlock = UnminedBlock(
            lastBlock.index + 1,
            System.currentTimeMillis(),
            transactions,
            lastBlock.hash,
        )
        blocks.add(mine(newBlock, difficulty))
    }

    fun isValid(): Boolean {
        for (i in 1 until blocks.size) {
            val currentBlock = blocks[i]
            val previousBlock = blocks[i - 1]

            if (currentBlock.hash != calculateHash(currentBlock.toUnminedBlock(), difficulty)) {
                return false
            }

            if (currentBlock.previousHash != previousBlock.hash) {
                return false
            }
        }
        return true
    }
}