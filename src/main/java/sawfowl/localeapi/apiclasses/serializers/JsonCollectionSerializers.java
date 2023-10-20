package sawfowl.localeapi.apiclasses.serializers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.math.NumberUtils;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import sawfowl.localeapi.apiclasses.serializers.json.JsonObjectSerializer;
import sawfowl.localeapi.apiclasses.serializers.json.JsonPrimitiveSerializer;
import sawfowl.localeapi.apiclasses.serializers.json.NumberSerializer;

public class JsonCollectionSerializers {

	public static final TypeSerializerCollection SERIALIZERS = TypeSerializerCollection.builder().register(Number.class, new NumberSerializer()).register(JsonPrimitive.class, new JsonPrimitiveSerializer()).register(JsonObject.class, new JsonObjectSerializer()).build();

	public static void serializeMapJsonElement(JsonObject jsonObject, ConfigurationNode node) throws SerializationException {
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

	public static void serializeJsonArray(JsonArray jsonArray, ConfigurationNode node) throws SerializationException {
		List<Object> toSet = new ArrayList<Object>();
		for(JsonElement element : jsonArray.asList()) {
			if(element.isJsonArray()) {
				toSet.add(getListToWrite(element.getAsJsonArray()));
				if(!toSet.isEmpty()) node.set(toSet);
			} else if(element.isJsonObject()) {
				Map<String, Object> map = getMapToWrite(element.getAsJsonObject().asMap());
				if(!map.isEmpty()) node.set(Arrays.asList(map));
			} else if(element.isJsonPrimitive() && getObjectToWrite(element.getAsJsonPrimitive()) != null) {
				toSet.add(getObjectToWrite(element.getAsJsonPrimitive()));
				if(isMapedString(element.getAsJsonPrimitive().getAsString())) {
					if(!toSet.isEmpty()) node.node(element.getAsString().contains("    ") ? "JsonConfig" : "JsonObject").set(toSet);
				} else {
					if(!toSet.isEmpty()) node.set(toSet);
				}
			}
		}
	}

	public static void writePrimitive(JsonPrimitive primitive, ConfigurationNode node) throws SerializationException {
		if(primitive.isNumber()) {
			node.set(primitive.getAsNumber());
		} else if(primitive.isBoolean()) {
			node.set(primitive.getAsBoolean());
		} else if(primitive.isString()) {
			if(isMapedString(primitive.getAsString())) {
				serializeMapJsonElement(JsonParser.parseString(primitive.getAsString()).getAsJsonObject(), node.node(primitive.getAsString().contains("    ") ? "JsonConfig" : "JsonObject"));
			} else if(primitive.getAsString().length() == 1) {
				node.set(primitive.getAsString().charAt(0));
			} else node.set(primitive.getAsString());
		}
	
	}

	public static boolean isMapedString(String string) {
		return string != null && string.contains("{") && string.contains("}");
	}

	public static Map<String, Object> getMapToWrite(Map<String, JsonElement> jsonMap) {
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

	public static List<Object> getListToWrite(JsonArray jsonArray) {
		List<Object> list = new ArrayList<Object>();
		jsonArray.forEach(element -> {
			if(element.isJsonArray()) list.add(getListToWrite(element.getAsJsonArray()));
			if(element.isJsonObject()) list.add(getMapToWrite(element.getAsJsonObject().asMap()));
			if(element.isJsonPrimitive() && getObjectToWrite(element.getAsJsonPrimitive()) != null) list.add(getObjectToWrite(element.getAsJsonPrimitive()));
		});
		return list;
	}

	public static Object getObjectToWrite(JsonPrimitive primitive) {
		if(primitive.isNumber()) return primitive.getAsNumber();
		if(primitive.isBoolean()) return primitive.getAsBoolean();
		if(primitive.isString()) {
			if(isMapedString(primitive.getAsString())) {
				return getMapToWrite(JsonParser.parseString(primitive.getAsString()).getAsJsonObject().asMap());
			} else if(primitive.getAsString().length() == 1) {
				return primitive.getAsString().charAt(0);
			} else return primitive.getAsString();
		}
		return null;
	}

	public static JsonPrimitive loadPrimitive(ConfigurationNode node) {
		if(node.raw().toString().equals("true") || node.raw().toString().equals("false")) return new JsonPrimitive(Boolean.getBoolean(node.raw().toString()));
		if(NumberUtils.isCreatable(node.raw().toString())) return new JsonPrimitive(NumberUtils.createNumber(node.raw().toString()));
		if(node.raw().toString().length() == 1) return new JsonPrimitive(node.raw().toString().charAt(0));
		return new JsonPrimitive(node.raw().toString());
	}

	public static JsonObject createFromNode(ConfigurationNode node) {
		try {
			return JsonParser.parseString(toJsonString(node)).getAsJsonObject();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		}
		return new JsonObject();
	}

	public static String toJsonString(ConfigurationNode node) {
		return new Gson().toJson(node.raw());
	}

}
