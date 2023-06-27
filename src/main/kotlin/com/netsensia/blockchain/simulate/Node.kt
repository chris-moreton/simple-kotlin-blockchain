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
import kotlin.math.min

private const val MIN_TXS_PER_BLOCK = 2
private const val LOAD_FROM_FILE = false
private const val MAX_FORKS = 50

class Node(val id: String) {

    val filename = "blockchain_${id}.txt"
    private var miningJob: Job? = null

    var mineLock = false

    val blockchainService = DefaultBlockchainService()
    val blockService = DefaultBlockService()

    lateinit var blockchain: Blockchain
    val forks = mutableListOf<Blockchain>()

    val transactions = CopyOnWriteArrayList<Transaction>()

    val transactionsReceived = mutableListOf<UUID>()
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

        val name: List<String> = listOf("Jim", "Fred", "Alice", "Bob", "Chrismo", "Dave")

        val sender = name.random()
        val recipient = name.random()

        val transaction = Transaction(UUID.randomUUID(), sender, recipient, (1..10000).random() / 100.0)

        transactions.add(transaction)
        transactionsReceived.add(transaction.id)

        broadcastTransaction(transaction)
    }

    fun receiveTransaction(transaction: Transaction) {
        if (transactionsReceived.count { it == transaction.id } > 0) {
            return
        }
        transactionsReceived.add(transaction.id)
        //println("Node $id received a new transaction from ${transaction.sender} to ${transaction.recipient} for ${transaction.amount}")
        if (transactions.count { it.id == transaction.id } == 0) {
            transactions.add(transaction.copy())
            broadcastTransaction(transaction)

            // If this node has enough transactions and is not currently mining, start mining
            considerMining()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun startMining() {
        if (!mineLock) {
            println("Node $id may already be mining. Won't start new job.")
            return
        }
        println("Node $id is starting to mine.")
        miningJob = GlobalScope.launch {
            mineBlock()
        }
    }

    suspend fun mineBlock() {
        // Add logic to mine a new block with the current transactions
        val transactionsToConsider = transactions.toList()
        val validTransactions = blockchain.validTransactionsOnly(transactionsToConsider)
        if (validTransactions.size >= MIN_TXS_PER_BLOCK) {
            println("Node $id has enough valid transactions to mine a new block with index ${blockchain.getLastBlock().index + 1}.")
            val block = blockService.mineBlock(blockchain.getLastBlock(), validTransactions)
            println("Node $id mined a new block ${block.hash}. Index is ${block.index}, previous hash is ${block.previousHash}")
            transactions.removeIf {
                it.id in transactionsToConsider.map { it.id }
            }
            blocksReceived.add(block.hash)
            blockchain = blockchain.addMinedBlock(block)
            broadcastBlock(block)
        } else {
            println("Node $id doesn't have enough transactions to mine a new block. Will wait for more.")
        }
        mineUnlock()
    }

    // Receive a block from a peer
    fun receiveBlock(block: Block.Mined) {
        if (blocksReceived.count { it == block.hash } > 0) {
            //println("Ignoring previously-received block ${block.hash}")
            return
        }
        blocksReceived.add(block.hash)
        println("Node $id received a new block ${block.hash} from a peer. Block has index ${block.index} and last chain index is ${blockchain.getLastBlock().index}")
        // Verify if block is valid and add it to the blockchain if so
        if (blockchainService.isValidNewBlock(block, blockchain.getLastBlock())) {
            if (miningJob?.isActive == true) {
                println("Node $id is cancelling mining job.")
                miningJob?.cancel()
                mineUnlock()
            }
            blockchain = blockchain.addMinedBlock(block)
            println("Node $id added a new block ${block.hash} to the chain!")
            considerMining()
            broadcastBlock(block)
        } else {
            println("Node $id received a block ${block.hash} that doesn't fit on the main chain.")

            // Does it fit on any fork?
            forks.forEachIndexed { index, fork ->
                if (blockchainService.isValidNewBlock(block, fork.getLastBlock())) {
                    forks[index] = fork.addMinedBlock(block)
                    println("Node $id added block ${block.hash} to an existing fork [$index]")
                    broadcastBlock(block)
                    return
                }
            }

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
                println("Node $id added block ${block.hash} to a new fork [${forks.size - 1}]")
                broadcastBlock(block)
                return
            }

            forks.add(blockchain.addMinedBlock(block))
            println("Node $id added block ${block.hash} to a new fork [${forks.size - 1}]")

            // If any fork is longer than the main chain, swap the main chain with the fork
            forks.forEachIndexed { index, fork ->
                if (fork.blocks.size > blockchain.blocks.size) {
                    println("Node $id has a fork that is longer than the main chain. Swapping to the fork.")
                    val temp = fork
                    forks[index] = blockchain
                    blockchain = temp
                    return
                }
            }
        }
        if (LOAD_FROM_FILE) {
            blockchainService.save(filename, blockchain)
        }
    }

    private fun considerMining() {
        mineLock()
        if (transactions.size >= MIN_TXS_PER_BLOCK) {
            println("Node $id has enough transactions to mine a new block.")
            startMining()
        }
    }

    private fun mineLock() {
        println("Node $id is locking mining.")
        mineLock = true
    }

    private fun mineUnlock() {
        println("Node $id is unlocking mining.")
        mineLock = false
    }
}