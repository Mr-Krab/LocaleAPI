package sawfowl.localeapi.apiclasses.serializers.json;

import java.lang.reflect.Type;

import org.checkerframework.checker.nullness.qual.Nullable;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import com.google.gson.JsonPrimitive;

import sawfowl.localeapi.apiclasses.serializers.JsonCollectionSerializers;

public class JsonPrimitiveSerializer implements TypeSerializer<JsonPrimitive> {

	@Override
	public JsonPrimitive deserialize(Type type, ConfigurationNode node) throws SerializationException {
		return JsonCollectionSerializers.loadPrimitive(node);
	}

	@Override
	public void serialize(Type type, @Nullable JsonPrimitive primitive, ConfigurationNode node) throws SerializationException {
		JsonCollectionSerializers.writePrimitive(primitive, node);
	}

}
