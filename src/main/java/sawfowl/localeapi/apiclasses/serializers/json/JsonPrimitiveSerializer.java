package sawfowl.localeapi.apiclasses.serializers.json;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class JsonPrimitiveSerializer implements TypeSerializer<JsonPrimitive> {

	@Override
	public JsonPrimitive deserialize(Type type, ConfigurationNode node) throws SerializationException {
		return null;
	}

	@Override
	public void serialize(Type type, @Nullable JsonPrimitive primitive, ConfigurationNode node) throws SerializationException {
		writePrimitive(primitive, node);
	}

	private void serializeMapJsonElement(Map<String, JsonElement> jsonMap, ConfigurationNode root) throws SerializationException {
		for(Entry<String, JsonElement> entry : jsonMap.entrySet()) {
			if(entry.getValue().isJsonPrimitive()) {
				writePrimitive(entry.getValue().getAsJsonPrimitive(), root.node(entry.getKey()));
			} else if(entry.getValue().isJsonObject() && !entry.getValue().getAsJsonObject().asMap().isEmpty()) {
				serializeMapJsonElement(entry.getValue().getAsJsonObject().asMap(), root.node(entry.getKey()));
			} else if(entry.getValue().isJsonArray()) {
				serializeJsonArray(entry.getValue().getAsJsonArray(), root.node(entry.getKey()));
			}
		}
	}

	private void serializeJsonArray(JsonArray jsonArray, ConfigurationNode node) throws SerializationException {
		List<Object> toSet = new ArrayList<Object>();
		for(JsonElement element : jsonArray.asList()) {
			if(element.isJsonArray()) {
				toSet.add(getListToWrite(element.getAsJsonArray()));
			}
			if(element.isJsonObject()) {
				toSet.add(getMapToWrite(element.getAsJsonObject().asMap()));
			}
			if(element.isJsonPrimitive() && getObjectToWrite(element.getAsJsonPrimitive()) != null) {
				toSet.add(getObjectToWrite(element.getAsJsonPrimitive()));
			}
		}
		node.set(toSet);
	}

	private void writePrimitive(JsonPrimitive primitive, ConfigurationNode node) throws SerializationException {
		if(primitive.isNumber()) node.set(primitive.getAsNumber());
		if(primitive.isBoolean()) node.set(primitive.getAsBoolean());
		if(primitive.isString()) {
			if(isMapedString(primitive.getAsString())) {
				JsonObject nbtJson = JsonParser.parseString(primitive.getAsString()).getAsJsonObject();
				serializeMapJsonElement(nbtJson.asMap(), node);
			} else if(primitive.getAsString().length() == 1) {
				node.set(primitive.getAsString().charAt(0));
			} else node.set(primitive.getAsString());
		}
		if(primitive.isNumber()) node.set(primitive.getAsNumber());
	
	}

	private boolean isMapedString(String string) {
		return string != null && string.contains("{") && string.contains("}");
	}

	private Map<String, Object> getMapToWrite(Map<String, JsonElement> jsonMap) {
		Map<String, Object> map = new HashMap<String, Object>();
		jsonMap.forEach((k, v) -> {
			if(v.isJsonPrimitive() && getObjectToWrite(v.getAsJsonPrimitive()) != null) map.put(k, getObjectToWrite(v.getAsJsonPrimitive()));
			if(v.isJsonArray()) map.put(k, getListToWrite(v.getAsJsonArray()));
			if(v.isJsonObject()) map.put(k, getMapToWrite(v.getAsJsonObject().asMap()));
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
		if(primitive.isNumber()) return primitive.getAsNumber();
		if(primitive.isBoolean()) return primitive.getAsBoolean();
		if(primitive.isString()) {
			if(isMapedString(primitive.getAsString())) {
				JsonObject nbtJson = JsonParser.parseString(primitive.getAsString()).getAsJsonObject();
				return nbtJson.asMap();
			} else if(primitive.getAsString().length() == 1) {
				return primitive.getAsString().charAt(0);
			} else return primitive.getAsString();
		}
		return null;
	}

}
