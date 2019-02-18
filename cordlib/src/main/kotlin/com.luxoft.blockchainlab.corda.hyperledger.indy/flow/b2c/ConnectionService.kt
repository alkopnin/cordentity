package com.luxoft.blockchainlab.corda.hyperledger.indy.flow.b2c

import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection
import com.luxoft.blockchainlab.corda.hyperledger.indy.Connection
import com.luxoft.blockchainlab.corda.hyperledger.indy.IndyParty
import com.luxoft.blockchainlab.hyperledger.indy.*
import net.corda.core.flows.FlowLogic
import net.corda.core.node.AppServiceHub
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.CordaSerializable
import net.corda.core.serialization.SingletonSerializeAsToken
import java.lang.RuntimeException
import java.util.*


@CordaService
class ConnectionService(serviceHub: AppServiceHub) : SingletonSerializeAsToken(), Connection {
    override fun getCounterParty() = connection!!.getCounterParty()

    override fun sendCredentialOffer(offer: CredentialOffer) = connection!!.sendCredentialOffer(offer)

    override fun receiveCredentialOffer() = connection!!.receiveCredentialOffer()

    override fun sendCredentialRequest(request: CredentialRequestInfo) = connection!!.sendCredentialRequest(request)

    override fun receiveCredentialRequest() = connection!!.receiveCredentialRequest()

    override fun sendCredential(credential: CredentialInfo) = connection!!.sendCredential(credential)

    override fun receiveCredential() = connection!!.receiveCredential()

    override fun sendProofRequest(request: ProofRequest) = connection!!.sendProofRequest(request)

    override fun receiveProofRequest() = connection!!.receiveProofRequest()

    override fun sendProof(proof: ProofInfo) = connection!!.sendProof(proof)

    override fun receiveProof() = connection!!.receiveProof()

    private val connection: AgentConnection? = if (serviceHub.myInfo.legalIdentities.first().name.organisation == "TreatmentCenter")
        AgentConnection("ws://10.255.255.21:8095/ws", userName = "user${Random().nextInt()}")
    else
        null

    fun getConnection(): AgentConnection {
        return connection!!
    }
}

fun FlowLogic<Any>.connectionService() = serviceHub.cordaService(ConnectionService::class.java)