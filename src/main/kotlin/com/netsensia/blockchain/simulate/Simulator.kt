package com.netsensia.blockchain.simulate

import com.netsensia.blockchain.SimpleKotlinBlockchainCommand.Companion.output
import com.netsensia.blockchain.model.Blockchain
import com.netsensia.blockchain.service.NetworkService
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

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
        val network = networkService.createNetwork(6)
        println("Randomly connecting network")
        network.randomlyConnect()

        GlobalScope.launch {
            (0..2500).forEach {
                network.randomlySelectNode().generateTransaction()
                Thread.sleep((0..5000).random().toLong())
            }
        }

        (0..50000).forEach {
            networkDetails(network, 2)
            val smallestChain = network.nodes.minByOrNull { it.blockchain.blocks.size }!!.blockchain
            val longestChain = network.nodes.maxByOrNull { it.blockchain.blocks.size }!!.blockchain
            if (smallestChain.blocks.size < longestChain.blocks.size - 1) {
                networkDetails(network)
                exitProcess(0)
            }
            Thread.sleep(1000)
        }
    }

    private fun networkDetails(network: Network, logLevel: Int = 0) {
        network.nodes.forEach {
            output("${it.id}: ${it.blockchain.blocks.size} ", logLevel)
        }
        output("", logLevel)
        val similarity = calculateSimilarityPercentage(network.nodes.map { it.blockchain }, logLevel).toInt()
        output("Similarity: $similarity", logLevel)
    }

    private fun calculateSimilarityPercentage(allChains: List<Blockchain>, logLevel: Int = 0): Double {
        val minLength = allChains.minByOrNull { it.blocks.size }!!.blocks.size
        var totalScore = 0
        val maxScore = minLength * allChains.size * allChains.size

        for (position in 0 until minLength) {
            val blocksAtPosition = allChains.map { it.blocks[position] }
            output("Blocks at position $position: ${blocksAtPosition.size}", logLevel)
            val scoreAtPosition = blocksAtPosition.sumOf { block ->
                blocksAtPosition.count { it.hash == block.hash }
            }
            output("Score at position $position: $scoreAtPosition", logLevel)
            totalScore += scoreAtPosition
            output("Total score: $totalScore", logLevel)
        }

        output("Total score: $totalScore, max score: $maxScore", logLevel)
        return (totalScore.toDouble() / maxScore.toDouble()) * 100
    }

    companion object {
        const val LOG_LEVEL = 0
    }

}