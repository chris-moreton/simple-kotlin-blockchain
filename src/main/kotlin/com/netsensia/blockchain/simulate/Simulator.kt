package com.netsensia.blockchain.simulate

import com.netsensia.blockchain.SimpleKotlinBlockchainCommand.Companion.output
import com.netsensia.blockchain.model.Blockchain
import com.netsensia.blockchain.service.NetworkService
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.StringBuilder
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
        val network = networkService.createNetwork(NUM_NODES, DIFFICULTY)
        println("Randomly connecting network")
        network.randomlyConnect()

        network.nodes.forEach {
            GlobalScope.launch {
                it.run()
            }
        }

        (0..50000).forEach {
            networkDetails(network, 2)
            val smallestChain = network.nodes.minByOrNull { it.blockchain.blocks.size }!!.blockchain
            val longestChain = network.nodes.maxByOrNull { it.blockchain.blocks.size }!!.blockchain
            if (smallestChain.blocks.size < longestChain.blocks.size - 20) {
                networkDetails(network, 2)
                exitProcess(0)
            }
            Thread.sleep(10000)
        }
    }

    private fun networkDetails(network: Network, logLevel: Int = 0) {
        network.nodes.forEach {
            val s = StringBuilder()
            s.append("${it.id}: ${it.blockchain.blocks.size} ")
            it.blockchain.blocks.forEach { block ->
                // add last four characters of hash
                s.append("${block.hash.substring(block.hash.length - 4)} ")
            }
            output(s.toString(), logLevel)
        }

        output("", logLevel)
        val similarity = calculateSimilarityPercentage(network.nodes.map { it.blockchain }, logLevel).toInt()
        output("Similarity: $similarity", logLevel)
    }

    private fun calculateSimilarityPercentage(allChains: List<Blockchain>, logLevel: Int = 0): Double {
        val minLength = allChains.minByOrNull { it.blocks.size }!!.blocks.size
        var totalScore = 0
        var sameUntilBlock = 0
        val maxScorePerPosition = allChains.size * allChains.size
        val maxScore = minLength * maxScorePerPosition

        for (position in 0 until minLength) {
            val blocksAtPosition = allChains.map { it.blocks[position] }
            val scoreAtPosition = blocksAtPosition.sumOf { block ->
                blocksAtPosition.count { it.hash == block.hash }
            }
            output("Score at position $position: $scoreAtPosition", 8)
            if (scoreAtPosition == maxScorePerPosition) {
                sameUntilBlock = position + 1
            }
            totalScore += scoreAtPosition
            output("Total score: $totalScore", 8)
        }

        output("Total score: $totalScore, max score: $maxScore", logLevel)
        output("Same until block: $sameUntilBlock / $minLength", logLevel)
        return (totalScore.toDouble() / maxScore.toDouble()) * 100
    }

    companion object {
        const val NUM_NODES = 12
        const val LOG_LEVEL = 2
        const val DIFFICULTY = 5
    }

}