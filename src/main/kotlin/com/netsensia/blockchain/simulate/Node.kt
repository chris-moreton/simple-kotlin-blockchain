package com.netsensia.blockchain.simulate

import com.netsensia.blockchain.SimpleKotlinBlockchainCommand.Companion.output
import com.netsensia.blockchain.model.Block
import com.netsensia.blockchain.model.Blockchain
import com.netsensia.blockchain.model.Transaction
import com.netsensia.blockchain.service.DefaultBlockchainService
import com.netsensia.blockchain.simulate.DefaultSimulator.Companion.DIFFICULTY
import java.io.File
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

private const val MIN_TXS_PER_BLOCK = 2
private const val MAX_TXS_PER_BLOCK = 4
private const val LOAD_FROM_FILE = false
private const val MAX_FORKS = 50

class Node(val id: String) {

    private var mineLock = false

    private val blockchainService = DefaultBlockchainService()

    lateinit var blockchain: Blockchain
    private val forks = CopyOnWriteArrayList<Blockchain>()

    private val transactions = CopyOnWriteArrayList<Transaction>()

    private val transactionsReceived = CopyOnWriteArrayList<UUID>()
    private val blocksReceived = mutableListOf<String>()

    private val peers = mutableListOf<Node>()

    fun loadChain() {
        blockchain = blockchainService.genesis()
    }

    fun run() {
        mineLock("Node $id has loaded blockchain with ${blockchain.blocks.size} blocks")
        generateTransaction()
        var lastTransactionGeneratedAt = System.currentTimeMillis()
        var lastMiningAttemptAt = System.currentTimeMillis()
        while (true) {
            if (System.currentTimeMillis() - lastTransactionGeneratedAt > 1000) {
                generateTransaction()
                lastTransactionGeneratedAt = System.currentTimeMillis()
            }

            if (System.currentTimeMillis() - lastMiningAttemptAt > 1000) {
                lastMiningAttemptAt = System.currentTimeMillis()
                tryMining()
            }
        }
    }

    fun connect(node: Node) {
        peers.add(node)
    }

    // Broadcast a block to all peers
    fun broadcastBlock(block: Block.Mined) {
        peers.forEach {
            output("Node $id is broadcasting block ${block.hash} to ${it.id}", 3)
            it.receiveBlock(block)
        }
    }

    private fun broadcastTransaction(transaction: Transaction) {
        peers.forEach {
            output("Node $id is broadcasting transaction ${transaction.id} to ${it.id}", 7)
            it.receiveTransaction(transaction)
        }
    }

    fun generateTransaction() {

        val name: List<String> = listOf("Alice", "Bob", "Chrismo")

        val sender = name.random()
        val recipient = name.random()

        val transaction = Transaction(UUID.randomUUID(), sender, recipient, (1..10000).random() / 500.0)

        transactions.add(transaction)
        transactionsReceived.add(transaction.id)

        output("Node $id has generated transaction ${transaction.id} from $sender to $recipient for ${transaction.amount}", 3)

        broadcastTransaction(transaction)
    }

    fun tryMining() {
        mineLock("Node $id is starting to mine (if there are enough valid transactions)")
        output("Node $id has ${transactions.size} transactions queued.", 3)
        val validTransactions = getMaxAllowedValidTransactions()
        output("Node $id has ${validTransactions.size} valid transactions.", 3)
        if (validTransactions.size >= MIN_TXS_PER_BLOCK) {
            output("Node $id has enough valid transactions to mine a new block with index ${blockchain.getLastBlock().index + 1}.", 3)

            val pair = mineBlock(validTransactions)
            val unminedBlock = pair.first
            val nonce = pair.second

            if (mineLock) {
                val minedBlock = unminedBlock.toMined(DIFFICULTY, nonce)
                output("Node $id mined a new block ${minedBlock.hash}. Index is ${minedBlock.index}, previous hash is ${minedBlock.previousHash}")
                output("Block ${minedBlock.hash} contains ${minedBlock.transactions}", 3)
                if (blockchainService.isValidNewBlock(minedBlock, blockchain.getLastBlock())) {
                    blockchain = blockchain.addMinedBlock(minedBlock)
                    output("Newly-mined block is valid, node $id added a new block ${minedBlock.hash} to chain tip ${minedBlock.previousHash}!")
                    blocksReceived.add(minedBlock.hash)
                    broadcastBlock(minedBlock)
                } else {
                    output("Newly-mined block is invalid. This really shouldn't happen.")
                }
            } else {
                output("Node $id stopped mining because the mineLock was lifted.")
            }
        }

        mineUnlock("there are not enough valid transactions")
    }

    private fun mineBlock(validTransactions: List<Transaction>): Pair<Block.Unmined, Int> {
        val unminedBlock = Block.Unmined(
            index = blockchain.getLastBlock().index + 1,
            timestamp = System.currentTimeMillis(),
            transactions = validTransactions.toList(),
            previousHash = blockchain.getLastBlock().hash
        )

        val target = "0".repeat(DIFFICULTY)
        var nonce = Random.nextInt()
        var hash = Block.calculateHash(unminedBlock, nonce)
        while (mineLock && hash.substring(0, DIFFICULTY) != target) {
            nonce = Random.nextInt()
            if (nonce % (Math.pow(
                    10.0,
                    DIFFICULTY.toDouble()
                )).toInt() == 0
            ) output("Currently being mined by $id to add to block ${blockchain.getLastBlock().hash}", 4)
            hash = Block.calculateHash(unminedBlock, nonce)
        }
        return Pair(unminedBlock, nonce)
    }

    private fun getMaxAllowedValidTransactions(): List<Transaction> {
        val transactionsToConsider = transactions.toList()
        val validTransactions = blockchain.validTransactionsOnly(transactionsToConsider)
        val invalidTransactions = transactionsToConsider.filter { it !in validTransactions }

        invalidTransactions.forEach {
            output("Node $id found invalid transaction ${it.id} from ${it.sender} to ${it.recipient} for ${it.amount.toInt()}", 5)
        }

        output("Removing ${invalidTransactions.size} invalid transactions from the queue.", 4)
        transactions.removeIf {
            it.id in invalidTransactions.map { it.id }
        }
        return validTransactions.take(MAX_TXS_PER_BLOCK)
    }

    private fun checkChainLengths(forkNumber: Int) {
        // If any fork is longer than the main chain, swap the main chain with the fork
        output("Node $id is checking if fork $forkNumber is longer than the main chain.")
        if (forks[forkNumber].blocks.size > blockchain.blocks.size) {
            output("Node $id has a fork [$forkNumber] that is longer than the main chain. Swapping to the fork.")
            val temp = forks[forkNumber]
            forks[forkNumber] = blockchain
            blockchain = temp
            return
        }
        output("Node $id found that fork [$forkNumber] is not longer than the main chain.")
    }

    fun receiveTransaction(transaction: Transaction) {
        output("Node $id received a new transaction ${transaction.id} from a peer.", 7)
        if (transactionsReceived.count { it == transaction.id } > 0) {
            output("Node $id is ignoring transaction ${transaction.id} because it has already been received from a peer.", 7)
            return
        }
        transactionsReceived.add(transaction.id)
        broadcastTransaction(transaction)
    }

    fun receiveBlock(block: Block.Mined) {
        if (blocksReceived.count { it == block.hash } > 0) {
            return
        }
        blocksReceived.add(block.hash)
        broadcastBlock(block)
        output("Node $id received a new block ${block.hash} from a peer. Block has index ${block.index}. Last chain index is ${blockchain.getLastBlock().index}. Chain size is ${blockchain.blocks.size}.", 1)
        // Verify if block is valid and add it to the blockchain if so
        if (blockchainService.isValidNewBlock(block, blockchain.getLastBlock())) {
            blockchain = blockchain.addMinedBlock(block)
            if (mineLock) {
                output("Node $id is cancelling mining job because a valid block was received from a peer.")
                mineUnlock("a valid block was received from a peer")
            }
            output("Node $id added a new block ${block.hash} to the chain!", 0)
        } else {
            output("Node $id received a block ${block.hash} that doesn't fit on the main chain. Block previous hash is ${block.previousHash}. Chain tip is ${blockchain.getLastBlock().hash}.", 1)

            output("Node $id has ${forks.size} forks.", 1)

            // Does it fit on any fork?
            forks.forEachIndexed { index, fork ->
                if (blockchainService.isValidNewBlock(block, fork.getLastBlock())) {
                    forks[index] = fork.addMinedBlock(block)
                    output("Node $id added block ${block.hash} to an existing fork [$index]")
                    checkChainLengths(index)
                    return
                }
            }

            output("Node $id could not fit block onto an existing fork.")

            if (forks.size == MAX_FORKS) {
                output("Node $id has reached the maximum number of forks. Removing oldest fork.")
                forks.sortedBy { it.getLastBlock().timestamp }.firstOrNull()?.let {
                    forks.remove(it)
                }
            }

            // Does the block fit in the same position as the latest block on the main chain?
            // If so, start a fork
            if (block.previousHash == blockchain.getLastBlock().previousHash && block.hash != blockchain.getLastBlock().hash) {
                output("Node $id is starting a new fork.")
                forks.add(blockchain.replaceLastBlock(block))
                output("Node $id added block ${block.hash} to a new fork [${forks.size - 1}] with size ${forks.last().blocks.size}")
                return
            }

            output("Node $id is ignoring block ${block.hash} because it doesn't fit on the main chain or any existing forks.")
        }
    }

    private fun mineLock(reason: String) {
        output("Node $id is locking mining because $reason.", 3)
        mineLock = true
    }

    private fun mineUnlock(reason: String) {
        output("Node $id is unlocking mining because $reason.", 3)
        mineLock = false
    }

}