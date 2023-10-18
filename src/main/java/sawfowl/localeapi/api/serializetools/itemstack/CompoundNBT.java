package sawfowl.localeapi.api.serializetools.itemstack;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

public interface CompoundNBT {

	/**
	 * Deleting a tag.
	 */
	public void remove(String key);

	/**
	 * Checking for the presence of a tag.
	 */
	public boolean containsTag(String key);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putString(String key, String value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putUUID(String key, UUID value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putShort(String key, short value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putInteger(String key, int value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putLong(String key, long value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putFloat(String key, float value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putDouble(String key, double value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putByte(String key, byte value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putBoolean(String key, boolean value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putIntArray(String key, int[] value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putIntList(String key, List<Integer> value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putLongArray(String key, long[] value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putLongList(String key, List<Long> value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.
	 */
	public void putByteArray(String key, byte[] value);

	/**
	 * Adding a tag. If a tag with the specified key already exists, it will be overwritten.<br>
	 * The class must be marked with the {@link ConfigSerializable} annotation.<br>
	 * Only objects marked with the {@link Setting} annotation are saved.
	 */
	public void putTag(String key, CompoundTag tag);

	/**
	 * Getting all the keys. 
	 */
	public Set<String> getAllKeys();

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<String> getString(String key);

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<UUID> getUUID(String key);

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<Short> getShort(String key);

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<Integer> getInteger(String key);

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<Long> getLong(String key);

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<Float> getFloat(String key);

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<Double> getDouble(String key);

	/**Getting the tag value. The method does not check for the type of value.
	 * 
	 */
	public Optional<Byte> getByte(String key);

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<Boolean> getBoolean(String key);

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<int[]> getIntArray(String key);

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<List<Integer>> getIntList(String key);

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<long[]> getLongArray(String key);

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<List<Long>> getLongList(String key);

	/**
	 * Getting the tag value. The method does not check for the type of value.
	 */
	public Optional<byte[]> getByteArray(String key);

	/**
	 * Getting the tag value. If the object has been changed after receiving it, it must be rewritten.<br>
	 * Example of getting a value - <a href="https://github.com/SawFowl/LocaleTestPlugin/blob/3d3f53d8a520757a2a1a15ee213845deaa943b6b/src/main/java/sawfowl/localetest/LocaleTest.java#L135">GitHub</a>
	 */
	public <T extends CompoundTag> Optional<T> getTag(String key, Class<T> clazz);

	/**
	 * Getting the number of tags in ItemStack.
	 */
	public int size();

}
