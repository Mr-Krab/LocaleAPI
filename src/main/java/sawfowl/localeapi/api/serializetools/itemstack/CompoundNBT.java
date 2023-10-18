package sawfowl.localeapi.api.serializetools.itemstack;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.plugin.PluginContainer;

public interface CompoundNBT {

	/**
	 * Deleting a tag.
	 */
	public void remove(PluginContainer container, String key);

	/**
	 * Checking for the presence of a tag.
	 */
	public boolean containsTag(PluginContainer container, String key);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putString(PluginContainer container, String key, String value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putUUID(PluginContainer container, String key, UUID value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putShort(PluginContainer container, String key, short value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putInteger(PluginContainer container, String key, int value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putLong(PluginContainer container, String key, long value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putFloat(PluginContainer container, String key, float value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putDouble(PluginContainer container, String key, double value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putByte(PluginContainer container, String key, byte value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putBoolean(PluginContainer container, String key, boolean value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putIntArray(PluginContainer container, String key, int[] value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putIntList(PluginContainer container, String key, List<Integer> value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putLongArray(PluginContainer container, String key, long[] value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putLongList(PluginContainer container, String key, List<Long> value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putByteArray(PluginContainer container, String key, byte[] value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.<br>
	 * The class must be marked with the {@link ConfigSerializable} annotation.<br>
	 * Only objects marked with the {@link Setting} annotation are saved.
	 */
	public void putTag(PluginContainer container, String key, CompoundTag tag);

	/**
	 * Getting all the keys. 
	 */
	public Set<String> getAllKeys(PluginContainer container);

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<String> getString(PluginContainer container, String key);

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<UUID> getUUID(PluginContainer container, String key);

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<Short> getShort(PluginContainer container, String key);

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<Integer> getInteger(PluginContainer container, String key);

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<Long> getLong(PluginContainer container, String key);

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<Float> getFloat(PluginContainer container, String key);

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<Double> getDouble(PluginContainer container, String key);

	/**Getting the tag value. The method does not check for the type of value.
	 * 
	 */
	public Optional<Byte> getByte(PluginContainer container, String key);

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<Boolean> getBoolean(PluginContainer container, String key);

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<int[]> getIntArray(PluginContainer container, String key);

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<List<Integer>> getIntList(PluginContainer container, String key);

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<long[]> getLongArray(PluginContainer container, String key);

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<List<Long>> getLongList(PluginContainer container, String key);

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<byte[]> getByteArray(PluginContainer container, String key);

	/**
	 * Getting the tag value. If the object has been changed after receiving it, it must be rewritten.<br>
	 * Example of getting a value - <a href="https://github.com/SawFowl/LocaleTestPlugin/blob/3d3f53d8a520757a2a1a15ee213845deaa943b6b/src/main/java/sawfowl/localetest/LocaleTest.java#L135">GitHub</a>
	 */
	public <T extends CompoundTag> Optional<T> getTag(PluginContainer container, String key, Class<T> clazz);

	/**
	 * Getting the number of tags in ItemStack.
	 */
	public int size(PluginContainer container);

}
