package com.luxoft.blockchainlab.corda.hyperledger.indy.service

import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection
import com.luxoft.blockchainlab.hyperledger.indy.*
import net.corda.core.flows.FlowLogic
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import java.util.*


@CordaService
class ConnectionService(serviceHub: AppServiceHub) : SingletonSerializeAsToken() {

    private val connection: AgentConnection? = try {
        AgentConnection(
                IndyProperties.getAgentWSEndpoint(),
                IndyProperties.getAgentUser(),
                IndyProperties.getAgentPassword()
        )
    } catch(e: Exception) {
        logger.error("Agent URL has not specified")
        null
    }

    fun getCounterParty() = getConnection().getCounterParty()

    fun sendCredentialOffer(offer: CredentialOffer) = getConnection().sendCredentialOffer(offer)

    fun receiveCredentialOffer() = getConnection().receiveCredentialOffer()

    fun sendCredentialRequest(request: CredentialRequestInfo) = getConnection().sendCredentialRequest(request)

    fun receiveCredentialRequest() = getConnection().receiveCredentialRequest()

    fun sendCredential(credential: CredentialInfo) = getConnection().sendCredential(credential)

    fun receiveCredential() = getConnection().receiveCredential()

    fun sendProofRequest(request: ProofRequest) = getConnection().sendProofRequest(request)

    fun receiveProofRequest() = getConnection().receiveProofRequest()

    fun sendProof(proof: ProofInfo) = getConnection().sendProof(proof)

    fun receiveProof() = getConnection().receiveProof()

    fun getConnection(): AgentConnection = connection!!
}

fun FlowLogic<Any>.connectionService() = serviceHub.cordaService(ConnectionService::class.java)