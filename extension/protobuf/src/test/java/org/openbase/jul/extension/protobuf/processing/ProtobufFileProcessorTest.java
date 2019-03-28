package org.openbase.jul.extension.protobuf.processing;

import com.google.protobuf.ByteString;
import org.junit.Test;
import org.openbase.type.domotic.authentication.LoginCredentialsType.LoginCredentials;
import org.openbase.type.domotic.authentication.LoginCredentialsType.LoginCredentials.Builder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class ProtobufFileProcessorTest {

    /*@Test
    public void testByteArraySerialization() throws Exception {
        final ProtoBufFileProcessor<LoginCredentials, LoginCredentials, Builder> fileProcessor = new ProtoBufFileProcessor<>(LoginCredentials.newBuilder());

        final File testFile = new File("/tmp/byteArraySerialization");
        final String password = "12345678";
        final LoginCredentials loginCredentials = LoginCredentials.newBuilder()
                .setId("user_id")
                .setCredentials(ByteString.copyFrom(hash(password).)).build();
        fileProcessor.serialize(loginCredentials, testFile);

        assertEquals(loginCredentials, fileProcessor.deserialize(testFile));
    }*/

    private byte[] hash(String toHash) throws Exception {
        byte[] key = toHash.getBytes(StandardCharsets.UTF_8);
        String HASH_ALGORITHM = "SHA-256";
        MessageDigest sha = MessageDigest.getInstance(HASH_ALGORITHM);
        key = sha.digest(key);
        return Arrays.copyOf(key, 16);
    }
}
