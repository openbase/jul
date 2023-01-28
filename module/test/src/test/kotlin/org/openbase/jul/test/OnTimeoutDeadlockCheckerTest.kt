package org.openbase.jul.test

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.extension.ExtendWith
import org.openbase.jul.communication.mqtt.test.OpenbaseDeadlockChecker
import org.openbase.jul.schedule.GlobalCachedExecutorService

@ExtendWith(OpenbaseDeadlockChecker::class)
class OnTimeoutDeadlockCheckerTest {

    @Test
    @Timeout(1)
    @Disabled
    fun `should print stacktrace on timeout that contains all openbase threads`() {

        GlobalCachedExecutorService.submit {
            Thread.sleep(999999)
        }

        try {
            Thread.sleep(10000)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}
