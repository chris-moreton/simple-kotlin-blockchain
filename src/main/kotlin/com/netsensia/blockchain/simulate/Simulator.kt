package com.netsensia.blockchain.simulate

import com.netsensia.blockchain.service.NetworkService
import jakarta.inject.Inject
import jakarta.inject.Singleton

interface Simulator {
    fun run()
}

@Singleton
class DefaultSimulator : Simulator {

    @Inject
    lateinit var networkService: NetworkService

    override fun run() {
        println("Creating network")
        val network = networkService.createNetwork(5)
        println("Randomly connecting network")
        network.randomlyConnect()
        (0..10000).forEach {
            network.randomlySelectNode().generateTransaction()
            network.nodes.forEach {
                println("Node ${it.id} chain length is ${network.randomlySelectNode().blockchain.blocks.size}")
            }
        }

        println("All transactions generated. Waiting to see what happens.")
        (0..10000).forEach {
            network.nodes.forEach {
                println("Node ${it.id} chain length is ${network.randomlySelectNode().blockchain.blocks.size}")
            }
            Thread.sleep(500)
        }
    }
}