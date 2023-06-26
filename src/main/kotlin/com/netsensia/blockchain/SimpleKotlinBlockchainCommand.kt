package com.netsensia.blockchain

import com.netsensia.blockchain.model.Network
import com.netsensia.blockchain.service.NetworkService
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import jakarta.inject.Inject

import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(name = "simple-kotlin-blockchain", description = ["..."],
        mixinStandardHelpOptions = true)
class SimpleKotlinBlockchainCommand : Runnable {

    @Inject
    lateinit var applicationContext: ApplicationContext

    @Inject
    lateinit var networkService: NetworkService

    @Option(names = ["-v", "--verbose"], description = ["..."])
    private var verbose : Boolean = false

    override fun run() {
        // business logic here
        if (verbose) {
            println("Hi!")
        }

        val network = networkService.createNetwork(100)
        network.randomlyConnect()
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            PicocliRunner.run(SimpleKotlinBlockchainCommand::class.java, *args)
        }
    }
}
