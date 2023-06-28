package com.netsensia.blockchain.simulate

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
                // sleep for a random amount of time between 0 and 1000 milliseconds
                //Thread.sleep((0..5000).random().toLong())
            }
        }

        (0..50000).forEach {
            network.nodes.forEach {
                print("${it.blockchain.blocks.size} ")
            }
            println()
            val similarity = calculateSimilarityPercentage(network.nodes.map { it.blockchain }).toInt()
            println("Similarity: $similarity")
            Thread.sleep(1000)
        }
    }

    private fun calculateSimilarityPercentage(allChains: List<Blockchain>): Double {
        val minLength = allChains.minByOrNull { it.blocks.size }!!.blocks.size
        var totalScore = 0
        val maxScore = minLength * allChains.size * allChains.size

        for (position in 0 until minLength) {
            val blocksAtPosition = allChains.map { it.blocks[position] }
            println("Blocks at position $position: ${blocksAtPosition.size}")
            val scoreAtPosition = blocksAtPosition.sumOf { block ->
                blocksAtPosition.count { it.hash == block.hash }
            }
            println("Score at position $position: $scoreAtPosition")
            totalScore += scoreAtPosition
            println("Total score: $totalScore")
        }

        println("Total score: $totalScore, max score: $maxScore")
        return (totalScore.toDouble() / maxScore.toDouble()) * 100
    }

}