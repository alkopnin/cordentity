package com.luxoft.blockchainlab.hyperledger.indy

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class VcxProvisionConfig(
        val agencyUrl: String = "http://10.255.255.21:8080",
        val agencyDid: String = "VsKV7grR1BUE29mG2Fm2kX",
        val agencyVerkey: String = "Hezce2UWMZ3wUhVkh2LfKSs8nDzWwzs2Win7EzNN3YaR",
        val walletName: String = "wallet-${Random().nextInt()}",
        val walletKey: String = "123",
        val paymentMethod: String = "null",
        val enterpriseSeed: String = "000000000000000000000000Trustee1"
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
data class VcxProvisionConfigDetails(
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

/**
 * {
 *  "data": {
 *      "data":["name","date","degree"],
 *      "name":"degree_schema",
 *      "payment_txn":null,
 *      "schema_id":"V4SGRU86Z58d6TV7PBUe6f:2:degree_schema:58.2.43",
 *      "sequence_num":0,
 *      "source_id":"schema_id",
 *      "version":"58.2.43"
 *  },
 *  "version":"1.0"
 * }
 */
data class VcxSchema(
        val data: List<String>,
        val name: String,
        val paymentTxn: String?,
        val schemaId: String,
        val sequenceNum: Long,
        val sourceId: String,
        val version: String
)
data class VcxSchemaResponse(val data: VcxSchema, val version: String)

/*
 * {
 *  "support_revocation":false,
 *  "tails_file": "/tmp/tailsfile.txt",
 *  "max_creds": 1
 * }
 */
data class VcxCredentialDefinitionConfig(
        val supportRevocation: Boolean = false,
        val tailsFile: String? = null,
        val maxCreds: Long? = null
)

/*
 * {
 *  "data":{
 *      "cred_def_payment_txn":null,
 *      "id":"V4SGRU86Z58d6TV7PBUe6f:3:CL:21:tag1",
 *      "issuer_did":"V4SGRU86Z58d6TV7PBUe6f",
 *      "name":"degree",
 *      "rev_reg_def":null,
 *      "rev_reg_def_payment_txn":null,
 *      "rev_reg_delta_payment_txn":null,
 *      "rev_reg_entry":null,
 *      "rev_reg_id":null,
 *      "source_id":"cred_def_id",
 *      "tag":"tag1",
 *      "tails_file":"/tmp/tailsfile.txt"
 *  },
 *  "version":"1.0"
 * }
 */
data class VcxCredentialDefinition(
        val credDefPaymentTxn: String?,
        val id: String,
        val issuerDid: String,
        val name: String,
        val tag: String,
        val sourceId: String,
        val tailsFile: String?,
        val revRegDef: String? = "{}",
        val revRegDefPaymentTxn: String?,
        val revRegDeltaPaymentTxn: String?,
        val revRegEntry: String?,
        val revRegId: String?
)

data class VcxCredentialDefinitionResponse(
        val data: VcxCredentialDefinition,
        val version: String
)

/*
 * {
 *  "data":{
 *      "agent_did":"",
 *      "agent_vk":"",
 *      "endpoint":"",
 *      "invite_detail":null,
 *      "invite_url":null,
 *      "public_did":null,
 *      "pw_did":"RP7XyCDaw3e95M8LmcfDA",
 *      "pw_verkey":"EHjtYoMRGpJ1Mg6p8w9aMusTwQUFt4Rz2jMvWeFhWZ6",
 *      "source_id":"Alice",
 *      "state":1,
 *      "their_public_did":null,
 *      "their_pw_did":"",
 *      "their_pw_verkey":"",
 *      "uuid":""
 *  },
 *  "version":"1.0"
 * }
 */
data class VcxConnectionDetails(
        val agentDid: String,
        val agentVk: String,
        val endpoint: String,
        val inviteDetail: String?,
        val inviteUrl: String?,
        val publicDid: String?,
        val pwDid: String,
        val pwVerkey: String,
        val sourceId: String,
        val state: Int,
        val theirPublicDid: String?,
        val theirPwDid: String,
        val theirPwVerkey: String,
        val uuid: String
)

data class VcxConnectionDetailsResponse(
        val data: VcxConnectionDetails,
        val version: String
)

/**
 * {"use_public_did": true}
 */
data class VcxConnectionConfig(val usePublicDid: Boolean = true)

enum class VcxState {
    Undefined,
    Initialized,
    OfferSent,
    RequestReceived,
    Accepted,
    Unfulfilled,
    Expired,
    Revoked,
}

enum class VcxProofState {
    Undefined,
    Verified,
    Invalid
}

/**
 * [
 *  [
 *      {
 *          "claim_id": "credential_id",
 *          "claim_name": "alice_degree_credential",
 *          "cred_def_id": "V4SGRU86Z58d6TV7PBUe6f:3:CL:15:tag1",
 *          "credential_attrs": {
 *              "date": "05-2018",
 *              "degree": "maths",
 *              "name": "alice"
 *          },
 *          "from_did": "QhjctWh2eyXWmyrmSDo4jb",
 *          "libindy_offer": "{\"schema_id\":\"V4SGRU86Z58d6TV7PBUe6f:2:degree_schema:973.368.192\",\"cred_def_id\":\"V4SGRU86Z58d6TV7PBUe6f:3:CL:15:tag1\",\"key_correctness_proof\":{\"c\":\"29833769742544868264422511117402098318050632797392719475654369530183935714254\",\"xz_cap\":\"568417715893749483850063381663272987377583639988979261019840248423916207625453379299601015779307493268675242238356511242186456323056637046341004282100660088840436257790632041415221401797199875897956025490663127324342071075565182278032974785587595803359755147436671181035852169742822832691305158772761291746398120920636475297041790730389079738837388112348237718305388848251271744781057302951991365233230159997652206705255027411453278760258542701656747726020403867586106291879059336515961122036946698790207113674453142792089414103059693644441151537771315003968719873855658266054712095751319230238539682126260289871429892658143529298761858994518154349268244958129499868218923184859502796329539537\",\"xr_cap\":[[\"name\",\"67696084472036637874405650046106254120046853598701886088442220651460139197856192453330779426842247017554145946202561694411891988399215757602916292978056722931159740636012825453391099058015638995005729401885141774055331976335023559774892063472589627838324924470218701228252748036255689345085154122407412551542624365595482859798670271352790291019252390827496757590946431820332720440050567062633801154003800480297286856182719880951631614178563686038329078345872674804961775789761065414182377207850588645844830477415868074999404005413710664114874101807850820862456005845865360770171913185174120485213562279715213620696696484352781473150209659480086978446439941760257232729602100839793935459159590\"],[\"date\",\"111511976083754321025010612515911066522968265329163933272248493542330784967413294171154886691663853483901851452023404065473509471976851285863702309628286263672714325403597031684288282717746443383948909327224931657747119759857159829464657343397725557489390107172276337320371390702279038310740898998741675828131784593696223351925640046380055401690230959921200076302057155565348492825380042205540243038695038783240795442374353687575244284932025924947662350348470621587834955812444764911467749307203421261613760191675409160453550620534787653171989293140234010466031996621579280601733496355553462677833214204466318361464769606622540238243828896165488638801532639504828527508218168481848675834514430\"],[\"master_secret\",\"162635903234677524334040205201809867321551517995636734801444458266924354808411929542659241941991318899613459188595350007588187222018179475348315721655245506407663321108323345997362750591578653457860728979085909775800037388386603515361861469907163511053960412806472807864206008837967514529421954599158223265286581941862946753523663815715737825265366988872670564811959223021129482299406747651824853928524877695835419372539862365037560261389104736955668373566070043736879344993387880619590217086852402359753050014438436035802344532934722007878242588302734477459373064785194414354113584444383345354795163220419435690024738620997356684806208220947468455278896672665758138122758500329793219157573696\"],[\"degree\",\"714718030081693273600050303333573063831229697254307488352240199575137745426202456795028052418237397081791959622105412340812390993183074321298558314624158071239260345889848735281455169363916068491989530605014277446163450428073687669039675744464799306251017494478113646722558560782960344786552807709713286647503620023399980148995789291044871946305267499422662741749439386729818626880223426264377070137977077345072787055856074540515208053046129625247027562309847327937281107505320606395043811252706960641077178550603346739918156898719713908577004704642358239258004627226171831046389972939648064704570426188362548468908815587097223464107873605543050218867994794054008043723719002593853407752076579\"]]},\"nonce\":\"151394781514736521880266\"}",
 *          "msg_ref_id": "6HRNIqw7lo",
 *          "msg_type": "CRED_OFFER",
 *          "schema_seq_no": 0,
 *          "to_did": "QhjctWh2eyXWmyrmSDo4jb",
 *          "version": "0.1"
 *      }
 *  ]
 * ]
 */

data class VcxCredentialOffer(
        val claimId: String,
        val claimName: String,
        val credDefId: String,
        val credentialAttrs: Map<String, String>,
        val fromDid: String,
        val libindyOffer: String,
        val msgRefId: String,
        val msgType: String,
        val schemaSeqNo: Long,
        val toDid: String,
        val version: String
)

/*
 * [
 *  {
 *    "@type": {
 *      "name": "PROOF_REQUEST",
 *      "version": "1.0"
 *    },
 *    "@topic": {
 *      "mid": 0,
 *      "tid": 0
 *    },
 *    "proof_request_data": {
 *      "nonce": "984362654229368736279183",
 *      "name": "proof from Alice",
 *      "version": "0.1",
 *      "requested_attributes": {
 *        "degree": {
 *          "name": "degree",
 *          "restrictions": [
 *            {
 *              "schema_id": null,
 *              "schema_issuer_did": null,
 *              "schema_name": null,
 *              "schema_version": null,
 *              "issuer_did": "V4SGRU86Z58d6TV7PBUe6f",
 *              "cred_def_id": null
 *            }
 *          ]
 *        },
 *        "name": {
 *          "name": "name",
 *          "restrictions": [
 *            {
 *              "schema_id": null,
 *              "schema_issuer_did": null,
 *              "schema_name": null,
 *              "schema_version": null,
 *              "issuer_did": "V4SGRU86Z58d6TV7PBUe6f",
 *              "cred_def_id": null
 *            }
 *          ]
 *        },
 *        "date": {
 *          "name": "date",
 *          "restrictions": [
 *            {
 *              "schema_id": null,
 *              "schema_issuer_did": null,
 *              "schema_name": null,
 *              "schema_version": null,
 *              "issuer_did": "V4SGRU86Z58d6TV7PBUe6f",
 *              "cred_def_id": null
 *            }
 *          ]
 *        }
 *      },
 *      "requested_predicates": {},
 *      "non_revoked": null
 *    },
 *    "msg_ref_id": "PKDofP30EQ",
 *    "from_timestamp": null,
 *    "to_timestamp": null
 *  }
 * ]
 */

data class VcxProofRequest(
        @JsonProperty("@type") val type: Any,
        @JsonProperty("@topic") val topic: Any,
        val proofRequestData: VcxProofRequestData,
        val msgRefId: String,
        val fromTimestamp: String?,
        val toTimestamp: String?
)

data class VcxProofRequestData(
        val nonce: String,
        val name: String,
        val version: String,
        val requestedAttributes: Any,
        val requestedPredicates: Any,
        val nonRevoked: String?
)

/*
 * {
 *  "attrs":{
 *      "date":[
 *          {
 *              "cred_info":{
 *                  "attrs":{
 *                      "date":"05-2018",
 *                      "degree":"maths",
 *                      "name":"alice"
 *                  },
 *                  "cred_def_id":"V4SGRU86Z58d6TV7PBUe6f:3:CL:35:tag1",
 *                  "cred_rev_id":null,
 *                  "referent":"44d106a6-9029-43e3-b9ac-cd93215b97f7",
 *                  "rev_reg_id":null,
 *                  "schema_id":"V4SGRU86Z58d6TV7PBUe6f:2:degree_schema:777.623.175"
 *              },
 *              "interval":null
 *          }
 *      ],
 *      "degree":[
 *          {
 *              "cred_info":{
 *                  "attrs":{
 *                      "date":"05-2018",
 *                      "degree":"maths",
 *                      "name":"alice"
 *                  },
 *                  "cred_def_id":"V4SGRU86Z58d6TV7PBUe6f:3:CL:35:tag1",
 *                  "cred_rev_id":null,
 *                  "referent":"44d106a6-9029-43e3-b9ac-cd93215b97f7",
 *                  "rev_reg_id":null,
 *                  "schema_id":"V4SGRU86Z58d6TV7PBUe6f:2:degree_schema:777.623.175"
 *               },
 *              "interval":null
 *          }
 *      ],
 *      "name":[
 *          {
 *              "cred_info":{
 *                  "attrs":{
 *                      "date":"05-2018",
 *                      "degree":"maths",
 *                      "name":"alice"
 *                  },
 *                  "cred_def_id":"V4SGRU86Z58d6TV7PBUe6f:3:CL:35:tag1",
 *                  "cred_rev_id":null,
 *                  "referent":"44d106a6-9029-43e3-b9ac-cd93215b97f7",
 *                  "rev_reg_id":null,
 *                  "schema_id":"V4SGRU86Z58d6TV7PBUe6f:2:degree_schema:777.623.175"
 *              },
 *              "interval":null
 *          }
 *      ]
 *  }
 * }
 */
data class VcxRetrievedCredentials(
        val attrs: Map<String, List<VcxRetrievedCredentialAttribute>>
)

data class VcxRetrievedCredentialAttribute(
        val credInfo: Any,
        val interval: String?
)

data class VcxRearrangedRetrievedCredentials(
        val attrs: Map<String, VcxRearrangedRetrievedCredentialAttribute>
)

data class VcxRearrangedRetrievedCredentialAttribute(
        val credential: VcxRetrievedCredentialAttribute
)
