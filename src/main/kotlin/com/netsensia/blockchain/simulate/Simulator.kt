package com.netsensia.blockchain.simulate

import com.netsensia.blockchain.service.NetworkService
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface Simulator {
    fun run()
}

@Singleton
class DefaultSimulator : Simulator {

    @Inject
    lateinit var networkService: NetworkService

    @OptIn(DelicateCoroutinesApi::class)
    override fun run() {
        println("Creating network")
        val network = networkService.createNetwork(3)
        println("Randomly connecting network")
        network.randomlyConnect()

        GlobalScope.launch {
            (0..25).forEach {
                network.randomlySelectNode().generateTransaction()
                // sleep for a random amount of time between 0 and 1000 milliseconds
                Thread.sleep((0..1000).random().toLong())
            }
        }

        (0..50).forEach {
            network.nodes.forEach {
                print("${it.blockchain.blocks.size} ")
            }
            println()
            Thread.sleep(5000)
        }
    }
}