package org.openbase.jul.communication.exception

class RPCException(
    message: String,
    cause: Throwable? = null
): Exception(message, cause)
