package com.luxoft.blockchainlab.hyperledger.indy


import com.evernym.sdk.vcx.connection.ConnectionApi
import com.evernym.sdk.vcx.credential.CredentialApi
import com.evernym.sdk.vcx.credentialDef.CredentialDefApi
import com.evernym.sdk.vcx.issuer.IssuerApi
import com.evernym.sdk.vcx.proof.DisclosedProofApi
import com.evernym.sdk.vcx.proof.ProofApi
import com.evernym.sdk.vcx.schema.SchemaApi
import com.evernym.sdk.vcx.utils.UtilsApi
import com.evernym.sdk.vcx.vcx.VcxApi
import com.luxoft.blockchainlab.hyperledger.indy.utils.EnvironmentUtils
import com.luxoft.blockchainlab.hyperledger.indy.utils.PoolManager
import com.luxoft.blockchainlab.hyperledger.indy.utils.SerializationUtils
import com.luxoft.blockchainlab.hyperledger.indy.utils.StorageUtils
import org.hyperledger.indy.sdk.pool.Pool
import org.hyperledger.indy.sdk.wallet.Wallet
import org.junit.*
import java.io.File
import java.nio.file.Files
import java.util.*
import java.nio.file.Paths


class AgentDemo {
    val faberProvisionConfig = VcxProvisionConfig()
    val aliceProvisionConfig = VcxProvisionConfig()
    lateinit var faberConfigDetails: VcxProvisionConfigDetails
    lateinit var aliceConfigDetails: VcxProvisionConfigDetails
    val INVITE_FILENAME = "/home/joinu/vcx-invite.txt"

    val TEST_GENESIS_FILE_PATH by lazy { javaClass.classLoader.getResource("docker.txn").file }
    val LOG_LEVEL = "TRACE"

    @Before
    @Throws(Exception::class)
    fun setUp() {
        println("Setting LOG_LEVEL to $LOG_LEVEL")
        System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, LOG_LEVEL)

        println("Linking libnullpay native library")
        Nullpay.INSTANCE.nullpay_init()
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
    }

    @Test
    @Ignore
    fun `vcx + indy using single wallet`() {
        println("Removing trash")
        StorageUtils.cleanupStorage()

        val config = VcxProvisionConfig()
        val configDetailsJson = UtilsApi.vcxAgentProvisionAsync(SerializationUtils.anyToJSON(config)).get()

        val configDetails: VcxProvisionConfigDetails = SerializationUtils.jSONToAny(configDetailsJson)
        configDetails.institutionName = "Me"
        configDetails.institutionLogoUrl = "http://robohash.org/234"
        configDetails.genesisPath = TEST_GENESIS_FILE_PATH

        VcxApi.vcxInitWithConfig(SerializationUtils.anyToJSON(configDetails)).get()

        val pool = PoolManager.openIndyPool(File(TEST_GENESIS_FILE_PATH), "pool1-indy")
        val wallet = openIndyWallet(configDetails.walletName, configDetails.walletKey)
        val me = IndyUser(pool, wallet, configDetails.institutionDid)

        val version = "${Random().nextInt() % 100 + 100}.${Random().nextInt() % 100 + 100}.${Random().nextInt() % 100 + 100}"
        val schemaHandle = SchemaApi.schemaCreate(
                "example",
                "example",
                version,
                SerializationUtils.anyToJSON(listOf("sex", "name", "height", "age")),
                0
        ).get()
        val schemaResponse = SerializationUtils.jSONToAny<VcxSchemaResponse>(SchemaApi.schemaSerialize(schemaHandle).get())

        val credDefHandle = CredentialDefApi.credentialDefCreate(
                "cred_def_id",
                "degree",
                schemaResponse.data.schemaId,
                configDetails.institutionDid,
                "tag1",
                SerializationUtils.anyToJSON(VcxCredentialDefinitionConfig()),
                0
        ).get()
        val credDefResponse = SerializationUtils.jSONToAny<VcxCredentialDefinitionResponse>(CredentialDefApi.credentialDefSerialize(credDefHandle).get())


        // DO SOME SHIT IN INDY
        val credValues = """
            {
                "sex": {"raw": "male", "encoded": "5944657099558967239210949258394887428692050081607692519917050"},
                "name": {"raw": "Alex", "encoded": "1139481716457488690172217916278103335"},
                "height": {"raw": "175", "encoded": "175"},
                "age": {"raw": "28", "encoded": "28"}
            }
        """.trimIndent()

        // Issuing credential
        val credOffer = me.createCredentialOffer(CredentialDefinitionId.fromString(credDefResponse.data.id))
        val credRequest = me.createCredentialRequest(me.did, credOffer, "main")
        val cred = me.issueCredential(credRequest, credValues, credOffer)

        // Receiving it
        me.receiveCredential(cred, credRequest, credOffer)

        // Request proof
        val fieldName = CredentialFieldReference("name", schemaResponse.data.schemaId, credDefResponse.data.id)
        val fieldSex = CredentialFieldReference("sex", schemaResponse.data.schemaId, credDefResponse.data.id)
        val fieldAge = CredentialFieldReference("age", schemaResponse.data.schemaId, credDefResponse.data.id)
        val proofReq = IndyUser.createProofRequest(
                version = "0.1",
                name = "proof_req_0.1",
                attributes = listOf(fieldName, fieldSex),
                predicates = listOf(CredentialPredicate(fieldAge, 18))
        )

        // Create proof
        val proofIndy = me.createProof(proofReq, "main")

        // Verify proof
        val usedData = IndyUser.getDataUsedInProof(me.did, pool, proofReq, proofIndy)
        assert(me.verifyProof(proofReq, proofIndy, usedData)) { "Proof is invalid" }

        // DO SAME SHIT IN VCX

        // Issuer create connection
        val connectionToMyselfProverHandle = ConnectionApi.vcxConnectionCreate("Me as Prover").get()
        ConnectionApi.vcxConnectionConnect(connectionToMyselfProverHandle, SerializationUtils.anyToJSON(VcxConnectionConfig())).get()
        ConnectionApi.vcxConnectionUpdateState(connectionToMyselfProverHandle).get()
        val inviteDetails = ConnectionApi.connectionInviteDetails(connectionToMyselfProverHandle, 0).get()

        // Prover create connection
        val connectionToMyselfIssuerHandle = ConnectionApi.vcxCreateConnectionWithInvite("Me as Issuer", inviteDetails).get()
        ConnectionApi.vcxConnectionConnect(connectionToMyselfIssuerHandle, SerializationUtils.anyToJSON(VcxConnectionConfig())).get()
        ConnectionApi.vcxConnectionUpdateState(connectionToMyselfIssuerHandle).get()

        // Update Issuer connection
        ConnectionApi.vcxConnectionUpdateState(connectionToMyselfProverHandle).get()

        // Issuer create proof request
        val proofRestrictions = listOf(mapOf("schema_id" to schemaResponse.data.schemaId))
        val proofAttributes = listOf(
                mapOf(
                        "name" to "name",
                        "restrictions" to proofRestrictions
                ),
                mapOf(
                        "name" to "sex",
                        "restrictions" to proofRestrictions
                ),
                mapOf(
                        "name" to "age"
                )
        )

        val proofPredicates = listOf(
                mapOf(
                        "name" to "age",
                        "p_type" to ">=",
                        "p_value" to 18,
                        "restrictions" to proofRestrictions
                )
        )

        val proofIssuerHandle = ProofApi.proofCreate(
                "My proof request",
                SerializationUtils.anyToJSON(proofAttributes),
                "[]", //SerializationUtils.anyToJSON(proofPredicates),
                "{}",
                "proof from me"
        ).get()
        ProofApi.proofSendRequest(proofIssuerHandle, connectionToMyselfProverHandle).get()

        // Prover create proof
        val proofRequestsJson = DisclosedProofApi.proofGetRequests(connectionToMyselfIssuerHandle).get()
        val proofRequests = SerializationUtils.jSONToAny<Array<VcxProofRequest>>(proofRequestsJson)
        val proofProverHandle = DisclosedProofApi.proofCreateWithRequest("My proof", SerializationUtils.anyToJSON(proofRequests.first())).get()

        val credentialsJson = DisclosedProofApi.proofRetrieveCredentials(proofProverHandle).get()
        val credentials = SerializationUtils.jSONToAny<VcxRetrievedCredentials>(credentialsJson)

        val rearrangedCredentials = VcxRearrangedRetrievedCredentials(
                credentials.attrs.keys.associate {
                    it to VcxRearrangedRetrievedCredentialAttribute(credentials.attrs.getValue(it).first())
                }
        )
        DisclosedProofApi.proofGenerate(proofProverHandle, SerializationUtils.anyToJSON(rearrangedCredentials), "{}").get()
        DisclosedProofApi.proofSend(proofProverHandle, connectionToMyselfIssuerHandle).get()

        // Issuer check proof
        ProofApi.proofUpdateState(proofIssuerHandle).get()
        val proof = ProofApi.getProof(proofIssuerHandle, connectionToMyselfProverHandle).get()
        assert(proof.proof_state == VcxProofState.Verified.ordinal) { "Proof is invalid" }

        // SHUTDOWN
        VcxApi.vcxShutdown(true)
        wallet.closeWallet().get()
        pool.closePoolLedger().get()
        Pool.deletePoolLedgerConfig("pool1-indy")
    }

    fun openIndyWallet(name: String, key: String): Wallet {

        val walletDir = EnvironmentUtils.getIndyHomePath("wallet")
        val target = Paths.get(walletDir, name)
        val link = Paths.get(walletDir, "$name-indy")

        if (Files.exists(link)) {
            Files.delete(link)
        }
        Files.createSymbolicLink(link, target)

        val walletConfig = SerializationUtils.anyToJSON(WalletConfig("$name-indy"))
        val walletCredentials = """{"key": "$key"}"""

        return Wallet.openWallet(walletConfig, walletCredentials).get()
    }

    @Test
    @Ignore
    fun faber() {
        println("Removing trash")
        StorageUtils.cleanupStorage()

        println("[Faber] Provision agent")
        val configDetailsJson = UtilsApi.vcxAgentProvisionAsync(SerializationUtils.anyToJSON(faberProvisionConfig)).get()

        faberConfigDetails = SerializationUtils.jSONToAny(configDetailsJson)
        faberConfigDetails.institutionName = "Faber"
        faberConfigDetails.institutionLogoUrl = "http://robohash.org/234"
        faberConfigDetails.genesisPath = TEST_GENESIS_FILE_PATH

        println("[Faber] Create pool and wallet")
        VcxApi.vcxInitWithConfig(SerializationUtils.anyToJSON(faberConfigDetails)).get()

        println("[Faber] Create a new schema on the ledger")
        val schemaFields = listOf("name", "date", "degree")

        // randomize version to prevent schema id collisions
        val schemaVersion = "${Random().nextInt(1000)}.${Random().nextInt(1000)}.${Random().nextInt(1000)}"

        val schemaHandle = SchemaApi.schemaCreate("schema_id", "degree_schema", schemaVersion, SerializationUtils.anyToJSON(schemaFields), 0).get()
        val schemaResponse = SerializationUtils.jSONToAny<VcxSchemaResponse>(SchemaApi.schemaSerialize(schemaHandle).get())

        println("Schema: $schemaResponse")

        println("[Faber] Create a new credential definition on the ledger")
        val credDefHandle = CredentialDefApi.credentialDefCreate(
                "cred_def_id",
                "degree",
                schemaResponse.data.schemaId,
                faberConfigDetails.institutionDid,
                "tag1",
                SerializationUtils.anyToJSON(VcxCredentialDefinitionConfig()),
                0
        ).get()
        val credDefResponse = SerializationUtils.jSONToAny<VcxCredentialDefinitionResponse>(CredentialDefApi.credentialDefSerialize(credDefHandle).get())
        println("Credential Definition: $credDefResponse")

        println("[Faber] Create a connection to Alice and print out the invite details")
        val connectionToAliceHandle = ConnectionApi.vcxConnectionCreate("Alice").get()
        val connectionToAliceResponse = SerializationUtils.jSONToAny<VcxConnectionDetailsResponse>(ConnectionApi.connectionSerialize(connectionToAliceHandle).get())

        println("Connection to Alice: $connectionToAliceResponse")
        ConnectionApi.vcxConnectionConnect(connectionToAliceHandle, SerializationUtils.anyToJSON(VcxConnectionConfig())).get()
        ConnectionApi.vcxConnectionUpdateState(connectionToAliceHandle).get()

        println("[Faber] Creating ~/vcx-invite.txt file and printing invite there")
        val inviteDetails = ConnectionApi.connectionInviteDetails(connectionToAliceHandle, 0).get()
        val file = File(INVITE_FILENAME)
        file.createNewFile()
        file.writeText(inviteDetails)
        println("--------- INVITE ---------")
        println(inviteDetails)
        println("--------------------------")

        println("[Faber] Poll agency and wait for Alice to accept the invitation (start alice.py now)")
        while (true) {
            ConnectionApi.vcxConnectionUpdateState(connectionToAliceHandle).get()
            val connectionToAliceState = ConnectionApi.connectionGetState(connectionToAliceHandle).get()
            if (connectionToAliceState == VcxState.Accepted.ordinal)
                break

            Thread.sleep(1000)
        }

        val credentialAttributes = mapOf(
                "name" to "alice",
                "date" to "05-2018",
                "degree" to "maths"
        )

        println("[Faber] Create a Credential object using the schema and credential definition")
        val credentialHandle = IssuerApi.issuerCreateCredential(
                "credential_id",
                credDefHandle,
                faberConfigDetails.institutionDid,
                SerializationUtils.anyToJSON(credentialAttributes),
                "alice_degree_credential",
                0
        ).get()

        println("[Faber] Send credential offer to Alice")
        IssuerApi.issuerSendcredentialOffer(credentialHandle, connectionToAliceHandle).get()

        println("[Faber] Poll agency and wait for Alice to send a credential request")
        while (true) {
            if (IssuerApi.issuerCredentialUpdateState(credentialHandle).get() == VcxState.RequestReceived.ordinal)
                break

            Thread.sleep(1000)
        }

        println("[Faber] Send real credential to Alice")
        IssuerApi.issuerSendCredential(credentialHandle, connectionToAliceHandle).get()

        Thread.sleep(10000)

        println("[Faber] Wait for Alice to accept credential")
        while (true) {
            if (IssuerApi.issuerCredentialUpdateState(credentialHandle).get() == VcxState.Accepted.ordinal)
                break

            Thread.sleep(1000)
        }

        val proofRestrictions = listOf(mapOf("issuer_did" to faberConfigDetails.institutionDid))
        val proofAttributes = listOf(
                mapOf(
                        "name" to "name",
                        "restrictions" to proofRestrictions
                ),
                mapOf(
                        "name" to "date",
                        "restrictions" to proofRestrictions
                ),
                mapOf(
                        "name" to "degree",
                        "restrictions" to proofRestrictions
                )
        )

        println("[Faber] Create a Proof object")
        val proofHandle = ProofApi.proofCreate("Faber's proof request", SerializationUtils.anyToJSON(proofAttributes), "[]", "{}", "proof from Alice").get()

        println("[Faber] Request proof of degree from Alice")
        ProofApi.proofSendRequest(proofHandle, connectionToAliceHandle).get()

        println("[Faber] Poll agency and wait for Alice to provide proof")
        while (true) {
            val proofState = ProofApi.proofUpdateState(proofHandle).get()
            if (proofState == VcxState.Accepted.ordinal)
                break

            Thread.sleep(1000)
        }

        println("[Faber] Process the proof provided by Alice")
        val proof = ProofApi.getProof(proofHandle, connectionToAliceHandle).get()

        println("[Faber] Check if proof is valid")
        assert(proof.proof_state == VcxProofState.Verified.ordinal) { "Proof is invalid" }

        println("Remove wallets")
        VcxApi.vcxShutdown(true)

        println("Removing trash")
        StorageUtils.cleanupStorage()
    }

    @Test
    @Ignore
    fun alice() {
        println("[Alice] Provision agent")
        val configDetailsJson = UtilsApi.vcxAgentProvisionAsync(SerializationUtils.anyToJSON(aliceProvisionConfig)).get()

        aliceConfigDetails = SerializationUtils.jSONToAny(configDetailsJson)
        aliceConfigDetails.institutionName = "Alice"
        aliceConfigDetails.institutionLogoUrl = "http://robohash.org/456"
        aliceConfigDetails.genesisPath = TEST_GENESIS_FILE_PATH

        println("[Alice] Create pool and wallet")
        VcxApi.vcxInitWithConfig(SerializationUtils.anyToJSON(aliceConfigDetails)).get()

        println("[Alice] Trying to read invite from file...")
        while (true) {
            if (File(INVITE_FILENAME).exists())
                break
            Thread.sleep(1000)
        }

        val file = File(INVITE_FILENAME)
        val inviteJson = file.readText()
        file.delete()
        println("[Alice] Invite read success: $inviteJson")

        val connectionHandle = ConnectionApi.vcxCreateConnectionWithInvite("Faber", inviteJson).get()
        ConnectionApi.vcxConnectionConnect(connectionHandle, SerializationUtils.anyToJSON(VcxConnectionConfig())).get()
        ConnectionApi.vcxConnectionUpdateState(connectionHandle).get()

        println("[Alice] Wait for Faber to issue a credential offer")
        var offersJson: String
        while (true) {
            offersJson = CredentialApi.credentialGetOffers(connectionHandle).get()
            if (offersJson.trim().isNotEmpty() && offersJson.trim() != "[]")
                break
            Thread.sleep(1000)
        }
        val offers = SerializationUtils.jSONToAny<List<List<VcxCredentialOffer>>>(offersJson)
        println("[Alice] Received next offers: $offers")

        val credentialHandle = CredentialApi.credentialCreateWithOffer("Alice's credential", SerializationUtils.anyToJSON(offers.first())).get()

        println("[Alice] After receiving credential offer, send credential request")
        CredentialApi.credentialSendRequest(credentialHandle, connectionHandle, 0).get()

        println("[Alice] Poll agency and accept credential offer from Faber")
        while (true) {
            val credentialState = CredentialApi.credentialUpdateState(credentialHandle).get()
            if (credentialState == VcxState.Accepted.ordinal)
                break
            Thread.sleep(1000)
        }

        println("[Alice] Poll agency for a proof request")
        var proofRequestsJson: String
        while (true) {
            proofRequestsJson = DisclosedProofApi.proofGetRequests(connectionHandle).get()
            if (proofRequestsJson.trim().isNotEmpty() && proofRequestsJson.trim() != "[]")
                break
            Thread.sleep(1000)
        }
        val proofRequests = SerializationUtils.jSONToAny<Array<VcxProofRequest>>(proofRequestsJson)
        println("[Alice] Received next proof requests: $proofRequests")

        println("[Alice] Create a Disclosed proof object from proof request")
        val proofHandle = DisclosedProofApi.proofCreateWithRequest("Alice's proof", SerializationUtils.anyToJSON(proofRequests.first())).get()

        println("[Alice] Query for credentials in the wallet that satisfy the proof request")
        val credentialsJson = DisclosedProofApi.proofRetrieveCredentials(proofHandle).get()
        val credentials = SerializationUtils.jSONToAny<VcxRetrievedCredentials>(credentialsJson)

        val rearrangedCredentials = VcxRearrangedRetrievedCredentials(
                credentials.attrs.keys.associate {
                    it to VcxRearrangedRetrievedCredentialAttribute(credentials.attrs[it]!!.first())
                }
        )

        println("[Alice] Generate the proof")
        DisclosedProofApi.proofGenerate(proofHandle, SerializationUtils.anyToJSON(rearrangedCredentials), "{}").get()

        println("[Alice] Send the proof to Faber")
        DisclosedProofApi.proofSend(proofHandle, connectionHandle).get()
    }
}

