package sawfowl.localeapi.api.serializetools.itemstack;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.plugin.PluginContainer;

public interface ComponentUtil {

	static String COMPONENTS = "components";
	static String CUSTOM_DATA = "minecraft:custom_data";
	static String PLUGINCOMPONENTS = "PluginComponents";

	static boolean containsPluginComponent(ItemStack item, PluginContainer plugin, String key) {
		return item.toContainer().contains(createPath(plugin.metadata().id(), key));
	}

	<T> ComponentUtil putObject(PluginContainer container, String key, T object);

	<T> ComponentUtil putObjects(PluginContainer container, String key, List<T> objects);

	<K, V> ComponentUtil putObjects(Class<K> mapKey, Class<V> mapValue, PluginContainer container, String key, Map<K, V> objects);

	<T extends PluginComponent> ComponentUtil putPluginComponent(PluginContainer container, String key, T object);

	ComponentUtil removeComponent(PluginContainer container, String key);

	boolean containsComponent(PluginContainer container, String key);

	<T> T getObject(PluginContainer container, String key, T def);

	<T> List<T> getObjectsList(Class<T> clazz, PluginContainer container, String key, List<T> def);

	<K, V> Map<K, V> getObjectsMap(Class<K> mapKey, Class<V> mapValue, PluginContainer container, String key, Map<K, V> def);

	<T extends PluginComponent> Optional<T> getPluginComponent(Class<T> clazz, PluginContainer container, String key);

	Set<String> getAllKeys(PluginContainer container);

	int size(PluginContainer container);

	static DataQuery createPath(String plugin, String key) {
		return DataQuery.of(COMPONENTS, CUSTOM_DATA, PLUGINCOMPONENTS, plugin, key);
	}
}
