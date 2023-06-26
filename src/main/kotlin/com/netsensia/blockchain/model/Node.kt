package com.netsensia.blockchain.model

import com.netsensia.blockchain.service.DefaultBlockchainService
import java.io.File

class Node(private val id: String) {

    val filename = "blockchain_${id}.txt"

    val blockchainService = DefaultBlockchainService()

    lateinit var blockchain: Blockchain

    private val peers = mutableListOf<Node>()

    fun loadChain() {
        blockchain = if (File(filename).exists()) {
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
    fun broadcast(block: Block.Mined) {
        peers.forEach { it.receive(block) }
    }

    // Receive a block from a peer
    fun receive(block: Block.Mined) {
        // Verify if block is valid and add it to the blockchain if so
        if (blockchainService.isValidNewBlock(block, blockchain.getLastBlock())) {
            blockchain = blockchain.addBlock(block.transactions, block.difficulty)
            println("Node $id added a new block to the chain!")
            broadcast(block)
        } else {
            // The received block is not valid, re-sync might be required
            println("Node $id received an invalid block, re-syncing might be needed.")
        }
    }
}