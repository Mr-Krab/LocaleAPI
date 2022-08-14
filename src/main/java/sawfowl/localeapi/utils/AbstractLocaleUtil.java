package sawfowl.localeapi.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.spongepowered.configurate.ConfigurationNode;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;

public abstract class AbstractLocaleUtil {

	/**
	 * Reload locale file
	 */
	public abstract void reload();

	/**
	 * Saving the localization file.
	 */
	public abstract void saveLocaleNode();

	/**
	 * Getting data from the localization file.
	 * 
	 * @return ConfigurationNode or CommentedConfigurationNode
	 */
	public abstract ConfigurationNode getLocaleRootNode();

	/**
	 * Getting data from the localization file.
	 * 
	 * @return ConfigurationNode or CommentedConfigurationNode
	 */
	public abstract ConfigurationNode getLocaleNode(Object... path);

	/**
	 * Getteng deserialized {@link Component} from locale configuration node.
	 * 
	 * @param json - If true, then the {@link Component} will be received from the JSON string.
	 * @param path - Path in the config file.
	 * @return {@link Component}
	 */
	public abstract Component getComponent(boolean json, Object... path);

	/**
	 * Getteng deserialized {@link Component} from locale configuration node.<br>
	 * After receiving the object, the elements are replaced according to the map. <br>
	 * The key of the map must be an element contained in the string. The key in the string will be replaced by the value.
	 * 
	 * @param map - Map for replacing elements from a line in the configuration.
	 * @param json - If true, then the {@link Component} will be received from the JSON string.
	 * @param path - Path in the config file.
	 * @return {@link Component}
	 */
	public Component getComponentReplaced1(Map<String, String> map, boolean json, Object... path) {
		return replace(getComponent(json, path), map);
	}

	/**
	 * Getteng deserialized {@link Component} from locale configuration node.<br>
	 * After receiving the object, the elements are replaced according to the map. <br>
	 * The key of the map must be an element contained in the string. The key in the string will be replaced by the value.
	 * 
	 * @param map - Map for replacing elements from a line in the configuration.
	 * @param json - If true, then the {@link Component} will be received from the JSON string.
	 * @param path - Path in the config file.
	 * @return {@link Component}
	 */
	public Component getComponentReplaced2(Map<String, Component> map, boolean json, Object... path) {
		return replaceToComponents(getComponent(json, path), map);
	}

	/**
	 * Getting a deserialized list of {@link Component} classes from the locale configuration node. 
	 * 
	 * @param json - If true, then the list of {@link Component} classes will be obtained from JSON strings.
	 * @param path - Path in the config file.
	 * @return "List&lt;Component&gt;"
	 */
	public abstract List<Component> getListComponents(boolean json, Object... path);

	/**
	 * Getting a deserialized list of {@link Component} classes from the locale configuration node. <br>
	 * After receiving the object, the elements are replaced according to the map. <br>
	 * The key of the map must be an element contained in the string. The key in the string will be replaced by the value.
	 * 
	 * @param map - Map for replacing elements from a line in the configuration.
	 * @param json - If true, then the {@link Component} will be received from the JSON string.
	 * @param path - Path in the config file.
	 * @return {@link Component}
	 */
	public List<Component> getListComponentsReplaced1(Map<String, String> map, boolean json, Object... path){
		return getListComponents(json, path).stream().map(component -> (replace(component, map))).collect(Collectors.toList());
	}

	/**
	 * Getting a deserialized list of {@link Component} classes from the locale configuration node. <br>
	 * After receiving the object, the elements are replaced according to the map. <br>
	 * The key of the map must be an element contained in the string. The key in the string will be replaced by the value.
	 * 
	 * @param map - Map for replacing elements from a line in the configuration.
	 * @param json - If true, then the {@link Component} will be received from the JSON string.
	 * @param path - Path in the config file.
	 * @return {@link Component}
	 */
	public List<Component> getListComponentsReplaced2(Map<String, Component> map, boolean json, Object... path){
		return getListComponents(json, path).stream().map(component -> (replaceToComponents(component, map))).collect(Collectors.toList());
	}

	/**
	 * Getting String from locale configuration node.
	 * 
	 * @param path - Path in the config file.
	 * @return "String"
	 */
	public abstract String getString(Object... path);

	/**
	 * Getting a list of strings from the locale configuration node.
	 * 
	 * @param path
	 * @return
	 */
	public abstract List<String> getListStrings(Object... path);

	/**
	 * Checking for the existence of a path in the localization file. And the serialization of the {@link Component} class to JSON string. <br>
	 * The path is created if it does not exist.
	 * 
	 * @param json - If true, the {@link Component} will be serialized to a JSON string.
	 * @param component - Component class
	 * @param comment - Comment to path. Not necessary. You can specify null. Not used with JSON configuration.
	 * @param path - Path in the config file.
	 * @return false if the path already exists. <br>true if path is created.
	 */
	public abstract boolean checkComponent(boolean json, Component component, String comment, Object... path);

	/**
	 * Checking for the existence of a path in the localization file. And the serialization of the {@link Component} classes list to JSON strings. <br>
	 * The path is created if it does not exist.
	 * 
	 * @param json - If true, then the list of {@link Component} classes will be serialized to JSON strings.
	 * @param components - List of {@link Component} classes.
	 * @param comment - Comment to path. Not necessary. You can specify null. Not used with JSON configuration.
	 * @param path - Path in the config file.
	 * @return false if the path already exists. <br>true if path is created.
	 */
	public abstract boolean checkListComponents(boolean json, List<Component> components, String comment, Object... path);

	/**
	 * Checking for the existence of a path in the localization file. And set String value. <br>
	 * The path is created if it does not exist.
	 * 
	 * @param string - String class
	 * @param comment - Comment to path. Not necessary. You can specify null. Not used with JSON configuration.
	 * @param path - Path in the config file.
	 * @return false if the path already exists. <br>true if path is created.
	 */
	public abstract boolean checkString(String string, String comment, Object... path);

	/**
	 * Checking for the existence of a path in the localization file. And set list of String classes. <br>
	 * The path is created if it does not exist.
	 * 
	 * @param strings - List of String classes
	 * @param comment - Comment to path. Not necessary. You can specify null. Not used with JSON configuration.
	 * @param path - Path in the config file.
	 * @return false if the path already exists. <br>true if path is created.
	 */
	public abstract boolean checkListStrings(List<String> strings, String comment, Object... path);

	/**
	 * Creating a map for replacing values in text components.<br>
	 * In this map all values are converted to strings.<br>
	 * For correct replacement it is necessary in both lists the order of adding data must be the same. For example key 1 = value 1, key 4 = value 4.
	 * 
	 * @param keys - Keys contained in the text.
	 * @param values - Values that should be placed in the text instead of keys.
	 */
	public static Map<String, String>  replaceMap(List<String> keys, List<Object> values) {
		Map<String, String> map = new HashMap<String, String>();
		int i = 0;
		for(String key : keys) {
			if(i >= keys.size() || i >= values.size()) break;
			map.put(key, values.get(i).toString());
			i++;
		}
		return map;
	}

	/**
	 * Creating a map for replacing values in text components.<br>
	 * For correct replacement it is necessary in both lists the order of adding data must be the same. For example key 1 = value 1, key 4 = value 4.
	 * 
	 * @param keys - Keys contained in the text.
	 * @param values - Values that should be placed in the text instead of keys.
	 */
	public static Map<String, Component> replaceMapComponents(List<String> keys, List<Component> values) {
		Map<String, Component> map = new HashMap<String, Component>();
		int i = 0;
		for(String key : keys) {
			if(i >= keys.size() || i >= values.size()) break;
			map.put(key, values.get(i));
			i++;
		}
		return map;
	}

	Component replace(Component component, Map<String, String> map) {
		for(Entry<String, String> entry : map.entrySet()) {
			component = component.replaceText(TextReplacementConfig.builder().match(entry.getKey()).replacement(Component.text(entry.getValue())).build());
		}
		return component;
	}

	Component replaceToComponents(Component component, Map<String, Component> map) {
		for(Entry<String, Component> entry : map.entrySet()) {
			component = component.replaceText(TextReplacementConfig.builder().match(entry.getKey()).replacement(entry.getValue()).build());
		}
		return component;
	}

}