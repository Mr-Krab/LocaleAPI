package sawfowl.localeapi.api.serializetools.itemstack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.plugin.PluginContainer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import sawfowl.localeapi.api.TextUtils;

public interface TagUtil {

	public <T extends CompoundTag> void putCompoundTag(PluginContainer container, String key, T object);

	<T extends CompoundTag> Optional<T> getCompoundTag(Class<T> clazz, PluginContainer container, String key);

	boolean containsTag(PluginContainer container, String key);

	public void removeTag(PluginContainer container, String key);

	public int size(PluginContainer container);

	interface Json extends TagUtil {

		void putJsonElement(PluginContainer container, String key, JsonElement object);

		JsonElement getJsonObject(PluginContainer container, String key);

		ConfigurationNode getAsConfigurationNode(PluginContainer container);

		default void putComponent(PluginContainer container, String key, Component component) {
			putJsonElement(container, key, JsonParser.parseString(GsonComponentSerializer.gson().serialize(component)));
		}

		default void putComponents(PluginContainer container, String key, List<Component> components) {
			JsonArray array = new JsonArray();
			components.forEach(component -> array.add(JsonParser.parseString(GsonComponentSerializer.gson().serialize(component))));
			putJsonElement(container, key, array);
		}

		default void putComponents(PluginContainer container, String key, Component... components) {
			putComponents(container, key, Arrays.asList(components));
		}

		default String getString(PluginContainer container, String key) {
			JsonElement element = getJsonObject(container, key);
			return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString() ? element.getAsString() : null;
		}

		default Number getNumber(PluginContainer container, String key) {
			JsonElement element = getJsonObject(container, key);
			return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber() ? element.getAsNumber() : null;
		}

		default Boolean getBoolean(PluginContainer container, String key) {
			JsonElement element = getJsonObject(container, key);
			return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean() ? element.getAsBoolean() : null;
		}

		default Component getComponent(PluginContainer container, String key) {
			JsonElement tag = getJsonObject(container, key);
			return tag != null && tag.isJsonObject() || (tag.isJsonPrimitive() && tag.getAsJsonPrimitive().isString()) ? TextUtils.deserialize(tag.toString()) : null;
		}

		default List<Component> getComponents(PluginContainer container, String key) {
			JsonElement tag = getJsonObject(container, key);
			return tag.isJsonArray() ? tag.getAsJsonArray().asList().stream().map(json -> json.isJsonObject() || (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) ? GsonComponentSerializer.gson().deserialize(json.toString()) : Component.empty()).toList() : null;
		}

		default ConfigurationNode getAsConfigurationNode(PluginContainer container, String key) {
			return getAsConfigurationNode(container).node(key);
		}

	}

	interface Advanced extends TagUtil {

		public <T> void putObject(PluginContainer container, String key, T object);

		public <T> void putObjects(PluginContainer container, String key, List<T> objects);

		public <K, V> void putObjects(Class<K> mapKey, Class<V> mapValue, PluginContainer container, String key, Map<K, V> objects);

		public <T> T getObject(Class<T> clazz, PluginContainer container, String key, T def);

		public <T> List<T> getObjectsList(Class<T> clazz, PluginContainer container, String key, List<T> def);

		public <K, V> Map<K, V> getObjectsMap(Class<K> mapKey, Class<V> mapValue, PluginContainer container, String key, Map<K, V> objects);

		public <T extends CompoundTag> Optional<T> getCompoundTag(Class<T> clazz, PluginContainer container, String key);

		public Set<String> getAllKeys(PluginContainer container);

		default <T> void putObjects(Class<T> clazz, PluginContainer container, String key, @SuppressWarnings("unchecked") T... objects) {
			putObjects(container, key, Arrays.asList(objects));
		}

		default <T> T getObject(Class<T> clazz, PluginContainer container, String key) {
			return getObject(clazz, container, key, null);
		}

		default <T> List<T> getObjectsList(Class<T> clazz, PluginContainer container, String key) {
			return getObjectsList(clazz, container, key, new ArrayList<T>());
		}

		@SuppressWarnings("unchecked")
		default <T> T[] getObjectsArray(Class<T> clazz, PluginContainer container, String key, T[] def) {
			return !containsTag(container, key) ? def : getObjectsList(clazz, container, key, Arrays.asList(def)).stream().filter(object -> object != null).toArray(generator -> (T[]) new Object[] {});
		}

		default <T> T[] getObjectsArray(Class<T> clazz, PluginContainer container, String key) {
			return getObjectsArray(clazz, container, key, null);
			
		}

		default String getString(PluginContainer container, String key) {
			return getObject(String.class, container, key);
		}

		default UUID getUUID(PluginContainer container, String key) {
			return getObject(UUID.class, container, key);
		}

		default Short getShort(PluginContainer container, String key) {
			return getObject(Short.class, container, key);
		}

		default Integer getInteger(PluginContainer container, String key) {
			return getObject(Integer.class, container, key);
		}

		default Long getLong(PluginContainer container, String key) {
			return getObject(Long.class, container, key);
		}

		default Float getFloat(PluginContainer container, String key) {
			return getObject(Float.class, container, key);
		}

		default Double getDouble(PluginContainer container, String key) {
			return getObject(Double.class, container, key);
		}

		default Byte getByte(PluginContainer container, String key) {
			return getObject(Byte.class, container, key);
		}

		default Boolean getBoolean(PluginContainer container, String key) {
			return getObject(Boolean.class, container, key);
		}

		default JsonObject getJsonObject(PluginContainer container, String key) {
			return getObject(JsonObject.class, container, key);
		}

		default Component getComponent(PluginContainer container, String key) {
			return getObject(Component.class, container, key);
		}

		default String[] getStringArray(PluginContainer container, String key) {
			return getObjectsArray(String.class, container, key, new String[] {});
		}

		default UUID[] getUUIDArray(PluginContainer container, String key) {
			return getObjectsArray(UUID.class, container, key, new UUID[] {});
		}

		default Short[] getShortArray(PluginContainer container, String key) {
			return getObjectsArray(Short.class, container, key, new Short[] {});
		}

		default Integer[] getIntegerArray(PluginContainer container, String key) {
			return getObjectsArray(Integer.class, container, key, new Integer[] {});
		}

		default Long[] getLongArray(PluginContainer container, String key) {
			return getObjectsArray(Long.class, container, key, new Long[] {});
		}

		default Float[] getFloatArray(PluginContainer container, String key) {
			return getObjectsArray(Float.class, container, key, new Float[] {});
		}

		default Double[] getDoubleArray(PluginContainer container, String key) {
			return getObjectsArray(Double.class, container, key, new Double[] {});
		}

		default Byte[] getByteArray(PluginContainer container, String key) {
			return getObjectsArray(Byte.class, container, key, new Byte[] {});
		}

		default JsonObject[] getJsonObjectArray(PluginContainer container, String key) {
			return getObjectsArray(JsonObject.class, container, key);
		}

		default Component[] getComponentArray(PluginContainer container, String key) {
			return getObjectsArray(Component.class, container, key);
		}

		default List<String> getStringList(PluginContainer container, String key) {
			return getObjectsList(String.class, container, key);
		}

		default List<UUID> getUUIDList(PluginContainer container, String key) {
			return getObjectsList(UUID.class, container, key);
		}

		default List<Short> getShortList(PluginContainer container, String key) {
			return getObjectsList(Short.class, container, key);
		}

		default List<Integer> getIntegerList(PluginContainer container, String key) {
			return getObjectsList(Integer.class, container, key);
		}

		default List<Long> getLongList(PluginContainer container, String key) {
			return getObjectsList(Long.class, container, key);
		}

		default List<Float> getFloatList(PluginContainer container, String key) {
			return getObjectsList(Float.class, container, key);
		}

		default List<Double> getDoubleList(PluginContainer container, String key) {
			return getObjectsList(Double.class, container, key);
		}

		default List<Byte> getByteList(PluginContainer container, String key) {
			return getObjectsList(Byte.class, container, key);
		}

		default List<JsonObject> getJsonObjectList(PluginContainer container, String key) {
			return getObjectsList(JsonObject.class, container, key);
		}

		default List<Component> getComponentList(PluginContainer container, String key) {
			return getObjectsList(Component.class, container, key);
		}

	}

}
