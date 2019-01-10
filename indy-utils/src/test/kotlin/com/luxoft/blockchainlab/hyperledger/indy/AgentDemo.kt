package com.luxoft.blockchainlab.hyperledger.indy

import com.evernym.sdk.vcx.LibVcx
import com.evernym.sdk.vcx.VcxJava
import com.evernym.sdk.vcx.schema.SchemaApi
import com.evernym.sdk.vcx.utils.UtilsApi
import com.evernym.sdk.vcx.vcx.VcxApi
import com.luxoft.blockchainlab.hyperledger.indy.utils.SerializationUtils
import com.luxoft.blockchainlab.hyperledger.indy.utils.StorageUtils
import org.junit.After
import org.junit.Before
import org.junit.Test


class AgentDemo {
    val provisionConfig = ProvisionConfig()

    @Before
    @Throws(Exception::class)
    fun setUp() {
        // Clean indy stuff
        StorageUtils.cleanupStorage()
        System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")

        Nullpay.INSTANCE.nullpay_init()
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        // Clean indy stuff
        StorageUtils.cleanupStorage()
    }

    @Test
    fun faber() {
        println("#1 Provision an agent and wallet, get back configuration details")
        val configDetailsJson = UtilsApi.vcxAgentProvisionAsync(SerializationUtils.anyToJSON(provisionConfig)).get()
        val configDetails = SerializationUtils.jSONToAny<ProvisionConfigDetails>(configDetailsJson)

        configDetails.institutionName = "Faber"
        configDetails.institutionLogoUrl = "http://robohash.org/234"
        configDetails.genesisPath = "docker.txn"

        println("#2 Initialize libvcx with new configuration")
        VcxApi.vcxInitWithConfig(SerializationUtils.anyToJSON(configDetails)).get()

        println("#3 Create a new schema on the ledger")
        val schemaFields = listOf("name", "date", "degree")
        val schemaHandle = SchemaApi.schemaCreate(configDetails.institutionDid, "degree schema", "0.0.1", SerializationUtils.anyToJSON(schemaFields), 0).get()
        val schema = SerializationUtils.jSONToAny<Schema>(SchemaApi.schemaSerialize(schemaHandle).get())

        println("Schema: $schema")
    }
}

data class ProvisionConfig(
        val agencyUrl: String = "http://localhost:8080",
        val agencyDid: String = "VsKV7grR1BUE29mG2Fm2kX",
        val agencyVerkey: String = "Hezce2UWMZ3wUhVkh2LfKSs8nDzWwzs2Win7EzNN3YaR",
        val walletName: String = "faber_wallet",
        val walletKey: String = "123",
        val paymentMethod: String = "null",
        val enterpriceSeed: String = "000000000000000000000000Trustee1"
)

/**
 * {
 *  "agency_did":"VsKV7grR1BUE29mG2Fm2kX",
 *  "agency_endpoint":"http://localhost:8080",
 *  "agency_verkey":"Hezce2UWMZ3wUhVkh2LfKSs8nDzWwzs2Win7EzNN3YaR",
 *  "genesis_path":"<CHANGE_ME>",
 *  "institution_did":"FrKoEzVkpaf6J6cV42QT8C",
 *  "institution_logo_url":"<CHANGE_ME>",
 *  "institution_name":"<CHANGE_ME>",
 *  "institution_verkey":"96T9r3hYmhfNpymcEzW2UtJsAv39CD2KTwjXJT2pff4g",
 *  "remote_to_sdk_did":"5wFQGGYUBdvFd4dRK5LqmJ",
 *  "remote_to_sdk_verkey":"3h1sdSKhB9n1ZNvevZD9oht4TSF5KoaBLvRwYDa14St7",
 *  "sdk_to_remote_did":"FrKoEzVkpaf6J6cV42QT8C",
 *  "sdk_to_remote_verkey":"96T9r3hYmhfNpymcEzW2UtJsAv39CD2KTwjXJT2pff4g",
 *  "wallet_key":"123",
 *  "wallet_name":"faber_wallet"
 * }
 */
data class ProvisionConfigDetails(
        var agencyDid: String,
        var agencyEndpoint: String,
        var agencyVerkey: String,
        var genesisPath: String,
        var institutionDid: String,
        var institutionLogoUrl: String,
        var institutionName: String,
        var institutionVerkey: String,
        var remoteToSdkDid: String,
        var remoteToSdkVerkey: String,
        var sdkToRemoteDid: String,
        var sdkToRemoteVerkey: String,
        var walletKey: String,
        var walletName: String
)