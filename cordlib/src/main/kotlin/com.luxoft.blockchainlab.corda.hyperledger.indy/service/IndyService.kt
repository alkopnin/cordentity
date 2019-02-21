package com.luxoft.blockchainlab.corda.hyperledger.indy.service

import com.luxoft.blockchainlab.hyperledger.indy.IndyUser
import com.luxoft.blockchainlab.hyperledger.indy.WalletConfig
import com.luxoft.blockchainlab.hyperledger.indy.utils.PoolManager
import com.luxoft.blockchainlab.hyperledger.indy.utils.SerializationUtils
import com.luxoft.blockchainlab.hyperledger.indy.utils.getRootCause
import com.natpryce.konfig.*
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import org.hyperledger.indy.sdk.did.DidJSONParameters
import org.hyperledger.indy.sdk.wallet.Wallet
import org.hyperledger.indy.sdk.wallet.WalletExistsException
import org.slf4j.LoggerFactory
import java.io.File

/**
 * A Corda service for dealing with Indy Ledger infrastructure such as pools, credentials, wallets.
 *
 * The current implementation is a POC and lacks any configurability.
 * It is planed to be extended in the future version.
 */
@CordaService
class IndyService(serviceHub: AppServiceHub) : SingletonSerializeAsToken() {
    private val logger = LoggerFactory.getLogger(IndyService::class.java.name)

    private val credentials = """{"key": "key"}"""

    val indyUser: IndyUser

    init {
        val walletConfig = SerializationUtils.anyToJSON(WalletConfig(
                try {
                    IndyProperties.getWalletName()
                } catch(e: Exception) {
                    logger.info("Wallet name is not specified in config file. Use organisation name")
                    serviceHub.myInfo.legalIdentities.first().name.organisation
                })
        )

        try {
            Wallet.createWallet(walletConfig, credentials).get()
        } catch (ex: Exception) {
            logger.info("Wallet has not been created")

            if (getRootCause(ex) !is WalletExistsException) {
                logger.error(ex.message)
                throw ex
            }

            logger.info("Wallet is already exists")
        }

        val wallet = Wallet.openWallet(walletConfig, credentials).get()

        val genesisFile = File(IndyProperties.getGenesisPath())
        val pool = PoolManager.openIndyPool(genesisFile)

        val isTrustee = try {
            (IndyProperties.getRole().compareTo("trustee", true) == 0)
        } catch (ex: Exception) {
            logger.info("Role has not been specified")
            false
        }

        indyUser = if(isTrustee) {
            val didConfig = DidJSONParameters.CreateAndStoreMyDidJSONParameter(
                    IndyProperties.getDid(),
                    IndyProperties.getSeed(),
                    null,
                    null
            ).toJson()
            IndyUser(pool, wallet, IndyProperties.getDid(), didConfig)
        } else {
            IndyUser(pool, wallet, null)
        }
    }
}