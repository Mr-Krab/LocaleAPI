package sawfowl.localeapi.serializetools;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * This interface is for creating your own NBT tags.
 */
@ConfigSerializable
public abstract class CompoundTag {

	/**
	 * Getting a class to deserialize a custom tag.
	 * 
	 * @param clazz - The class is a descendant of CompoundTag.
	 */
	@SuppressWarnings({"unchecked" }) 
	public static Class<CompoundTag> getClass(Class<?> clazz) {
		try {
			return (Class<CompoundTag>) (Object) clazz;
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		return null;
	}

}
