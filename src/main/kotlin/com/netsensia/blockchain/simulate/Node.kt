package com.netsensia.blockchain.simulate

import com.netsensia.blockchain.model.Block
import com.netsensia.blockchain.model.Blockchain
import com.netsensia.blockchain.model.Transaction
import com.netsensia.blockchain.service.DefaultBlockService
import com.netsensia.blockchain.service.DefaultBlockchainService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

private const val MIN_TXS_PER_BLOCK = 2
private const val MAX_TXS_PER_BLOCK = 4
private const val LOAD_FROM_FILE = false
private const val MAX_FORKS = 50

class Node(val id: String) {

    val filename = "blockchain_${id}.txt"
    private var miningJob: Job? = null

    var mineLock = false

    val blockchainService = DefaultBlockchainService()
    val blockService = DefaultBlockService()

    lateinit var blockchain: Blockchain
    val forks = CopyOnWriteArrayList<Blockchain>()

    val transactions = CopyOnWriteArrayList<Transaction>()

    val transactionsReceived = CopyOnWriteArrayList<UUID>()
    val blocksReceived = mutableListOf<String>()

    private val peers = mutableListOf<Node>()

    fun loadChain() {
        blockchain = if (LOAD_FROM_FILE && File(filename).exists()) {
            blockchainService.load(filename)
        } else {
            blockchainService.genesis()
        }
    }

    // Connect this node to another
    fun connect(node: Node) {
        peers.add(node)
    }

    // Broadcast a block to all peers
    fun broadcastBlock(block: Block.Mined) {
        peers.forEach {
            it.receiveBlock(block)
        }
    }

    fun broadcastTransaction(transaction: Transaction) {
        peers.forEach {
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

        broadcastTransaction(transaction)
    }

    fun receiveTransaction(transaction: Transaction) {
        if (transactionsReceived.count { it == transaction.id } > 0) {
            return
        }
        transactionsReceived.add(transaction.id)
        broadcastTransaction(transaction)
        considerMining("new transaction was received")
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun startMining() {
        println("Node $id is starting to mine.")
        miningJob = GlobalScope.launch {
            mineBlock()
        }
    }

    suspend fun mineBlock() {
        // Add logic to mine a new block with the current transactions
        println("Node $id has ${transactions.size} transactions queued.")
        val validTransactions = getMaxAllowedValidTransactions()
        println("Node $id has ${validTransactions.size} valid transactions.")
        if (validTransactions.size >= MIN_TXS_PER_BLOCK) {
            println("Node $id has enough valid transactions to mine a new block with index ${blockchain.getLastBlock().index + 1}.")
            val block = blockService.mineBlock(blockchain.getLastBlock(), validTransactions)
            println("Node $id mined a new block ${block.hash}. Index is ${block.index}, previous hash is ${block.previousHash}")
            blocksReceived.add(block.hash)
            blockchain = blockchain.addMinedBlock(block)
            broadcastBlock(block)
            mineUnlock("mined a block")
            considerMining("mined a block")
        } else {
            mineUnlock("there are not enough valid transactions")
        }
    }

    private fun getMaxAllowedValidTransactions(): List<Transaction> {
        val transactionsToConsider = transactions.toList()
        val validTransactions = blockchain.validTransactionsOnly(transactionsToConsider)
        val invalidTransactions = transactionsToConsider.filter { it !in validTransactions }
        println("Removing ${invalidTransactions.size} invalid transactions from the queue.")
        transactions.removeIf {
            it.id in invalidTransactions.map { it.id }
        }
        return validTransactions.take(MAX_TXS_PER_BLOCK)
    }

    // Receive a block from a peer
    fun receiveBlock(block: Block.Mined) {
        if (blocksReceived.count { it == block.hash } > 0) {
            return
        }
        blocksReceived.add(block.hash)
        broadcastBlock(block)
        println("Node $id received a new block ${block.hash} from a peer. Block has index ${block.index}. Last chain index is ${blockchain.getLastBlock().index}. Chain size is ${blockchain.blocks.size}.")
        // Verify if block is valid and add it to the blockchain if so
        if (blockchainService.isValidNewBlock(block, blockchain.getLastBlock())) {
            if (miningJob?.isActive == true) {
                println("Node $id is cancelling mining job because a valid block being received from a peer.")
                miningJob?.cancel()
                mineUnlock("mining job was cancelled")
            }
            blockchain = blockchain.addMinedBlock(block)
            println("Node $id added a new block ${block.hash} to the chain!")
            considerMining("new block was added to chain")
            broadcastBlock(block)
        } else {
            println("Node $id received a block ${block.hash} that doesn't fit on the main chain.")

            println("Node $id has ${forks.size} forks.")

            // Does it fit on any fork?
            forks.forEachIndexed { index, fork ->
                if (blockchainService.isValidNewBlock(block, fork.getLastBlock())) {
                    forks[index] = fork.addMinedBlock(block)
                    println("Node $id added block ${block.hash} to an existing fork [$index]")
                    checkChainLengths(index)
                    return
                }
            }

            println("Node $id could not fit block onto an existing fork.")

            if (forks.size == MAX_FORKS) {
                println("Node $id has reached the maximum number of forks. Removing oldest fork.")
                forks.sortedBy { it.getLastBlock().timestamp }.firstOrNull()?.let {
                    forks.remove(it)
                }
            }

            // Does the block fit in the same position as the latest block on the main chain?
            // If so, start a fork
            if (block.previousHash == blockchain.getLastBlock().previousHash && block.hash != blockchain.getLastBlock().hash) {
                println("Node $id is starting a new fork.")
                forks.add(blockchain.replaceLastBlock(block))
                println("Node $id added block ${block.hash} to a new fork [${forks.size - 1}] with size ${forks.last().blocks.size}")
                return
            }

            println("Node $id is ignoring block ${block.hash} because it doesn't fit on the main chain or any existing forks.")
        }
    }

    private fun checkChainLengths(forkNumber: Int) {
        // If any fork is longer than the main chain, swap the main chain with the fork
        println("Node $id is checking if fork $forkNumber is longer than the main chain.")
        if (forks[forkNumber].blocks.size > blockchain.blocks.size) {
            println("Node $id has a fork [$forkNumber] that is longer than the main chain. Swapping to the fork.")
            val temp = forks[forkNumber]
            forks[forkNumber] = blockchain
            blockchain = temp
            return
        }
        println("Node $id found that fork [$forkNumber] is not longer than the main chain.")
    }

    private fun considerMining(reason: String) {
        println("Node $id is considering mining because $reason.")
        if (mineLock) {
            println("Node $id is already mining. Won't consider mining ($reason).")
            return
        }
        mineLock(reason)
        startMining()
    }

    private fun mineLock(reason: String) {
        println("Node $id is locking mining because $reason.")
        mineLock = true
    }

    private fun mineUnlock(reason: String) {
        println("Node $id is unlocking mining because $reason.")
        mineLock = false
    }
}