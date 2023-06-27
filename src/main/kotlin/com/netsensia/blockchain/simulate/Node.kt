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
private const val LOAD_FROM_FILE = false

class Node(val id: String) {

    val filename = "blockchain_${id}.txt"
    private var miningJob: Job? = null

    val blockchainService = DefaultBlockchainService()
    val blockService = DefaultBlockService()

    lateinit var blockchain: Blockchain

    val transactions = CopyOnWriteArrayList<Transaction>()

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

        println("Node $id generated a new transaction from $sender to $recipient for ${transaction.amount}")

        broadcastTransaction(transaction)
    }

    fun receiveTransaction(transaction: Transaction) {
        if (transactions.count { it.id == transaction.id } == 0) {
            transactions.add(transaction.copy())
        }

        // If this node has enough transactions and is not currently mining, start mining
        if (miningJob?.isActive != true && transactions.size >= MIN_TXS_PER_BLOCK) {
            startMining()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun startMining() {
        miningJob = GlobalScope.launch {
            mineBlock()
        }
    }

    suspend fun mineBlock() {
        // Add logic to mine a new block with the current transactions
        val transactionsToConsider = transactions.toList()
        val validTransactions = blockchain.validTransactionsOnly(transactionsToConsider)
        if (validTransactions.size >= MIN_TXS_PER_BLOCK) {
            val block = blockService.mineBlock(blockchain.getLastBlock(), validTransactions)
            println("Node $id mined a new block!")
            transactions.removeIf {
                it.id in transactionsToConsider.map { it.id }
            }
            broadcastBlock(block)
        }
    }

    // Receive a block from a peer
    fun receiveBlock(block: Block.Mined) {
        println("Node $id received a new block from a peer! Block has index ${block.index} - last chain index is ${blockchain.getLastBlock().index}")
        // Verify if block is valid and add it to the blockchain if so
        if (blockchainService.isValidNewBlock(block, blockchain.getLastBlock())) {
            miningJob?.cancel()
            blockchain = blockchain.addMinedBlock(block)
            println("Node $id added a new block to the chain!")
            broadcastBlock(block)
            if (transactions.size >= MIN_TXS_PER_BLOCK) {
                startMining()
            }
        } else {
            // The received block is not valid, re-sync might be required
            println("Node $id received an invalid block, re-syncing might be needed.")
        }
        if (LOAD_FROM_FILE) {
            blockchainService.save(filename, blockchain)
        }
    }
}