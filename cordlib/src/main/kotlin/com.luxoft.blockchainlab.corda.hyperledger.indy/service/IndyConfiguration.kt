package com.luxoft.blockchainlab.corda.hyperledger.indy.service

import com.natpryce.konfig.PropertyGroup
import com.natpryce.konfig.getValue
import com.natpryce.konfig.stringType

/**
 *  "indyuser" shows the family of properties.
 *  In indy.properties every new records starts with indyuser
 */
@Suppress("ClassName")
object indyuser : PropertyGroup() {
    val did by stringType
    val seed by stringType
    val role by stringType
    val walletName by stringType
    val genesisFile by stringType
    val agentWSEndpoint by stringType
    val agentPassword by stringType
    val agentUser by stringType
}

interface IndyConfiguration {

    fun getWalletName(): String

    fun getGenesisPath(): String

    fun getDid(): String

    fun getSeed(): String

    fun getRole(): String

    fun getAgentWSEndpoint(): String

    fun getAgentPassword(): String

    fun getAgentUser(): String
}