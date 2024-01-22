package sawfowl.localeapi.api.serializetools.itemstack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.plugin.PluginContainer;

public interface TagUtil {

	public <T> void putObject(PluginContainer container, String key, T object);

	public <T extends CompoundTag> void putCompoundTag(PluginContainer container, String key, T object);

	public boolean containsTag(PluginContainer container, String key);

	public void removeTag(PluginContainer container, String key);

	public <T> T getObject(Class<T> clazz, PluginContainer container, String key, T def);

	public <T> List<T> getObjectsList(Class<T> clazz, PluginContainer container, String key, List<T> def);

	public <T extends CompoundTag> Optional<T> getCompoundTag(Class<T> clazz, PluginContainer container, String key);

	public Set<String> getAllKeys(PluginContainer container);

	public int size(PluginContainer container);

	default <T> T getObject(Class<T> clazz, PluginContainer container, String key) {
		return getObject(clazz, container, key, null);
	}

	default <T> List<T> getObjectsList(Class<T> clazz, PluginContainer container, String key) {
		return getObjectsList(clazz, container, key, new ArrayList<T>());
	}

	@SuppressWarnings("unchecked")
	default <T> T[] getObjectsArray(Class<T> clazz, PluginContainer container, String key, T[] def) {
		return getObjectsList(clazz, container, key, Arrays.asList(def)).stream().filter(object -> object != null).toArray(generator -> (T[]) new Object[] {});
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

}
