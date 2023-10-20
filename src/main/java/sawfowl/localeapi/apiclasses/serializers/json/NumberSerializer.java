package sawfowl.localeapi.apiclasses.serializers.json;

import java.lang.reflect.Type;

import org.apache.commons.lang3.math.NumberUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

public class NumberSerializer implements TypeSerializer<Number> {

	@Override
	public Number deserialize(Type type, ConfigurationNode node) throws SerializationException {
		if(NumberUtils.isCreatable(node.raw().toString())) return NumberUtils.createNumber(node.raw().toString());
		return 0;
	}

	@Override
	public void serialize(Type type, @Nullable Number number, ConfigurationNode node) throws SerializationException {
		
	}

}
