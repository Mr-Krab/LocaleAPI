package sawfowl.localeapi.api;

import java.util.List;

import org.spongepowered.api.event.lifecycle.RegisterBuilderEvent;
import org.spongepowered.configurate.ConfigurationNode;

import net.kyori.adventure.text.Component;

public interface PluginLocale {

	/**
	 * Reload locale file
	 */
	void reload();

	/**
	 * Saving the localization file.
	 */
	void saveLocaleNode();

	/**
	 * Getting data from the localization file.
	 * 
	 * @return ConfigurationNode or CommentedConfigurationNode
	 */
	ConfigurationNode getLocaleRootNode();

	/**
	 * Getting data from the localization file.
	 * 
	 * @return ConfigurationNode or CommentedConfigurationNode
	 */
	ConfigurationNode getLocaleNode(Object... path);

	/**
	 * Getteng deserialized {@link Component} from locale configuration node.
	 * 
	 * @param path - Path in the config file.
	 * @return {@link Component}
	 */
	Component getComponent(Object... path);

	/**
	 * Getting a deserialized list of {@link Component} classes from the locale configuration node. 
	 * 
	 * @param path - Path in the config file.
	 * @return "List&lt;Component&gt;"
	 */
	List<Component> getListComponents(Object... path);

	/**
	 * Getting deserialized text in the constructor for its further modification.<br>
	 * The operation is possible only after the constructor is registered in the {@link RegisterBuilderEvent} event.
	 */
	Text getText(Object... path);

	/**
	 * Getting deserialized text in the constructor for its further modification.<br>
	 * The operation is possible only after the constructor is registered in the {@link RegisterBuilderEvent} event.
	 */
	List<Text> getTexts(Object... path);

	/**
	 * Getting String from locale configuration node.
	 * 
	 * @param path - Path in the config file.
	 * @return "String"
	 */
	String getString(Object... path);

	/**
	 * Getting a list of strings from the locale configuration node.
	 * 
	 * @param path
	 * @return
	 */
	List<String> getListStrings(Object... path);

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
	boolean checkComponent(boolean json, Component component, String comment, Object... path);

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
	boolean checkListComponents(boolean json, List<Component> components, String comment, Object... path);

	/**
	 * Checking for the existence of a path in the localization file. And set String value. <br>
	 * The path is created if it does not exist.
	 * 
	 * @param string - String class
	 * @param comment - Comment to path. Not necessary. You can specify null. Not used with JSON configuration.
	 * @param path - Path in the config file.
	 * @return false if the path already exists. <br>true if path is created.
	 */
	boolean checkString(String string, String comment, Object... path);

	/**
	 * Checking for the existence of a path in the localization file. And set list of String classes. <br>
	 * The path is created if it does not exist.
	 * 
	 * @param strings - List of String classes
	 * @param comment - Comment to path. Not necessary. You can specify null. Not used with JSON configuration.
	 * @param path - Path in the config file.
	 * @return false if the path already exists. <br>true if path is created.
	 */
	boolean checkListStrings(List<String> strings, String comment, Object... path);

}
