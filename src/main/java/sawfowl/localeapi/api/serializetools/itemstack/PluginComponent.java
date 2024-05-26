package sawfowl.localeapi.api.serializetools.itemstack;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import com.google.gson.JsonObject;

/**
 * This interface is for creating your own NBT tags.
 */
@ConfigSerializable
public interface PluginComponent {

	/**
	 * Convert class objects to json data array.<br>
	 * It is recommended not to do `return null`.
	 */
	JsonObject toJsonObject();

}
