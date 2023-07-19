package org.openbase.jul.exception

class RedundantExecutionException(
    context: String
): VerificationFailedException("Redundant execution of $context detected!")
