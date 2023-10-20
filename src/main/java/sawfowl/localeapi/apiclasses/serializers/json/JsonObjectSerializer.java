package sawfowl.localeapi.apiclasses.serializers.json;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;

import org.checkerframework.checker.nullness.qual.Nullable;

import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import sawfowl.localeapi.api.serializetools.SerializeOptions;
import sawfowl.localeapi.apiclasses.serializers.JsonCollectionSerializers;

public class JsonObjectSerializer implements TypeSerializer<JsonObject> {

	@Override
	public JsonObject deserialize(Type type, ConfigurationNode node) throws SerializationException {
		//ConfigurationNode copyNode = createWriter(new StringWriter()).createNode().from(node);
		findJsonObject(node);
		return JsonCollectionSerializers.createFromNode(node);
	}

	@Override
	public void serialize(Type type, @Nullable JsonObject obj, ConfigurationNode node) throws SerializationException {
		JsonCollectionSerializers.serializeMapJsonElement(obj, node);
	}

	private void findJsonObject(ConfigurationNode node) {
		findJsonConfig(node);
		if(node.isMap()) {
			node.childrenMap().values().forEach(this::findJsonObject);
		} else if(node.isList()) {
			node.childrenList().forEach(this::findJsonObject);
		}
		if(node.key() != null && node.key().equals("JsonObject")) {
			try {
				if(node.isList()) {
					node.parent().setList(String.class, node.childrenList().stream().map(JsonCollectionSerializers::toJsonString).toList());
				} else node.parent().set(JsonCollectionSerializers.createFromNode(node).toString());
			} catch (ConfigurateException | JsonSyntaxException e) {
				e.printStackTrace();
			}
		}
	}

	private void findJsonConfig(ConfigurationNode node) {
		if(node.isMap()) {
			node.childrenMap().values().forEach(this::findJsonConfig);
		} else if(node.isList()) {
			node.childrenList().forEach(this::findJsonConfig);
		}
		if(node.key() != null && node.key().equals("JsonConfig")) {
			StringWriter sink = new StringWriter();
			GsonConfigurationLoader loader = createWriter(sink);
			ConfigurationNode tempNode = loader.createNode();
			try {
				tempNode.set(node.raw().toString());
				loader.save(tempNode);
				node.parent().set(sink.toString());
			} catch (ConfigurateException e) {
				e.printStackTrace();
			}
			sink = null;
			loader = null;
			tempNode = null;
		}
	}

	private static GsonConfigurationLoader createWriter(StringWriter sink) {
		return GsonConfigurationLoader.builder().defaultOptions(SerializeOptions.selectOptions(2)).sink(() -> new BufferedWriter(sink)).build();
	}

}
