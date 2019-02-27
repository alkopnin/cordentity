package com.luxoft.blockchainlab.corda.hyperledger.indy.service

import com.natpryce.konfig.*
import java.io.File

object IndyProperties : IndyConfiguration {

    val CFG_PATH = "indyconfig"
    val CFG_NAME = "indy.properties"

    private val config = EmptyConfiguration
                // file with common name if you go for file-based config
                .ifNot(ConfigurationProperties.fromFileOrNull(File(CFG_PATH, CFG_NAME)), indyuser)
                // Good for docker-compose, ansible-playbook or similar
                .ifNot(EnvironmentVariables(), indyuser)

    override fun getWalletName() = config[indyuser.walletName]
    override fun getDid()  = config[indyuser.did]
    override fun getSeed() = config[indyuser.seed]
    override fun getRole() = config[indyuser.role]
    override fun getGenesisPath() = config[indyuser.genesisFile]
    override fun getAgentWSEndpoint() = config[indyuser.agentWSEndpoint]
    override fun getAgentPassword() = config[indyuser.agentPassword]
    override fun getAgentUser() = config[indyuser.agentUser]
}