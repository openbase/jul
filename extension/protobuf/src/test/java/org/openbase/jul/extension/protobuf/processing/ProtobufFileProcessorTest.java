package org.openbase.jul.extension.protobuf.processing;

/*-
 * #%L
 * JUL Extension Protobuf
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;
import org.openbase.type.domotic.authentication.LoginCredentialsType.LoginCredentials;
import org.openbase.type.domotic.authentication.LoginCredentialsType.LoginCredentials.Builder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class ProtobufFileProcessorTest {

    @Test
    public void testByteArraySerialization() throws Exception {
        final ProtoBufFileProcessor<LoginCredentials, LoginCredentials, Builder> fileProcessor = new ProtoBufFileProcessor<>(LoginCredentials.newBuilder());

        final File testFile = new File("/tmp/byteArraySerialization");
        final String password = "12345678";
        final LoginCredentials loginCredentials = LoginCredentials.newBuilder()
                .setId("user_id")
                .setCredentials(ByteString.copyFrom(hash(password))).build();
        fileProcessor.serialize(loginCredentials, testFile);

        assertEquals(loginCredentials, fileProcessor.deserialize(testFile));
    }

    private byte[] hash(String toHash) throws Exception {
        byte[] key = toHash.getBytes(StandardCharsets.UTF_16);
        String HASH_ALGORITHM = "SHA-256";
        MessageDigest sha = MessageDigest.getInstance(HASH_ALGORITHM);
        key = sha.digest(key);
        return Arrays.copyOf(key, 16);
    }
}
