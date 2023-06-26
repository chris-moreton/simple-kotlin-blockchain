package com.netsensia.blockchain.service

import com.netsensia.blockchain.model.Network
import jakarta.inject.Singleton

interface NetworkService {
    fun createNetwork(numberOfNodes: Int): Network
}

@Singleton
class DefaultNetworkService : NetworkService {

    override fun createNetwork(numberOfNodes: Int): Network {
        return Network.createNetwork(numberOfNodes)
    }
}