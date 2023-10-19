package sawfowl.localeapi.apiclasses.serializers;

import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import com.google.gson.JsonPrimitive;

import io.leangen.geantyref.TypeToken;

import sawfowl.localeapi.apiclasses.serializers.json.JsonPrimitiveSerializer;

public class JsonCollectionSerializers {

	public static final TypeSerializerCollection SERIALIZERS = TypeSerializerCollection.builder().register(TypeToken.get(JsonPrimitive.class), new JsonPrimitiveSerializer()).build();

}
