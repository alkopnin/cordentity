package com.luxoft.blockchainlab.corda.hyperledger.indy

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection.MESSAGE_TYPES.CONNECT
import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection.MESSAGE_TYPES.GENERATE_INVITE
import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection.MESSAGE_TYPES.GET_MESSAGES
import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection.MESSAGE_TYPES.INVITE_GENERATED
import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection.MESSAGE_TYPES.INVITE_RECEIVED
import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection.MESSAGE_TYPES.MESSAGE_RECEIVED
import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection.MESSAGE_TYPES.RECEIVE_INVITE
import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection.MESSAGE_TYPES.REQUEST_RECEIVED
import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection.MESSAGE_TYPES.RESPONSE_RECEIVED
import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection.MESSAGE_TYPES.SEND_MESSAGE
import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection.MESSAGE_TYPES.SEND_REQUEST
import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection.MESSAGE_TYPES.SEND_RESPONSE
import com.luxoft.blockchainlab.hyperledger.indy.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Thread.sleep
import java.net.URI
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

data class IndyParty(val did: String, val endpoint: String, val verkey: String? = null)

interface Connection {
    fun getCounterParty(): IndyParty?

    fun sendCredentialOffer(offer: CredentialOffer)
    fun receiveCredentialOffer(): CredentialOffer

    fun sendCredentialRequest(request: CredentialRequestInfo)
    fun receiveCredentialRequest(): CredentialRequestInfo

    fun sendCredential(credential: CredentialInfo)
    fun receiveCredential(): CredentialInfo

    fun sendProofRequest(request: ProofRequest)
    fun receiveProofRequest(): ProofRequest

    fun sendProof(proof: ProofInfo)
    fun receiveProof(): ProofInfo
}

class AgentConnection(val myAgentUrl: String, val invite: String? = null, val userName: String = "user1", val passphrase: String = "test") : Connection {

    private var counterParty: IndyParty? = null

    object MESSAGE_TYPES {
        val CONN_BASE = "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/connections/1.0/"
        val ADMIN_CONNECTIONS_BASE = "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/admin_connections/1.0/"
        val ADMIN_BASE = "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/admin/1.0/"
        val ADMIN_WALLETCONNECTION_BASE = "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/admin_walletconnection/1.0/"
        val ADMIN_BASICMESSAGE_BASE = "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/admin_basicmessage/1.0/"

        val CONNECT = ADMIN_WALLETCONNECTION_BASE + "connect"
        val DISCONNECT = ADMIN_WALLETCONNECTION_BASE + "disconnect"

        val SEND_MESSAGE = ADMIN_BASICMESSAGE_BASE + "send_message"
        val MESSAGE_RECEIVED = ADMIN_BASICMESSAGE_BASE + "message_received"
        val GET_MESSAGES = ADMIN_BASICMESSAGE_BASE + "get_messages"
        val MESSAGES = ADMIN_BASICMESSAGE_BASE + "messages"

        val GENERATE_INVITE = ADMIN_CONNECTIONS_BASE + "generate_invite"
        val INVITE_GENERATED = ADMIN_CONNECTIONS_BASE + "invite_generated"
        val INVITE_RECEIVED = ADMIN_CONNECTIONS_BASE + "invite_received"
        val RECEIVE_INVITE = ADMIN_CONNECTIONS_BASE + "receive_invite"

        val SEND_REQUEST = ADMIN_CONNECTIONS_BASE + "send_request"
        val REQUEST_RECEIVED = ADMIN_CONNECTIONS_BASE + "request_received"

        val SEND_RESPONSE = ADMIN_CONNECTIONS_BASE + "send_response"
        val RESPONSE_RECEIVED = ADMIN_CONNECTIONS_BASE + "response_received"

        val STATE_REQUEST = ADMIN_BASE + "state_request"
    }

    private val webSocket = AgentWebSocketClient(URI(myAgentUrl))

    init {
        webSocket.apply {
            connectBlocking()
            sendJson(WalletConnect(userName, passphrase))
            if (invite != null) {
                sendJson(ReceiveInviteMessage(invite))
                val invite = waitForMessageOfType<InviteReceivedMessage>(INVITE_RECEIVED)
                sendRequest(invite.key)
                val response = waitForMessageOfType<RequestResponseReceivedMessage>(RESPONSE_RECEIVED)
                counterParty = IndyParty(response.their_did, invite.endpoint)
            }
        }
    }

    fun sendJson(obj: Any) = webSocket.sendJson(obj)

    fun genInvite(): ReceiveInviteMessage {
        webSocket.sendJson(SendMessage(type = GENERATE_INVITE))
        return webSocket.waitForMessageOfType<ReceiveInviteMessage>(INVITE_GENERATED)
    }

    fun waitForCounterParty() {
        webSocket.waitForMessageOfType<RequestReceivedMessage>(REQUEST_RECEIVED).also {
            counterParty = IndyParty(it.did, it.endpoint)
            sendJson(RequestSendResponseMessage(it.did))
        }
    }

    fun sendRequest(key: String) = webSocket.sendJson(SendRequestMessage(key))


    override fun getCounterParty(): IndyParty? {
        return counterParty
    }

    override fun sendCredentialOffer(offer: CredentialOffer) = webSocket.sendTypedMessage(offer, counterParty!!)

    override fun receiveCredentialOffer(): CredentialOffer = webSocket.waitForTypedMessage()

    override fun sendCredentialRequest(request: CredentialRequestInfo) = webSocket.sendTypedMessage(request, counterParty!!)

    override fun receiveCredentialRequest(): CredentialRequestInfo = webSocket.waitForTypedMessage()

    override fun sendCredential(credential: CredentialInfo) = webSocket.sendTypedMessage(credential, counterParty!!)

    override fun receiveCredential(): CredentialInfo = webSocket.waitForTypedMessage()
    override fun sendProofRequest(request: ProofRequest) = webSocket.sendTypedMessage(request, counterParty!!)

    override fun receiveProofRequest(): ProofRequest = webSocket.waitForTypedMessage()

    override fun sendProof(proof: ProofInfo) = webSocket.sendTypedMessage(proof, counterParty!!)

    override fun receiveProof(): ProofInfo = webSocket.waitForTypedMessage()
}

class AgentWebSocketClient(serverUri: URI) : WebSocketClient(serverUri) {
    private val log = Logger.getLogger(this::class.java.name)
    val gson = Gson()

    override fun onOpen(handshakedata: ServerHandshake?) {
        log.info { "Connection opened: $handshakedata" }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        log.info { "Connection closed: $code,$reason,$remote" }
    }

    val receivedMessages = mutableListOf<String>()

    override fun onMessage(message: String?) {
        log.info { "Message: $message" }
        if (message != null)
            synchronized(receivedMessages) { receivedMessages.add(message) }
    }

    override fun onError(ex: Exception?) {
        log.log(Level.WARNING, "Connection error", ex)
    }

    fun sendJson(obj: Any) = send(gson.toJson(obj))

    fun popMessageOfType(type: String): String? {
        synchronized(receivedMessages) {
            val result = receivedMessages.find {
                gson.fromJson(it, JsonObject::class.java).get("@type").asString?.contentEquals(type) ?: false
            }
            if (result != null)
                receivedMessages.remove(result)
            return result
        }
    }

    inline fun <reified T> popMessageOfType(type: String): T {
        return gson.fromJson(popMessageOfType(type), T::class.java)
    }

    fun waitForMessageOfType(type: String): String {
        var messageOfType: String? = null
        while (messageOfType == null) {
            sleep(500)
            messageOfType = popMessageOfType(type)
        }
        return messageOfType
    }

    inline fun <reified T> waitForMessageOfType(type: String): T {
        return gson.fromJson(waitForMessageOfType(type), T::class.java)
    }

    fun sendTypedMessage(message: TypedBodyMessage, counterParty: IndyParty) = sendJson(SendMessage(counterParty.did, message))
    inline fun <reified T : Any> sendTypedMessage(message: T, counterParty: IndyParty) = sendTypedMessage(TypedBodyMessage(message, T::class.java.canonicalName), counterParty)
    inline fun <reified T : Any> popTypedMessage(): T? {
        synchronized(receivedMessages) {
            val message = receivedMessages.filter {
                gson.fromJson(it, JsonObject::class.java).get("@type").asString
                        ?.contentEquals(MESSAGE_RECEIVED) ?: false
            }.find { gson.fromJson(it, MessageReceived::class.java).message.content.clazz == T::class.java.canonicalName }
            if (message != null) {
                val result = gson.fromJson(gson.toJsonTree(gson.fromJson(message, MessageReceived::class.java).message.content.message), T::class.java)
                receivedMessages.remove(message)
                return result
            }
            return null
        }
    }

    inline fun <reified T : Any> waitForTypedMessage(): T {
        var messageOfType: T? = null
        while (messageOfType == null) {
            sleep(500)
            messageOfType = popTypedMessage()
        }
        return messageOfType
    }
}

data class WalletConnect(val name: String, val passphrase: String, @SerializedName("@type") val type: String = CONNECT)
data class ReceiveInviteMessage(val invite: String, val label: String = "", @SerializedName("@type") val type: String = RECEIVE_INVITE)
data class InviteReceivedMessage(val key: String, val label: String, val endpoint: String, @SerializedName("@type") val type: String)

data class SendRequestMessage(val key: String, @SerializedName("@type") val type: String = SEND_REQUEST)
data class RequestReceivedMessage(val label: String, val did: String, val endpoint: String, @SerializedName("@type") val type: String)
data class RequestSendResponseMessage(val did: String, @SerializedName("@type") val type: String = SEND_RESPONSE)
data class RequestResponseReceivedMessage(val their_did: String, val history: JsonObject, @SerializedName("@type") val type: String)

data class SendMessage(val to: String? = null, val message: TypedBodyMessage? = null, @SerializedName("@type") val type: String = SEND_MESSAGE)
data class MessageReceivedMessage(val from: String, val timestamp: Number, val content: TypedBodyMessage)
data class MessageReceived(val id: String, val with: String?, val message: MessageReceivedMessage, @SerializedName("@type") val type: String = SEND_MESSAGE)
data class LoadMessage(val with: String, @SerializedName("@type") val type: String = GET_MESSAGES)
data class TypedBodyMessage(val message: Any, @SerializedName("@class") val clazz: String, val correlationId: String = UUID.randomUUID().toString())