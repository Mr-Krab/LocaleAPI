package sawfowl.localeapi.api.serializetools.itemstack;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.plugin.PluginContainer;

import com.google.gson.JsonObject;

public interface ComponentUtil {

	static String COMPONENTS = "components";
	static String CUSTOM_DATA = "minecraft:custom_data";
	static String PLUGINCOMPONENTS = "PluginComponents";

	/**
	 * Checks if a component is present in the item.
	 * 
	 * @param item - Checkable Item.
	 * @param plugin - The plugin to which the component belongs.
	 * @param key - Component Key.
	 * @return true if the component exists.
	 */
	static boolean containsPluginComponent(ItemStack item, PluginContainer plugin, String key) {
		return item.toContainer().contains(createPath(plugin.metadata().id(), key));
	}

	/**
	 * Adding a simple object to a subject. Only primitives are accepted!
	 * 
	 * @param container - The plugin to which the component belongs.
	 * @param key - Component Key.
	 * @param object - The object to be added as a component.
	 */
	<T> ComponentUtil putObject(PluginContainer container, String key, T object);

	/**
	 * Adding a list of objects to a subject. Only primitives are accepted!
	 * 
	 * @param container - The plugin to which the components belongs.
	 * @param key - Components Key.
	 * @param objects - The objects list to be added as a component.
	 */
	<T> ComponentUtil putObjects(PluginContainer container, String key, List<T> objects);

	/**
	 * Adding an objects map to a subject. Only primitives are accepted!
	 * 
	 * @param mapKey - Class defining the type of map key.
	 * @param mapValue - A class defining the type of the map value.
	 * @param container - The plugin to which the components belongs.
	 * @param key - Components Key.
	 * @param objects - Map of objects.
	 */
	<K, V> ComponentUtil putObjects(Class<K> mapKey, Class<V> mapValue, PluginContainer container, String key, Map<K, V> objects);

	/**
	 * Adding a complex component to an item.<br>
	 * This component must be a serializable class <br>
	 * and implement a method to create a {@link JsonObject}
	 * 
	 * @param container - The plugin to which the components belongs.
	 * @param key - Components Key.
	 * @param object - Serializable component.
	 */
	<T extends PluginComponent> ComponentUtil putPluginComponent(PluginContainer container, String key, T object);

	/**
	 * Removing a component from an item.
	 * 
	 * @param container - The plugin to which the components belongs.
	 * @param key - Components Key.
	 */
	ComponentUtil removeComponent(PluginContainer container, String key);

	/**
	 * Checks if a component is present in the item.
	 * 
	 * @param container - The plugin to which the components belongs.
	 * @param key - Components Key.
	 */
	boolean containsComponent(PluginContainer container, String key);

	/**
	 * Getting an object from an item.
	 * 
	 * @param container - The plugin to which the components belongs.
	 * @param key - Components Key.
	 * @param def - Default value.
	 * @return - Found object or default value.
	 */
	<T> T getObject(PluginContainer container, String key, T def);

	/**
	 * Getting a list of objects from an item.
	 * 
	 * @param clazz - The type of object being searched for.
	 * @param container - The plugin to which the components belongs.
	 * @param key - Components Key.
	 * @param def - Default value.
	 * @return - Found objects list or default value.
	 */
	<T> List<T> getObjectsList(Class<T> clazz, PluginContainer container, String key, List<T> def);

	/**
	 * Getting a map of objects from an item.
	 * 
	 * @param mapKey - Class defining the type of map key.
	 * @param mapValue - A class defining the type of the map value.
	 * @param container - The plugin to which the components belongs.
	 * @param key - Components Key.
	 * @param def - Default value.
	 * @return - Found objects map or default value.
	 */
	<K, V> Map<K, V> getObjectsMap(Class<K> mapKey, Class<V> mapValue, PluginContainer container, String key, Map<K, V> def);

	/**
	 * Getting a serialized component from an item.
	 * 
	 * @param clazz - Class of object.
	 * @param container - The plugin to which the components belongs.
	 * @param key - Components Key.
	 * @return - If the component is not found or the deserialization fails, {@link Optional#empty()} will be returned.
	 */
	<T extends PluginComponent> Optional<T> getPluginComponent(Class<T> clazz, PluginContainer container, String key);

	/**
	 * Getting the list of keys of all components related to the plugin.
	 * 
	 * @param container - Container of the plugin for which you want to get the list of keys.
	 */
	Set<String> getAllKeys(PluginContainer container);

	/**
	 * Getting the volume of components related to a certain plugin.
	 * 
	 * @param container - Plugin container for which you need to count the volume of installed components.
	 */
	int size(PluginContainer container);

	static DataQuery createPath(String plugin, String key) {
		return DataQuery.of(COMPONENTS, CUSTOM_DATA, PLUGINCOMPONENTS, plugin, key);
	}
}
