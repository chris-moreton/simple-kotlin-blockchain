package com.netsensia.blockchain

import com.netsensia.blockchain.service.NetworkService
import com.netsensia.blockchain.simulate.DefaultSimulator
import com.netsensia.blockchain.simulate.Simulator
import io.micronaut.configuration.picocli.PicocliRunner
import jakarta.inject.Inject

import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(name = "simple-kotlin-blockchain", description = ["..."],
        mixinStandardHelpOptions = true)
class SimpleKotlinBlockchainCommand : Runnable {

    @Inject
    lateinit var simulator: Simulator

    @Option(names = ["-v", "--verbose"], description = ["..."])
    private var verbose : Boolean = false

    override fun run() {

        if (verbose) {
            println("Hi!")
        }

        simulator.run()
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            PicocliRunner.run(SimpleKotlinBlockchainCommand::class.java, *args)
        }

        fun output(s: String, logLevel: Int = 0) {
            if (logLevel <= DefaultSimulator.LOG_LEVEL) {
                println(s)
            }
        }
    }
}
