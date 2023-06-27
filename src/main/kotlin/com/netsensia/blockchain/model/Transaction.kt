package com.netsensia.blockchain.model

import java.util.*

data class Transaction(val id: UUID, val sender: String, val recipient: String, val amount: Double)
