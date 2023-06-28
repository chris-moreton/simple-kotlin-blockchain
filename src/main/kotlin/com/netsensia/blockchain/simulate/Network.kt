package com.netsensia.blockchain.simulate

import kotlin.random.Random

class Network {

    val nodes = mutableListOf<Node>()
    var difficulty = 4

    fun createNode(id: String): Node {
        val node = Node(id)
        node.loadChain()
        nodes.add(node)
        return node
    }

    // This function connects each node to a random number of other nodes (between 1 and 10)
    fun randomlyConnect() {
        for (node in nodes) {
            val numberOfPeers = Random.nextInt(5, 11)
            val peers = nodes.filter { it != node }.shuffled().take(numberOfPeers)
            peers.forEach { node.connect(it) }
        }
    }

    companion object {

        fun createNetwork(numberOfNodes: Int, difficulty: Int): Network {
            val network = Network()
            network.difficulty = difficulty
            for (i in 1..numberOfNodes) {
                network.createNode("Node$i")
            }
            network.randomlyConnect()
            return network
        }
    }
}