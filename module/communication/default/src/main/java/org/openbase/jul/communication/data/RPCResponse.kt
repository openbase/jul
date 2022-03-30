package org.openbase.jul.communication.data

data class RPCResponse<RETURN>(
    val response: RETURN,
    val properties: Map<String,String>
)
