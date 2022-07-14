package org.openbase.jul.communication.exception;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.type.communication.mqtt.ResponseType.Response;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RPCResolvedExceptionTest {

    @Test
    public void testExceptionResolving() {
        final NotAvailableException thrownException = new NotAvailableException("error");
        final CouldNotPerformException wrappingException = new CouldNotPerformException("Could not execute internal task", thrownException);

        final String stackTrace = stackTraceToString(wrappingException);
        final Response response = Response.newBuilder().setError(stackTrace).build();

        final RPCException ex = new RPCException(response.getError(), null);

        final Exception resolveRPCException = RPCResolvedException.resolveRPCException(ex);

        assertEquals(CouldNotPerformException.class, resolveRPCException.getClass());
        assertEquals(wrappingException.getMessage(), resolveRPCException.getMessage());

        final Throwable cause = resolveRPCException.getCause();
        assertEquals(NotAvailableException.class, cause.getClass());
        assertEquals(thrownException.getMessage(), cause.getMessage());
    }

    /**
     * Print a stack trace to a string. This creates exactly the same result as the kotlin methods `stackTraceToString`
     * defined on throwables (https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/stack-trace-to-string.html).
     *
     * @param ex the exception from which the stack trace is printed
     * @return the stack trace as a string
     */
    private String stackTraceToString(final Exception ex) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        ex.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
