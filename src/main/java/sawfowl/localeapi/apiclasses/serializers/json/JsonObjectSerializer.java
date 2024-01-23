package sawfowl.localeapi.apiclasses.serializers.json;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.math.NumberUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;

import sawfowl.localeapi.api.serializetools.SerializeOptions;

public class JsonObjectSerializer implements TypeSerializer<JsonObject> {

	final TypeAdapter<JsonElement> strictAdapter = new Gson().getAdapter(JsonElement.class);

	@Override
	public JsonObject deserialize(Type type, ConfigurationNode node) throws SerializationException {
		return createFromNode(findJsonObject(createWriter(new StringWriter()).createNode().from(node)));
	}

	@Override
	public void serialize(Type type, @Nullable JsonObject obj, ConfigurationNode node) throws SerializationException {
		serializeMapJsonElement(obj, node);
	}

	private void serializeMapJsonElement(JsonObject jsonObject, ConfigurationNode node) throws SerializationException {
		for(Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			if(entry.getValue().isJsonPrimitive()) {
				writePrimitive(entry.getValue().getAsJsonPrimitive(), node.node(entry.getKey()));
			} else if(entry.getValue().isJsonObject() && !entry.getValue().getAsJsonObject().asMap().isEmpty()) {
				serializeMapJsonElement(entry.getValue().getAsJsonObject(), node.node(entry.getKey()));
			} else if(entry.getValue().isJsonArray()) {
				serializeJsonArray(entry.getValue().getAsJsonArray(), node.node(entry.getKey()));
			}
		}
	}

	private void serializeJsonArray(JsonArray jsonArray, ConfigurationNode node) throws SerializationException {
		List<Object> toSet = new ArrayList<Object>();
		for(JsonElement element : jsonArray.asList()) {
			if(element.isJsonObject()) {
				toSet.add(getMapToWrite(element.getAsJsonObject().asMap()));
				if(!toSet.isEmpty()) node.set(toSet);
			} else if(element.isJsonArray()) {
				toSet.add(getListToWrite(element.getAsJsonArray()));
				if(!toSet.isEmpty()) node.set(toSet);
			} else if(element.isJsonObject()) {
				Map<String, Object> map = getMapToWrite(element.getAsJsonObject().asMap());
				if(!map.isEmpty()) node.set(Arrays.asList(map));
			} else if(element.isJsonPrimitive() && getObjectToWrite(element.getAsJsonPrimitive()) != null) {
				toSet.add(getObjectToWrite(element.getAsJsonPrimitive()));
				if(isJsonString(element.getAsJsonPrimitive().getAsString())) {
					if(!toSet.isEmpty()) node.node("SerializedJsonObject").set(toSet);
				} else {
					if(!toSet.isEmpty()) node.set(toSet);
				}
			}
		}
	}

	private void writePrimitive(JsonPrimitive primitive, ConfigurationNode node) throws SerializationException {
		if(primitive.isNumber()) {
			node.set(convertPrimitiveNumber(primitive.getAsNumber()));
		} else if(primitive.isBoolean()) {
			node.set(primitive.getAsBoolean());
		} else if(primitive.isString()) {
			if(isJsonString(primitive.getAsString())) {
				serializeMapJsonElement(JsonParser.parseString(primitive.getAsString()).getAsJsonObject(), node.node("SerializedJsonObject"));
			} else if(primitive.getAsString().length() == 1) {
				node.set(primitive.getAsString().charAt(0));
			} else node.set(primitive.getAsString());
		}
	
	}

	private boolean isJsonString(String string) {
		try {
			strictAdapter.fromJson(string);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	private Map<String, Object> getMapToWrite(Map<String, JsonElement> jsonMap) {
		Map<String, Object> map = new HashMap<String, Object>();
		jsonMap.forEach((k, v) -> {
			if(v.isJsonPrimitive() && getObjectToWrite(v.getAsJsonPrimitive()) != null) {
				map.put(k, getObjectToWrite(v.getAsJsonPrimitive()));
			} else if(v.isJsonArray()) {
				map.put(k, getListToWrite(v.getAsJsonArray()));
			} else if(v.isJsonObject()) {
				Map<String, Object> map2 = getMapToWrite(v.getAsJsonObject().asMap());
				if(!map2.isEmpty())map.put(k, map);
			}
		});
		return map;
	}

	private List<Object> getListToWrite(JsonArray jsonArray) {
		List<Object> list = new ArrayList<Object>();
		jsonArray.forEach(element -> {
			if(element.isJsonArray()) list.add(getListToWrite(element.getAsJsonArray()));
			if(element.isJsonObject()) list.add(getMapToWrite(element.getAsJsonObject().asMap()));
			if(element.isJsonPrimitive() && getObjectToWrite(element.getAsJsonPrimitive()) != null) list.add(getObjectToWrite(element.getAsJsonPrimitive()));
		});
		return list;
	}

	private Object getObjectToWrite(JsonPrimitive primitive) {
		if(primitive.isNumber()) return convertPrimitiveNumber(primitive.getAsNumber());
		if(primitive.isBoolean()) return primitive.getAsBoolean();
		if(primitive.isString()) {
			if(isJsonString(primitive.getAsString())) {
				return getMapToWrite(JsonParser.parseString(primitive.getAsString()).getAsJsonObject().asMap());
			} else if(primitive.getAsString().length() == 1) {
				return primitive.getAsString().charAt(0);
			} else return primitive.getAsString();
		}
		return null;
	}

	private JsonObject createFromNode(ConfigurationNode node) {
		try {
			return JsonParser.parseString(toJsonString(node)).getAsJsonObject();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		}
		return new JsonObject();
	}

	private String toJsonString(ConfigurationNode node) {
		return new Gson().toJson(node.raw());
	}

	private static Number convertPrimitiveNumber(Number number) {
		return NumberUtils.createNumber(number.toString());
	}

	private ConfigurationNode findJsonObject(ConfigurationNode node) {
		if(node.isMap()) {
			node.childrenMap().values().forEach(this::findJsonObject);
		} else if(node.isList()) {
			node.childrenList().forEach(this::findJsonObject);
		}
		if(node.key() != null && node.key().equals("SerializedJsonObject")) {
			try {
				if(node.isList()) {
					node.parent().setList(String.class, node.childrenList().stream().map(this::toJsonString).toList());
				} else node.parent().set(createFromNode(node).toString());
			} catch (ConfigurateException | JsonSyntaxException e) {
				e.printStackTrace();
			}
		}
		return node;
	}

	private static GsonConfigurationLoader createWriter(StringWriter sink) {
		return GsonConfigurationLoader.builder().defaultOptions(SerializeOptions.selectOptions(2)).sink(() -> new BufferedWriter(sink)).build();
	}

}
