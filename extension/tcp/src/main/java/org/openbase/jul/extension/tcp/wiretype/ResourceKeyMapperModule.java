/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.jul.extension.tcp.wiretype;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.IOException;

/**
 *
 * @author divine
 */
public class ResourceKeyMapperModule extends SimpleModule {

	public ResourceKeyMapperModule(ObjectMapper mapper) {
		addKeySerializer(ResourceKey.class, new ResourceKeyDeserializer());
		addKeyDeserializer(ResourceKey.class, new ResourceKeySerializer());
		MapType myMapType = TypeFactory.defaultInstance().constructMapType(ResourceConfigMap.class, ResourceKey.class, AbstractResourceConfig.class);
		mapper.registerModule(this).writerWithType(myMapType);
	}

	public class ResourceKeyDeserializer extends JsonSerializer<ResourceKey> {

		@Override
		public void serialize(ResourceKey value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
			jgen.writeFieldName(value.getKey());
		}
	}

	public class ResourceKeySerializer extends KeyDeserializer {

		@Override
		public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			return new ResourceKey(key);
		}
	}
}