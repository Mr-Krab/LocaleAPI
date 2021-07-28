package sawfowl.localeapi.utils;

import java.util.List;

import javax.annotation.Nullable;

import org.spongepowered.configurate.ConfigurationNode;

import net.kyori.adventure.text.Component;

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
	 * Getteng deserialized "Component" from locale configuration node.
	 * 
	 * @param json - If true, then the "Component" will be received from the JSON string.
	 * @param path - Path in the config file.
	 * @return "Component"
	 */
	public abstract Component getComponent(boolean json, Object... path);

	/**
	 * Getting a deserialized list of "Component" classes from the locale configuration node. 
	 * 
	 * @param json - If true, then the list of "Component" classes will be obtained from JSON strings.
	 * @param path - Path in the config file.
	 * @return "List&lt;Component&gt;"
	 */
	public abstract List<Component> getListComponents(boolean json, Object... path);

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
	 * Checking for the existence of a path in the localization file. And the serialization of the "Component" class to JSON string. <br>
	 * The path is created if it does not exist.
	 * 
	 * @param json - If true, the "Component" will be serialized to a JSON string.
	 * @param component - Component class
	 * @param comment - Comment to path. Not necessary. You can specify null. Not used with JSON configuration.
	 * @param path - Path in the config file.
	 * @return false if the path already exists. <br>true if path is created.
	 */
	public abstract boolean checkComponent(boolean json, Component component, @Nullable String comment, Object... path);

	/**
	 * Checking for the existence of a path in the localization file. And the serialization of the "Component" classes list to JSON strings. <br>
	 * The path is created if it does not exist.
	 * 
	 * @param json - If true, then the list of "Component" classes will be serialized to JSON strings.
	 * @param components - List of "Component" classes.
	 * @param comment - Comment to path. Not necessary. You can specify null. Not used with JSON configuration.
	 * @param path - Path in the config file.
	 * @return false if the path already exists. <br>true if path is created.
	 */
	public abstract boolean checkListComponents(boolean json, List<Component> components, @Nullable String comment, Object... path);

	/**
	 * Checking for the existence of a path in the localization file. And set String value. <br>
	 * The path is created if it does not exist.
	 * 
	 * @param string - String class
	 * @param comment - Comment to path. Not necessary. You can specify null. Not used with JSON configuration.
	 * @param path - Path in the config file.
	 * @return false if the path already exists. <br>true if path is created.
	 */
	public abstract boolean checkString(String string, @Nullable String comment, Object... path);

	/**
	 * Checking for the existence of a path in the localization file. And set list of String classes. <br>
	 * The path is created if it does not exist.
	 * 
	 * @param strings - List of String classes
	 * @param comment - Comment to path. Not necessary. You can specify null. Not used with JSON configuration.
	 * @param path - Path in the config file.
	 * @return false if the path already exists. <br>true if path is created.
	 */
	public abstract boolean checkListStrings(List<String> strings, @Nullable String comment, Object... path);

}